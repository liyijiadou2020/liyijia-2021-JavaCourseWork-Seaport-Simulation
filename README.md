# liyijia-2021-JavaCourseWork-Seaport-Simulation

Course project

Course project is proposed to create a model for servicing the flow of requests for unloading from cargo ships arriving at the seaport. The model should consist of three services, the implementation of which is performed in 2 stages, which are delivered sequentially. The deadline for the first stage is the fourth lesson.
General description of services

Service 1	

  Implement a vessel arrival timetable generator. The schedule includes:
  
    ● day and time of arrival
    
    ● name of the vessel
    
    ● type of cargo (bulk, liquid, container) and its weight in tons or pieces (for containers)
    
    ● planned stay at the port for unloading - calculated based on the weight of the cargo and the crane's performance
    
Service 2

  Receives data from service 1 and service 3 and saves them to a json file. Provide for the possibility of manually adding entries through the console.
  
Service 3

  Cargo ships arrive at the port according to the schedule (the port is open 24hours), but delays and early arrivals of ships are possible.
  
  For unloading ships in the port, three types of unloading cranes are used, corresponding to three types of cargo: bulk and liquid cargo, containers. The number of unloading cranes of each type is limited (initially, all cranes are 1 each), so incoming requests for unloading one type of cargo form a queue. The duration of the ship's unloading depends on the type and weight of its cargo. Each crane operates in a separate stream, one vessel can be unloaded by no more than two cranes at a time.
  
  Any additional (over the planned period) hour of the vessel's stay in the port (due to waiting for unloading in a queue or due to a delay in unloading itself) entails the payment of a fine of 100 cu. That is, for every hour the vessel is idle.
  
  When modeling the arrival of ships, their deviation from the schedule is considered as a random variable in the interval from -7 to 7 days. Another random variable, varying in the range from 0 to 1440 minutes, is the delay in the end of unloading of the vessel in comparison with the usual one (depending only on the type of cargo and its weight).
  The purpose of modeling the seaport operation is to determine, for a given schedule of ships' arrival, the minimum sufficient number of cranes in the port, which allows minimizing penalties at a crane cost of $ 30,000. The simulation period is 30 days. The simulation parameters should include the ship arrival schedule and the number of cranes of each type.
  
  As a result of the program, a report should be generated containing: a list of unloading performed, which indicates the name of the loaded vessel, the time of its arrival at the port and the waiting time in the queue for unloading (in the format dd: hh: mm), the start time of unloading and its duration , and also at the end of the simulation, the final statistics should be displayed: the number of unloaded vessels, the average length of the queue for unloading, the average waiting time in the queue, the maximum and average delays in unloading, the total amount of the fine and the final required number of cranes of each type.
  

Stages of implementation

Stage 1:

  Service 1: 
  
    output the result to the console.
    
  Service 2: 
  
    calls the generation method described in service 1; receives data from service 3 at the method input
    
  Service 3:
  
    ○ a json file obtained as a result of service 2 is used;
    
    ○ the result of the work is displayed in the console.
    
Stage 2:

  ● Service 1: GET - endpoint that returns the schedule.
  
  ● Service 2: accesses a GET request to the endpoints that needed. Provides the following endpoints:
  
    ○ GET - endpoint for receiving the schedule in the form of a json document;
    
    ○ GET - an endpoint that returns a schedule by the name of a json file or an error if there is no such file;
    
    ○ POST endpoint for saving the results of service 3 to a json document.
    
  ● Service 3:
  
    ○ refers to GET - service endpoint 2 to get the schedule as a json file
    
    ○ the result of the work is sent to the POST endpoint of the service 2.
    






Курсовой проект

В рамках курсового проекта предлагается создать модель обслуживания потока заявок на разгрузку, поступающих от грузовых судов, прибывающих в морской порт. Модель должна состоять из трёх сервисов, реализация которых выполняется в 2 этапа, которые сдаются последовательно. Дедлайн первого этапа - четвертое занятие.
Общее описание сервисов
Сервис 1
Реализовать генератор расписания прибытия судов. Расписание включает:
●	день и время прибытия
●	название судна
●	вид груза (сыпучий, жидкий, контейнер) и его вес в тоннах или штуках (для контейнеров)
●	планируемый срок стоянки в порту для разгрузки - вычисляется на основе веса груза и производительности крана
Сервис 2
Получает данные из сервиса 1 и сервиса 3 и сохраняет их в json-файл. Предусмотреть возможность ручного добавления записей через консоль.
Сервис 3
Грузовые суда прибывают в порт согласно расписанию (порт работает круглосуточно), но возможны опоздания и досрочные прибытия. 
Для разгрузки судов в порту используются три вида разгрузочных кранов, соответствующих трем видам грузов: сыпучим и жидким грузам, контейнерам. Число разгрузочных кранов каждого вида ограничено (изначально всех кранов по 1), так что поступающие заявки на разгрузку одного вида груза образуют очередь. Длительность разгрузки судна зависит от вида и веса его груза. Каждый кран работает в отдельном потоке, одно судно могут разгружать не более двух кранов одновременно.
Любой дополнительный (сверх запланированного срока) час стояния судна в порту (из-за ожидания разгрузки в очереди или из-за задержки самой разгрузки) влечет за собой выплату штрафа 100 у. е. за каждый час простоя судна.
При моделировании прибытия судов отклонение их от расписания рассматривается как случайная величина в интервале от -7 до 7 дней. Еще одной случайной величиной, изменяющейся в диапазоне от 0 до 1440 минут, является время задержки окончания разгрузки судна по сравнению с обычным (зависящим только от вида груза и его веса).
Цель моделирования работы морского порта – определение для заданного расписания прибытия судов минимально достаточного числа кранов в порту, позволяющего минимизировать штрафные суммы при стоимости крана в 30 000 у. е. Период моделирования – 30 дней. В параметры моделирования следует включить расписание прибытия судов и количество кранов каждого вида.
В результате работы программы должен быть сформирован отчёт, содержащий: список произведенных разгрузок, в котором указывается название загруженного судна, время его прихода в порт и время ожидания в очереди на разгрузку (в формате дд:чч:мм), время начала разгрузки и ее продолжительность, а также по окончании моделирования должна быть выведена итоговая статистика: число разгруженных судов, средняя длина очереди на разгрузку, среднее время ожидания в очереди, максимальная и средняя задержка разгрузки, общая сумма штрафа и итоговое необходимое количество кранов каждого вида.
Этапы реализации
Этап 1: 
●	Сервис 1: результат выводить в консоль.
●	Сервис 2: вызывает метод генерации, описанный в сервисе 1; получает на вход метода данные от сервиса 3
●	Сервис 3: 
○	используется json-файл, полученный в результате работы сервиса 2; 
○	результат работы выводится в консоль.
Этап 2:
●	Сервис 1: GET - эндпоинт возвращающий расписание.
●	Сервис 2: обращается GET - запросом к соответствующему эндпоинту. Предоставляет следующие эндпоинты:
○	GET - эндпоинт для получения расписания в виде json-документа;
○	GET - эндпоинт, возвращающий расписание по имени json-файла или ошибку, если такого файла нет;
○	POST-эндпоинт для сохранения результатов работы сервиса 3 в json-документ.
●	Сервис 3:
○	обращается к GET - эндпоинту сервиса 2 для получения расписания в виде json-файла
○	результат работы отправляется на POST-эндпоинт сервиса 2.
