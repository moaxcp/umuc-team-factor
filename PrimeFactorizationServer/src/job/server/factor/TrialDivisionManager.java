package job.server.factor;


import job.server.factor.FactorizationManager;
import job.Job;
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
public class TrialDivisionManager extends FactorizationManager {

    @Override
    public void getNextJob(UUID id) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void returnJob(UUID id, Job job) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
