package callcenter;
import java.io.Serializable;
import java.util.Date;

/**Используется для создания объектов "звонок".
 * @author Роман
 * @version 1.0*/
public class Call implements Serializable{
    private CallType callType;
    private long telephone;
    private long duration;
    /**Конструктор принимает и инициализирует параметры.
    * @param callType категория звонка.
    * @param telephone номер телефона.
    * @param duration длительность.*/
    public Call(CallType callType, long telephone, long duration) {
        this.callType = callType;
        this.telephone = telephone;
        this.duration = duration;
    }
    /**@return Возвращает категорию звонка.*/
    public CallType getCallType() {
        return callType;
    }
    /**@return Возвращает длительность звонка.*/
    public long getDuration() {
        return duration;
    }
    /**@return Возвращает номер телефона.*/
    public long getTelephone() {
        return telephone;
    }
    /**Обновляет поле длительности звонка.*/
    public void updateDuration() {
        this.duration += this.duration - new Date().getTime();
    }
    /**Перечисления категорий звонков.*/
    public enum CallType {
        TECH, POTENTIAL, FINANCE
    }
}