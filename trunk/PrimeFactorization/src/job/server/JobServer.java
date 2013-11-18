package job.server;


import job.Job;
import job.client.ClientCallback;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author john
 */
public interface JobServer extends Remote {
    UUID getSession(ClientCallback client) throws RemoteException;
    void endSession(UUID id) throws RemoteException, SessionExpiredException;
    Job getNextJob(UUID id) throws RemoteException, SessionExpiredException;
    void returnJob(UUID id, Job job) throws RemoteException, SessionExpiredException;
}
