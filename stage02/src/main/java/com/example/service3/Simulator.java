package com.example.service3;

import com.example.pojo.*;
import com.example.service1.Timetable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static com.example.pojo.CargoType.*;
import static com.example.utils.Constant.*;
import static com.example.utils.ParameterFormer.stringShip;
import static com.example.utils.ParameterFormer.stringStatistics;


public class Simulator {

    /**
     * моделирование + принт результат
     * @throws IOException
     */
    public static void simulate() throws IOException {

        System.out.println("simulating...");

        /**
         * моделировать
         */
        optimize();

        /**
         * принт разгруженные судны
         */
        printUnloadedShips();
        /**
         * собираем все статискити, запишем в result
         */
        generateResultStatistics();
        /**
         * принт результат
         */
        System.out.println(stringStatistics(result));
    }

    // modified 4-27
    static int modelingTimes = 0;

    /**
     * restTemplate : для запусзить сервис третий
     */
    static RestTemplateBuilder builder = new RestTemplateBuilder();
    static RestTemplate restTemplate = builder.build();

    /**
     * result : отчёт, содержающий результат моделирования
     */
    private static Statistics result = new Statistics();

    /**
     * timetable : заданное расписание
     */
    private static Timetable timetable;

    /**
     * ships : все судна
     */
    private static ArrayList<Ship> ships=new ArrayList<>();

    /**
     * штрафные суммы минимизированные или нет
     */
    private static final Map<CargoType, Boolean> isMinFine = new HashMap<>();

    /**
     * countUnloaded : число разгруженных судов
     */
    private static int countUnloaded=0;

    /**
     * sumFine : общая сумма штрафа
     */
    public static Integer sumFine = 0;

    /**
     * TODO
     * Таймер
     */
    public static volatile TaskTimer timer;

    /** TODO
     * Средство синхронизации, которое позволяет ряд потоков всем, ожидает друг друга, чтобы достигнуть общей точки барьера.
     */
    public static CyclicBarrier cyclicBarrier; // make all threads wait for completion before proceeding to the next step

    /**
     * -----------
     * Разделяю все судны на три флота(fleet) по виду груза: сыпучий флот, жидкий флот и флот контейнера
     * Для каждого флота необходимо определить число кранов, чтобы сумма штрафа ВСЕХ СУДОВ была минимизированная
     * Можем сказать, что у каждого флота есть своя статистика, которая включит:
     *      -сумма продолжительности разгрузки : mapSumUD
     *      -сумма задержки окончания разгруки : mapSumDelay
     *      -максимальная задержика окончания разгруки : mapMaxDelay
     *      -число ожидающих суднов на разгрузку : mapCountWaiting
     *      -число разгруженных суднов : mapCountUnloaded
     *      -сумма времени ожидания : mapSumWD
     *      -лист краноа(потоков) : mapCranes
     *      -число кранов(потоков) : countCranes
     *      -производительность кранов : mapPerformanceCrane
     * Все статистики расположенные в Map, чтобы для каждого занного вида грузка можем получить нужную статистику
     * -----------
     */
    public static Map<CargoType, Double> mapSumUD = new HashMap<>(); /* 停靠时间总和, sumTimeStop */
    public static Map<CargoType, Integer> mapSumDelay = new HashMap<>(); /* 停靠时间总和, sumTimeStop */
    public static Map<CargoType, Integer> mapMaxDelay = new HashMap<>();/* 最大停靠时间, maxTimeStop, mapMaxPD */
    public static Map<CargoType, Integer> mapCountWaiting = new HashMap<>(); /* 等待卸货的船的个数?, countWaitUnloadingShips */
    public static Map<CargoType, Integer> mapCountUnloaded = new HashMap<>(); /* 完成卸货的船的个数, countShipsUnloaded */
    public static Map<CargoType, Integer> mapSumWD = new HashMap<>();
    public static Map<CargoType, List<Thread>> mapCranes = new HashMap<>(); // 起重机列表
    public static Map<CargoType, Integer> countCranes = new HashMap<>(); // 起重机数量
    public static Map<CargoType, Integer> mapPerformanceCrane = new HashMap<>(); // 速度

