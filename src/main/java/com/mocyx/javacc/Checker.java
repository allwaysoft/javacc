package com.mocyx.javacc;

import com.mocyx.javacc.printer.Printer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Administrator
 */
@Component
@Slf4j
public class Checker {
    private static final String str = "ABCABCABCABCABCABCABCABCABCABC";
    @Autowired
    private MockConsoleService consoleService;

    public void check(Printer printer) {
        consoleService.clear();
        printer.print();
        String result = consoleService.getResult();
        log.info("check result {}",result);
        if (!Objects.equals(result, str)) {
            throw new RuntimeException("error");
        }
    }
}
