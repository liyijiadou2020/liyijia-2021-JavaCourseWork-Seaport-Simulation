package com.example.service2;

import com.example.service1.Timetable;
import com.example.service3.Simulator;
import com.example.pojo.Performance;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public static void writeTimetable(Timetable timeTable) throws IOException {

        Path pathToTimeTable = Paths.get("timetable.json"); // param: the path string or part of the path string
        ObjectWriter objectWriterTimeTable = new ObjectMapper().writer().withDefaultPrettyPrinter(); /* 导入json的三个依赖,记得设置为统一的版本,否则会找不到类报错! */
        Writer jsonFileTimeTable = new FileWriter(String.valueOf(pathToTimeTable));
        objectWriterTimeTable.writeValue(jsonFileTimeTable, timeTable);  // 将timeTable写入timeTable.json

    }

    public static void writePerformance(Performance performanceCranes) throws IOException{

        Path pathToPerformanceCranes = Paths.get("performance.json");
        ObjectWriter objectWriterPerformanceCranes = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Writer jsonFilePerformanceCranes = new FileWriter(String.valueOf(pathToPerformanceCranes));
        objectWriterPerformanceCranes.writeValue(jsonFilePerformanceCranes, performanceCranes); // 将Cranes的速率写入performanceCranes.json
    }


    // TODO 将结果写入result.json
    public static void writeSimulationResult() throws IOException {
        /**
         * 写入timeTable.json&performanceCranes.json
         */
        Path pathResult = Paths.get("result.json"); // param: the path string or part of the path string
        ObjectWriter objectWriterTimeTable = new ObjectMapper().writer().withDefaultPrettyPrinter(); /* 导入json的三个依赖,记得设置为统一的版本,否则会找不到类报错! */
        Writer jsonFileTimeTable = new FileWriter(String.valueOf(pathResult));
        objectWriterTimeTable.writeValue(jsonFileTimeTable, Simulator.getResult());  // 将timeTable写入timeTable.json

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

}
