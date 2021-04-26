package pojo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Запись судна
 */
@JsonAutoDetect
public class Schedule implements Comparable<Schedule>{
   /**
    * arriveTime - день и время прибытия
    * nameShip - название судна
    * cargo - вид груза и его вес
    * unloadDuration - планируемый срок стоянки для разгрузки
    *
    */
   private DayHourMinute arriveTime;
   private String nameShip;
   private Cargo cargo;
   private DayHourMinute unloadDuration;

   public Schedule() {
   }

   public Schedule(DayHourMinute dataAndTimeOfArrival, String nameShip, Cargo cargo, DayHourMinute parkingTime) {
      this.arriveTime = dataAndTimeOfArrival;
      this.nameShip = nameShip;
      this.cargo = cargo;
      this.unloadDuration = parkingTime;
   }

   public DayHourMinute getArriveTime() {
      return arriveTime;
   }

   public String getNameShip() {
      return nameShip;
   }

   public Cargo getCargo() {
      return cargo;
   }

   public DayHourMinute getUnloadDuration() {
      return unloadDuration;
   }

   public void setArriveTime(DayHourMinute arriveTime) {
      this.arriveTime = arriveTime;
   }

   public void setNameShip(String nameShip) {
      this.nameShip = nameShip;
   }

   public void setCargo(Cargo cargo) {
      this.cargo = cargo;
   }

   public void setUnloadDuration(DayHourMinute unloadDuration) {
      this.unloadDuration = unloadDuration;
   }

   @Override
   public int compareTo(Schedule nodeTimeTable) {
      if (nodeTimeTable == null) {
         throw new IllegalArgumentException("Null parameter");
      }

      return arriveTime.compareTo(nodeTimeTable.getArriveTime());
   }

   @Override
   public String toString() {
      return "Schedule{" +
              "arriveTime=" + arriveTime +
              ", nameShip='" + nameShip + '\'' +
              ", cargo=" + cargo +
              ", unloadMinutes=" + unloadDuration +
              '}';
   }
}
