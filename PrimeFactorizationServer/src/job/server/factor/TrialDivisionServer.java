/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.server.factor;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import job.server.JobServer;

/**
 *
 * @author john
 */
public class TrialDivisionServer {
    public static void main(String... args) throws RemoteException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        JobServer server = new TrialDivisionManager();
        JobServer stub = (JobServer)UnicastRemoteObject.exportObject(server, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("JobServer", stub);
    }
}
