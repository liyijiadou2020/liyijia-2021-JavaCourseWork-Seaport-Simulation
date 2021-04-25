package Service2;

import Service3.Simulator;
import Service3.Statistics;
import org.jetbrains.annotations.Nullable;
import pojo.Performance;
import pojo.Timetable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static utils.Constant.*;

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

}
