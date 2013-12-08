package job.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.Job;
import job.JobStatus;
import job.client.ClientCallback;
import job.client.ClientStatus;
import job.server.JobServer;
import job.server.SessionExpiredException;

/**
 * JobClient gets jobs from the JobServer and executes them.
 */
public class JobClient extends UnicastRemoteObject implements Runnable, ClientCallback {

    private Map<UUID, Thread> threads;
    private Map<UUID, Job> jobs;
    private UUID id;
    private JobServer server;
    private boolean run = true;
    private final int MAX_THREADS;
    private boolean stopJobs = false;
    private String registryHost;
    private int registryPort;
    private String registryServerObject;
    private long serverRetryWait;
    private long threadJoinWait;
    private long loopCycleWait;
    private static final Properties defaults;

    static {
        defaults = new Properties();
        defaults.setProperty("registryHost", "localhost");
        defaults.setProperty("registryPort", "1099");
        defaults.setProperty("registryServerObject", "JobServer");
        defaults.setProperty("serverRetryWait", Long.valueOf(30 * 1000).toString());
        defaults.setProperty("threadJoinWait", Long.valueOf(5 * 1000).toString());
        defaults.setProperty("loopCycleWait", Long.valueOf(1 * 1000).toString());
    }

    private synchronized boolean isStopJobs() {
        return stopJobs;
    }

    private synchronized boolean isRun() {
        return run;
    }

    private synchronized void setStopJobs(boolean stop) {
        this.stopJobs = stop;
    }

    public static Properties getDefaultProperties() {
        return defaults;
    }

    private void useProperties(Properties properties) {
        registryHost = properties.getProperty("registryHost");
        registryPort = Integer.valueOf(properties.getProperty("registryPort"));
        registryServerObject = properties.getProperty("registryServerObject");
        serverRetryWait = Long.valueOf(properties.getProperty("serverRetryWait"));
        threadJoinWait = Long.valueOf(properties.getProperty("threadJoinWait"));
        loopCycleWait = Long.valueOf(properties.getProperty("loopCycleWait"));
    }

    public JobClient() throws RemoteException {
        useProperties(defaults);
        int cores = Runtime.getRuntime().availableProcessors() - 1;
        this.MAX_THREADS = cores <= 1 ? 1 : cores;
        threads = new HashMap<UUID, Thread>();
        jobs = new HashMap<UUID, Job>();
    }

    public JobClient(Properties properties) throws RemoteException {
        useProperties(properties);
        int cores = Runtime.getRuntime().availableProcessors() - 1;
        this.MAX_THREADS = cores <= 1 ? 1 : cores;
        threads = new HashMap<UUID, Thread>();
        jobs = new HashMap<UUID, Job>();
    }

