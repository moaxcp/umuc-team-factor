
import java.math.BigInteger;
import java.util.UUID;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author john
 */
public abstract class Job implements Runnable {
    private UUID id;
    protected StopWatch watch;
    protected boolean run;
    
    public Job() {
        id = UUID.randomUUID();
        run = false;
    }
    
    public synchronized UUID getId() {
        return id;
    }
    
    public synchronized long getTime() {
        return watch.getTime();
    }
    
    public synchronized String getTimeString() {
        return watch.toString();
    }
    
    public synchronized void stop() {
        run = false;
    }
}
