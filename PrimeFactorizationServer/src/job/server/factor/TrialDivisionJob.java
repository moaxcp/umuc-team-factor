package job.server.factor;

import java.math.BigInteger;
import job.JobStatus;

/**
 * Performs trial division of a number for a specific range. Once the number is
 * factored run returns with leftFactor and rightFactor set. If a factor is not
 * found run returns without setting leftFactor or rightFactor.
 */
public class TrialDivisionJob extends FactorizationJob {

    private BigInteger start;
    private BigInteger end;
    private BigInteger leftFactor;
    private BigInteger rightFactor;

    public TrialDivisionJob(BigInteger number, BigInteger start, BigInteger end) {
        super(number);
        this.start = start;
        this.end = end;
    }

    private synchronized boolean divide(BigInteger i) {
        boolean r = false;
        BigInteger[] divMod = number.divideAndRemainder(i);
        if (divMod[1].equals(BigInteger.ZERO)) {
            leftFactor = i;
            rightFactor = divMod[0];
            r = true;
        }
        //System.out.println(i + " divides " + number + " = " + r);
        return r;
    }

    /**
     * @return the number
     */
    public synchronized BigInteger getNumber() {
        return number;
    }

    /**
     * @return the start
     */
    public synchronized BigInteger getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public synchronized BigInteger getEnd() {
        return end;
    }

    /**
     * @return the leftFactor
     */
    public synchronized BigInteger getLeftFactor() {
        return leftFactor;
    }

    /**
     * @return the rightFactor
     */
    public synchronized BigInteger getRightFactor() {
        return rightFactor;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (status != JobStatus.NEW) {
                return;
            }
            status = JobStatus.RUNNING;
            watch.start();
        }
        
        Wheel wheel = Wheel.getWheel();
        
        //find wheel start
        BigInteger i = start;
        BigInteger multiplier = start.subtract(BigInteger.valueOf(wheel.getPrime() + wheel.getStartAdd())).divide(BigInteger.valueOf(wheel.getLength())).add(BigInteger.ONE);
        BigInteger patternStart = BigInteger.valueOf(wheel.getPrime() + wheel.getStartAdd()).add(multiplier.multiply(BigInteger.valueOf(wheel.getLength())));
        
        for (;; i = i.add(BigInteger.ONE)) {
            synchronized (this) {
                if (status != JobStatus.RUNNING) {
                    watch.stop();
                    return;
                }

                if (i.compareTo(end) >= 0) {
                    status = JobStatus.COMPLETE;
                    watch.stop();
                    return;
                }
                if (divide(i)) {
                    status = JobStatus.COMPLETE;
                    watch.stop();
                    return;
                } else if(i.compareTo(patternStart) == 0) {
                    break;
                }
            }
        }
        
        for (int j = 0;; i = i.add(BigInteger.valueOf(wheel.getPattern()[j])), j++) {
            synchronized (this) {
                if (status != JobStatus.RUNNING) {
                    watch.stop();
                    return;
                }

                if (i.compareTo(end) >= 0) {
                    status = JobStatus.COMPLETE;
                    watch.stop();
                    return;
                }
                if (divide(i)) {
                    status = JobStatus.COMPLETE;
                    watch.stop();
                    return;
                }
                if(j >= wheel.getPattern().length) {
                    j = 0;
                }
            }
        }
    }

    @Override
    public synchronized String toString() {
        return "TrialDivisionJob " + status + " in " + watch + " from " + start + " to " + end + " number:" + number + " = " + leftFactor + " * " + rightFactor;
    }
}
