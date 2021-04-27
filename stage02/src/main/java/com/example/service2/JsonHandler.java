package com.example.service2;

import com.example.service1.Timetable;
import com.example.service3.Simulator;
import com.example.pojo.Performance;
import com.example.pojo.Statistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;


import java.io.*;
import java.util.Locale;

import static com.example.utils.Constant.*;

/**
 * @Autor: liyijiadou
 * @create :2021-04-23 13:23:00
 * @Version: 1.0
 * @Descriptions:
 */
public class JsonHandler {
    static ObjectMapper mapper = new ObjectMapper();
    static RestTemplateBuilder builder = new RestTemplateBuilder();
    static RestTemplate restTemplate = builder.build();


    /**
     * Write into timeTable.json & performanceCranes.json
     * Сохраняет timetable в timetable.json
     */
    public static void writeTimetable(Timetable timetable) throws IOException {

        File file = new File(TIMETABLE_JSON_PATH);
        /**
         * Если не существует такого файла, то создать новый файл
         */
        if (TIMETABLE_JSON_PATH.isEmpty() || !TIMETABLE_JSON_PATH.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new FileNotFoundException("[ERROR] File path is empty or not a Json file");
        }else if (!file.exists()) {
            file.createNewFile();
        }

        mapper.writeValue(new FileWriter(file), timetable);
        System.out.println(file.getName()+" is formed.");

    }

    /**
     * Write into timeTable.json & performanceCranes.json
     * Сохраняет производительность кранов в performance.json
     */
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
    public static void writeStatistics(Statistics statistics) throws IOException {
        File file = new File(RESULT_JSON_PATH);
        if (RESULT_JSON_PATH.isEmpty() || !RESULT_JSON_PATH.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new FileNotFoundException("[ERROR] File path is empty or not a Json file");
        }else if (!file.exists()) {
            file.createNewFile();
        }
        mapper.writeValue(new FileWriter(file), statistics);
        System.out.println(file.getName()+" is formed.");
    }

    /**
     * @throws IOException
     */
    public static void postStatistics() throws IOException {
        /**
         * отправляется на POST-endpoint сервиса 2
         */
        restTemplate.postForObject("http://localhost:8090/service2/result", Simulator.getResult(),
                Statistics.class);
    }


    @Nullable
    public static String getJson(Performance performance) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(performance);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Nullable
    public static String getJson(Timetable timeTable) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(timeTable);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }



}
