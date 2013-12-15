/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.server.factor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.StopWatch;
import job.server.JobServer;

/**
 *
 * @author john
 */
public class TrialDivisionServer {

    private TrialDivisionManager manager;
    private Thread managerThread;
    private String registryHost;
    private int registryPort;
    private String registryServerObject;
    private long sessionExpireTime;
    private File progressFile;
    private StopWatch watch;
    
    private static final Properties defaults;
    
    static {
        defaults = new Properties();
        defaults.setProperty("registryHost", "localhost");
        defaults.setProperty("registryPort", "1099");
        defaults.setProperty("registryServerObject", "JobServer");
        defaults.setProperty("sessionExpireTime", Long.valueOf(5 * 60 * 1000).toString());
        defaults.setProperty("progressFile", "progressFile");
    }
    
    public static Properties getDefaultProperties() {
        return defaults;
    }
    
    private void useProperties(Properties properties) {
        registryHost = properties.getProperty("registryHost");
        registryPort = Integer.valueOf(properties.getProperty("registryPort"));
        registryServerObject = properties.getProperty("registryServerObject");
        sessionExpireTime = Long.valueOf(properties.getProperty("sessionExpireTime"));
        progressFile = new File(properties.getProperty("progressFile"));
    }

    /**
     * creates a trial division server.
     */
    public TrialDivisionServer() {
        watch = new StopWatch();
        useProperties(defaults);
    }
    
    public TrialDivisionServer(Properties properties) {
        watch = new StopWatch();
        useProperties(properties);
    }
    
    public void printResults() {
        System.out.println("Factors are " + manager.getSolution().getLeaves());
        Map<BigInteger, StopWatch> times = manager.getTimes();
        StopWatch jobWatch = new StopWatch();
        for(BigInteger bi : times.keySet()) {
            jobWatch.setTime(jobWatch.getTime() + times.get(bi).getTime());
        }
        System.out.println("Times: " + manager.getTimes());
        System.out.println("Total job time: " + jobWatch);
        System.out.println("Total problem time: " + watch);
    }

    /**
     * adds the server to the rmi registry.
     *
     * @throws RemoteException
     */
    public void init() throws RemoteException {
        if(progressFile.exists()) {
            progressFile.delete();
        }
        manager = new TrialDivisionManager(progressFile);
        manager.setSessionExpireTime(sessionExpireTime);
        managerThread = new Thread(manager);
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        JobServer stub = (JobServer) UnicastRemoteObject.exportObject(manager, 0);
        Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
        registry.rebind(registryServerObject, stub);
        managerThread.start();
    }

    /**
     * displays the menu to the user and continues to check the status of the
     * number as the server works on solving it.
     */
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
                    continue;
                } catch (NumberFormatException ex) {
                    Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                watch = new StopWatch();
                watch.start();
                BigDecimal percent = manager.currenNumberPercentComplete();
                while (true) {
                    synchronized (manager) {
                        if (manager.getSolution().isComplete()) {
                            watch.stop();
                            break;
                        }
                        BigDecimal next = manager.currenNumberPercentComplete();
                        if (!percent.equals(next)) {
                            percent = next;
                            System.out.println("Working on " + manager.getCurrentNumber() + "... " + manager.currenNumberPercentComplete() + "%");
                            printResults();
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                synchronized (manager) {
                    FactorTree solution = manager.getSolution();
                    Map<BigInteger, Integer> leaves = solution.getLeaves();
                    printResults();
                    for (BigInteger bi : leaves.keySet()) {
                        if (solution.isPrime(bi)) {
                            System.out.println(bi + " is prime.");
                        }
                    }
                }
            }
        }
    }

    public static void main(String... args) throws FileNotFoundException, IOException {
        TrialDivisionServer server = new TrialDivisionServer();
        
        Properties props = new Properties(TrialDivisionServer.getDefaultProperties());
        if (args.length == 1) {
            //load from user location
            FileInputStream fin = new FileInputStream(new File(args[0]));
            props.load(fin);
        } else {
            //load from default location
            File file = new File("server.properties");
            if (file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                props.load(fin);
                fin.close();
            } else {
                //store defaults to default location (used as template)
                FileOutputStream fout = new FileOutputStream(file);
                TrialDivisionServer.getDefaultProperties().store(fout, "default server.properties");
                fout.close();
                FileInputStream fin = new FileInputStream(file);
                props.load(fin);
                fin.close();
            }
        }
        while (true) {
            try {
                server.init();
                server.menu();
            } catch (RemoteException ex) {
                Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(TrialDivisionServer.class.getName()).info("Attempting to connect to registry again in 10 seconds.");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
