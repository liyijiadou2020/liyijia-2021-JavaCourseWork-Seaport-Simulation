package com.example.pojo;


public class Ship implements Comparable<Ship>{

    /**
     * arriveTime - день и время прибытия
     * name - название судна
     * cargo - вид груза и его вес
     * cranesCount - число разгрузочных кранов
     * unloadDuration - продолжительность разгрузки (зависит только от вида груза и его веса(т.е. cargo))
     * unloadDelay - время задержки окончания разгрузки судна по сравнению обычным
     * startUnloadTime - время начала разгрузки
     * finishUnloadTime -  время окончания разгрузки
     * waitDuration - время ожидания в очерери
     *
     */
    private DayHourMinute arriveTime; /*Arrive time*/
    private String name;
    private Cargo cargo;
    private int cranesCount = 0;
    private boolean isUnloading = false;
    private DayHourMinute unloadDuration; /* parkingTime,unloadMinutes: 卸货所用的分钟数, == UD*/
    private DayHourMinute unloadDelay;
    private DayHourMinute startUnloadTime = new DayHourMinute(); /* startTimeUnloading */
    private DayHourMinute finishUnloadTime = new DayHourMinute(); /* finishTimeUnloading */
    private DayHourMinute waitDuration = new DayHourMinute();


    @Override
    public String toString() {
        return "Ship{" +
                "arriveTime=" + arriveTime +
                ", name='" + name + '\'' +
                ", cargo=" + cargo +
                ", unloadDuration=" + unloadDuration +
                ", UnloadDelay=" + unloadDelay +
                ", startUnloadTime=" + startUnloadTime +
                ", finishUnloadTime=" + finishUnloadTime +
                ", waitDuration=" + waitDuration +
                ", cranesCount=" + cranesCount +
                ", isUnloading=" + isUnloading +
                '}';
    }

    public DayHourMinute getUnloadDelay() {
        return unloadDelay;
    }

    public void setUnloadDelay(DayHourMinute unloadDelay) {
        this.unloadDelay = unloadDelay;
    }


    public Ship() {
        arriveTime = new DayHourMinute();
        name = "";
        cargo = new Cargo();
        unloadDuration = new DayHourMinute();
    }

    public Ship(DayHourMinute dataAndTimeOfArrival, String nameShip, Cargo cargo, DayHourMinute parkingTime) {
        this.arriveTime = dataAndTimeOfArrival;
        this.name = nameShip;
        this.cargo = cargo;
        this.unloadDuration = parkingTime;
    }

    public Ship(Ship ship) {
        arriveTime = new DayHourMinute(ship.arriveTime);
        name = ship.name;
        cargo = new Cargo(ship.cargo);
        unloadDuration = new DayHourMinute(ship.unloadDuration);
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public DayHourMinute getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(DayHourMinute arriveTime) {
        this.arriveTime = arriveTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DayHourMinute getUnloadDuration() {
        return unloadDuration;
    }

    public void setUnloadDuration(DayHourMinute unloadDuration) {
        this.unloadDuration = unloadDuration;
    }

    @Override
    public int compareTo(Ship ship) {
        if (ship == null) {
            throw new IllegalArgumentException("-->Ship: Null parameter!");
        }

        return arriveTime.compareTo(ship.getArriveTime());
    }

    public int getCranesCount() {
        return cranesCount;
    }

    public void setCranesCount(int cranesCount) {
        this.cranesCount = cranesCount;
    }

    public void increaseCrane() {
        cranesCount++;
    }

    public boolean isUnloading() {
        return isUnloading;
    }

    public void setUnloading(boolean unloading) {
        isUnloading = unloading;
    }

    public DayHourMinute getStartUnloadTime() {
        return startUnloadTime;
    }

    public void setStartUnloadTime(DayHourMinute startUnloadTime) {
        this.startUnloadTime = startUnloadTime;
    }

    public DayHourMinute getFinishUnloadTime() {
        return finishUnloadTime;
    }

    public void setFinishUnloadTime(DayHourMinute finishUnloadTime) {
        this.finishUnloadTime = finishUnloadTime;
    }

    public DayHourMinute getWaitDuration() {
        return waitDuration;
    }

    public void setWaitDuration(DayHourMinute waitDuration) {
        this.waitDuration = waitDuration;
    }

}
