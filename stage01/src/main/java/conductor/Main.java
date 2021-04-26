package conductor;
import Service3.Simulator;
import pojo.Performance;
import pojo.Timetable;
import static Service2.JsonHandler1.*;
import static utils.ParameterFormer.*;


public class Main {

    public static void main(String[] args){

        try {
            /**
             * generate timetable & performance
             */
            Timetable timetable = randomTimetable();
            Performance performance = getDefaultPerformanceCranes();
            printTimetable(timetable);
            printPerformance(performance);

            /**
             * manually input Schedule
             */
//            AddParameter.enterSchedule(timetable);
//            AddParameter.enterPerformance(performance);

            /**
             * write timetable to timetable.json & write performance to performance.json
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
            Simulator.simulate();

            /**
             * write into result.json
             */
            writeStatistics(Simulator.getResult());
        }
        catch (Exception e){
            e.printStackTrace();
        }// catch

    } // main

} //Main
