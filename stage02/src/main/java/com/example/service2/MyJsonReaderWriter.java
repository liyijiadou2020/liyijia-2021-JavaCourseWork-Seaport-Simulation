package com.example.service2;

import com.example.service1.Timetable;
import com.example.service3.Simulator;
import com.example.pojo.Performance;
import com.example.service3.Statistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
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

    static ObjectMapper mapper = new ObjectMapper();
    static RestTemplateBuilder builder = new RestTemplateBuilder(); // 发出请求
    static RestTemplate restTemplate = builder.build();
    static ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();

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

        mapper.writeValue(new FileWriter(file), result);
        System.out.println(file.getName()+" is formed.");

    }


    @Nullable
    public static Performance getPerformanceFromJson(String filename) {
        Path pathToPerformanceCranes = Paths.get(filename);

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
        Timetable timetable=null;
        try {
            timetable = mapper.readValue(new File(String.valueOf(pathToTimetable)), Timetable.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return timetable;
    }

    /**
     * TODO 需要发送json格式post请求数据
     * TODO 向2提出POST请求,并且发送Statistics对象.这里发送的是字符串
     * @throws IOException
     */
    public static void postSimulationResult() throws IOException {

        /**
         * 向2发出GET请求,出错
         */
//        restTemplate.postForObject("http://localhost:8090/service2/result", getJson(Simulator.getResult()),
//                String.class);
        /**
         * 向2发出GET请求,ok
         */
        restTemplate.postForObject("http://localhost:8090/service2/result", Simulator.getResult(),
                Statistics.class);
    }

    @Nullable
    private static String getJson(Statistics statistics) throws IOException {

        String json = null;
        try {
            json = mapper.writeValueAsString(statistics);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }


}
