package com.example.service3;

import com.example.pojo.*;

import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.service3.Simulator.*;
import static com.example.utils.Constant.MINUTE_IN_THIRTY_DAYS;

public class Crane implements Runnable {

    private final Integer performance;
    private final CargoType typeCargo;
    private static final ConcurrentMap<CargoType, Lock> mapLockers = new ConcurrentHashMap<>(); //ConcurrentMap- 每个线程对应一把锁

    public Crane(Integer performance, CargoType typeOfCargo) {
        this.performance = performance;
        this.typeCargo = typeOfCargo;
    }

    public Integer getPerformance() {
        return performance;
    }

    public CargoType getTypeCargo() {
        return typeCargo;
    }


    @Override
    public void run() {

        Thread.currentThread().setName(Thread.currentThread().getName()+"-"+typeCargo.toString());
        CyclicBarrier cyclicBarrier = Simulator.cyclicBarrier; // 把Port的cyclicBarrier复制一份

        Ship currentShip = null;
        int currentDelay = 0;

        try {
            while (now() < MINUTE_IN_THIRTY_DAYS) {

                mapLockers.get(typeCargo).lock();
                mapCountWaiting.put(typeCargo, mapCountWaiting.get(typeCargo) + waiting.get(typeCargo).size());

                if ( (!queuesAllShips.get(typeCargo).isEmpty())
                        && Objects.requireNonNull(queuesAllShips.get(typeCargo).peek()).getArriveTime().inMinutes() <=
                        now()) {
                    waiting.get(typeCargo).add(queuesAllShips.get(typeCargo).poll());
                }

                if ( (currentShip == null) && (!waiting.get(typeCargo).isEmpty())) {


                    currentShip = waiting.get(typeCargo).poll();
                    unloading.get(typeCargo).add(currentShip);

                    currentShip.increaseCrane();
                    currentDelay = DayHourMinute.randomMinute(0, 1440);
                    currentShip.setUnloadDelay(new DayHourMinute(currentDelay));

                    mapMaxDelay.put(typeCargo, Integer.max(mapMaxDelay.get(typeCargo), currentDelay));
                    mapSumDelay.put(typeCargo, currentDelay); // modified 4-22新增
                    mapSumUD.put(typeCargo, mapSumUD.get(typeCargo) + currentDelay);
                    mapFreeCranesCount.replace(typeCargo, mapFreeCranesCount.get(typeCargo) - 1);
                }


                if ( currentShip == null && !unloading.get(typeCargo).isEmpty()) {
                    for (Ship ship : unloading.get(typeCargo)) {
                        if (ship.getCranesCount() < 2 && mapFreeCranesCount.get(typeCargo) > 0) {
                            currentShip = ship;
                            currentShip.increaseCrane();
                            mapFreeCranesCount.replace(typeCargo, mapFreeCranesCount.get(typeCargo) - 1);
                            break;
                        }
                    }
                }

                if (currentShip == null) {
                    mapLockers.get(typeCargo).unlock();
                    cyclicBarrier.await();
                    continue;
                }
                else {
                    if (!currentShip.isUnloading())
                    {
                        currentShip.setUnloading(true);
                        DayHourMinute startTimeUnloading = new DayHourMinute();
                        startTimeUnloading.setMinute(now());
                        currentShip.setStartUnloadTime(startTimeUnloading);

                        DayHourMinute wd = new DayHourMinute();
                        wd.setMinute(currentShip.getStartUnloadTime().getMinute()-currentShip.getArriveTime().getMinute());
                        currentShip.setWaitDuration(wd);
                        mapSumWD.put(currentShip.getCargo().getCargoType(), wd.getMinute());
                    }

                    if (currentShip.isUnloading()) {
                        currentShip.getCargo().reduceAmountOfCargo(performance);
                    }

                    if (currentShip.getCargo().getWeight() == 0) {
                        if (currentShip.getCranesCount() > 0) {
                            currentShip.setCranesCount(currentShip.getCranesCount() - 1);
                            mapFreeCranesCount.replace(typeCargo, mapFreeCranesCount.get(typeCargo) + 1);
                        }

                        if (currentShip.getCranesCount() == 0) {

                            DayHourMinute finishTimeUnloading = new DayHourMinute();
                            finishTimeUnloading.setMinute(now());
                            currentShip.setFinishUnloadTime(finishTimeUnloading);
                            unloaded.get(typeCargo).add(currentShip);
                            currentShip.setUnloading(false);
                            unloading.get(typeCargo).remove(currentShip);
                        }
                        currentShip = null;
                    }

                }
                mapLockers.get(typeCargo).unlock();
                cyclicBarrier.await();
            }
            mapLockers.get(typeCargo).lock();

            if (currentShip != null) {
                int timeFine = now() - (currentShip.getArriveTime().inMinutes() +
                        currentShip.getUnloadDuration().inMinutes());
                if (timeFine > 0) {
                    timeFine = timeFine / 60 +
                            timeFine % 60 != 0 ? 1 : 0;
                    int fine = timeFine * 100;
                    mapFine.replace(typeCargo, mapFine.get(typeCargo) + fine);
                }
            }
            mapLockers.get(typeCargo).unlock();

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }



    static {
        for (CargoType typeOfCargo : CargoType.values()) {
            mapLockers.put(typeOfCargo,  new ReentrantLock());
        }
    }

}





















