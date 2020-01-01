package com.mocyx.javacc;

import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
public class MockConsoleService {

    private StringBuilder stringBuilder = new StringBuilder();

    public synchronized void clear() {
        stringBuilder = new StringBuilder();
    }

    public synchronized void print(String s) {
        stringBuilder.append(s);
    }

    public synchronized String getResult() {
        return stringBuilder.toString();
    }
}
