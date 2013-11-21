package job;

import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author john
 */
public class StopWatch implements Serializable {

    protected static final long millisecond = 1000000;
    /**
     * number of miliseconds in a second
     */
    protected static final long second = 1000 * millisecond;
    /**
     * number of miliseconds in a minute
     */
    protected static final long minute = 60 * second;
    /**
     * number of miliseconds in a hour
     */
    protected static final long hour = 60 * minute;
    /**
     * number of miliseconds in a day
     */
    protected static final long day = 24 * hour;

    private long time;
    private long current;

    public synchronized void start() {
        current = System.nanoTime();
    }

    public synchronized void stop() {
        time += System.nanoTime() - current;
        start();
    }

    public synchronized void restart() {
        time = 0;
        current = System.nanoTime();
    }

    public synchronized long getTime() {
        return time;
    }

    public synchronized void setTime(long time) {
        this.time = time;
    }

    public synchronized String toString() {
        long days = (time / day);
        long hours = (time / hour) % 24;
        long minutes = (time / minute) % 60;
        long seconds = (time / second) % 60;
        long milli = (time / millisecond) % 1000;
        long nano = time % 1000000;
        String sdays = (days < 10 ? "0" + days : ("" + days)) + ":";
        String shours = (hours < 10 ? "0" + hours : ("" + hours)) + ":";
        String sminutes = (minutes < 10 ? "0" + minutes : "" + minutes) + ":";
        String sseconds = (seconds < 10 ? "0" + seconds : "" + seconds) + ":";
        String smilli = (milli < 100 ? milli < 10 ? "00" + milli : "0" + milli : "" + milli) + ":";
        String snano = nano < 100000 ? nano < 10000 ? nano < 1000 ? nano < 100 ? nano < 10 ? "00000" + nano : "0000" + nano : "000" + nano : "00" + nano : "0" + nano : "" + nano;
        String postfix = days >= 1 ? "day" : hours >= 1 ? "hour" : minutes >= 1 ? "min" : seconds >= 1 ? "sec" : milli >= 1 ? "milli" : nano >= 1 ? "nano" : "";
        return (days == 0 ? "" : sdays) + (days == 0 && hours == 0 ? "" : shours) + (days == 0 && hours == 0 && minutes == 0 ? "" : sminutes) + (days == 0 && hours == 0 && minutes == 0 && seconds == 0 ? "" : sseconds) + (days == 0 && hours == 0 && minutes == 0 && seconds == 0 && milli == 0 ? "" : smilli) + snano + " " + postfix;
    }
}
