/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.server.factor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import static musings.EliminateDivisors.primeSize;
import static musings.EliminateDivisors.primes;

/**
 *
 * @author john
 */
public class Wheel {

    private static Wheel wheel;
    private int prime;
    private int startAdd;
    private byte[] pattern;
    private long length;

    private static Integer findPattern(List<Byte> list) {
        TreeMap<Integer, Integer> sizeIndex = new TreeMap<Integer, Integer>();
        Map<Integer, Integer> sizeCount = new HashMap<Integer, Integer>();
        size_loop:
        for (int i = 1; i <= list.size(); i++) {
            List<Integer> remove = new ArrayList<Integer>();
            boolean removing = false;
            for (Integer j : sizeIndex.keySet()) {
                if (removing) {
                    //System.out.println("removing " + list.subList(0, j));
                    remove.add(j);
                    continue;
                }
                Byte stringAt = list.get(sizeIndex.get(j));
                Byte listAt = list.get(i - 1);
                //System.out.println("Checking " + list.subList(0, j) + " stringAt[" + sizeIndex.get(j) + "]: " + stringAt + " listAt[" + (i - 1) + "]: " + listAt);
                if (!stringAt.equals(listAt)) {
                    //System.out.println("removing " + list.subList(0, j));
                    remove.add(j);
                } else {
                    if (j - 1 == sizeIndex.get(j)) {
                        //System.out.println("setting back to " + 0 + " " + list.subList(0, j));
                        sizeIndex.put(j, 0);
                        sizeCount.put(j, sizeCount.get(j) + 1);
                        if (sizeCount.get(j) >= 2) {
                            for (Integer x : sizeIndex.keySet()) {
                                if (x != j) {
                                    remove.add(j);
                                }
                            }
                            break size_loop;
                        }
                    } else {
                        //System.out.println("incrementing to " + (sizeIndex.get(j) + 1) + " " + list.subList(0, j));
                        sizeIndex.put(j, sizeIndex.get(j) + 1);
                        removing = true;
                    }
                }
            }
            for (int j : remove) {
                sizeIndex.remove(j);
            }
            //System.out.println("adding: " + list.subList(0, i));
            sizeIndex.put(i, 0);
            sizeCount.put(i, 0);
        }

        int size = sizeIndex.firstEntry().getKey();
        //System.out.println("size: " + size + " list.size()" + list.size() + " count: " + sizeCount.get(size));
        if (size != list.size() && sizeCount.get(size) >= 2) {
            return size;
        } else {
            return null;
        }
    }

    private static synchronized void init(int number) {
        Logger.getLogger(Wheel.class.getName()).info("initializing Wheel for " + number);
        Map<Integer, Integer> primeSize = new TreeMap<Integer, Integer>();
        primeSize.put(2, 9);
        primeSize.put(3, 24);
        primeSize.put(5, 97);
        primeSize.put(7, 641);
        primeSize.put(11, 6943);
        primeSize.put(13, 90107);
        primeSize.put(17, 1531549);
        primeSize.put(19, 29099093);
        primeSize.put(23, 600000000);

        SortedSet<Integer> primes = new TreeSet<Integer>();
        primes.add(2);
        primes.add(3);
        primes.add(5);
        primes.add(7);
        primes.add(11);
        primes.add(13);
        primes.add(17);
        primes.add(19);
        primes.add(23);
        primes.add(29);

        int prime = number;
        int size = primeSize.get(number);

        //List<Integer> line = new ArrayList<Integer>();
        List<Byte> counts = new ArrayList<Byte>();
        int divisions = 0;
        byte counter = 0;

        Iterator<Integer> current = primes.iterator();
        Iterator<Integer> next = primes.iterator();
        next.next();
        int currenti = current.next();
        int nexti = next.next();

        while (currenti <= number) {
            //line.add(currenti);
            counts.add((byte) (nexti - currenti));
            divisions++;
            currenti = current.next();
            if (currenti <= number) {
                nexti = next.next();
            }
        }

        int startAdd = counts.get(counts.size() - 1);

        //line.add(currenti);
        divisions++;
        currenti += 1;
        counter += 1;


        while (currenti <= size) {
            boolean match = true;
            for (long l : primes) {
                if (l > number) {
                    break;
                }
                match &= !(currenti % l == 0);
            }
            if (match) {
                //line.add(currenti);
                counts.add(counter);
                counter = 0;
                divisions++;
            }
            counter += 1;
            currenti += 1;
        }

        int countSize = counts.size();
        for (Integer i : primes) {
            if (i.compareTo(number) > 0) {
                break;
            }
            counts.remove(0);
        }

        int patternSize = findPattern(counts);

        byte[] pattern = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            pattern[i] = counts.get(i);
        }

        wheel = new Wheel(prime, startAdd, pattern);
        Logger.getLogger(Wheel.class.getName()).info("created wheel for " + number);
    }

    public static synchronized Wheel getWheel() {
        if (wheel == null) {
            Logger.getLogger(Wheel.class.getName()).info("memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024));
            init(13);
        }
        return wheel;
    }

    private Wheel(int start, int startAdd, byte[] pattern) {
        this.prime = start;
        this.startAdd = startAdd;
        this.pattern = pattern;
        for(byte b : pattern) {
            length += b;
        }
    }

    /**
     * @return the start
     */
    public int getPrime() {
        return prime;
    }

    /**
     * @return the startAdd
     */
    public int getStartAdd() {
        return startAdd;
    }

    /**
     * @return the pattern
     */
    public byte[] getPattern() {
        return pattern;
    }
    
    public long getLength() {
        return length;
    }

    public String toString() {
        return prime + ":" + startAdd + ":" + Arrays.toString(pattern);
    }

    public static void main(String... args) {
        System.out.println(Wheel.getWheel());

        Wheel wheel = Wheel.getWheel();

        List<Long> tests = new ArrayList<Long>();

        for (long i = 3; i < 5000; i += 2) {
            //tests.add(i);
        }
        tests.add(1071514531l);
        tests.add(1070532961l);
        tests.add(2345678917l);

        for (Long number : tests) {
            long divisions = 0;
            long i = 0;
            
            long max = (long) Math.sqrt(number);
            long factor = 0;

            long start = 0;

            for (i = 2; i <= max; i++) {
                    divisions++;
                if (number % i == 0) {
                    factor = i;
                    System.out.println("setup found " + factor);
                    break;
                } else if (i % wheel.getPrime() == 0) {
                    start = i;
                    break;
                }
            }
            
            i = 0;
            long j = 0;
            
            System.out.println("start is " + start + " next is " + (start + wheel.getStartAdd()));

            if (start != 0 && factor == 0) {
                for (i = start + wheel.getStartAdd(), j = 0; i <= max; i += wheel.getPattern()[(int)j], j++) {
                    System.out.println(number + " % " + i + " = " + (number % i));
                    divisions++;
                    if (number % i == 0) {
                        factor = i;
                        System.out.println("wheel found " + factor);
                        break;
                    }
                    if(j >= wheel.getPattern().length) {
                        j = 0;
                    }
                    System.out.println("i is " + i + " + " + wheel.getPattern()[(int)j] + " = " + (i + wheel.getPattern()[(int)j]));
                }
            }
            System.out.println("factor for " + number + " is " + factor + ". in " + divisions + " divisions instead of " + i);
        }
    }
}
