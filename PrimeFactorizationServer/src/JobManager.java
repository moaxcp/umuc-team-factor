
import java.util.Map;
import java.util.UUID;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author john
 */
public abstract class JobManager {
    
    private Map<UUID, TrialDivisionJob> working;
    private Map<UUID, TrialDivisionJob> expired;
    
    public abstract Job getNextJob();
    
    public void expire(UUID id) {
        
    }
}
