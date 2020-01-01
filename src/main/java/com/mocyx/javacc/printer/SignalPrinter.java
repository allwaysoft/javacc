package com.mocyx.javacc.printer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用Lock and Condition进行同步
 *
 * @author Administrator
 */
@Component
public class SignalPrinter implements Printer {
    private final Lock lock = new ReentrantLock();
    private volatile int counter = 0;

    class Worker implements Runnable {

        Condition curCondition;
        Condition nextCondition;
        String pstr;
        int max;
        int index;
        int gap;

        public Worker(String pstr, int index, int gap, int max, Condition curCondition, Condition nextCondition) {
            this.pstr = pstr;
            this.max = max;
            this.curCondition = curCondition;
            this.nextCondition = nextCondition;
            this.index = index;
            this.gap = gap;
        }

        private boolean isMyTurn() {
            return counter % gap == index;
        }

        @Override
        public void run() {
            while (true) {
                lock.lock();
                try {
                    while (!isMyTurn()) {
                        try {
                            curCondition.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (counter < max) {
                        System.out.print(pstr);
                    }
                    counter += 1;
                    nextCondition.signalAll();
                } finally {
                    lock.unlock();
                }
                if (counter >= max) {
                    return;
                }
            }
        }
    }

    @Override
    public void print() {
        List<Thread> threads = new ArrayList<>();
        List<Condition> conditions = new ArrayList<>();

        conditions.add(lock.newCondition());
        conditions.add(lock.newCondition());
        conditions.add(lock.newCondition());

        threads.add(new Thread(new Worker("A", 0, 3, 30, conditions.get(0), conditions.get(1))));
        threads.add(new Thread(new Worker("B", 1, 3, 30, conditions.get(1), conditions.get(2))));
        threads.add(new Thread(new Worker("C", 2, 3, 30, conditions.get(2), conditions.get(0))));

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
