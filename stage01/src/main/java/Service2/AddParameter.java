package Service2;
import Service1.Timetable;
import pojo.*;

import java.util.Scanner;

import static utils.ParameterFormer.*;

/**
 * @Autor: liyijiadou
 * @create :2021-04-23 12:28:00
 * @Version: 0.0
 * @Descriptions:
 */
public class AddParameter {

    // TODO 错误检查
    public static void enterSchedule(Timetable timeTable) {
        int counter=0;

        System.out.println("Do you want to add a ship?(y-yes, other-no)");
        Scanner input=new Scanner(System.in);
        String answer=input.next();

        while (answer.equals("y")) {
            System.out.println("Enter arrival data of ship... Now enter day: (1-30)");
            int day = input.nextInt();
            System.out.println("Enter hour: (0-23)");
            int hour = input.nextInt();
            System.out.println("Enter minute: (0-59)");
            int min = input.nextInt();
            // 检验输入合法
            System.out.println("Enter ship name: ");
            String name=input.next();
            System.out.println("Enter cargo type: (1-Loose, 2-Liquid, 3-Container)");
            int cgNum = input.nextInt();
            Cargo cargo = getCargo(cgNum);
            int unloadDuration = calculateUnloadDuration(cargo);

            Schedule schedule = new Schedule(new DayHourMinute(day, hour, min), name, cargo, new DayHourMinute(unloadDuration));
            timeTable.getSchedules().add(schedule);
            System.out.println("Succeed in entering a schedule of ship: "+schedule);
            ++counter;

            System.out.println("Do you want to add one more?(y-yes, other-no)");
            answer = input.next();

        }
        System.out.println("You have enteres "+counter+" schedule(s).");

    }

    public static void enterPerformance(Performance performance) {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter performance of Loose:");
        int perfLoose = input.nextInt();
        System.out.println("Please enter performance of Liquid:");
        int perfLiquid = input.nextInt();
        System.out.println("Please enter performance of Loose:");
        int perfContainer = input.nextInt();

        performance.setLoosePerformance(perfLoose);
        performance.setLiquidPerformance(perfLiquid);
        performance.setContainerPerformance(perfContainer);
    } //enterPerformance

    
} // AddParameter
