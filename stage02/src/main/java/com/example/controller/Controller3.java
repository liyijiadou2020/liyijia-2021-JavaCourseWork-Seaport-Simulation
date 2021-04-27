package com.example.controller;

import com.example.pojo.Performance;
import com.example.service1.Timetable;
import com.example.service2.EnterParameter;
import com.example.service3.Simulator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.SQLOutput;

import static com.example.service2.JsonHandler.*;
import static com.example.service2.JsonHandler.postStatistics;
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

            System.out.println(performance);

            /**
             * manually input Schedule
             */
            EnterParameter.enter(timetable);
//            EnterParameter.enter(performance);

            /**
             * write into timetable.json & performance.json
             */
            writeTimetable(timetable);
            writePerformance(performance);

            /**
             * print timetable
             */
//            printTimetable(timetable);


            /**
             * обращается к http://localhost:8090/service2/timetable/timetable.json для получения расписвния в виде json-file
             */
            Simulator.initTimetableFromURL();
            /**
             * обращается к http://localhost:8090/service2/performance для получения производительности кранов в виде json-file
             */
            Simulator.initPerformanceFromURL();
            Simulator.init();

            /**
             * моделирование
             */
            Simulator.simulate();
            /**
             *
             * результат работы отправляется на POST-endpoint сервиса 2
             */
            postStatistics();
        }
        catch (Exception e){
            e.printStackTrace();
        }// catch
        System.out.println("COMPLETED!");


    }

}
