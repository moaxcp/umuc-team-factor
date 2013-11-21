/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.server.factor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.server.JobServer;

/**
 *
 * @author john
 */
public class TrialDivisionServer {

    private TrialDivisionManager manager;
    private Thread managerThread;

    public TrialDivisionServer() {
        manager = new TrialDivisionManager();
        managerThread = new Thread(manager);

    }

    public void init() throws RemoteException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        JobServer stub = (JobServer) UnicastRemoteObject.exportObject(manager, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("JobServer", stub);
        managerThread.start();
    }

    public void menu() {
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            NumberFormat nf = NumberFormat.getInstance();
            while (true) {
                System.out.println("Please enter a positive number: ");
                try {
                    String s = br.readLine();
                    BigInteger number = new BigInteger(s);
                    manager.setNumber(number);
                } catch (IOException ex) {
                    Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NumberFormatException ex) {
                    Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                BigDecimal percent = manager.currenNumberPercentComplete();
                while (!manager.getSolution().isComplete()) {

                    BigDecimal next = manager.currenNumberPercentComplete();
                    if (!percent.equals(next)) {
                        percent = next;
                        System.out.println("Working... " + manager.currenNumberPercentComplete() + "%");
                        System.out.println("factors so far: " + manager.getSolution().getLeaves());
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("Factors for " + manager.getSolution().getNumber() + " are: " + manager.getSolution().getLeaves());
            }
        }
    }

    public static void main(String... args) throws RemoteException {
        TrialDivisionServer server = new TrialDivisionServer();
        server.init();
        server.menu();
    }
}
