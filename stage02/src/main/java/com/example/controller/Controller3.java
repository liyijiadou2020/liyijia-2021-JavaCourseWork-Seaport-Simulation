package com.example.controller;

import com.example.pojo.Performance;
import com.example.service1.Timetable;
import com.example.service2.AddParameter;
import com.example.service3.Simulator;
import com.example.service3.Statistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static com.example.service2.MyJsonReaderWriter.*;
import static com.example.service2.MyJsonReaderWriter.postSimulationResult;
import static com.example.utils.ParameterFormer.*;

@SpringBootApplication
@Controller
public class Controller3 {
    
    // http://localhost:8090/service3/start
    @GetMapping("service3/start")
    public static void start() {

        try {
            /**
             * generate timetable & performance
             */

            Timetable timetable = randomTimetable();
            Performance performance = getDefaultPerformanceCranes();

//        printTimetable(timetable);
            System.out.println(performance);

            /**
             * manually input Schedule
             */
//            AddParameter.enterSchedule(timetable);
//            AddParameter.enterPerformance(performance);

            /**
             * write into timetable.json & performance.json
             */
            writeTimetable(timetable);
            writePerformance(performance);

            /**
             * print timetable
             */
            printTimetable(timetable);

            /**
             * simulate
             */
            Simulator.initTimetableFromURL();
            Simulator.initPerformanceFromURL(); // fixme 不成功
            Simulator.init();
            Simulator.simulate();

            /**
             * write into result.json
             */
            postSimulationResult();
        }
        catch (Exception e){
            e.printStackTrace();
        }// catch


    }


}
