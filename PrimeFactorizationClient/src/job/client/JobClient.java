/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.client;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.Job;
import job.server.JobServer;

/**
 *
 * @author john
 */
public class JobClient implements Runnable, ClientCallback {

    private Map<UUID, Thread> threads;
    private Map<UUID, Job> jobs;
    private UUID id;
    private JobServer server;
    private boolean run = true;
    private final int MAX_THREADS;

    public JobClient(JobServer server, int maxThreads) {
        this.server = server;
        this.MAX_THREADS = maxThreads;
    }

    private void getSession() {
        while (run) {
            try {
                id = server.getSession(this);
                return;
            } catch (RemoteException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }
    
    private Job getJob() {
        Job j = null;
        while(run) {
            try {
                j = server.getNextJob(id);
            } catch (RemoteException ex) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return j;
    }
    
    private void returnJob(Job job) {
        try {
            server.returnJob(id, job);
        } catch (RemoteException ex) {
            Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException ex1) {
                Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private void stopJob(UUID id) throws InterruptedException {
        Thread t = threads.get(id);
        Job j = jobs.get(id);
        j.stop();
        t.join();
    }

    @Override
    public void run() {
        while (run) {
            getSession();
            while (run) {
                Job j = getJob();
                if(j == null) {
                    try {
                        Thread.sleep(30 * 1000);
                        continue;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                j.run();
                returnJob(j);
            }
        }

        try {
            server.endSession(id);
        } catch (RemoteException ex) {
            Logger.getLogger(JobClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stopJobs() throws RemoteException {
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
    public ClientStatus status() throws RemoteException {
        return new ClientStatus(jobs, id);
    }
}
