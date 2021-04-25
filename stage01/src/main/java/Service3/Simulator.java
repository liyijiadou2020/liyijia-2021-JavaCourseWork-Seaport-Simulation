package Service3;

import Service2.MyJsonReaderWriter;
import pojo.Ship;
import pojo.Timetable;
import pojo.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import static Service2.MyJsonReaderWriter.getTimetableFromJson;
import static pojo.CargoType.*;
import static utils.Constant.*;
import static utils.ParameterFormer.*;


public class Simulator {

    private static Statistics result = new Statistics();
    private static Timetable timetable;
    private static ArrayList<Ship> ships=new ArrayList<>(); //TreeSet<Ship>
    private static final Map<CargoType, Boolean> isMinFine = new HashMap<>();
    private static int countUnloaded=0;
    public static Integer sumFine = 0;
    public static volatile TaskTimer timer; // todo 用来干什么??? volatile-写的时候强制所有线程可见,用来在多线程数据同步的
    public static CyclicBarrier cyclicBarrier; //它的作用就是会让所有线程都等待完成后才会继续下一步行动。

    public static Map<CargoType, Double> mapSumUD = new HashMap<>(); /* 停靠时间总和, sumTimeStop */
    public static Map<CargoType, Integer> mapSumDelay = new HashMap<>(); /* 停靠时间总和, sumTimeStop */ // modified 4-22新增
    public static Map<CargoType, Integer> mapMaxDelay = new HashMap<>();/* 最大停靠时间, maxTimeStop, mapMaxPD */
    public static Map<CargoType, Integer> mapCountWaiting = new HashMap<>(); /* 等待卸货的船的个数?, countWaitUnloadingShips */
    public static Map<CargoType, Integer> mapCountUnloaded = new HashMap<>(); /* 完成卸货的船的个数, countShipsUnloaded */
    public static Map<CargoType, Integer> mapSumWD = new HashMap<>();

    /**
     * CopyOnWriteArrayList-Copy when writing, thread safe, fix to the situation that read more but write less often
     */
    public static ConcurrentMap<CargoType, CopyOnWriteArrayList<Ship>> unloading = new ConcurrentHashMap<>(); /*正在卸货的船*/
    public static ConcurrentMap<CargoType, CopyOnWriteArrayList<Ship>> unloaded = new ConcurrentHashMap<>();  /*完成卸货的船*/
    /**
     * ConcurrentLinkedQueue- in CAS algorithm,(non-blocking queue), FIFO.
     */
    public static ConcurrentMap<CargoType, LinkedBlockingQueue<Ship>> queuesAllShips = new ConcurrentHashMap<>();  /*队列中的卸货的船*/
    public static ConcurrentMap<CargoType, LinkedBlockingQueue<Ship>> waiting = new ConcurrentHashMap<>(); /* ??? 和上面的区别是?queuesForUnloading, queuesWaiting */
    public static ConcurrentMap<CargoType, Integer> mapFreeCranesCount = new ConcurrentHashMap<>(); //空闲的起重机, executeModeling/mapCountFreeCranes
    public static ConcurrentMap<CargoType, Integer> mapFine = new ConcurrentHashMap<>(); // 罚金
    public static Map<CargoType, List<Thread>> mapCranes = new HashMap<>(); // 起重机列表
    public static Map<CargoType, Integer> cranesCount = new HashMap<>(); // 起重机数量
    public static Map<CargoType, Integer> mapPerformanceCrane = new HashMap<>(); // 速度


    public static Statistics getResult() {
        return result;
    }

    public static void simulate() throws IOException {
        init();
        optimize();

        printUnloadedShips();
        generateResultStatistics();
        printStatistics(result);

    }

