package com.example.controller;

import com.example.pojo.Performance;
import com.example.service1.Timetable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.example.service2.JsonHandler.getJson;
import static com.example.utils.ParameterFormer.getDefaultPerformanceCranes;
import static com.example.utils.ParameterFormer.randomTimetable;

/**
 * @Autor: liyijiadou
 * @create :2021-04-24 06:28:00
 * @Version: 1.0
 * @Descriptions:
 */

@SpringBootApplication
@RestController
public class Controller1 {

    static ObjectWriter ow= new ObjectMapper().writer().withDefaultPrettyPrinter();

    /**
     * GET-endpoint, возвращающий расписание
     * http://localhost:8090/service1/timetable
     * @return timetable in json
     */
    @GetMapping("/service1/timetable")
    public static String generateTimetable() throws IOException {
        System.out.println("[Service 1] GET-endpoint(/service1/timetable) received request, starts generating timetable...");
        Timetable timetable = randomTimetable();
        return getJson(timetable);
    }

    /**
     * GET-endpoint, возвращающий производительность
     * http://localhost:8090/service1/performance
     * @return performance in json
     */
    @GetMapping("/service1/performance")
    public static String generatePerformance() throws IOException {
        System.out.println("[Service 1] GET-endpoint(/service1/performance) received request, starts generating performance...");
        Performance performance=getDefaultPerformanceCranes();
        return getJson(performance);
    }


}
