package Service3;

import pojo.Ship;
import pojo.DayHourMinute;
import pojo.CargoType;
import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static Service3.Simulator.*;
import static utils.Constant.COAST_PER_HOUR;
import static utils.Constant.MINUTE_IN_THIRTY_DAYS;

public class Crane implements Runnable {

    private final Integer performance;
    private final CargoType cargoType;
    private static final ConcurrentMap<CargoType, Lock> mapLockers = new ConcurrentHashMap<>(); //ConcurrentMap- 每个线程对应一把锁
    public static volatile int fineCounter=0;

    public Crane(Integer performance, CargoType typeOfCargo) {
        this.performance = performance;
        this.cargoType = typeOfCargo;
    }

    public Integer getPerformance() {
        return performance;
    }

    public CargoType getCargoType() {
        return cargoType;
    }

    @Override
    /**
     * waiting -> current -> unloading -> unloaded
     */
    public void run() {
        Thread.currentThread().setName(Thread.currentThread().getName()+"-"+ cargoType.toString());
        /**
         * init
         */
        CyclicBarrier cyclicBarrier = Simulator.cyclicBarrier;
        Ship currentShip = null;
        int currentDelay = 0;

        try {
            while (now() < MINUTE_IN_THIRTY_DAYS) { // simulation duration : 30 days

                /**
                 * Current Crane try to lock. If not succeed, thread span and wait(CAS)
                 */
                mapLockers.get(cargoType).lock(); //lockers-CocurrentMap<TypeCargo, Lock>, 获取typeCargo对应的锁

                /**
                 * mapCountWaiting中【卸载中船的个数】加等于queuesForUnloading中船的个数
                 * 更新unloading（卸载中的船）的个数
                 *
                 *  mapCountWaiting - used to count waiting ships(for result statistics)
                 *  update mapCountWaiting : += waiting ships' size
                 */
                mapCountWaiting.put(cargoType, mapCountWaiting.get(cargoType) + waiting.get(cargoType).size());
                /**
                 * if 某船队的queuesAllShips不为空 且 某船队的queuesAllShips的第一支船（不可以为null）的到达时间<=now(queuesAllShips队首的船已经到达)
                 *  向queuesForUnloading中加入队列中的第一条船，把队列中的第一条船移出队列
                 * end
                 *
                 * queuesAllShips-all ships in 3 fleets
                 *  when the head of queuesAllShips arrives, add it into wait queue ( queuesAllShips.head-->waiting )
                 *
                 */
                    if ( (!queuesAllShips.get(cargoType).isEmpty())
                            && Objects.requireNonNull(queuesAllShips.get(cargoType).peek()).getArriveTime().inMinutes() <=
                            now()) {
                        // modified
                        System.out.println("Arrived a ship: "+ queuesAllShips.get(cargoType).peek().getName()
                                + "("+ queuesAllShips.get(cargoType).peek().getCargo().getCargoType() +"): arrival time: "
                                + queuesAllShips.get(cargoType).peek().getArriveTime()+" ...\n"
                                + "-1->Crane: "
                                + Thread.currentThread()+" is adding "+ queuesAllShips.get(cargoType).peek().getName()
                                +" into waiting queue...");
                        waiting.get(cargoType).add(queuesAllShips.get(cargoType).poll());
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
                if ( (currentShip == null) && (!waiting.get(cargoType).isEmpty())) {

                        System.out.println("-2->Crane: "+Thread.currentThread()+" is adding "+ waiting.get(cargoType).peek().getName()+" into loading queue...");

                        currentShip = waiting.get(cargoType).poll();
                        unloading.get(cargoType).add(currentShip); // queuesForUnloading --> nowInUnloading

                        currentShip.increaseCrane(); // 当前为unloadingShip服务的起重机数量++
                        currentDelay = DayHourMinute.generateRandomMinute(0, 1440); // 设置随机的 完成卸货偏移时间delay, set a random dalay(0~1440)
                        currentShip.setUnloadDelay(new DayHourMinute(currentDelay));
                        // （船的卸货时间也许会比预期更多，范围是0~1440分钟.也就是说delay的范围是0~1440分钟）

                        mapMaxDelay.put(cargoType, Integer.max(mapMaxDelay.get(cargoType), currentDelay)); // 比较：maxTimeStop中和timeStop比较，谁大
                        mapSumDelay.put(cargoType, currentDelay); // modified 4-22新增
                        mapSumUD.put(cargoType, mapSumUD.get(cargoType) + currentDelay); // todo 将sumTimeStop加上timeStop(有点问题)
                        mapFreeCranesCount.replace(cargoType, mapFreeCranesCount.get(cargoType) - 1); // 船队所对应的countFreeCranes数量-1

                        System.out.println("-3->Crane: "+Thread.currentThread()+", currentShip:"+ currentShip.getName() +" delay="+currentDelay+"(min), cranes= "+currentShip.getCranesCount());
                    }

                /**
                 * 如果queuesWaiting已经为空,那么currentShip就为null, 这种情况下会进去这个循环
                 * 【进入条件】queuesWaiting为空，但unloading不为空。
                 * 那么应该把unloading中的ship
                 */
                    if ( currentShip == null && !unloading.get(cargoType).isEmpty()) {
                        for (Ship ship : unloading.get(cargoType)) {
                            /**
                             * 只会对【队首的一条船】进行cranes++。完成之后即刻break
                             */
                            if (ship.getCranesCount() < 2 && mapFreeCranesCount.get(cargoType) > 0) {
                                currentShip = ship;
                                currentShip.increaseCrane(); // todo 这里是不是应该把UD/=2？
                                mapFreeCranesCount.replace(cargoType, mapFreeCranesCount.get(cargoType) - 1);
                                System.out.println("-4->Crane: "+Thread.currentThread()+". Unloading is not empty. currentShip="+currentShip.getName()
                                        +", cranes="+currentShip.getCranesCount()
                                +"(after reducing)Free cranes count="+mapFreeCranesCount.get(cargoType));
                                break;
                            }
                        }
                    }

                if (currentShip == null) {
//                    System.out.println(
//                                "【这里的输出实在太多了】-5->Crane: "+Thread.currentThread()+"unloading.size="+unloading.size()+Thread.currentThread()+" releasing lock...");
                    mapLockers.get(cargoType).unlock();
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
                        System.out.println("~~> ship: "+currentShip.getName() + " wait duration: "+ currentShip.getWaitDuration().getMinute());
                        // ------ 4-21 -----
                        System.out.println("-6->Crane: "+Thread.currentThread()+". currentShip: "+currentShip.getName()+" starts unloading...");
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
                            mapFreeCranesCount.replace(cargoType, mapFreeCranesCount.get(cargoType) + 1);
                        }

                        if (currentShip.getCranesCount() == 0) {

                            DayHourMinute finishTimeUnloading = new DayHourMinute();
//                            System.out.println("######getTime()="+getTime()); // todo【？？】
                            finishTimeUnloading.setMinute(now());
                            currentShip.setFinishUnloadTime(finishTimeUnloading);
                            System.out.println("-10->Crane: "+Thread.currentThread()+ " finished unloading " + currentShip.getName()
                                    +". Finished unload time:"+finishTimeUnloading);

                            System.out.println("-11-> unloading.size="+unloading.get(cargoType).size()+" unloaded.size="+unloaded.get(cargoType).size());

                            unloaded.get(cargoType).add(currentShip);
                            currentShip.setUnloading(false);
                            //System.out.println(unloadedShips.get(typeOfCargo).size() + typeOfCargo.toString());
                            unloading.get(cargoType).remove(currentShip);

                            System.out.println("-12->Crane: "+Thread.currentThread()+" removed "+currentShip.getName()+" to unloaded.");

                            System.out.println("-13-> unloading.size="+unloading.get(cargoType).size()+" unloaded.size="+unloaded.get(cargoType).size());
                            //                          System.out.println(queuesAllShips.get(typeOfCargo).size() + typeOfCargo.toString());
                        }
                        currentShip = null;
                    }

                }
                mapLockers.get(cargoType).unlock();
                cyclicBarrier.await();
            } // while
            mapLockers.get(cargoType).lock();

            /**
             * Calculate fine.
             * 算法1: timeFine= FUT-(AT+UD)
             *                 int timeFine = currentShip.getFinishUnloadTime().inMinutes() - (currentShip.getArriveTime().inMinutes()
             *                         + currentShip.getUnloadDuration().inMinutes());
             * 算法2: timeFine= WD+delay
             *      int timeFine = currentShip.getWaitDuration().inMinutes()+currentShip.getUnloadDelay().inMinutes();
             *
             */
            if (currentShip != null) {
                // modified
                System.out.println("-->Crane: Counted ships num="+(++fineCounter));
                int timeFine = currentShip.getWaitDuration().inMinutes()+currentShip.getUnloadDelay().inMinutes();
                if (timeFine > 0) {
                    timeFine = timeFine / 60 + timeFine % 60 != 0 ? 1 : 0;
                    int fine = timeFine * COAST_PER_HOUR;
                    mapFine.replace(cargoType, mapFine.get(cargoType) + fine);
                    System.out.println("current ship is " + currentShip.getName()+" fine is "+fine);
                }
            }
            mapLockers.get(cargoType).unlock();

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }


    /**
     * 3 times.
     */
    static {
        for (CargoType typeOfCargo : CargoType.values()) {
            System.out.println("static block: typeCargo="+typeOfCargo);
            mapLockers.put(typeOfCargo,  new ReentrantLock()); /*不公平队列*/
        }
    }

}



























/**
 * static object - all the cranes share mapLockers, because in specific fleet(e.x. fleet liquid ships)
 *                 they need to conduct unload task serially
 *  As the following text suggests:
 *  | all LOOSE -- a lock     |
 *  | all LIQUID -- a lock    |
 *  | all CONTAINER -- a lock |
 *
 * ConcurrentHashMap(CHM):
 *  1. Retrieval operations (including get) generally do not block, so may overlap with update operations (including put and remove).
 *  2.
 *  Why CHM?
 *      1. Efficient, one of the most common used concurrent container
 *      2. Every fleet owners a lock, but fleets can work concurrently.
 *          (e.x. When LIQUID fleet works, CONTAINER fleet is not blocking. Instead, it can work too)
 *
 */




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