    /**
     * Один кран, это один поток.
     * Когда несколько кранов работает всместе, необходимо использовать потокобезопасные контейнер, чтобы изменения данного
     * потока были видны для остальных потоков
     *      -очерезь разгружающих суднов : unloading
     *      -очередь разгруженных судов : unloaded
     *      -очередь ожидающих суднов на разгрузку : waiting
     * Для очерези я взяла CopyOnWriteArrayList, потому что нам нужно чаще читать а меншее записать. Тем более, CopyOnWriteArrayList
     * позволяет несколько потоков допустить к очереди суднов(CopyOnWriteArrayList<Ship>)
     *
     * CopyOnWriteArrayList-Copy when writing, thread safe, fix to the situation that read more but write less often
     */
    public static ConcurrentMap<CargoType, CopyOnWriteArrayList<Ship>> unloading = new ConcurrentHashMap<>(); /*正在卸货的船*/
    public static ConcurrentMap<CargoType, CopyOnWriteArrayList<Ship>> unloaded = new ConcurrentHashMap<>();  /*完成卸货的船*/
    public static ConcurrentMap<CargoType, LinkedBlockingQueue<Ship>> waiting = new ConcurrentHashMap<>(); /* ??? 和上面的区别是?queuesForUnloading, queuesWaiting */

    /**
     *      -очередь всех суднов, из которого нужно оптимизировать : queuesAllShips
     *      -число свободных кранов : mapFreeCranesCount
     *      -сумма штрафа : mapFine
     * ConcurrentLinkedQueue- in CAS algorithm,(non-blocking queue), FIFO.
     */
    public static ConcurrentMap<CargoType, LinkedBlockingQueue<Ship>> queuesAllShips = new ConcurrentHashMap<>();  /*队列中的卸货的船*/
    public static ConcurrentMap<CargoType, Integer> mapFreeCranesCount = new ConcurrentHashMap<>(); //空闲的起重机, executeModeling/mapCountFreeCranes
    public static ConcurrentMap<CargoType, Integer> mapFine = new ConcurrentHashMap<>(); // 罚金



    public static Statistics getResult() {
        return result;
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
                    mapSumWD.put(typeCargo,mapSumWD.get(typeCargo) + ( ship.getStartUnloadTime().receiveTimeInMinute() - ship.getArriveTime().receiveTimeInMinute() ));
            }

