package job.client;


import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author john
 */
public interface ClientCallback extends Remote {
    void stop() throws RemoteException;
    ClientStatus status() throws RemoteException;
}
