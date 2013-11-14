package job.client;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import job.server.JobServer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author john
 */
public class JobClientApplication {

    public static void main(String... args) throws RemoteException, NotBoundException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        Registry registry = LocateRegistry.getRegistry();
        JobServer comp = (JobServer) registry.lookup("JobServer");
    }
}
