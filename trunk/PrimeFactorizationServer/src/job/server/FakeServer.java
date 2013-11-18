/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import job.Job;
import job.client.ClientCallback;

/**
 *
 * @author john
 */
public class FakeServer implements JobServer {
    
    private Map<UUID, ClientCallback> clients;
    
    public FakeServer() {
        clients = new HashMap<UUID, ClientCallback>();
    }

    @Override
    public synchronized UUID getSession(ClientCallback client) throws RemoteException {
        UUID id = UUID.randomUUID();
        clients.put(id, client);
        return id;
    }

    @Override
    public synchronized void endSession(UUID id) throws RemoteException {
        clients.remove(id);
    }

    @Override
    public synchronized Job getNextJob(UUID id) throws RemoteException {
        return new FakeJob();
    }

    @Override
    public synchronized void returnJob(UUID id, Job job) throws RemoteException {
        
    }
    
    public static void main(String... args) throws RemoteException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        FakeServer server = new FakeServer();
        JobServer stub = (JobServer)UnicastRemoteObject.exportObject(server, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("JobServer", stub);
    }
}
