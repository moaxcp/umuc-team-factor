package job.client;


import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import job.server.JobServer;

/**
 * Creates a JobClient and runs it.
 */
public class JobClientApplication {

    public static void main(String... args) throws RemoteException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        
        int cores = Runtime.getRuntime().availableProcessors() - 1;
        
        JobClient client = new JobClient(cores <= 1 ? 1 : cores);
        client.run();
    }
}
