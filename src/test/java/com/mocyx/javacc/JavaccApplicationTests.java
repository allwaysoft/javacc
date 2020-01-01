package com.mocyx.javacc;

import com.mocyx.javacc.printer.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JavaccApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private Checker checker;

    @Autowired
    private SleepPrinter sleepPrinter;

    @Autowired
    private PollingPrinter pollingPrinter;

    @Autowired
    private SignalPrinter signalPrinter;
    @Test
    void test1() {
        checker.check(sleepPrinter);
    }
    @Test
    void test2() {
        checker.check(pollingPrinter);
    }
    @Test
    void test3() {
        checker.check(signalPrinter);
    }

    @Autowired
    private QueuePrinter queuePrinter;
    @Test
    void test4() {
        checker.check(queuePrinter);
    }

    @Autowired
    private SemaphorePrinter semaphorePrinter;
    @Test
    void test5() {
        checker.check(semaphorePrinter);
    }

}
