package job.server;

import job.Job;
import job.client.ClientCallback;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.StopWatch;
import job.client.ClientStatus;

/**
 * Partial implementation of a JobServer. ProcessManager implements session
 * management. When a session is ended or expired the jobs will be kept for
 * future requests to execute. Session management is in the run() method Any
 * object of this class or subclass should be executed in a thread.
 */
public abstract class ProcessManager implements JobServer, Runnable {

    protected Map<UUID, Job> expired;
    protected Map<UUID, Session> sessions;
    private boolean run;
    private boolean stopJobs;
    private long sessionExpireTime = 5 * 60 * 1000;

    /**
     * A session tracks every client on this server and the jobs that have been
     * issued to it. When a session is ended this object is used to stop the
     * jobs on the server and put them in the expired queue.
     */
    protected class Session {

        public UUID id;
        public ClientCallback client;
        public Map<UUID, Job> jobs;

        public Session(ClientCallback client) {
            this.client = client;
            id = UUID.randomUUID();
            jobs = new HashMap<UUID, Job>();
        }
    }

    /**
     * initializes the ProcessManager.
     */
    public ProcessManager() {
        expired = new HashMap<UUID, Job>();
        sessions = new HashMap<UUID, Session>();
    }

    public synchronized boolean isStopJobs() {
        return stopJobs;
    }

    public synchronized void setStopJobs(boolean stop) {
        this.stopJobs = stop;
    }

    public synchronized void setSessionExpireTime(long time) {
        this.sessionExpireTime = time;
    }

    /**
     * Creates a new session for this client and returns the session id.
     *
     * @param client
     * @return the session id.
     */
    @Override
    public synchronized UUID getSession(ClientCallback client) {
        Session session = new Session(client);
        sessions.put(session.id, session);
        Logger.getLogger(ProcessManager.class.getName()).info("created session " + session.id);
        return session.id;
    }

    /**
     * ends the session. All jobs sent to the client are put in the expired
     * queue.
     *
     * @param id
     * @throws SessionExpiredException
     */
    @Override
    public synchronized void endSession(UUID id) throws SessionExpiredException {
        Session session = sessions.get(id);
        if (session != null) {
            for (UUID i : session.jobs.keySet()) {
                expired.put(i, session.jobs.get(i));
            }
            sessions.remove(session.id);
            Logger.getLogger(ProcessManager.class.getName()).info("ended session " + session.id);
        } else {
            throw new SessionExpiredException();
        }
    }

    /**
     * adds a job to the session.
     *
     * @param session
     * @param job
     */
    protected synchronized void addJob(UUID session, Job job) {
        Session s = sessions.get(session);
        s.jobs.put(job.getId(), job);
    }

    /**
     * stops jobs on all clients.
     */
    protected synchronized void stopJobs() {
        stopJobs = true;
    }

    /**
     * causes run to return.
     */
    public synchronized void stop() {
        run = false;
    }

    /**
     * Checks sessions every 5 minutes and expires clients that are
     * unresponsive.
     */
    @Override
    public void run() {
        run = true;

        StopWatch sessionWatch = new StopWatch();
        sessionWatch.start();

        while (run) {
            if (sessionWatch.getTime() > sessionExpireTime * 1000000) {
                sessionWatch.reset();
                Map<UUID, Session> copy = new HashMap<UUID, Session>();
                synchronized (this) {
                    copy = Collections.unmodifiableMap(sessions);
                }

                List<UUID> endSessions = new ArrayList<UUID>();
                for (UUID id : copy.keySet()) {
                    ClientCallback c = copy.get(id).client;
                    try {
                        ClientStatus status = c.status();
                        Logger.getLogger(ProcessManager.class.getName()).info("got client status for " + id + ": " + status.getSessionID() + " -" + status.getJobStatus());
                        if (status.getSessionID() == null || !status.getSessionID().equals(id)) {
                            endSessions.add(id);
                        }
                    } catch (Exception ex) {
                        endSessions.add(id);
                        Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                for (UUID id : endSessions) {
                    try {
                        endSession(id);
                    } catch (SessionExpiredException ex) {
                        Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            if (isStopJobs()) {
                List<ClientCallback> clients = new ArrayList<ClientCallback>();
                synchronized (this) {
                    for (UUID session : sessions.keySet()) {
                        Session s = sessions.get(session);
                        Logger.getLogger(ProcessManager.class.getName()).info("Will stop jobs for " + session);
                        clients.add(s.client);
                    }
                }
                for (ClientCallback c : clients) {
                    try {
                        c.stopJobs();
                    } catch (RemoteException ex) {
                        Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                setStopJobs(false);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        synchronized (this) {
            for (UUID id : sessions.keySet()) {
                try {
                    sessions.get(id).client.stopJobs();
                } catch (RemoteException ex) {
                    Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
