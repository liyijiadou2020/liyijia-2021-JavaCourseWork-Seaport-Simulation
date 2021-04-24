package com.example.controller;

import com.example.service1.Timetable;
import com.example.pojo.Performance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.example.service2.MyJsonReaderWriter.writePerformance;
import static com.example.service2.MyJsonReaderWriter.writeTimetable;
import static com.example.utils.ParameterFormer.printTimetable;

/**
 * @Autor: liyijiadou
 * @create :2021-04-24 06:33:00
 * @Version: 0.0
 * @Descriptions:
 */

@SpringBootApplication
@RestController
public class Controller2 {

    /**
     * 成功
     * http://localhost:8090/service2/timetable
     * @return
     */
    @GetMapping("/service2/timetable")
    public static String generateTimetable() throws IOException {
        System.out.println("#Service 2-generateTimetable: received request.");

        /**
         * Get object from the return of url:"http://localhost:8089/service1/timetable"
         */
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();
        String stringTimetable = restTemplate.getForObject("http://localhost:8089/service1/timetable", String.class);

        ObjectMapper mapper = new ObjectMapper();
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
    @GetMapping("/service2/performance")
    public static String generatePerformance() throws IOException {
        System.out.println("#Service 2-generatePerformance: received request.");

        /**
         * 成功
         * Get object from the return of url:"http://localhost:8089/service1/performance"
         */
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();
        String stringPerformance = restTemplate.getForObject("http://localhost:8089/service1/performance", String.class);

        ObjectMapper mapper = new ObjectMapper();
        Performance performance = mapper.readValue(stringPerformance, Performance.class);

        /**
         * Enter performance manually
         * modified 如果不添加手工输入
         */
//        enterPerformance(performance);
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

//        RestTemplateBuilder builder = new RestTemplateBuilder();
//        RestTemplate restTemplate = builder.build();

        File file = new File(tName);
        if (!file.exists()) {
            return "Not have this file!"; // TODO throw
        }
        Path pathToTimeTable = Paths.get(tName);

        /**
         * Get timetable
         */
        ObjectMapper objectMapper = new ObjectMapper();
        Timetable timetable = objectMapper.readValue(new File(String.valueOf(pathToTimeTable)), Timetable.class);
        return getJson(timetable);
    }

    /**
     * TODO 检测
     * 将report.json中的内容写入report，发送给Service 3
     * @param report：从Service3法来的请求
     * @throws IOException
     */
    @PostMapping("/service2/result")
    public static void receiveResult(@RequestBody String report) throws IOException {

        System.out.println("#Service2-receiveReport: Service 2 received POST-request, receiving result.json from Service 3...");

        Path pathToReport = Paths.get("result.json");

        /**
         * write to result.json
         */
        ObjectWriter objectWriterTimeTable = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Writer jsonFileTimeTable = new FileWriter(String.valueOf(pathToReport));
        objectWriterTimeTable.writeValue(jsonFileTimeTable, report);
    }





}//Controller2
