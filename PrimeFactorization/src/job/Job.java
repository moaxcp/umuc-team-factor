package job;


import java.io.Serializable;
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
public abstract class Job implements Runnable, Serializable {
    private UUID id;
    protected StopWatch watch;
    protected JobStatus status;
    
    public Job() {
        id = UUID.randomUUID();
        status = JobStatus.NEW;
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
    
    public synchronized JobStatus getStatus() {
        return status;
    }
    
    public synchronized void stop() {
        status = JobStatus.STOPPED;
    }
}
