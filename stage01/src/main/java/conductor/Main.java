package conductor;
import service2.EnterParameter;
import service3.Port;
import pojo.Performance;
import service1.Timetable;

import static service2.JsonHandler.*;
import static utils.ParameterFormer.*;


public class Main {

    public static void main(String[] args){

        try {

            /**
             * Генерируем расписания прибытия судов и производительность
             * generate timetable & performance
             * Default: Loose:65, Liquid:78, Container:99
             */
            Timetable timetable = randomTimetable();
            Performance performance = getDefaultPerformanceCranes();

//            printTimetable(timetable);
//            printPerformance(performance);

            /**
             * ручно добавить записи через консоль
             * manually input Schedule
             */
            EnterParameter.enter(timetable);
//            EnterParameter.enter(performance);

            /**
             * сохроняет данные в файл
             * write timetable to timetable.json & write performance to performance.json
             */
            writeTimetable(timetable);
            writePerformance(performance);

            /**
             * print timetable
             */
            printTimetable(timetable);
            System.out.println(timetable);

            /**
             * моделируем
             * simulate
             */
            Port.simulate();

            /**
             * write into result.json
             */
            writeStatistics(Port.getResult());
        }
        catch (Exception e){
            e.printStackTrace();
        }// catch

    } // main

} //Main
