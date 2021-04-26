package com.example.controller;

import com.example.pojo.Performance;
import com.example.service1.Timetable;
import com.example.service3.Statistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.example.service2.MyJsonReaderWriter.*;

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
     * 成功
     * http://localhost:8090/service2/timetable
     * @return
     *  /getUserName/{id}
     */
    @GetMapping("/service2/timetable")
    public static String getTimetable() throws IOException {
        System.out.println("#Service 2-generateTimetable: received request.");

        /**
         * Get object from the return of url:"http://localhost:8090/service1/timetable"
         */
        String stringTimetable = restTemplate.getForObject("http://localhost:8090/service1/timetable", String.class);
        Timetable timetable = mapper.readValue(stringTimetable, Timetable.class);

//        printTimetable(timetable);

        /**
         * FIXME 如果没有,就创建一个
         * write into timetable.json
         */
        writeTimetable(timetable);

        String json = getJson(timetable);
        return json;
    }

    @Nullable
    private static String getJson(Timetable timeTable) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(timeTable);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }


    /**
     * fixme 需要手工输入
     *
     * http://localhost:8090/service2/performance
     * @return
     */
    @GetMapping(value="/service2/performance",produces = "application/json")
    public static String getPerformance() throws IOException {
        System.out.println("#Service 2-generatePerformance: received request.");

        /**
         * 成功
         * Get object from the return of url:"http://localhost:8090/service1/performance"
         */
        String stringPerformance = restTemplate.getForObject("http://localhost:8090/service1/performance", String.class);
        Performance performance = mapper.readValue(stringPerformance, Performance.class);

        /**
         * Enter performance manually
         * modified 如果不添加手工输入
         */
        System.out.println(performance);

        /**
         * write into timetable.json
         */
        writePerformance(performance);
        return getJson(performance);
    }

    // TODO 重复!!!
    @Nullable
    private static String getJson(Performance performance) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(performance);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }


    /**
     * 成功
     * http://localhost:8090/service2/timetable/timetable.json
     * TODO!!!
     *
     * @param tName
     * @return
     * @throws IOException
     */
    @GetMapping("/service2/timetable/{tableName}")
    public static String getTimeTableByName(@PathVariable("tableName")String tName) throws IOException {
        System.out.println("#Service2-getTimeTableByName: Service 2 received GET-request, getting timetable by name...");
        System.out.println("Received table name is "+tName);


        File file = new File(tName);
        if (!file.exists()) {
            return "Not have this file!"; // TODO throw
        }
        Path pathToTimeTable = Paths.get(tName);

        /**
         * Get timetable
         */
        Timetable timetable = mapper.readValue(new File(String.valueOf(pathToTimeTable)), Timetable.class);
        return getJson(timetable);
    }

    /**
     * TODO 检测
     * 接受来自service3的POST请求,
     * @param stringStatistics：从Service3法来的请求
     * @throws IOException
     * // ,consumes = "application/json", produces = "application/json"
     * consumes: Need to send json-form request. //fixme
     * produces: Need to respond with json-form request
     */
    @PostMapping(value="/service2/result",produces = "application/json", consumes = "application/json")
    public static String receiveResult(@RequestBody String stringStatistics) throws IOException {
        System.out.println("#Service2-receiveReport: Service 2 received POST-request, receiving result.json from Service 3...");

        System.out.println("in SERVICE2, received:"+stringStatistics);

        /**
         * 从stringStatistics中提取java对象,成功了
         */
        Statistics statistics = mapper.readValue(stringStatistics, Statistics.class);


        /**
         * 这里收到的好像不是json对象?
         * write to result.json
         */
        writeSimulationResult(statistics);

        return stringStatistics;

    }



}//Controller2
