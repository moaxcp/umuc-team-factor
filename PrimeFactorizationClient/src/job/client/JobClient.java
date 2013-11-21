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
 * JobClient gets jobs from the JobServer and executes them.
 * @author john
 */
public class JobClient implements Runnable, ClientCallback, Serializable {

    private Map<UUID, Thread> threads;
    private Map<UUID, Job> jobs;
    private UUID id;
    private JobServer server;
    private boolean run = true;
    private final int MAX_THREADS;

    /**
     * Creates a JobClient that executes jobs in parallel up to maxThreads.
     * @param server the job server to use
     * @param maxThreads how many threads to run.
     */
    public JobClient(JobServer server, int maxThreads) {
        this.server = server;
        this.MAX_THREADS = maxThreads;
        threads = new HashMap<UUID, Thread>();
        jobs = new HashMap<UUID, Job>();
    }

    /**
     * Attempts to get a session from the server. If it is unable to connect it will try every
     * 30 seconds and continue trying.
     */
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

    /**
     * Attempts to get a Job. If it cannot connect to the server it will try every 30 seconds.
     * It will not retry if the job returned is null or if SessionExpiredException is thrown.
     * @return
     * @throws SessionExpiredException 
     */
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

    /**
     * Attempts to connect to the server and return a job. If it cannot connect it will retry every 30 seconds.
     * It will not attempt if a SessionExpiredException is thrown.
     * @param job
     * @throws SessionExpiredException 
     */
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

    /**
     * stops the job that matches id. Attempts to join the thread running the job. If
     * the thread does not join it indicates Job is not implemented correctly.
     * @param id 
     */
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

    /**
     * causes the client to stop all jobs and run to return.
     */
    public synchronized void stop() {
        run = false;
    }

    /**
     * fills jobs up to MAX_THREADS.
     * @return 
     */
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

    /**
     * returns a List of job ids that match status.
     * @param status
     * @return 
     */
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

    /**
     * Attempts to join all threads running jobids.
     * @param jobids 
     */
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

    /**
     * returns all jobs in jobids to the JobServer.
     * @param jobids
     * @return 
     */
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

    /**
     * Manages the JobClient.
     */
    @Override
    public void run() {
        session_loop:
        while (run) {
            stopJobs();
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

    /**
     * stops all jobs and removes them from the client.
     */
    @Override
    public synchronized void stopJobs() {
        for (UUID id : jobs.keySet()) {
            stopJob(id);
            threads.remove(id);
            jobs.remove(id);
        }
    }

    /**
     * returns the status of this client.
     * @return 
     */
    @Override
    public synchronized ClientStatus status() {
        return new ClientStatus(jobs, id);
    }
}
