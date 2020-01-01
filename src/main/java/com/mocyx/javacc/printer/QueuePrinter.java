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
