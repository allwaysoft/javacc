package com.mocyx.javacc.printer;

import com.mocyx.javacc.MockConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询AtomicInteger实现交替输出ABC
 * @author Administrator
 */
@Component
public class PollingPrinter implements Printer {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    @Autowired
    private MockConsoleService consoleService;

    class Worker implements Runnable {

        private String pstr;
        private int index;
        private int gap;
        private int max;

        public Worker(String pstr, int index, int gap, int max) {
            this.pstr = pstr;
            this.index = index;
            this.gap = gap;
            this.max = max;
        }

        @Override
        public void run() {
            while (true) {
                int v = atomicInteger.get();
                if (v == max) {
                    return;
                } else {
                    if (v % gap == index) {
                        consoleService.print(pstr);
                        atomicInteger.set(v + 1);
                    }
                }
            }
        }
    }


    @Override
    public void print() {
        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(new Worker("A", 0, 3, 30)));
        threads.add(new Thread(new Worker("B", 1, 3, 30)));
        threads.add(new Thread(new Worker("C", 2, 3, 30)));
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
