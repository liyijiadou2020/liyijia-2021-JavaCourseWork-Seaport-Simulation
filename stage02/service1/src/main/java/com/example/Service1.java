package com.example;

import com.example.controller.Controller1;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class Service1 {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Controller1.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", "8089"));
        app.run(args);
    }

}
