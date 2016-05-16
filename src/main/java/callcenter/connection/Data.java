package callcenter.connection;

import callcenter.Operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**Класс, формирующий сервером или клиентом сериализованные
 * данные, для чего и реализует интерфейс-маркер Serializable.
 * @author Роман
 * @version 1.0*/
public class Data implements Serializable {
    private ArrayList<CopyOnWriteArrayList> data;
    private final DataType dataType;
    private Operator operator;
    /**Коструктор, инициализирует оператора.
     * @param operator авторизованный оператор.*/
    public Data(Operator operator) {
        this.operator = operator;
        this.dataType = DataType.DATA_NEW_OPERATOR;
    }
    /**Коструктор, инициализирует данные.
     * @param data списки операторов и звонков.*/
    public Data(ArrayList<CopyOnWriteArrayList> data) {
        this.data = data;
        this.dataType = DataType.DATA_LISTS;
    }
    /**Коструктор, вызывается когда создается запрос.
     * @param dataType тип запроса.*/
    public Data(DataType dataType) {
        this.dataType = dataType;
    }
    /**Метод, возвращает данные.*/
    public ArrayList<CopyOnWriteArrayList> getData() {
        return data;
    }
    /**Метод, возвращает тип данных.*/
    public DataType getDataType() {
        return dataType;
    }
    /**Метод, возвращает авторизованного оператора.*/
    public Operator getOperator() {
        return operator;
    }
    /**Перечисления всех типов данных.*/
    public enum DataType {
        REQUEST_LOGIN_AND_ID, DATA_REQUEST, DATA_LISTS, DATA_NEW_OPERATOR,
        OPERATION_HOLD, OPERATION_RESUME, OPERATION_RESET,
        OPERATION_RETURN_TO_LINE, OPERATION_OUT_TO_THE_BREAK
    }
}