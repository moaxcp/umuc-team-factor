package job.server.factor;

import java.math.BigInteger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author john
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
        BigInteger[] divMod = number.divideAndRemainder(i);
        if (divMod[1].equals(BigInteger.ZERO)) {
            leftFactor = start;
            rightFactor = divMod[0];
            return true;
        }
        return false;
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
        run = true;
        watch.start();
        try {
            //check start is odd
            if (start.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
                start = start.add(BigInteger.ONE);
            }

            //check all odd
            for (BigInteger i = start; run && i.compareTo(end) < 0; i = i.add(BigInteger.valueOf(2))) {
                if (divide(i)) {
                    return;
                }
            }
        } finally {
            run = false;
            watch.stop();
        }
    }
}
