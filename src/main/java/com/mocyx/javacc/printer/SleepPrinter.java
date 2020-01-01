package com.mocyx.javacc.printer;

import com.mocyx.javacc.MockConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * sleep and sleep
 *
 * @author Administrator
 */
@Component
public class SleepPrinter implements Printer {

    static class Worker implements Runnable {
        private MockConsoleService consoleService;
        private String printStr;
        private int sleepGap;
        private int delay;
        private int count;

        public Worker(String printStr, MockConsoleService consoleService, int delay, int sleepGap, int count) {
            this.printStr = printStr;
            this.consoleService = consoleService;
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
                    consoleService.print(printStr);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Autowired
    private MockConsoleService consoleService;

    @Override
    public void print() {
        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(new Worker("A", consoleService, 00, 30, 10)));
        threads.add(new Thread(new Worker("B", consoleService, 10, 30, 10)));
        threads.add(new Thread(new Worker("C", consoleService, 20, 30, 10)));
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
