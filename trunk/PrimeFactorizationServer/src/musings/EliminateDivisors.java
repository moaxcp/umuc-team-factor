/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author john
 */
public class EliminateDivisors {

    public static void printHeader(int size) {
        System.out.printf("%1$7s", "");
        for (int i = 1; i <= size; i++) {
            if (i % 10 == 0) {
                System.out.printf("%1$5d", i);
            } else {
                System.out.print("     ");
            }
        }
        System.out.println();
    }

    public static void printLine(String msg, List<Integer> line) {
        System.out.printf("%1$7s", msg);
        for (Integer i : line) {
            System.out.printf("%1$5d", i);
        }
        System.out.println();
    }

    public static void printLargePattern(Map<List<Integer>, Integer> strings) {
        if (strings.keySet().size() >= 1) {
            List<Integer> large = strings.keySet().iterator().next();
            Integer count = strings.get(large);
            for (List<Integer> i : strings.keySet()) {
                if(strings.get(i) > count) {
                    large = i;
                    count = strings.get(i);
                }
            }
            int high = 0;
            for(int i : large) {
                if(i > high) {
                    high = i;
                }
            }
            System.out.printf("%1$7s", "");
            System.out.println(large);
            System.out.printf("%1$7s", "");
            System.out.println(" size: " + large.size() + " count: " + count + " high: " + high);
        }
    }

    public static Map<List<Integer>, Integer> findPattern(List<Integer> list) {
        Map<List<Integer>, Integer> patterns = new HashMap<List<Integer>, Integer>();
        for (int i = 1; i <= list.size(); i++) {
            List<Integer> string = new ArrayList<Integer>();
            for (int j = 0; j < i; j++) {
                string.add(list.get(j));
            }
            boolean match = true;
            int k = 0;
            int count = 0;
            for (int j = 0; j < list.size(); j++) {
                if (k == string.size()) {
                    count++;
                    k = 0;
                }
                if (!list.get(j).equals(string.get(k))) {
                    match = false;
                    break;
                }
                k++;
            }
            if (match) {
                Integer oldCount = patterns.get(string);
                oldCount = oldCount == null ? 0 : oldCount;
                patterns.put(string, count + oldCount);
            }
        }
        return patterns;
    }

    public static void main(String... args) {

        int size = 50;
        List<Integer> line = new ArrayList<Integer>();
        List<Integer> counts = new ArrayList<Integer>();
        int counter = 0;

        printHeader(size);

        line.add(2);
        for (int i = 3; i <= size; i++) {
            if (i == 3) {
                line.add(i);
                counts.add(1);
            } else if (i % 2 != 0 && i % 3 != 0) {
                line.add(i);
                counts.add(counter);
                counter = 0;
            }
            counter++;
        }
        printLine("3:", line);
        line.clear();
        printLine("", counts);
        counts.remove(0);
        counts.remove(0);
        printLargePattern(findPattern(counts));
        counts.clear();
        counter = 0;
        
        size = 100;

        line.add(2);
        for (int i = 3; i <= size; i++) {
            if (i == 3) {
                line.add(i);
                counts.add(1);
            } else if (i == 5) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i % 2 != 0 && i % 3 != 0 && i % 5 != 0) {
                line.add(i);
                counts.add(counter);
                counter = 0;
            }
            counter++;
        }
        printLine("5:", line);
        line.clear();
        printLine("", counts);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        printLargePattern(findPattern(counts));
        counts.clear();
        counter = 0;
        
        size = 1000;

        line.add(2);
        for (int i = 3; i <= size; i++) {

            if (i == 3) {
                line.add(i);
                counts.add(1);
            } else if (i == 5) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i == 7) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i % 2 != 0 && i % 3 != 0 && i % 5 != 0 && i % 7 != 0) {
                line.add(i);
                counts.add(counter);
                counter = 0;
            }
            counter++;
        }
        printLine("7:", line);
        line.clear();
        printLine("", counts);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        printLargePattern(findPattern(counts));
        counts.clear();
        counter = 0;
        
        size = 5000;

        line.add(2);
        for (int i = 3; i <= size; i++) {
            if (i == 3) {
                line.add(i);
                counts.add(1);
            } else if (i == 5) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i == 7) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i == 11) {
                line.add(i);
                counts.add(4);
                counter = 0;
            } else if (i % 2 != 0 && i % 3 != 0 && i % 5 != 0 && i % 7 != 0 && i % 11 != 0) {
                line.add(i);
                counts.add(counter);
                counter = 0;
            }
            counter++;
        }
        printLine("11:", line);
        line.clear();
        printLine("", counts);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        printLargePattern(findPattern(counts));
        counts.clear();
        counter = 0;
        
        size = 70000;

        line.add(2);
        for (int i = 3; i <= size; i++) {

            if (i == 3) {
                line.add(i);
                counts.add(1);
            } else if (i == 5) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i == 7) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i == 11) {
                line.add(i);
                counts.add(4);
                counter = 0;
            } else if (i == 13) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i % 2 != 0 && i % 3 != 0 && i % 5 != 0 && i % 7 != 0 && i % 11 != 0 && i % 13 != 0) {
                line.add(i);
                counts.add(counter);
                counter = 0;
            }
            counter++;
        }
        printLine("13:", line);
        line.clear();
        printLine("", counts);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        printLargePattern(findPattern(counts));
        counts.clear();
        counter = 0;
        
        size = 1500000;

        line.add(2);
        for (int i = 3; i <= size; i++) {

            if (i == 3) {
                line.add(i);
                counts.add(1);
            } else if (i == 5) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i == 7) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i == 11) {
                line.add(i);
                counts.add(4);
                counter = 0;
            } else if (i == 13) {
                line.add(i);
                counts.add(2);
                counter = 0;
            } else if (i == 17) {
                line.add(i);
                counts.add(4);
                counter = 0;
            } else if (i % 2 != 0 && i % 3 != 0 && i % 5 != 0 && i % 7 != 0 && i % 11 != 0 && i % 13 != 0 && i % 17 != 0) {
                line.add(i);
                counts.add(counter);
                counter = 0;
            }
            counter++;
        }
        printLine("17:", line);
        line.clear();
        printLine("", counts);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        counts.remove(0);
        printLargePattern(findPattern(counts));
        counts.clear();
        counter = 0;
    }
}
