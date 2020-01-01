# 三线程打印ABC，循环十次的N种实现方式

> 编写一个程序，开启 3 个线程 A,B,C，这三个线程的输出分别为 A、B、C，每个线程将自己的 输出在屏幕上打印 10 遍，要求输出的结果必须按顺序显示。如：ABCABCABC....

核心在于多线程同步

## 方法1，轮询AtomicInteger
缺点是轮询白耗CPU，性能很差
```
package com.mocyx.javacc.printer;

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
                        System.out.print(pstr);
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

```
## 方法2，使用Lock & Condition同步
使用Lock & Condition要注意：
1 检查条件谓词，避免信号丢失和过早唤醒
2 注意在finally中进行unlock，否则出现异常会hang住
```
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

```
## 方法3，使用Semphore进行同步
相比Lock & Condition，使用Semphore代码比较简洁，不容易出错
```
package com.mocyx.javacc.printer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author Administrator
 */
@Component
public class SemaphorePrinter implements Printer {



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
                    System.out.print(pstr);
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

```
## 方法4，使用BlockingQueue进行同步
思路跟go channel类似，通过BlockingQueue传递信息
```
package com.mocyx.javacc.printer;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 使用阻塞队列进行同步
 *
 * @author Administrator
 */
public class QueuePrinter implements Printer {


    static class Msg {
        public static final Msg PRINT_SUCCESS = new Msg();
        public static final Msg PRINT = new Msg();
        public static final Msg QUIT = new Msg();
    }

    class Channel {
        BlockingQueue<Msg> inQueue = new ArrayBlockingQueue<Msg>(100);
        BlockingQueue<Msg> outQueue = new ArrayBlockingQueue<Msg>(100);
    }

    class Worker implements Runnable {

        Channel inChannel;
        String pstr;

        Worker(String pstr, Channel inChannel) {
            this.inChannel = inChannel;
            this.pstr = pstr;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Msg msg = inChannel.inQueue.take();
                    if (msg == Msg.PRINT) {
                        System.out.print(pstr);
                        inChannel.outQueue.put(Msg.PRINT_SUCCESS);
                    } else if (msg == Msg.QUIT) {
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void print() {
        List<Thread> threads = new ArrayList<>();

        List<Channel> channels = new ArrayList<>();
        channels.add(new Channel());
        channels.add(new Channel());
        channels.add(new Channel());


        threads.add(new Thread(new Worker("A", channels.get(0))));
        threads.add(new Thread(new Worker("B", channels.get(1))));
        threads.add(new Thread(new Worker("C", channels.get(2))));

        for (Thread t : threads) {
            t.start();
        }

        for (int i = 0; i < 30; i++) {
            try {
                channels.get(i % channels.size()).inQueue.put(Msg.PRINT);
                channels.get(i % channels.size()).outQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Channel c : channels) {
            c.inQueue.add(Msg.QUIT);
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

```
## 方法0，Sleep同步法
这个方法确实能工作，但是可能会被面试官打
```
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

```
