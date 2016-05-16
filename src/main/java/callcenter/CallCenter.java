package callcenter;

import callcenter.Call.CallType;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**<h>Класс реализует основной функционал работы колл-центра.</h>
 * <p>Содержит списки звонков и операторов (каждый список содержит в себе
 * операторов определенного статуса, т.е. операторов готовых
 * к обработке, которые находятся в обработке и пр.). Так как списки
 * могут одновременно использоваться несколькими потоками сразу,
 * то в классе исользованны потоко-безопасные списки CopyOnWriteArrayList
 * во избежание конфликторв. </p>
 * <p>Когда сервер принимает определенные запросы, именное методы этого класса
 * и занимаются их обработкой (в основном это перемещение объектов между списками).</p>
 * <p>Также в этом классе отдельными нитями генерируются звонки, а также берутся
 * на обработку и снимаются с нее, все это реализованно во внутренних классах.</p>
 * @author Роман
 * @version 1.0*/
public class CallCenter {
    private ArrayList<CopyOnWriteArrayList> allLists = new ArrayList<>();
    private CopyOnWriteArrayList<Operator> readyOperators = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Operator> busyOperators = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Operator> holdOperators = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Operator> absentOperators = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Call> calls = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<int[]> infoAboutCalls = new CopyOnWriteArrayList<>();
    private int[] receivedCalls = new int[3];
    private int[] processingCalls = new int[3];
    private int[] processedCalls = new int[3];
    private int[] lostCalls = new int[3];
    /*В список иформации о количестве различных звонков(всего принятых
    обработанных и пр.) добавляются массивы содержащие эти количества для каждой
    категории. А затем все списки добавляются в один общий список, который в последствии
    будет сериализован и отправлен клиенту для отображения. Также запускается отдельный
    поток, генерирующий звонки.*/
    public CallCenter() {
        infoAboutCalls.add(processingCalls);
        infoAboutCalls.add(receivedCalls);
        infoAboutCalls.add(processedCalls);
        infoAboutCalls.add(lostCalls);
        allLists.add(readyOperators);
        allLists.add(busyOperators);
        allLists.add(holdOperators);
        allLists.add(absentOperators);
        allLists.add(calls);
        allLists.add(infoAboutCalls);
        new CallGeneration().start();
    }
    /**Метод используется для добавление нового оператора в список готовых.
     * @param operator авторизованный оператор.*/
    public void addNewOperator(Operator operator){
        readyOperators.add(operator);
    }
    /**Метод используется для добавление оператора в список готовых.*/
    public void addToReadyList(Operator operator){
        /*Так как неизвестно в каком списке он находится, вызываем
        метод, который находит его среди списков и удаляет из него.*/
        remove(operator);
        operator.setStatus(Operator.Status.READY);
        readyOperators.add(operator);
    }
    /**Метод используется для перемещения оператора из
     * списка удерживающих звонок в список обработки. */
    public void addToBusyList(Operator operator){
        holdOperators.remove(operator);
        operator.setStatus(Operator.Status.BUSY);
        busyOperators.add(operator);
        /*Так как оператор устанавливается в конец списка, то при этом может
         быть не соответсвие (в списке операторы должны быть в порядке убывания
         времени), поэтому список сортируется вспомогательным методом.*/
        sortBusyList();
    }
    /**Метод для перемещения оператора из списка отсутствовавших в список готовых.*/
    public void addToReadyListFromAbsent(Operator operator) {
        absentOperators.remove(operator);
        operator.setStatus(Operator.Status.READY);
        readyOperators.add(operator);
    }
    /**Метод для перемещения оператора из списка
     * обрабатывающих звонок в список удерживающих.*/
    public void addToHoldList(Operator operator){
        busyOperators.remove(operator);
        operator.setStatus(Operator.Status.HOLD);
        holdOperators.add(operator);
    }
    /**Метод для перемещения оператора в список отсутствующих.*/
    public void addToAbsentList(Operator operator) {
        remove(operator);
        operator.setStatus(Operator.Status.ABSENT);
        absentOperators.add(operator);
    }
    /**Метод, возвращает общий список.*/
    public ArrayList<CopyOnWriteArrayList> getAllLists() {
        new CallCenterProcess();
        return allLists;
    }
    /*Один из перегруженных методов, возвращающих
    * номер перечисления категорий звонка.*/
    private int getOrdinalCallType(Call call){
        return call.getCallType().ordinal();
    }
    /*Один из перегруженных методов, возвращающих
    * номер перечисления категорий звонка.*/
    private int getOrdinalCallType(CallType callType){
        return callType.ordinal();
    }
    /*Вспомогательный метод сортировки операторов по убыванию времени.*/
    private void sortBusyList() {
        List<Operator> tmpList = new ArrayList<>(busyOperators);
        Collections.copy(tmpList, busyOperators);
        Collections.sort(tmpList);
        Collections.copy(busyOperators, tmpList);
    }
    /**Метод для определения местонахождения оператора среди всех списков.*/
    public void remove(Operator operator) {
        /*Если статус оператора при этом не "готов", то фискируем
         звонок, как потерянный и снятый с обработки.*/
        if(operator.getStatus() != Operator.Status.READY) {
            processingCalls[getOrdinalCallType(operator.getCall())]--;
            lostCalls[getOrdinalCallType(operator.getCall())] ++;
        }
        if(readyOperators.contains(operator))
            readyOperators.remove(operator);
        else if(busyOperators.contains(operator))
            busyOperators.remove(operator);
        else if(holdOperators.contains(operator))
            holdOperators.remove(operator);
        else if(absentOperators.contains(operator))
            absentOperators.remove(operator);
    }
    /*Класс генерации звонков наследуется от класса Thread и переопределяет
    * метод run. Звонок генерируется случайным образом (спустя случайный
    * интервал времени) и со случайными параметрами (категория, длительность
    * звонка и номер телефона). Изменяя константы INTERVAL можно регулировать
    * частоту генерации, а при помощи DURATION длительность звонка. В конце
    * инкрементируем количество принятых звонков, для его категории.*/
    private class CallGeneration extends Thread{
        private final int MIN_INTERVAL = 10;
        private final int MAX_INTERVAL = 20;
        private final int MIN_DURATION = 15;
        private final int MAX_DURATION = 15;
        private final long TELEPHONE_CODE = 380990000000L;
        private final int MAX_TELEPHONE = 9999999;
        @Override
        public void run() {
            while (true){
                int interval = random(MAX_INTERVAL) + MIN_INTERVAL;
                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException ignored) { }
                CallType randomCallType = CallType.values()[random(3)];
                long randomPhoneNumber = TELEPHONE_CODE + random(MAX_TELEPHONE);
                long randomDuration = new Date().getTime() +
                        (random(MAX_DURATION) + MIN_DURATION) * 1000;
                calls.add(new Call(randomCallType, randomPhoneNumber, randomDuration));
                receivedCalls[getOrdinalCallType(randomCallType)]++;
            }
        }
        private int random(int value) {
            return new Random().nextInt(value);
        }
    }
    /*Внутренний класс реализующий, взятие звонка на обработку первым из списка
    готовых операторов и завершение обработки звонка, если время превысило его
    длительность. Оба процесса выполняются параллельно, не только в отношении
    друг друга, но и всем существующим потокам. Если звонок взят на обработку,
    то инкрименттируем количество обрабатываемых звонков. А если снят с обработки
    декриментируем этот показатель и инкриментируем количество обработанных.*/
    private class CallCenterProcess {
        CallCenterProcess() {
            new Runnable() {
                @Override
                public void run() {
                    if(!readyOperators.isEmpty() && !calls.isEmpty()){
                        Operator busyOperator = readyOperators.get(0);
                        readyOperators.remove(busyOperator);
                        Call call = calls.get(0);
                        calls.remove(call);
                        busyOperator.setCall(call);
                        busyOperator.setStatus(Operator.Status.BUSY);
                        busyOperators.add(busyOperator);
                        processingCalls[getOrdinalCallType(call)]++;
                    }
                }
            }.run();
            new Runnable() {
                @Override
                public void run() {
                    if(!busyOperators.isEmpty())
                        for (Operator operator : busyOperators)
                            if(operator.getLongFromTime() > operator.getCall().getDuration()){
                                busyOperators.remove(operator);
                                operator.setStatus(Operator.Status.READY);
                                readyOperators.add(operator);
                                processingCalls[getOrdinalCallType(operator.getCall())]--;
                                processedCalls[getOrdinalCallType(operator.getCall())]++;
                            }
                }
            }.run();
        }
    }
}