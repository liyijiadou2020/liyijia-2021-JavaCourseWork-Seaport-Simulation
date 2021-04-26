package com.example.service3;

import com.example.pojo.CargoType;
import com.example.pojo.DayHourMinute;
import com.example.pojo.Ship;

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
    /**
     * static object - all the cranes share mapLockers, because in specific fleet(e.x. fleet liquid ships)
     *                 they need to conduct unload task serially
     *  As the following text suggests:
     *  | all LOOSE -- lock     |
     *  | all LIQUID -- lock    |
     *  | all CONTAINER -- lock |
     *
     * ConcurrentHashMap(CHM):
     *  1. Retrieval operations (including get) generally do not block, so may overlap with update operations (including put and remove).
     *  2.
     *  Why CHM?
     *      1. efficient, one of the most common used concurrent container
     *      2. Every fleet owners a lock, but fleets can work concurrently.
     *          (e.x. When LIQUID fleet works, CONTAINER fleet is not blocking, instead it can work too)
     */
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
        // modified 4-22
        Thread.currentThread().setName(Thread.currentThread().getName()+"-"+typeCargo.toString());
        CyclicBarrier cyclicBarrier = Simulator.cyclicBarrier; // 把Port的cyclicBarrier复制一份

        Ship currentShip = null;
        int currentDelay = 0; // currentParkDuration

        try {
            while (now() < MINUTE_IN_THIRTY_DAYS) { //30天内

                /**
                 * Current Crane try to lock, if not succeed, Thread span and wait
                 */
                mapLockers.get(typeCargo).lock();

                /**
                 * mapCountWaiting中【卸载中船的个数】加等于queuesForUnloading中船的个数
                 * 更新unloading（卸载中的船）的个数
                 *
                 *  mapCountWaiting - used to count waiting ships(for result statistics)
                 *  update mapCountWaiting : += waiting ships' size
                 */
                mapCountWaiting.put(typeCargo, mapCountWaiting.get(typeCargo) + waiting.get(typeCargo).size());
                /**
                 * if 某船队的queuesAllShips不为空 且 某船队的queuesAllShips的第一支船（不可以为null）的到达时间<=now(queuesAllShips队首的船已经到达)
                 *  向queuesForUnloading中加入队列中的第一条船，把队列中的第一条船移出队列
                 * end
                 *
                 * queuesAllShips-all ships in 3 fleets
                 *  when the head of queuesAllShips arrives, add it into wait queue ( queuesAllShips.head-->waiting )
                 *
                 */
                    if ( (!queuesAllShips.get(typeCargo).isEmpty())
                            && Objects.requireNonNull(queuesAllShips.get(typeCargo).peek()).getArriveTime().receiveTimeInMinute() <=
                            now()) {
                        // modified
                        System.out.println("Arrived a ship: "+ queuesAllShips.get(typeCargo).peek().getName()
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

                        System.out.println("-2->Crane: "+Thread.currentThread()+" is adding "+ waiting.get(typeCargo).peek().getName()+" into loading queue...");

                        currentShip = waiting.get(typeCargo).poll();
                        unloading.get(typeCargo).add(currentShip); // queuesForUnloading --> nowInUnloading

                        currentShip.increaseCrane(); // 当前为unloadingShip服务的起重机数量++
                        currentDelay = DayHourMinute.randomMinute(0, 1440); // 设置随机的 完成卸货偏移时间delay, set a random dalay(0~1440)
                        currentShip.setUnloadDelay(new DayHourMinute(currentDelay));
                        // （船的卸货时间也许会比预期更多，范围是0~1440分钟.也就是说delay的范围是0~1440分钟）

                        mapMaxDelay.put(typeCargo, Integer.max(mapMaxDelay.get(typeCargo), currentDelay)); // 比较：maxTimeStop中和timeStop比较，谁大
                        mapSumDelay.put(typeCargo, currentDelay); // modified 4-22新增
                        mapSumUD.put(typeCargo, mapSumUD.get(typeCargo) + currentDelay); // todo 将sumTimeStop加上timeStop(有点问题)
                        mapFreeCranesCount.replace(typeCargo, mapFreeCranesCount.get(typeCargo) - 1); // 船队所对应的countFreeCranes数量-1

                        System.out.println("-3->Crane: "+Thread.currentThread()+", currentShip:"+ currentShip.getName() +" delay="+currentDelay+"(min), cranes= "+currentShip.getCranesCount());
                    }

                /**
                 * if 没有正在卸货的船（nowInUnloading）
                 *    if 某种船队的nowInUnloading非空
                 *    then
                 *      for 船队的每一条nowInUnloading中的船ship
                 *          if ship的服务起重机数量<2并且有空闲的起重机
                 *          then 把ship放到unloadingShip去
                 *               unloadingShip的起重机数量++
                 *               countFreeCranes的数量-1
                 *          end
                 *      end
                 *    end
                 * end
                 *
                 */

                /**
                 * 如果queuesWaiting已经为空,那么currentShip就为null, 这种情况下会进去这个循环
                 * 【进入条件】queuesWaiting为空，但unloading不为空。
                 * 那么应该把unloading中的ship
                 */
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
//                    System.out.println(
//                                "【这里的输出实在太多了】-5->Crane: "+Thread.currentThread()+"unloading.size="+unloading.size()+Thread.currentThread()+" releasing lock...");
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

                        // ----- modified 4-21新增：sumWD  WD=SUT-AT
                        DayHourMinute wd = new DayHourMinute();
                        wd.setMinute(currentShip.getStartUnloadTime().getMinute()-currentShip.getArriveTime().getMinute());
                        currentShip.setWaitDuration(wd);
                        mapSumWD.put(currentShip.getCargo().getCargoType(), wd.getMinute());
                        System.out.println("~~~> Crane: current ship WD="+wd.getMinute());
                        // ------ 4-21 -----
                        System.out.println("-6->Crane: "+Thread.currentThread()+". currentShip: "+currentShip.getName()+"starts unloading...");
                    }

                    if (currentShip.isUnloading()) {
                        currentShip.getCargo().reduceAmountOfCargo(performance);
//                        System.out.println(
//                                "【这里的输出实在太多了】-7->Crane: "+Thread.currentThread()+ " is working: removing "+performance+" T/PCS from "
//                                        + currentShip.getName());
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
//                            System.out.println("######getTime()="+getTime()); // todo【？？】
                            finishTimeUnloading.setMinute(now());
                            currentShip.setFinishUnloadTime(finishTimeUnloading);
                            System.out.println("-10->Crane: "+Thread.currentThread()+ " finished unloading " + currentShip.getName()
                                    +". Finished unload time:"+finishTimeUnloading);

                            System.out.println("-11-> unloading.size="+unloading.get(typeCargo).size()+" unloaded.size="+unloaded.get(typeCargo).size());

                            unloaded.get(typeCargo).add(currentShip);
                            currentShip.setUnloading(false);
                            //System.out.println(unloadedShips.get(typeOfCargo).size() + typeOfCargo.toString());
                            unloading.get(typeCargo).remove(currentShip);

                            System.out.println("-12->Crane: "+Thread.currentThread()+" removed "+currentShip.getName()+" to unloaded.");

                            System.out.println("-13-> unloading.size="+unloading.get(typeCargo).size()+" unloaded.size="+unloaded.get(typeCargo).size());
                            //                          System.out.println(queuesAllShips.get(typeOfCargo).size() + typeOfCargo.toString());
                        }
                        currentShip = null;
                    }

                }
                mapLockers.get(typeCargo).unlock();
                cyclicBarrier.await();
            } // while
            mapLockers.get(typeCargo).lock();

            if (currentShip != null) {
                int timeFine = now() - (currentShip.getArriveTime().receiveTimeInMinute() +
                        currentShip.getUnloadDuration().receiveTimeInMinute());
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


    /**
     * ConcurrentMap是一个接口。 ConcurrentHashMap是一个实现类
     * public interface ConcurrentMap<K, V> extends Map<K, V> {
     *     V putIfAbsent(K key, V value);               //插入元素
     *     boolean remove(Object key, Object value);    //移除元素
     *     boolean replace(K key, V oldValue, V newValue);  //替换元素
     *     V replace(K key, V value);  //替换元素
     * }
     * ConcurrentHashMap是一个线程安全，并且是一个高效的HashMap。
     *
     */

    /**
     * 3 times.
     */
    static {
        for (CargoType typeOfCargo : CargoType.values()) {
            System.out.println("TypeCargo="+typeOfCargo);
            mapLockers.put(typeOfCargo,  new ReentrantLock()); /*不公平队列*/
        }
    }

}
