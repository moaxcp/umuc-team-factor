package job.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * Creates a JobClient and runs it.
 */
public class JobClientApplication {

    public static void main(String... args) throws RemoteException, FileNotFoundException, IOException {
        Properties props = new Properties(JobClient.getDefaultProperties());
        if (args.length == 1) {
            //load from user location
            FileInputStream fin = new FileInputStream(new File(args[0]));
            props.load(fin);
        } else {
            //load from default location
            File file = new File("client.properties");
            if (file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                props.load(fin);
            } else {
                //store defaults to default location (used as template)
                FileOutputStream fout = new FileOutputStream(file);
                JobClient.getDefaultProperties().store(fout, "default client.properties");
            }
        }
        
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        
        JobClient client = new JobClient(props);
        client.run();
    }
}