    private static void generateResultStatistics() {

        double countWaiting = 0;
        int countUnloaded = 0;
        double sumWD = 0;
        double sumDelay = 0.0;
        int maxDelay = 0;

        for (CargoType typeCargo : values()) {
            for (Ship ship : unloaded.get(typeCargo)) {
                if (!isMinFine.get(typeCargo))  // care 没有进去 //WD+=(SUT-AT)
                    mapSumWD.put(typeCargo,mapSumWD.get(typeCargo) + ( ship.getStartUnloadTime().inMinutes() - ship.getArriveTime().inMinutes() ));
            }

            mapCountUnloaded.put(typeCargo, mapCountUnloaded.get(typeCargo) + unloaded.get(typeCargo).size());
            sumFine += mapFine.get(typeCargo) - (cranesCount.get(typeCargo) - 1) * COAST_ONE_CRANE;
            sumDelay += mapSumDelay.get(typeCargo); // modified:原本:sumDelay += mapSumUD.get(typeCargo)
            countUnloaded += mapCountUnloaded.get(typeCargo);
            countWaiting += mapCountWaiting.get(typeCargo);
            maxDelay = Integer.max(mapMaxDelay.get(typeCargo), maxDelay);
        }
        sumWD = summaryWD();


        result.setCountUnloaded(countUnloaded);
        result.setCountLoose(cranesCount.get(LOOSE));
        result.setCountLiquid(cranesCount.get(LIQUID));
        result.setCountContainer(cranesCount.get(CONTAINER));
        result.setTotalFine(sumFine);
        result.setSumWaitDuration(sumWD);
        result.setAvrWaitDuration((sumWD/countUnloaded));
        result.setSumDelay(sumDelay);
        result.setAvrDelay(sumDelay/countUnloaded);
        result.setMaxDelay(maxDelay);
        result.setAvrWaitLength(countWaiting/MINUTE_IN_THIRTY_DAYS);

    }

    private static void init() {
        initTimetableFromJson("timetable.json");
        initPerformanceFromJson("performance.json");
        initShips();
        initCountCranes();

    }

    private static void initPerformanceFromJson(String filename) {
        Performance performanceCranes =  MyJsonReaderWriter.getPerformanceFromJson(filename);
        mapPerformanceCrane.put(LOOSE, performanceCranes.getLoosePerformance());
        mapPerformanceCrane.put(LIQUID, performanceCranes.getLiquidPerformance());
        mapPerformanceCrane.put(CONTAINER, performanceCranes.getContainerPerformance());
    }


    private static void initTimetableFromJson(String filename) {
        timetable = getTimetableFromJson(filename);
    }



