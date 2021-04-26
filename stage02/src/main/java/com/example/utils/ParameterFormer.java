/**
 * -*- coding: utf-8 -*-
 *
 * @Time : 2021/4/11 20:44
 * @Author : NekoSilverfox
 * @FileName: InfoGenerator
 * @Software: IntelliJ IDEA
 * @Versions: v0.1
 * @Github ：https://github.com/NekoSilverFox
 */
package com.example.utils;


import com.example.pojo.*;
import com.example.service1.Timetable;
import com.example.pojo.Statistics;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.example.utils.Constant.*;

public class ParameterFormer {


    public static @NotNull String stringStatistics(Statistics statistics) {
        return (STATISTIC_HEADER+String.format("\n%-50s", "| Count of loose cranes")+ String.format("|    %-20s |", statistics.getCountLoose())
                + String.format("\n%-50s", "| Count of liquid cranes  ") + String.format("|    %-20s |", statistics.getCountLiquid())
                + String.format("\n%-50s", "| Count of container cranes  ") + String.format("|    %-20s |", statistics.getCountContainer())
                + String.format("\n%-50s", "| Count of unloaded ships  ") + String.format("|    %-20s |", statistics.getCountUnloaded())
                + String.format("\n%-50s", "| Total fine") + String.format("|    %-20s |", statistics.getTotalFine())
                + String.format("\n%-50s", "| Max delay  ")      + String.format("|    %-20s |", statistics.getMaxDelay())
                + String.format("\n%-50s", "| Average delay  ")  + String.format("|    %-20s |", statistics.getAvrDelay())
                + String.format("\n%-50s", "| Average wait duration on one ship(min)  ")   + String.format("|    %-20s |", statistics.getAvrWaitDuration())
                + String.format("\n%-50s", "| Sum wait duration(min)  ") + String.format("|    %-20s |", statistics.getSumWaitDuration())
                + String.format("\n%-50s", "| Average wait length in one day") + String.format("|    %-20s |", statistics.getAvrWaitLength())+STATISTIC_BOTTOM // fixme
        );
    }

    public static @NotNull String stringShip(Ship ship){
        return (String.format("%-10s", "| Name : ")+ String.format("  %-5s |", ship.getName())
                + String.format("%-15s", " Arrival time : ") + String.format("  %-10s |", ship.getArriveTime())
                + String.format("%-15s", " Type Cargo : ") + String.format("  %-9s |", ship.getCargo().getCargoType())
                + String.format("%-6s", " Weight : ") + String.format("  %-4s |", ship.getCargo().getWeight())
                + String.format("%-15s", " Unload Duration : ") + String.format("  %-10s |", ship.getUnloadDuration())
                + String.format("%-15s", " Unload Delay : ") + String.format("  %-10s |", ship.getUnloadDelay())
                + String.format("%-15s", " Start Unload Time : ") + String.format("  %-10s |", ship.getStartUnloadTime())
                + String.format("%-15s", " Finish Unload Time : ") + String.format("  %-10s |", ship.getFinishUnloadTime())
                + String.format("%-15s", " Wait Duration : ") + String.format("  %-10s |", ship.getWaitDuration())
        );
    }


    public static String stringSchedule(Schedule schedule){
            return (String.format("%-10s", "| Name : ")+ String.format("  %-4s |", schedule.getNameShip())
                    + String.format("%-15s", " Arrival time : ") + String.format("  %-10s |", schedule.getArriveTime())
                    + String.format("%-15s", " Type Cargo : ") + String.format("  %-15s |", schedule.getCargo().getCargoType())
                    + String.format("%-6s", " Weight : ") + String.format("  %-8s |", schedule.getCargo().getWeight())
                    + String.format("%-15s", " Unload Duration : ") + String.format("  %-10s |", schedule.getUnloadDuration())
            );
    }

    public static void printTimetable(Timetable timeTable) {

        System.out.println(TIMETABLE_HEADER_LINE);
        System.out.println("SIZE="+timeTable.getSchedules().size());
        for (Schedule s: timeTable.getSchedules()) {
            System.out.println(stringSchedule(s));
        }
        System.out.println(BOTTOM_LINE);
    }


        public static String randomName() {
        int lenName = new Random().nextInt(3) + 3;
        StringBuffer name = new StringBuffer();

        // 生成首字母后的随机字母
        for (int i = 0; i < lenName - 1; i++) {
            char letter = (char) (new Random().nextInt(26) + 65);
            name.append(letter);
        }
        return name.toString();
    }

    public static Performance getDefaultPerformanceCranes() {
        return new Performance(PERFORMANCE_LOOSE, PERFORMANCE_LIQUID, PERFORMANCE_CONTAINER);
    }

    public static int calculateUnloadDuration(Cargo cargo){
        int UD =0;
        switch (cargo.getCargoType()) {
            case LOOSE: UD = cargo.getWeight() / PERFORMANCE_LOOSE;  break;
            case LIQUID: UD = cargo.getWeight() / PERFORMANCE_LIQUID; break;
            case CONTAINER: UD = cargo.getWeight() / PERFORMANCE_CONTAINER; break;
            default: break;
        }
        return UD;
    }

    public static Cargo getCargo(int num){
        Cargo cargo=null;
        switch (num) {
            case 1: cargo = new Cargo(CargoType.LOOSE, randomInt(MIN_LOOSE_SHIP, MAX_LOOSE_SHIP)); break;
            case 2: cargo = new Cargo(CargoType.LIQUID, randomInt(MIN_LIQUID_SHIP, MAX_LIQUID_SHIP)); break;
            case 3: cargo = new Cargo(CargoType.CONTAINER, randomInt(MIN_CONTAINER_SHIP, MAX_CONTAINER_SHIP)); break;
            default: break;
        }
        return cargo;
    }


    public static Timetable randomTimetable() {

        int countSchedules = randomInt(MIN_COUNT_OF_NODES, MAX_COUNT_OF_NODES);
        Timetable timetable = new Timetable();
        for (int i = 0; i < countSchedules; i++) {
            int cargoType = randomInt(1, 3); //random cargo type
            Cargo cargo = null;
            int UD = 0;
            switch (cargoType) {
                case 1:
                    cargo = new Cargo(CargoType.LOOSE, randomInt(MIN_LOOSE_SHIP, MAX_LOOSE_SHIP));
                    UD = cargo.getWeight() / PERFORMANCE_LOOSE;
                    break;
                case 2:
                    cargo = new Cargo(CargoType.LIQUID, randomInt(MIN_LIQUID_SHIP, MAX_LIQUID_SHIP));
                    UD = cargo.getWeight() / PERFORMANCE_LIQUID;
                    break;
                case 3:
                    cargo = new Cargo(CargoType.CONTAINER, randomInt(MIN_CONTAINER_SHIP, MAX_CONTAINER_SHIP));
                    UD = cargo.getWeight() / PERFORMANCE_CONTAINER;
                    break;
                default:
                    break;
            }
            timetable.getSchedules().add(new Schedule(DayHourMinute.randomDayHourMinute(0, SIMULATION_DURATION),
                    ParameterFormer.randomName(),
                    cargo,
                    new DayHourMinute(UD)
            ));
        }// for

        return timetable;
    }

    private static int randomInt(int lowerRange, int upperRange){
        return (int) (Math.floor(Math.random() * (upperRange - lowerRange + 1)) + lowerRange);
    }



}
