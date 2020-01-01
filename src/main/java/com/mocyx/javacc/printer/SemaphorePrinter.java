package com.mocyx.javacc.printer;

import com.mocyx.javacc.MockConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author Administrator
 */
@Component
public class SemaphorePrinter implements Printer {

    @Autowired
    private MockConsoleService consoleService;


    class Worker implements Runnable {
        private String pstr;

        private Semaphore curSemphore;
        private Semaphore nextSemphore;
        private int count = 0;

        Worker(String pstr, int count, Semaphore curSemphore, Semaphore nextSemphore) {
            this.pstr = pstr;
            this.count = count;
            this.curSemphore = curSemphore;
            this.nextSemphore = nextSemphore;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                try {
                    curSemphore.acquire(1);
                    consoleService.print(pstr);
                    nextSemphore.release(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void print() {
        List<Thread> threads = new ArrayList<>();
        List<Semaphore> semaphores = new ArrayList<>();

        semaphores.add(new Semaphore(0));
        semaphores.add(new Semaphore(0));
        semaphores.add(new Semaphore(0));

        threads.add(new Thread(new Worker("A", 10, semaphores.get(0), semaphores.get(1))));
        threads.add(new Thread(new Worker("B", 10, semaphores.get(1), semaphores.get(2))));
        threads.add(new Thread(new Worker("C", 10, semaphores.get(2), semaphores.get(0))));

        for (Thread t : threads) {
            t.start();
        }

        semaphores.get(0).release(1);

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
