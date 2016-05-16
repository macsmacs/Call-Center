package callcenter.client;

import callcenter.Call;
import callcenter.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** <p>Класс содержит данные, принимаемые клиентом от сервера (списки операторов, звонком и пр.)
 *  а также методы, для манипуляций с этими данными.</p>
 *  <p>Отделен от реализации их графического отображения
 *  и не работает напрямую с сервером. Практически, все методы доступны классам в пределах этого пакета.</p>
 * @author Роман
 * @version 1.0*/
class ClientModel {
    private Operator clientOperator;
    private List<Operator> readyOperators = new ArrayList<>();
    private List<Operator> busyOperators = new ArrayList<>();
    private List<Operator> absentOperators = new ArrayList<>();
    private List<Operator> holdOperators = new ArrayList<>();
    private List<Call> calls = new ArrayList<>();
    private List<int[]> infoAboutCalls = new ArrayList<>();
    /**@return Возвращает список операторов готовых к обработке.*/
    List<Operator> getReadyOperators() {
        return readyOperators;
    }
    /**@return Возвращает список операторов, находящихся в обработке.*/
    List<Operator> getBusyOperators() {
        return busyOperators;
    }
    /**@return Возвращает список отсутствующих в линии операторов.*/
    List<Operator> getAbsentOperators() {
        return absentOperators;
    }
    /**@return Возвращает список операторов в удержании звонка.*/
    List<Operator> getHoldOperators() {
        return holdOperators;
    }
    /**@return Возвращает список звонков.*/
    List<Call> getCalls() {
        return calls;
    }
    /**@return Возвращает статистику о количестве звонков.*/
    List<int[]> getInfoAboutCalls() {
        return infoAboutCalls;
    }
    /**@return Возвращает клиента-оператора.*/
    Operator getClientOperator() {
        return clientOperator;
    }
    /**Метод возвращает клиента-оператора из всех обновленных списков.
     * @return Возвращает клиента-оператора.*/
    Operator getUpdateClientOperator() {
        if(findClientOperatorFromLists(readyOperators)) return clientOperator;
        else if(findClientOperatorFromLists(busyOperators)) return clientOperator;
        else if(findClientOperatorFromLists(holdOperators)) return clientOperator;
        else if(findClientOperatorFromLists(absentOperators)) return clientOperator;
        return clientOperator;
    }
    /*Вспомогательный метод, который ищет клиента-оператора
     из переданного, в качестве параметра, списка.*/
    private boolean findClientOperatorFromLists(List<Operator> operators){
        for (Operator operator:operators)
            if(operator.getId().equals(clientOperator.getId())){
                clientOperator = operator;
                return true;
            }
        return false;
    }
    /**Метод распределяет по отдельным спискам, принятые от сервера данные.
     * @param data данные, содержащие все списки.*/
    void setData(ArrayList<CopyOnWriteArrayList> data) {
        readyOperators = data.get(0);
        busyOperators = data.get(1);
        holdOperators = data.get(2);
        absentOperators = data.get(3);
        calls = data.get(4);
        infoAboutCalls = data.get(5);
    }
    /**Метод, инициализирующий клиента-оператора.*/
     //Вызывается только один раз, после авторизации.
    void setClientOperator(Operator clientOperator) {
        this.clientOperator = clientOperator;
    }
}