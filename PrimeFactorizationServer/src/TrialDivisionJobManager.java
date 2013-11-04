
import java.math.BigInteger;
import java.util.List;
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
public class TrialDivisionJobManager extends JobManager {
    
    private FactorTree solution;
    
    public TrialDivisionJobManager(BigInteger number) {
        solution = new FactorTree(number);
    }

    @Override
    public Job getNextJob() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
