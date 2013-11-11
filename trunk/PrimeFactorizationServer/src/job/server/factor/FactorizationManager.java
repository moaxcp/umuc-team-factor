package job.server.factor;

import java.math.BigInteger;
import job.server.ProcessManager;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author john
 */
public abstract class FactorizationManager extends ProcessManager {
    private BigInteger number;
    private FactorTree solution;
    
    public void setNumber(BigInteger number) {
        this.number = number;
    }
}
