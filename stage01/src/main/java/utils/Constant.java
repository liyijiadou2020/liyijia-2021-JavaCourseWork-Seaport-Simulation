/**
 * -*- coding: utf-8 -*-
 *
 * @Time : 2021/4/11 15:38
 * @Author : NekoSilverfox
 * @FileName: ConstantsTable
 * @Software: IntelliJ IDEA
 * @Versions: v0.1
 * @Github ：https://github.com/NekoSilverFox
 */
package utils;

public class Constant {

    public static final String TIMETABLE_JSON_PATH = "C:\\LIYIJIA-SpringCourseWork-2021\\stage01\\src\\main\\resources\\static\\timetab.json";
    public static final String RESULT_JSON_PATH = "C:\\LIYIJIA-SpringCourseWork-2021\\stage01\\src\\main\\resources\\static\\result.json";
    public static final String PERFORMANCE_JSON_PATH = "C:\\LIYIJIA-SpringCourseWork-2021\\stage01\\src\\main\\resources\\static\\performance.json";

    public static final int COAST_ONE_CRANE = 30000;
    public static final int COAST_PER_HOUR = 100;


    public static final int MIN_COUNT_OF_NODES = 200;
    public static final int MAX_COUNT_OF_NODES = 300;


    public static final int PERFORMANCE_LOOSE = 65;
    public static final int PERFORMANCE_LIQUID = 78;
    public static final int PERFORMANCE_CONTAINER = 99;

    public static final int MIN_LOOSE_SHIP = 40000;
    public static final int MAX_LOOSE_SHIP = 60000;
    public static final int MIN_LIQUID_SHIP = 50000;
    public static final int MAX_LIQUID_SHIP = 70000;
    public static final int MIN_CONTAINER_SHIP = 2000;
    public static final int MAX_CONTAINER_SHIP = 6000;

    public static final Integer MINUTE_IN_SEVEN_DAYS = 10080;
    public static final Integer MINUTE_IN_THIRTY_DAYS = 43200;
    public static final Integer SIMULATION_DURATION = 30;
    public static final String BOTTOM_LINE = "============================================================================================================================================";
    public static final String TIMETABLE_HEADER_LINE = "================================================== TIMETABLE =====================================================================";
    public static final String PERFORMANCE_HEADER_LINE = "================================================== PERFORMANCE =====================================================================";
    public static final String RESULT_HEADER_LINE = "================================================== RESULT =====================================================================";
    public static final String SINGLE_BOTTOM_LINE = "------------------------------------------------------------------------------------------------------------------------------------|";
    public static final String UNLOADED_HEADER = "--------------------------------------Port: UNLOADED SHIPS--------------------------------------------------------------------------|";
    public static final String STATISTIC_HEADER = "|-------------Statistics of unloaded ships----------------------------------|";
    public static final String STATISTIC_BOTTOM = "\n----------------------------------------------------------------------------|";

}
