package com.example;

import com.example.controller.Controller2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class Service2 {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(Controller2.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", "8090"));
        app.run(args);

    }
}
