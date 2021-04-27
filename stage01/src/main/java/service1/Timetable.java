package service1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pojo.Schedule;

import java.util.ArrayList;

@JsonAutoDetect
public class Timetable {

    @JsonDeserialize(as = ArrayList.class)
    public ArrayList<Schedule> schedules;


    public Timetable() {
        schedules = new ArrayList<Schedule>();
    }

    public Timetable(ArrayList<Schedule> timeTable) {
        this.schedules = timeTable;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<Schedule> schedules) {
        this.schedules = schedules;
    }

    @Override
    public String toString() {
        return "TimeTable{" +
                "timeTable=" + schedules +
                '}'+'\n';
    }
}
