package com.mocyx.javacc.printer;


import java.util.ArrayList;
import java.util.List;

/**
 * sleep and sleep
 *
 * @author Administrator
 */
public class SleepPrinter implements Printer {

    static class Worker implements Runnable {
        private String printStr;
        private int sleepGap;
        private int delay;
        private int count;

        public Worker(String printStr,  int delay, int sleepGap, int count) {
            this.printStr = printStr;
            this.sleepGap = sleepGap;
            this.delay = delay;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < count; i++) {
                try {
                    Thread.sleep(sleepGap);
                    System.out.print(printStr);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    @Override
    public void print() {
        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(new Worker("A", 00, 30, 10)));
        threads.add(new Thread(new Worker("B", 10, 30, 10)));
        threads.add(new Thread(new Worker("C", 20, 30, 10)));
        for (Thread t : threads) {
            t.start();
        }
        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
