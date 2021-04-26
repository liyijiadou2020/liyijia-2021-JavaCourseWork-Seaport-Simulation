package com.example.controller;

import com.example.pojo.Performance;
import com.example.service1.Timetable;
import com.example.pojo.Statistics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

import static com.example.service2.JsonHandler.*;
import static com.example.utils.Constant.PACKET_PATH;

/**
 * @Autor: liyijiadou
 * @create :2021-04-24 06:33:00
 * @Version: 0.0
 * @Descriptions:
 */

@SpringBootApplication
@RestController
public class Controller2 {

    static RestTemplateBuilder builder = new RestTemplateBuilder();
    static RestTemplate restTemplate = builder.build();
    static ObjectMapper mapper = new ObjectMapper();


    /**
     * GET-endpoint для получения расписания в виде json-file
     * http://localhost:8090/service2/timetable
     */
    @GetMapping("/service2/timetable")
    public static String getTimetable() throws IOException {
        System.out.println("#Service 2-generateTimetable: received request.");

        /**
         * Get object from the return of url:"http://localhost:8090/service1/timetable"
         */
        String stringTimetable = restTemplate.getForObject("http://localhost:8090/service1/timetable", String.class);
        Timetable timetable = mapper.readValue(stringTimetable, Timetable.class);

        /**
         * write into timetable.json
         */
        writeTimetable(timetable);

        String json = getJson(timetable);
        return json;
    }



    /**
     * GET-endpoint для получения расписания в виде json-file
     *
     * http://localhost:8090/service2/performance
     * @return
     */
    @GetMapping(value="/service2/performance",produces = "application/json")
    public static String getPerformance() throws IOException {
        System.out.println("#Service 2-generatePerformance: received request.");

        /**
         * Get Performance from the return of url:"http://localhost:8090/service1/performance"
         */
        String stringPerformance = restTemplate.getForObject("http://localhost:8090/service1/performance", String.class);
        Performance performance = mapper.readValue(stringPerformance, Performance.class);

        /**
         * write into timetable.json
         */
        writePerformance(performance);
        return getJson(performance);
    }

    /**
     * GET-endpoint, возвращающий расписание по имени json-file или ощибку, если такого файла нет
     * http://localhost:8090/service2/timetable/timetable.json
     *
     * @param tName - имя файла
     * @return
     * @throws IOException
     */
    @GetMapping("/service2/timetable/{tableName}")
    public static String getTimeTableByName(@PathVariable("tableName")String tName) throws IOException {
        System.out.println("#Service2-getTimeTableByName: Service 2 received GET-request, getting timetable by name...");
        System.out.println("Received table name is "+tName);

        String path = PACKET_PATH+tName;

        /**
         * возвращает ощибку, если такого файла нет
         */
        File file = new File(path);
        if (!file.exists()) {
            return "The file "+path+" does not exist!";
        }

        /**
         * Get timetable
         */
        Timetable timetable = mapper.readValue(new File(path), Timetable.class);
        System.out.println("-->Service 2: TIMETABLE SIZE="+timetable.getSchedules().size());
        return getJson(timetable);
    }

    /**
     *
     * POST-endpoint для сохронения результата работы сервиса 3
     * @param stringStatistics：результат работы сервиса 3 в виде json
     * @throws IOException
     */
    @PostMapping(value="/service2/result",produces = "application/json", consumes = "application/json")
    public static String receiveResult(@RequestBody String stringStatistics) throws IOException {
        System.out.println("#Service2-receiveReport: Service 2 received POST-request, receiving result.json from Service 3...");
        System.out.println("in SERVICE2, received:"+stringStatistics);
        Statistics statistics = mapper.readValue(stringStatistics, Statistics.class);

        /**
         * сохронение
         */
        writeStatistics(statistics);
        return stringStatistics;
    }



}//Controller2
