package com.example.pojo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.jetbrains.annotations.NotNull;


@JsonAutoDetect
public class DayHourMinute implements Comparable<DayHourMinute> {
    private int day;
    private int hour;
    private int minute;

    public DayHourMinute() {
        day = 0;
        hour = 0;
        minute = 0;
    }

    public DayHourMinute(int minute) {
        convert(0, 0, minute);
    }

    public DayHourMinute(DayHourMinute dayAndTime) {
        day = dayAndTime.day;
        hour = dayAndTime.hour;
        minute = dayAndTime.minute;
    }

    public DayHourMinute(Integer day, Integer hour, Integer minute) {
        if (day<30 || hour<60 || minute<60){
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            return;
        }
        convert(day, hour, minute);
    }

    private void convert(int day, int hour, int minute) {

        int day1 = day;
        int hour1 = hour;
        int min1 = minute;

        // minute --> hour
        int tmpHour = min1 / 60;
        hour1 += tmpHour;
        min1 %= 60;

        // hour --> day
        int tmpDay = hour1 / 24;
        day1 += tmpDay;
        hour1 %= 24;


        tmpHour = min1 / 60;

        if (tmpHour < 0) {
            hour1 += tmpHour;
            min1 += 60 * Math.abs(tmpHour);
            if (min1 % 60 != 0) {
                hour1--;
                min1 += 60;
            }
        }

        tmpDay = hour1 / 24;
        if (tmpDay < 0) {
            day1 += tmpDay;
            hour1 += 24 * Math.abs(tmpDay);
            if (hour1 % 24 != 0) {
                day1--;
                hour1 += 24;
            }
        }


        if (day1 < 0 || day1 > 30)  {
            throw new IllegalArgumentException("-->DayHourMinute: #INVALID INPUT!# ");
        }

        if (min1 <0){
            this.day =0;
            this.hour =0;
            this.minute=0;
            return;
        }

        this.day = day1;
        this.hour = hour1;
        this.minute = min1;
    }

    public Integer inMinutes() {
        return minute + hour * 60 + day * 24 * 60;
    }

    public static DayHourMinute randomDayHourMinute(Integer minDay, Integer maxDay) {
        return new DayHourMinute((int) (Math.floor(Math.random() * (maxDay - minDay)) + minDay),
                (int) (Math.floor(Math.random() * 24)),
                (int) (Math.floor(Math.random() * 60)));
    }

    public static int randomMinute(Integer minMinute, Integer maxMinute) {
        return (int) Math.floor(Math.random() * (maxMinute - minMinute));
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        convert(day, hour, minute);
    }

    public Integer getHour() {
        return hour;
    }

    public DayHourMinute addTime(Integer minute) {
        convert(day, hour, this.minute + minute);
        return this;
    }

    public DayHourMinute addTime(Integer hour, Integer minute) {
        convert(day, this.hour + hour, this.minute + minute);
        return this;
    }

    public DayHourMinute addTime(Integer day, Integer hour, Integer minute) {
        convert(this.day + day, this.hour + hour, this.minute + minute);
        return this;
    }

    public void setHour(Integer hour) {
        convert(day, hour, minute);
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        convert(day, hour, minute);
    }

    public Integer receiveTimeInMinute() {
        return minute + hour * 60 + day * 24 * 60;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        DayHourMinute dayAndTime = (DayHourMinute) obj;
        return (day == dayAndTime.getDay()) && (hour == dayAndTime.getHour()) && (minute == dayAndTime.getMinute());
    }

    @Override
    public String toString() {
        return String.format("%02d", day) +
                ":" + String.format("%02d", hour) +
                ":" + String.format("%02d", minute);
    }

    @Override
    public int compareTo(@NotNull DayHourMinute other) {
        Integer copyDay = day;
        Integer copyHour = hour;
        Integer copyMinute = minute;

        int result;
        if ((result = copyDay.compareTo(other.getDay())) == 0) {
            if ((result = copyHour.compareTo(other.getHour())) == 0) {
                result = copyMinute.compareTo(other.getMinute());
            }
        }

        return result;
    }
}
