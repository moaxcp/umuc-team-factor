package job.server;


import job.server.JobServer;
import job.Job;
import job.client.ClientCallback;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author john
 */
public abstract class ProcessManager implements JobServer, Runnable {
    protected Map<UUID, Job> expired;
    protected Map<UUID, Session> sessions;
    protected boolean run;
    
    protected class Session {
        private UUID id;
        private ClientCallback client;
        private Map<UUID, Job> jobs;
        
        public Session(ClientCallback client) {
            this.client = client;
            id = UUID.randomUUID();
            jobs = new HashMap<UUID, Job>();
        }
    }
    
    public ProcessManager() {
        expired = new HashMap<UUID, Job>();
        sessions = new HashMap<UUID, Session>();
    }
    
    public UUID getSession(ClientCallback client) {
        Session session = new Session(client);
        sessions.put(session.id, session);
        return session.id;
    }
    
    public void endSession(UUID id) {
        Session session = sessions.get(id);
        if(session != null) {
            for(UUID i : session.jobs.keySet()) {
                expired.put(i, session.jobs.get(i));
            }
            sessions.remove(session.id);
        }
    }
    
    protected void stop() {
        run = false;
    }
    
    public void run() {
        run = true;
        
        while(run) {
            //get status
            //end session if could not get status
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for(UUID id : sessions.keySet()) {
            try {
                sessions.get(id).client.stop();
            } catch (RemoteException ex) {
                Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
