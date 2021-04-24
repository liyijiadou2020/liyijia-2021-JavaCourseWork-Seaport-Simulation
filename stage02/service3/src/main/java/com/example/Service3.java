package com.example;

import com.example.pojo.Performance;
import com.example.service1.Timetable;
import com.example.service2.AddParameter;
import com.example.service3.Simulator;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.example.service2.MyJsonReaderWriter.*;
import static com.example.utils.ParameterFormer.*;

@SpringBootApplication
public class Service3 {

    public static void main(String[] args) {

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
            AddParameter.enterSchedule(timetable);
            AddParameter.enterPerformance(performance);

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
