package com.example.service2;

import com.example.service1.Timetable;
import com.example.service3.Simulator;
import com.example.pojo.Performance;
import com.example.service3.Statistics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static com.example.utils.Constant.*;

/**
 * @Autor: liyijiadou
 * @create :2021-04-23 13:23:00
 * @Version: 0.0
 * @Descriptions:
 */
public class MyJsonReaderWriter {

    /**
     * Write into timeTable.json & performanceCranes.json
     */
    public static void writeTimetable(Timetable timetable) throws IOException {

        File file = new File(TIMETABLE_JSON_PATH);
        if (TIMETABLE_JSON_PATH.isEmpty() || !TIMETABLE_JSON_PATH.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new FileNotFoundException("[ERROR] File path is empty or not a Json file");
        }else if (!file.exists()) {
            file.createNewFile();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new FileWriter(file), timetable);

        System.out.println(file.getName()+" is formed.");

    }

    public static void writePerformance(Performance performanceCranes) throws IOException{

        File file = new File(PERFORMANCE_JSON_PATH);
        if (PERFORMANCE_JSON_PATH.isEmpty() || !PERFORMANCE_JSON_PATH.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new FileNotFoundException("[ERROR] File path is empty or not a Json file");
        }else if (!file.exists()) {
            file.createNewFile();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new FileWriter(file), performanceCranes);

        System.out.println(file.getName()+" is formed.");
    }


    // TODO 将结果写入result.json
    public static void writeSimulationResult(Statistics result) throws IOException {

        File file = new File(RESULT_JSON_PATH);
        if (RESULT_JSON_PATH.isEmpty() || !RESULT_JSON_PATH.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new FileNotFoundException("[ERROR] File path is empty or not a Json file");
        }else if (!file.exists()) {
            file.createNewFile();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new FileWriter(file), result);

        System.out.println(file.getName()+" is formed.");

    }


    @Nullable
    public static Performance getPerformanceFromJson(String filename) {
        Path pathToPerformanceCranes = Paths.get(filename);
        ObjectMapper mapper = new ObjectMapper();

        Performance performanceCranes = null;
        try {
            performanceCranes = mapper.readValue(new File(String.valueOf(pathToPerformanceCranes)),
                    Performance.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return performanceCranes;
    }

    @Nullable
    public static Timetable getTimetableFromJson(String filename) {

        Path pathToTimetable = Paths.get(filename);
        ObjectMapper objectMapper = new ObjectMapper();
        Timetable timetable=null;
        try {
            timetable = objectMapper.readValue(new File(String.valueOf(pathToTimetable)), Timetable.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return timetable;
    }

    public static void postSimulationResult() throws IOException {
        /**
         * 写入timeTable.json&performanceCranes.json
         */
        Path pathResult = Paths.get(RESULT_JSON_PATH); // param: the path string or part of the path string
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter(); /* 导入json的三个依赖,记得设置为统一的版本,否则会找不到类报错! */
        Writer jsonFileTimeTable = new FileWriter(String.valueOf(pathResult));
        writer.writeValue(jsonFileTimeTable, Simulator.getResult());  // 将Statistics写入result.json


        RestTemplateBuilder builder = new RestTemplateBuilder(); // 发出请求
        RestTemplate restTemplate = builder.build();
        /**
         * 传递json对象
         * getForObject -- GET
         * postForObject -- POST
         */
        restTemplate.postForObject("http://localhost:8090/service2/result", Files.readString(pathResult),
                String.class);

    }

}
