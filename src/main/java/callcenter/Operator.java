package callcenter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**<p>Класс, который необходим для создания объектов "оператор" и манипуляций над ними.</p>
 *  <p>Поддерживает сериализацию, поэтому реализует интерфейс-маркер
 *  Serializable. Объекты этого класса в процессе выполнения программы
 *  будут сериализоваться и обмениваться сервером и клиентом. </p>
 *  <p>Также класс реализует параметризированный интерфейс Comparable<>.</p>
 * @author Роман
 * @version 1.0*/
public class Operator implements Serializable, Comparable<Operator> {
    private final String login;
    private final String id;
    private Status status;
    private Date time;
    private long tmpTime;
    private Call call;
    /**Коструктор инициализирует неизменяемые поля login и id. При создании
     * объекта его статус оператора устанавливается, как готовый к обработке.
     * @param login логин оператора.
     * @param id идентификатор оператора.*/
    public Operator(String login, String id) {
        this.login = login;
        this.id = id;
        this.status = Status.READY;
        this.time = new Date();
    }
    /**Метод, передающий оператору звонок при взятии его на обработку.*/
    public void setCall(Call call) {
        this.call = call;
    }
    /**Метод изменяет состояние оператора.*/
    /*При смене статуса, устанавливается новое время, чтобы его отчет запустился заново.
    * Если устанавливается звонок на удержание, то время фиксируется и стартует новый
    * отчет времени. Если звонок возобновляется, то возвращается фиксированное значение
    * и время обработки уже будет с учетом времени удержания, также необходимо увеличить
    * длительность звонка, чтобы после длительного удержания звонок не считался обработанным.*/
    public void setStatus(Status status) {
        if(this.status == Status.BUSY && status == Status.HOLD){
            tmpTime = time.getTime();
            time = new Date();
        }
        else if(this.status == Status.HOLD && status == Status.BUSY){
            time = new Date(tmpTime);
            this.getCall().updateDuration();
        }
        else time = new Date();
        this.status = status;
    }
    /**Метод возвращает логин оператора.*/
    public String getLogin() {
        return login;
    }
    /**Метод возвращает идентификатор оператора.*/
    public String getId() {
        return id;
    }
    /**Метод возвращает текущее состояние оператора.*/
    public Status getStatus() {
        return status;
    }
    /**Метод возвращает звонок, обрабатываемый оператором.*/
    public Call getCall() {
        return call;
    }
    /**Метод getTime() возвращает строковое представление времени в заданном формате.*/
    // Время определяется разницей текущего времени и времени запуска.
    public String getTime() {
        Date current = new Date();
        long time = current.getTime() - this.time.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        current.setTime(time);
        current.setHours(0);
        return dateFormat.format(current);
    }
    public long getLongFromTime() {
        return new Date().getTime();
    }
    /**Переопределенный метод сравнения, используется для сортировки списка занятых операторов.*/
    /*Метод используется для того чтобы при возврате оператора в состояние обработки из состояния
     удержания звонка, оператор устанавливался не в конец списка, а чтобы список сортировался по
     времени обработки. В классе String есть собственный метод compareTo, который сортирует строки
     по возрастанию. Для того, чтобы список сортировался по убыванию, результат умножается на -1.*/
    @Override
    public int compareTo(Operator operator) {
        return getTime().compareTo(operator.getTime()) * (-1);
    }
    /**Перечисления возможных состояний, которые может принимать оператор.*/
    public enum Status {
        READY, ABSENT, HOLD, BUSY
    }
}