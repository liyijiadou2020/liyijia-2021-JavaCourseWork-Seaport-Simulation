package com.example;

import com.example.controller.Controller3;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Demo1Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Controller3.class);
        app.run(args);
    }

}
