/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.client;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.Job;
import job.JobStatus;
import job.server.JobServer;
import job.server.SessionExpiredException;

/**
 *
 * @author john
 */
public class JobClient implements Runnable, ClientCallback, Serializable {

    private Map<UUID, Thread> threads;
    private Map<UUID, Job> jobs;
    private UUID id;
    private JobServer server;
    private boolean run = true;
    private final int MAX_THREADS;

    public JobClient(JobServer server, int maxThreads) {
        this.server = server;
        this.MAX_THREADS = maxThreads;
        threads = new HashMap<UUID, Thread>();
        jobs = new HashMap<UUID, Job>();
    }

    private synchronized void getSession() {
        while (run) {
            try {
                id = server.getSession(this);
                Logger.getLogger(JobClient.class.getName()).info("Session id is " + id);
                return;
            } catch (RemoteException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, "Could not get session from server. Waiting 30 secs to try again.", ex);
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    private synchronized Job getJob() throws SessionExpiredException {
        Job j = null;
        while (run) {
            try {
                j = server.getNextJob(id);
                if (j != null) {
                    Logger.getLogger(JobClient.class.getName()).info("Got Job. Job id is " + j.getId());
                    break;
                } else {
                    Logger.getLogger(JobClient.class.getName()).info("Server returned null. Waiting 10 secs for Job.");
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (RemoteException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, "Could not get Job from server. Waiting 30 secs to reconnect.", ex);
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return j;
    }

    private synchronized void returnJob(Job job) throws SessionExpiredException {
        while (run) {
            try {
                server.returnJob(id, job);
                Logger.getLogger(JobClient.class.getName()).info("Returned Job. Job id is " + job.getId());
                break;
            } catch (RemoteException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, "Could not return Job to server. Waiting 30 secs to try again.", ex);
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    private synchronized void stopJob(UUID id) throws InterruptedException {
        Thread t = threads.get(id);
        Job j = jobs.get(id);
        j.stop();
        t.join();
        Logger.getLogger(JobClient.class.getName()).info("Stopped Job. Job id is " + j.getId());
    }

    @Override
    public void run() {
        session_loop: while (run) {
            getSession();
            while (run) {
                synchronized (this) {
                    List<UUID> complete = new ArrayList<UUID>();
                    for (UUID jobID : jobs.keySet()) {
                        Job j = jobs.get(jobID);
                        if (j.getStatus() == JobStatus.COMPLETE) {
                            complete.add(j.getId());
                        } else if (j.getStatus() != JobStatus.RUNNING) {
                            Logger.getLogger(JobClient.class.getName()).severe("Job should be RUNNING but it is " + j.getStatus());
                        }
                    }
                    for (UUID jobID : complete) {
                        Thread t = threads.get(jobID);
                        try {
                            t.join();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            returnJob(jobs.get(jobID));
                        } catch (SessionExpiredException ex) {
                            Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        threads.remove(jobID);
                        jobs.remove(jobID);
                    }
                }

                synchronized (this) {
                    while (jobs.keySet().size() < MAX_THREADS) {
                        Job j;
                        try {
                            j = getJob();
                        } catch (SessionExpiredException ex) {
                            try {
                                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                                stopJobs();
                            } catch (RemoteException ex1) {
                                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                            continue session_loop;
                        }
                        Thread t = new Thread(j);
                        jobs.put(j.getId(), j);
                        threads.put(j.getId(), t);
                        t.start();
                        Logger.getLogger(JobClient.class.getName()).info("There are " + threads.size() + " threads.");
                    }
                }
            }
        }

        try {
            server.endSession(id);
        } catch (RemoteException ex) {
            Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void stopJobs() throws RemoteException {
        for (UUID id : jobs.keySet()) {
            try {
                stopJob(id);
            } catch (InterruptedException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                throw new RemoteException("Could not stop thread for Job.", ex);
            }
            threads.remove(id);
            jobs.remove(id);
        }
    }

    @Override
    public synchronized ClientStatus status() throws RemoteException {
        return new ClientStatus(jobs, id);
    }
}
