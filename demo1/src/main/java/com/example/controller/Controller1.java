package com.example.controller;

import com.example.pojo.Performance;
import com.example.service1.Timetable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.utils.ParameterFormer.getDefaultPerformanceCranes;
import static com.example.utils.ParameterFormer.randomTimetable;

/**
 * @Autor: liyijiadou
 * @create :2021-04-24 06:28:00
 * @Version: 0.0
 * @Descriptions:
 */

@SpringBootApplication
@RestController
public class Controller1 {

    /**
     * 成功
     * http://localhost:8089/service1/timetable
     * @return
     */
    @GetMapping("/service1/timetable")
    public static String generateTimetable(){
        System.out.println("#Service 1-generateTimetable: received request, start generating timetable...");
        Timetable timetable = randomTimetable();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter(); //TODO 搞懂
        String json = null;

        try {
            json = ow.writeValueAsString(timetable);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    /**
     * 成功
     * http://localhost:8089/service1/performance
     * @return
     */
    @GetMapping("/service1/performance")
    public static String generatePerformance(){
        System.out.println("#Service 1-generatePerformance: received request, start generating performance...");
        Performance performance=getDefaultPerformanceCranes();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter(); //TODO 搞懂
        String json = null;

        try {
            json = ow.writeValueAsString(performance);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }


}