    private void setupServer() {
        while (isRun()) {
            try {
                Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
                synchronized (this) {
                    server = (JobServer) registry.lookup(registryServerObject);
                    //attempt to connect to server. must call something to see if server works.
                    server.endSession(UUID.randomUUID());
                }
                return;
            }catch (SessionExpiredException ex) {
                //caused by end session. The server worked so return.
                return;
            } catch (RemoteException | NotBoundException ex) {
                Logger.getLogger(JobClient.class.getName()).info("Could not setup server: " + ex + ". Waiting " + serverRetryWait + "ms to try again.");
                try {
                    Thread.sleep(serverRetryWait);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    /**
     * Attempts to get a session from the server. If it is unable to connect it
     * will try every 30 seconds and continue trying.
     */
    private void getSession() {
        while(isRun()) {
            try {
                if(server == null) {
                    Logger.getLogger(JobClient.class.getName()).info("Server is null.");
                    setupServer();
                }
                UUID sid = server.getSession(this);
                synchronized (this) {
                    id = sid;
                }
                Logger.getLogger(JobClient.class.getName()).info("Session id is " + id);
                return;
            } catch (RemoteException ex) {
                Logger.getLogger(JobClient.class.getName()).info("Could not get session from server: " + ex + ". setting up server.");
                setupServer();
            }
        }
    }

    /**
     * Attempts to get a Job. If it cannot connect to the server it will try
     * every 30 seconds. It will not retry if the job returned is null or if
     * SessionExpiredException is thrown.
     *
     * @return
     * @throws SessionExpiredException
     */
    private Job getJob() throws SessionExpiredException {
        Job j = null;
        while (isRun()) {
            try {
                if(server == null) {
                    Logger.getLogger(JobClient.class.getName()).info("Server is null.");
                    setupServer();
                }
                j = server.getNextJob(id);
                if (j != null) {
                    Logger.getLogger(JobClient.class.getName()).info("Got Job " + j);
                    break;
                } else {
                    Logger.getLogger(JobClient.class.getName()).fine("Server returned null job.");
                    break;
                }
            } catch (RemoteException ex) {
                Logger.getLogger(JobClient.class.getName()).info("Could not get job from server: " + ex + ". setting up server.");
                setupServer();
            }
        }
        return j;
    }

    /**
     * Attempts to connect to the server and return a job. If it cannot connect
     * it will retry every 30 seconds. It will not attempt if a
     * SessionExpiredException is thrown.
     *
     * @param job
     * @throws SessionExpiredException
     */
    private void returnJob(Job job) throws SessionExpiredException {
        while (isRun()) {
            try {
                if(server == null) {
                    Logger.getLogger(JobClient.class.getName()).info("Server is null.");
                    setupServer();
                }
                server.returnJob(id, job);
                Logger.getLogger(JobClient.class.getName()).info("Returned Job " + job);
                return;
            } catch (RemoteException ex) {
                Logger.getLogger(JobClient.class.getName()).info("Could not return job to server: " + ex + ". setting up server.");
                setupServer();
            }
        }
    }

    /**
     * stops the job that matches id. Attempts to join the thread running the
     * job. If the thread does not join it indicates Job is not implemented
     * correctly.
     *
     * @param id
     */
    private synchronized void stopJob(UUID id) {
        Thread t = threads.get(id);
        Job j = jobs.get(id);
        j.stop();
        if (t.isAlive()) {
            try {
                t.join(threadJoinWait);
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
     *
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
        if(threads.size() > 0) {
            Logger.getLogger(JobClient.class.getName()).info("There are " + threads.size() + " threads.");
        } else {
            Logger.getLogger(JobClient.class.getName()).fine("There are " + threads.size() + " threads.");
        }
        return false;
    }

    /**
     * returns a List of job ids that match status.
     *
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
     *
     * @param jobids
     */
    private synchronized void joinThreads(List<UUID> jobids) {
        for (UUID jobID : jobids) {
            Thread t = threads.get(jobID);
            try {
                if (t.isAlive()) {
                    t.join(threadJoinWait);
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
     *
     * @param jobids
     * @return
     */
    private boolean returnJobs(List<Job> jobs) {
        boolean sessionEnded = false;
        for (Job j : jobs) {
            try {
                returnJob(j);
            } catch (SessionExpiredException ex) {
                sessionEnded = true;
                Logger.getLogger(JobClient.class.getName()).info("session expired " + ex);
            }
        }
        return sessionEnded;
    }

    /**
     * Manages the JobClient.
     */
    @Override
    public void run() {
        setupServer();
        session_loop:
        while (isRun()) {
            synchronized (this) {
                if (!jobs.isEmpty()) {
                    realStopJobs();
                }
            }
            getSession();
            boolean sleep = true;
            while (isRun()) {
                List<Job> completeJobs = new ArrayList<Job>();
                List<UUID> complete = statusQuery(JobStatus.COMPLETE);
                joinThreads(complete);
                synchronized (this) {
                    for (UUID jobID : complete) {
                        completeJobs.add(jobs.get(jobID));
                        threads.remove(jobID);
                        jobs.remove(jobID);
                    }
                }

                //cannot be synchronized when calling.
                boolean sessionEnded = returnJobs(completeJobs);
                if (sessionEnded) {
                    continue session_loop;
                }

                synchronized (this) {
                    if (stopJobs) {
                        realStopJobs();
                        stopJobs = false;
                    }
                }


                sessionEnded = fillJobs();
                if (sessionEnded) {
                    continue session_loop;
                }
                if (complete.isEmpty()) {
                    sleep = true;
                } else {
                    sleep = false;
                }
                if (sleep) {
                    try {
                        Logger.getLogger(JobClient.class.getName()).fine("Job loop cycled. Waiting " + loopCycleWait + ".");
                        Thread.sleep(loopCycleWait);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    Logger.getLogger(JobClient.class.getName()).info("Job loop cycled. Need jobs. Not waiting.");
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

    private synchronized void realStopJobs() {
        Logger.getLogger(JobClient.class.getName()).info("Stopping jobs...");
        for (UUID id : jobs.keySet()) {
            stopJob(id);
        }
        threads.clear();
        jobs.clear();
    }

    /**
     * stops all jobs and removes them from the client.
     */
    @Override
    public synchronized void stopJobs() {
        stopJobs = true;
    }

    /**
     * returns the status of this client.
     *
     * @return
     */
    @Override
    public synchronized ClientStatus status() {
        ClientStatus status = new ClientStatus(id, jobs);
        Logger.getLogger(JobClient.class.getName()).info("returning status " + status);
        return status;
    }
}