            mapCountUnloaded.put(typeCargo, mapCountUnloaded.get(typeCargo) + unloaded.get(typeCargo).size());
            sumFine += mapFine.get(typeCargo) - (countCranes.get(typeCargo) - 1) * COAST_ONE_CRANE;
            sumDelay += mapSumDelay.get(typeCargo); // modified:原本:sumDelay += mapSumUD.get(typeCargo)
            countUnloaded += mapCountUnloaded.get(typeCargo);
            countWaiting += mapCountWaiting.get(typeCargo);
            maxDelay = Integer.max(mapMaxDelay.get(typeCargo), maxDelay);
        }
        sumWD = summaryWD();


        result.setCountUnloaded(countUnloaded);
        result.setCountLoose(countCranes.get(LOOSE));
        result.setCountLiquid(countCranes.get(LIQUID));
        result.setCountContainer(countCranes.get(CONTAINER));
        result.setTotalFine(sumFine);
        result.setSumWaitDuration(sumWD);
        result.setAvrWaitDuration((sumWD/countUnloaded));
        result.setSumDelay(sumDelay);
        result.setAvrDelay(sumDelay/countUnloaded);
        result.setMaxDelay(maxDelay);
        result.setAvrWaitLength(countWaiting/MINUTE_IN_THIRTY_DAYS);

    }

    // modified 4-23 private->public
    public static void init() {
        initShips();
        initCountCranes();
    }

    public static void initPerformanceFromURL() {

        System.out.println("[Service 3]: going to visit endpoint service 2 for performance...");
        String stringPerformance = restTemplate.getForObject("http://localhost:8090/service2/performance", String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        Performance performance=null;
        try {
            performance = objectMapper.readValue(stringPerformance, Performance.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        mapPerformanceCrane.put(LOOSE, performance.getLoosePerformance());
        mapPerformanceCrane.put(LIQUID, performance.getLiquidPerformance());
        mapPerformanceCrane.put(CONTAINER, performance.getContainerPerformance());
    }


    public static void initTimetableFromURL() {

// get timetable by name: http://localhost:8090/service2/timetable/timetable.json
//        String stringTimetable = restTemplate.getForObject("http://localhost:8090/service2/timetable/timetable.json", String.class);

        System.out.println("[Service 3]: going to visit endpoint service 2 for timetable...");
        String stringTimetable = restTemplate.getForObject("http://localhost:8090/service2/timetable", String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            timetable = objectMapper.readValue(stringTimetable, Timetable.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }



    private static void initShips() {
        for (Schedule nodeTimetable : timetable.getSchedules()) {
            int randomTimeAdd = DayHourMinute.randomMinute(-MINUTE_IN_SEVEN_DAYS, MINUTE_IN_SEVEN_DAYS);
            Ship ship;
            try {
                ship = new Ship(nodeTimetable.getArriveTime().addTime(randomTimeAdd), //初始化一条船, arrive time
                        nodeTimetable.getNameShip(), // shipName
                        nodeTimetable.getCargo(), // cargo
                        nodeTimetable.getUnloadDuration()); // unload minutes
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            ships.add(ship);
        }
        // modified 4-27
        ships.sort(Comparator.naturalOrder());
//        System.out.println("########Simulator: Sorted ships by times");
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
         * Нужно обнулить все переменные на процессе оптимизирования. Эти пероеменные включают:
         *      -Статистика: mapSumWD.put(cargo, 0);
         *                   mapCountWaiting.put(cargo, 0);
         *                   mapMaxDelay.put(cargo, 0);
         *                   mapSumUD.put(cargo, 0.0);
         *                   mapSumDelay.put(cargo, 0);
         *                   mapCountUnloaded.put(cargo, 0);
         *                   mapFine.put(cargo, 0);
         *      -очереди суднов: queuesAllShips
         *
         * MUST clear old data before new-round optimization!
         *         initStatistic();
         *         initQueues();
         *         modeling();
         *         countFine();
         */
        initBeforeOptimization();
        /**
         * моделирование : заданные числа кранов кажного флота, начинаем все их потоки, после время прошло 30 дня, высчислить сумму штрафа
         * Попропуем допавить один кран, и повторяем процесс моделирования, высчислить сумму штрафа, сравниваем с прошедщего варианта.
         * Если для данного варианта сумма штрафа уже выше, чем прошедщего варианта, то можем сказать, что мы нашли оптимизированный результат
         */
        modeling();
        /**
         * высчислить сумму штрафа после моделирования
         */
        countFine();

        /**
         * Mark down the old solution(count of 3 types of cranes)
         * Отметить старое решение (всего 3 типа кранов).
         */
        ConcurrentMap<CargoType, Integer> oldMapFine = new ConcurrentHashMap<>();
        for (CargoType typeCargo : values()) {
            oldMapFine.put(typeCargo, mapFine.get(typeCargo));
        }

        /**
         * Только тогда, когда все 3 типа найдут оптимизацию, цикл может прерваться.
         *
         */
        while (!isMinFine.get(LOOSE) || !isMinFine.get(LIQUID) || !isMinFine.get(CONTAINER)) {
            /**
             *
             * Попробуем добавить один кран, поко не наидем оптимизацию
             */
            for (CargoType typeCargo : values()) {
                if (!isMinFine.get(typeCargo))   mapFreeCranesCount.put(typeCargo, mapFreeCranesCount.get(typeCargo) + 1);
                else  mapFreeCranesCount.put(typeCargo, 0);
            }
            initBeforeOptimization();
            modeling();
            countFine();

            /**
             * Здесь мы определяем, достигает ли минимальной штрафа через сравнения с прошедщего варианта. Если да,
             * то можем выйти от цикла
             */
            for (CargoType typeCargo : values()) {
                if (!isMinFine.get(typeCargo)) {
                    if (mapFine.get(typeCargo) + COAST_ONE_CRANE * (mapFreeCranesCount.get(typeCargo) - 1) > oldMapFine.get(typeCargo)){
                        mapFine.put(typeCargo, oldMapFine.get(typeCargo));
                        countCranes.put(typeCargo, mapFreeCranesCount.get(typeCargo) - 1);
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
     * 计算金额的总数：
     * 对于每一个类型
     *  对于类型下的每一条已经完成卸载的船
     *      计算罚款时间（ 公式：罚款时间 = 完成卸载时刻-（实际到达时刻+卸货的时间） ）
     *      计算罚款金额（ 公式：罚款时间*100， 注意，未满1小时要按照1小时收费 ）
     *      算好了金额，放入mapFine中
     *  对于类型下每一条等待卸货的船
     *      计算罚款时间（ 公式：30天的时间-（到达时刻+卸货的时间） ）
     *      计算罚款金额（ 公式：罚款时间*100， 注意，未满1小时要按照1小时收费 ）
     *      算好了金额，放入mapFine中
     *
     */
    private static void countFine() {
        for (CargoType typeCargo : values()) {
            for (Ship ship : unloaded.get(typeCargo)) { //对于已经卸货的船
                int timeFine = (ship.getFinishUnloadTime().receiveTimeInMinute() - // FUT-(AT+UD) (罚款时间 = 完成卸载时刻-（实际到达时刻+卸货的时间）)
                        (ship.getArriveTime().receiveTimeInMinute() +
                                ship.getUnloadDuration().receiveTimeInMinute()));
                if (timeFine > 0) {
                    timeFine = timeFine / 60 + (timeFine % 60 == 0 ? 0 : 1);
                    int fine = timeFine * 100;
                    mapFine.put(typeCargo, mapFine.get(typeCargo) + fine); // 算好了金额，放入mapFine中
                }
            }

            for (Ship ship : waiting.get(typeCargo)) {
                int timeFine = (MINUTE_IN_THIRTY_DAYS - /* 罚款时间=30天的时间-（到达时刻+卸货的时间） */
                        (ship.getArriveTime().receiveTimeInMinute() +
                                ship.getUnloadDuration().receiveTimeInMinute()));
                if (timeFine > 0) {
                    timeFine = timeFine / 60 + (timeFine % 60 == 0 ? 0 : 1);
                    int fine = timeFine * 100;
                    mapFine.put(typeCargo, mapFine.get(typeCargo) + fine);
                }
            }
        }
    }

    private static void modeling() {

        // modified 4-27
//        System.out.println("######## modeling times="+(++modelingTimes));

        timer = new TaskTimer(); // Инициализируем время здесь
        int countAllCranes = 0;
        Map<CargoType, Integer> mapOldFreeCranesCount = new HashMap<>();

        /**
         * высчислаем число кранов.
         * копируем числа свободных кранов
         *
         * 将countOfFreeCranes中的值刷入countAllCranes，将countOfFreeCranes的值刷入oldCountOfFreeCranes（注意，oldCountOfFreeCranes不是同步的）
         */
        for (CargoType typeCargo : values()) {
            countAllCranes += mapFreeCranesCount.get(typeCargo); // 将countOfFreeCranes中的所有值加入countAllCranes
            mapOldFreeCranesCount.put(typeCargo, mapFreeCranesCount.get(typeCargo)); // 把countOfFreeCranes中的值放入oldCountOfFreeCranes
        }
        cyclicBarrier = new CyclicBarrier(countAllCranes, timer);
        /**
         * 这一个for循环里面做了什么：
         * 对于每个类型：
         * 1. 初始化nowInUnloading，unloadedShips，mapOfCranes
         * 2. countOfFreeCranes里的所有起重机全部加入mapOfCranes
         * 3. 启动mapOfCranes中的所有起重机
         *
         * Что делается в этом цикле for:
         *  Для каждого флота, который пока не нашел оптимизацию:
         *      1. Инициализировать все очереди: unloading, unloaded, mapCranes, waiting
         *      2. Все краны в mapOfCranes
         *      3. Запустите все краны в mapOfCranes.
         *
         */
        for (CargoType typeCargo : values()) {

            if (!isMinFine.get(typeCargo)) {

                // modified 4-27
//                System.out.println("#Port-------> init queues... "+typeCargo+" is not min. Now initialize <unloading, unloaded, waiting, mapCranes> and try to start all cranes...");

                unloading.put(typeCargo, new CopyOnWriteArrayList<>());
                unloaded.put(typeCargo, new CopyOnWriteArrayList<>());
                mapCranes.put(typeCargo, new LinkedList<>());
                waiting.put(typeCargo, new LinkedBlockingQueue<>());

                for (int i = 0; i < mapFreeCranesCount.get(typeCargo); i++) {
                    mapCranes.get(typeCargo).add(new Thread(new Crane(mapPerformanceCrane.get(typeCargo),
                            typeCargo)));
                }


                int howManyThreadStart = 0;
                for (Thread crane : mapCranes.get(typeCargo)) { //把该类型下的所有起重机启动起来
//                    System.out.println(typeCargo + " ---->Simulator: crane start: "+(++howManyThreadStart));
                    crane.start();
                }
            }
        }

        // modified 4-27
//        System.out.println("-->>>>>>>>>>>>> All cranes start modeling... <<<<<<<<<<<<<<<<<------");

        /**
         * Ждем, пока не все потоки выполняет разгрузку
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
        // modified 4-27
//        System.out.println("-->>>>>>>>>>>>> All cranes stop modeling... <<<<<<<<<<<<<<<<<------");

    } // modeling


    private static void initQueues() {
        for (CargoType typeCargo : values()) {
            queuesAllShips.put(typeCargo, new LinkedBlockingQueue<>());
        }
        for (Ship ship : ships) {
            queuesAllShips.get(ship.getCargo().getCargoType()).add(new Ship(ship));
        }
    }


    public static void initCountCranes() {
        for (CargoType typeCargo : values()) {
            countCranes.put(typeCargo, 1);
            mapFreeCranesCount.put(typeCargo, 1);
        }
    }

    public static int now() {
        return timer.getTime();
    }

}












         /* Cyclic-可以重用，Barrier-想要让一组线程等待到某个状态的那个状态成为barrier
                 *
                 * CyclicBarrier：countAllCranes-一起跑的线程数. timer-任务类,里面放了所有线程完成了(跑到了终点)之后要执行的动作
                 * 当countAllCranes这么多个线程到达了之后，最后一个线程去做timer的任务：nowTime++
                 * 需求：一个线程组的线程需要等待所有线程完成任务后再继续执行下一次任务
                 */