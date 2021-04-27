package service3;

import pojo.CargoType;
import pojo.DayHourMinute;
import pojo.Ship;

import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static service3.Port.*;
import static utils.Constant.MINUTE_IN_THIRTY_DAYS;

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
        CyclicBarrier cyclicBarrier = Port.cyclicBarrier; // 把Port的cyclicBarrier复制一份

        Ship currentShip = null;

        try {
            while (now() < MINUTE_IN_THIRTY_DAYS) { //30天内

                /**
                 * Схватит lock
                 */
                mapLockers.get(typeCargo).lock();

                /**
                 * число ожидающих суднов += size waiting
                 *
                 *  mapCountWaiting - used to count waiting ships(for result statistics)
                 *  update mapCountWaiting : += waiting ships' size
                 */
                mapCountWaiting.put(typeCargo, mapCountWaiting.get(typeCargo) + waiting.get(typeCargo).size());

                if ( (!queuesAllShips.get(typeCargo).isEmpty())
                        && Objects.requireNonNull(queuesAllShips.get(typeCargo).peek()).getArriveTime().inMinutes() <=
                        now()) {
                    // modified
                    System.out.println("Arrived a ship: "+ queuesAllShips.get(typeCargo).peek().getName()
                            + " arrival time: " + queuesAllShips.get(typeCargo).peek().getArriveTime()
                            + "("+ queuesAllShips.get(typeCargo).peek().getCargo().getCargoType() +")...\n"
                            + "-1->Crane: "
                            + Thread.currentThread()+" is adding "+ queuesAllShips.get(typeCargo).peek().getName()
                            +" into waiting queue...");
                    waiting.get(typeCargo).add(queuesAllShips.get(typeCargo).poll());
                }

                /**
                 * currentShip==null means no ship in the front of currentShip
                 * Then we add this current ship into loading queue:
                 *  1.print info
                 *  2.move: waiting.head --> unloading
                 *  3.update current ship:
                 *      -cranes amount ++
                 *      -unload delay <--- a random value between 0 and 1440
                 *  4.update simulator data:
                 *      -update max delay: max(maxDelay, currentDelay)
                 *      -update sum delay: +=currentDelay
                 *      -update sum unload duration: += currentDelay
                 *  5.print info of current ship
                 */
                if ( (currentShip == null) && (!waiting.get(typeCargo).isEmpty())) {
                    int currentDelay = 0;

                    System.out.println("-2->Crane: "+Thread.currentThread()+" is adding "+ waiting.get(typeCargo).peek().getName()+" into loading queue...");

                    currentShip = waiting.get(typeCargo).poll();
                    unloading.get(typeCargo).add(currentShip); // queuesForUnloading --> nowInUnloading

                    currentShip.increaseCrane();
                    currentDelay = DayHourMinute.randomMinute(0, 1440);
                    currentShip.setUnloadDelay(new DayHourMinute(currentDelay));

                    mapMaxDelay.put(typeCargo, Integer.max(mapMaxDelay.get(typeCargo), currentDelay)); // 比较：maxTimeStop中和timeStop比较，谁大
                    mapSumDelay.put(typeCargo, currentDelay); // modified 4-22新增
                    mapSumUD.put(typeCargo, mapSumUD.get(typeCargo) + currentDelay);
                    mapFreeCranesCount.replace(typeCargo, mapFreeCranesCount.get(typeCargo) - 1); // 船队所对应的countFreeCranes数量-1

                    System.out.println("-3->Crane: "+Thread.currentThread()+", currentShip:"+ currentShip.getName() +" delay="+currentDelay+"(min), cranes= "+currentShip.getCranesCount());
                }

                if ( currentShip == null && !unloading.get(typeCargo).isEmpty()) {
                    for (Ship ship : unloading.get(typeCargo)) {
                        /**
                         * 只会对【队首的一条船】进行cranes++。完成之后即刻break
                         */
                        if (ship.getCranesCount() < 2 && mapFreeCranesCount.get(typeCargo) > 0) {
                            currentShip = ship;
                            currentShip.increaseCrane(); // todo 这里是不是应该把UD/=2？
                            mapFreeCranesCount.replace(typeCargo, mapFreeCranesCount.get(typeCargo) - 1);
                            System.out.println("-4->Crane: "+Thread.currentThread()+". Unloading is not empty. currentShip="+currentShip.getName()
                                    +", cranes="+currentShip.getCranesCount()
                                    +"(after reducing)Free cranes count="+mapFreeCranesCount.get(typeCargo));
                            break;
                        }
                    }
                }

                if (currentShip == null) {
                    mapLockers.get(typeCargo).unlock();
                    cyclicBarrier.await();
                    continue;
                }
                else { // currentShip != empty, != unloading
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
                        System.out.println("~~~> Crane: current ship WD="+wd.getMinute());
                        System.out.println("-6->Crane: "+Thread.currentThread()+". currentShip: "+currentShip.getName()+"starts unloading...");
                    }

                    if (currentShip.isUnloading()) {
                        currentShip.getCargo().reduceAmountOfCargo(performance);
                    }

                    if (currentShip.getCargo().getWeight() == 0) {
                        System.out.println("-8->Crane: "+Thread.currentThread()+" finish unloading "+currentShip.getName()); // 根本没有船进来!!!

                        if (currentShip.getCranesCount() > 0) {
                            System.out.println(
                                    "-9->Crane "+Thread.currentThread()+ " finished unloading " + currentShip.getName()
                                            + "with " + currentShip.getCranesCount() + " crane(s). Releasing the crane...");

                            currentShip.setCranesCount(currentShip.getCranesCount() - 1);
                            mapFreeCranesCount.replace(typeCargo, mapFreeCranesCount.get(typeCargo) + 1);
                        }

                        if (currentShip.getCranesCount() == 0) {

                            DayHourMinute finishTimeUnloading = new DayHourMinute();

                            finishTimeUnloading.setMinute(now());
                            currentShip.setFinishUnloadTime(finishTimeUnloading);
                            System.out.println("-10->Crane: "+Thread.currentThread()+ " finished unloading " + currentShip.getName()
                                    +". Finished unload time:"+finishTimeUnloading);

                            System.out.println("-11-> unloading.size="+unloading.get(typeCargo).size()+" unloaded.size="+unloaded.get(typeCargo).size());

                            unloaded.get(typeCargo).add(currentShip);
                            currentShip.setUnloading(false);
                            unloading.get(typeCargo).remove(currentShip);

                            System.out.println("-12->Crane: "+Thread.currentThread()+" removed "+currentShip.getName()+" to unloaded.");

                            System.out.println("-13-> unloading.size="+unloading.get(typeCargo).size()+" unloaded.size="+unloaded.get(typeCargo).size());
                        }
                        currentShip = null;
                    }

                }
                mapLockers.get(typeCargo).unlock();
                cyclicBarrier.await();
            } // while
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
            System.out.println("TypeCargo="+typeOfCargo);
            mapLockers.put(typeOfCargo,  new ReentrantLock());
        }
    }

}
