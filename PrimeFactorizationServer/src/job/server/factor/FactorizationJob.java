package job.server.factor;


import java.math.BigInteger;
import job.Job;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author john
 */
public abstract class FactorizationJob extends Job {
    protected BigInteger number;
    
    public BigInteger getNumber() {
        return number;
    }
    
    public FactorizationJob(BigInteger number) {
        this.number = number;
    }
}
