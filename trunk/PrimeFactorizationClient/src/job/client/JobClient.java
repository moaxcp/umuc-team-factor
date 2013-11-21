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
                    Logger.getLogger(JobClient.class.getName()).info("Got Job " + j);
                    break;
                } else {
                    Logger.getLogger(JobClient.class.getName()).info("Server returned null job.");
                    break;
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
                Logger.getLogger(JobClient.class.getName()).info("Returned Job " + job);
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

    private synchronized void stopJob(UUID id) {
        Thread t = threads.get(id);
        Job j = jobs.get(id);
        j.stop();
        if (t.isAlive()) {
            try {
                t.join(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (t.isAlive()) {
                Logger.getLogger(JobClient.class.getName()).severe("Thread for \"" + j + "\" is complete but still alive. The Job does not implement COMPLETE correctly.");
            }
        }
        Logger.getLogger(JobClient.class.getName()).info("Stopped Job " + j);
    }

    public synchronized void stop() {
        run = false;
    }

    private synchronized boolean fillJobs() {
        while (jobs.keySet().size() < MAX_THREADS) {
            Job j;
            try {
                j = getJob();
            } catch (SessionExpiredException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                stopJobs();
                return true;
            }
            if (j != null) {
                Thread t = new Thread(j);
                jobs.put(j.getId(), j);
                threads.put(j.getId(), t);
                t.start();
            } else {
                break;
            }
        }
        Logger.getLogger(JobClient.class.getName()).info("There are " + threads.size() + " threads.");
        return false;
    }

    private synchronized List<UUID> statusQuery(JobStatus status) {
        List<UUID> jobids = new ArrayList<UUID>();
        for (UUID jobID : jobs.keySet()) {
            Job j = jobs.get(jobID);
            if (j.getStatus() == status) {
                jobids.add(j.getId());
            }
        }
        return jobids;
    }

    private synchronized void joinThreads(List<UUID> jobids) {
        for (UUID jobID : jobids) {
            Thread t = threads.get(jobID);
            try {
                if (t.isAlive()) {
                    t.join(1000);
                    if (t.isAlive()) {
                        Logger.getLogger(JobClient.class.getName()).severe("Thread for " + jobID + " is complete but still alive. The Job does not implement COMPLETE correctly.");
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private synchronized boolean returnJobs(List<UUID> jobids) {
        boolean sessionEnded = false;
        for (UUID id : jobids) {
            Job j = jobs.get(id);
            try {
                returnJob(j);
            } catch (SessionExpiredException ex) {
                sessionEnded = true;
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sessionEnded;
    }

    @Override
    public void run() {
            stopJobs();
        session_loop:
        while (run) {
            getSession();
            while (run) {
                List<UUID> complete = statusQuery(JobStatus.COMPLETE);
                joinThreads(complete);
                boolean sessionEnded = returnJobs(complete);
                if (sessionEnded) {
                    continue session_loop;
                }
                synchronized (this) {
                    for (UUID jobID : complete) {
                        threads.remove(jobID);
                        jobs.remove(jobID);
                    }
                }

                sessionEnded = fillJobs();
                if (sessionEnded) {
                    continue session_loop;
                }

                try {
                    Logger.getLogger(JobClient.class.getName()).info("Job loop cycled. Waiting 1 sec.");
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
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
    public synchronized void stopJobs() {
        for (UUID id : jobs.keySet()) {
            stopJob(id);
            threads.remove(id);
            jobs.remove(id);
        }
    }

    @Override
    public synchronized ClientStatus status() {
        return new ClientStatus(jobs, id);
    }
}
