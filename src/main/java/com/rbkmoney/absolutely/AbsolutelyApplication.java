package com.rbkmoney.absolutely;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class AbsolutelyApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbsolutelyApplication.class, args);
    }

}