    private static void initShips() {
        for (Schedule nodeTimetable : timetable.getSchedules()) {
            Ship ship;
            try {
                ship = new Ship(nodeTimetable.getArriveTime().addTime(DayHourMinute.generateRandomMinute(-MINUTE_IN_SEVEN_DAYS, MINUTE_IN_SEVEN_DAYS)), //初始化一条船, arrive time
                        nodeTimetable.getNameShip(), // shipName
                        nodeTimetable.getCargo(), // cargo
                        nodeTimetable.getUnloadDuration()); // unload minutes
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            ships.add(ship);
        }
    }

    private static void initBeforeOptimization() {
        initStatistic();
        initQueues();
    }



        static {
        for (CargoType typeCargo: values()) {
            isMinFine.put(typeCargo, false);
        }
    }

    private static void initStatistic() {
        sumFine = 0;
        for (CargoType cargo : values()) {
            if (!isMinFine.get(cargo)) {
                mapSumWD.put(cargo, 0);
                mapCountWaiting.put(cargo, 0);
                mapMaxDelay.put(cargo, 0);
                mapSumUD.put(cargo, 0.0);
                mapSumDelay.put(cargo, 0);
                mapCountUnloaded.put(cargo, 0);
                mapFine.put(cargo, 0);
            }
        }
    }


    private static void optimize() {

        /**
         * MUST clear old data before new-round optimization:
         *         initStatistic(); //  set countWaiting, countUnloaded, sumDelay, maxDelay, sumWD, sumUD, fine to 0
         *         initQueues(); // initialize queueShips. Put all the ships into queueShips to start new-round optimize
         *         modeling(); //
         *         countFine();
         *
         */
        initBeforeOptimization();
        modeling();
        countFine();

        /**
         * Mark down the old solution(count of 3 types of cranes)
         */
        ConcurrentMap<CargoType, Integer> oldMapFine = new ConcurrentHashMap<>();
        for (CargoType typeCargo : values()) {
            oldMapFine.put(typeCargo, mapFine.get(typeCargo));
        }

        /**
         * Only when all 3 fleets(exist 3 fleets: container type, loose type and liquid type)
         * find optimization, can the loop breaks.
         */
        while (!isMinFine.get(LOOSE) || !isMinFine.get(LIQUID) || !isMinFine.get(CONTAINER)) {
            /**
             * If not optimical yet, add a crane for fleet, and try new solution.
             */
            for (CargoType typeCargo : values()) {
                if (!isMinFine.get(typeCargo))   mapFreeCranesCount.put(typeCargo, mapFreeCranesCount.get(typeCargo) + 1);
                else  mapFreeCranesCount.put(typeCargo, 0);
            }
            initBeforeOptimization();
            modeling();
            countFine();

            for (CargoType typeCargo : values()) {
                if (!isMinFine.get(typeCargo)) {
                    if (mapFine.get(typeCargo) + COAST_ONE_CRANE * (mapFreeCranesCount.get(typeCargo) - 1) > oldMapFine.get(typeCargo)){
                        mapFine.put(typeCargo, oldMapFine.get(typeCargo));
                        cranesCount.put(typeCargo, mapFreeCranesCount.get(typeCargo) - 1);
                        isMinFine.put(typeCargo, true);
                    } else{
                        oldMapFine.put(typeCargo, mapFine.get(typeCargo) + COAST_ONE_CRANE * (mapFreeCranesCount.get(typeCargo) - 1));
                    }
                }
            }
        }

        System.out.println("Optimal result is found!");
    }

    private static void printUnloadedShips() {
        System.out.println(RESULT_HEADER_LINE);
        System.out.println(UNLOADED_HEADER);
        int counter = 0;
        for (CargoType typeCargo : values()) {
            for (Ship ship : unloaded.get(typeCargo)) {
                System.out.println(stringShip(ship));
                ++counter;
            }
        }
        System.out.println("| COUNT="+counter);
        System.out.println(SINGLE_BOTTOM_LINE);
    }

    private static double summaryWD(){
        double sum=0;
        for (CargoType t: CargoType.values()) {
            for (Ship s: unloaded.get(t)) {
                sum += s.getWaitDuration().getMinute();
            }
        }
        return sum;
    }

    /**
     *
     * 计算金额的总数：
     * 对于每一个类型
     *  对于船队中的每一条已经完成卸载的船
     *      计算罚款时间（ 公式：罚款时间 = 完成卸载时刻-（实际到达时刻+卸货的时间） ） timeFine=FUT-(AT+UD)
     *      计算罚款金额（ 公式：罚款时间*100， 注意，未满1小时要按照1小时收费 ）
     *      算好了金额，放入mapFine中
     *  对于船队中每一条等待卸货的船
     *      计算罚款时间（ 公式：30天的时间-（到达时刻+卸货的时间） ） 30daysEndTime - (AT+UD)
     *      计算罚款金额（ 公式：罚款时间*100， 注意，未满1小时要按照1小时收费 ）
     *      算好了金额，放入mapFine中
     *
     */
    private static void countFine() {
        for (CargoType typeCargo : values()) {
            for (Ship ship : unloaded.get(typeCargo)) { //对于已经卸货的船
                int timeFine = (ship.getFinishUnloadTime().inMinutes() - // (罚款时间 = 完成卸载时刻-（实际到达时刻+卸货的时间）)
                        (ship.getArriveTime().inMinutes() +
                                ship.getUnloadDuration().inMinutes()));
                if (timeFine > 0) {
                    timeFine = timeFine / 60 + (timeFine % 60 == 0 ? 0 : 1);
                    int fine = timeFine * 100;
                    mapFine.put(typeCargo, mapFine.get(typeCargo) + fine); // 算好了金额，放入mapFine中
                }
            }

            for (Ship ship : waiting.get(typeCargo)) {
                int timeFine = (MINUTE_IN_THIRTY_DAYS - /* 罚款时间=30天的时间-（到达时刻+卸货的时间） */
                        (ship.getArriveTime().inMinutes() +
                                ship.getUnloadDuration().inMinutes()));
                if (timeFine > 0) {
                    timeFine = timeFine / 60 + (timeFine % 60 == 0 ? 0 : 1);
                    int fine = timeFine * 100;
                    mapFine.put(typeCargo, mapFine.get(typeCargo) + fine);
                }
            }
        }
    }// countFine

    private static void modeling() {

        /**
         * initialize timer
         */
        timer = new TaskTimer();
        int countAllCranes = 0;
        Map<CargoType, Integer> mapOldFreeCranesCount = new HashMap<>();

        /**
         * 1. summary all cranes into countAllCranes
         * 2. put all cranes in mapFreeCranesCount into mapOldFreeCranesCount (for comparing later )
         * 将countOfFreeCranes中的值刷入countAllCranes，
         * 将countOfFreeCranes的值刷入oldCountOfFreeCranes（注意，oldCountOfFreeCranes不是同步的,因为它不需要同步）
         */
        for (CargoType typeCargo : values()) {
            countAllCranes += mapFreeCranesCount.get(typeCargo); // 将countOfFreeCranes中的所有值加入countAllCranes
            mapOldFreeCranesCount.put(typeCargo, mapFreeCranesCount.get(typeCargo)); // 把countOfFreeCranes中的值放入oldCountOfFreeCranes
        }
        /**
         * Cyclic-可以重用，Barrier-想要让一组线程等待到某个状态的那个状态成为barrier
         *
         * CyclicBarrier：countAllCranes-一起跑的线程数. timer-任务类,里面放了所有线程完成了(跑到了终点)之后要执行的动作
         * 当countAllCranes这么多个线程到达了之后，最后一个线程去做timer的任务：nowTime++
         * 使用需求：一个线程组的线程需要等待所有线程完成任务后再继续执行下一次任务
         */
        cyclicBarrier = new CyclicBarrier(countAllCranes, timer);
        /**
         *
         * For every fleet, find optimization. If not optimal yet:
         *  1. initialize queues, used in Crane.run(): unloading, unloaded, mapCranes, waiting
         *  2. put all cranes in mapFreeCranesCount into mapCranes
         *  3. start threads in mapCranes
         *
         *
         * 这一个for循环里面做了什么：
         * 对于每个类型：
         * 1. 初始化nowInUnloading，unloadedShips，mapOfCranes
         * 2. countOfFreeCranes里的所有起重机全部加入mapOfCranes
         * 3. 启动mapOfCranes中的所有起重机
         *
         */
        for (CargoType typeCargo : values()) {

            if (!isMinFine.get(typeCargo)) {

                System.out.println("#Port-------> init queues");

                // 初始化
                unloading.put(typeCargo, new CopyOnWriteArrayList<>()); // 初始化等待中的船
                unloaded.put(typeCargo, new CopyOnWriteArrayList<>()); // 初始化完成卸货的船b
                mapCranes.put(typeCargo, new LinkedList<>()); // 初始化所拥有的cranes
                waiting.put(typeCargo, new LinkedBlockingQueue<>()); // modified 4-22 ConcurrentLinkedQueue

                /**
                 * 1. 把freeCrane（空闲的起重机）加入到mapCranes中（总的起重机）。
                 *  mapCranes： Map<TC, List<Thread>>
                 *        List<Thread>就是所有起重机的list
                 * 2. 将mapCranes的List<Thread>中的每一个Thread（即Crane）都启动起来
                 *
                 */
                for (int i = 0; i < mapFreeCranesCount.get(typeCargo); i++) {
                    mapCranes.get(typeCargo).add(new Thread(new Crane(mapPerformanceCrane.get(typeCargo),
                            typeCargo)));
                }

                for (Thread crane : mapCranes.get(typeCargo)) {
                    crane.start(); //TODO
                }
            }
        }

        /**
         * TODO
         * For every crane in every fleet, if any optimal solution not found, wait others util optimal found.
         * If not min fine, then main thread wait until crane finish its task(run).
         * After finishing, put the mapOldFreeCranesCount into mapFreeCranesCount
         *
         *
         *
         *      【？】如果有任意一个船队没有取得达到最优解，那么当前线程阻塞，直到起重机完成它的任务（run里面的内容）。
         *      完成了之后把oldCountOfFreeCranes中的int放入countOfFreeCranes
         */
        for (CargoType typeCargo : values()) {
            for (Thread crane : mapCranes.get(typeCargo)) {
                if (!isMinFine.get(typeCargo)) {
                    try {
                        crane.join(); // 直到crane执行完它的任务，主线程才继续。
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mapFreeCranesCount.put(typeCargo, mapOldFreeCranesCount.get(typeCargo));
                }

            }
        }
    } // executeModeling

    /**
     * initialize queueShips. Put all the ships into queueShips to start new-round optimize
     * 用ships把ConcurrentLinkedQueue<Ship>填满
     */
    private static void initQueues() {
        for (CargoType typeCargo : values()) {
            queuesAllShips.put(typeCargo, new LinkedBlockingQueue<>()); // modified 4-22 原本是ConcurrentLinkedQueue<>,改成了LinkedBlockingQueue
        }
        for (Ship ship : ships) {
            queuesAllShips.get(ship.getCargo().getTypeCargo()).add(new Ship(ship));
        }
    }

    /**
     * countOfCranes的value全部置1，countOfFreeCranes全部置1
     */
    public static void initCountCranes() {
        for (CargoType typeCargo : values()) {
            cranesCount.put(typeCargo, 1);
            mapFreeCranesCount.put(typeCargo, 1);
        }
    }

    public static int now() {
        return timer.getTime();
    }

}












