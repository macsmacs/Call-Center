package callcenter.client.tablemodule;
import callcenter.Call;
import callcenter.Call.CallType;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**<p>Класс используется для создания модели таблицы, которая отображает
 * информацию о звонках (всего принятых, обработанных, потеряных,
 * текущих в очереди и в обработке) для каждой категории.</p>
 * <p>С этой целью данный класс наследуется от абстрактного класса AbstractTableModel
 * и переопределяет его абстрактные методы, необходимые для отображения данных.</p>
 * @author Роман
 * @version 1.0*/
public class CallsTableModule extends AbstractTableModel {
    private List<Call> calls;
    private List<int[]> infoAboutCalls;
    /**Конструктор инициализирует список для отображения.*/
    public CallsTableModule() {
        calls = new ArrayList<>();
    }
    /**В этом методе указывется количество строк таблицы.*/
    /*В данном случае их три, потому что кажданя строка
     *этой таблице соответвует одной из трех категорий.*/
    @Override
    public int getRowCount() {
        return 3;
    }
    /**В этом методе указывется количество столбцов таблицы.*/
    @Override
    public int getColumnCount() {
        return 6;
    }
    /**В этом методе задаются названия стобцов таблицы.*/
    public String getColumnName(int column) {
        String columnName = "";
        switch (column) {
            case 0: columnName = "Категория"; break;
            case 1: columnName = "Очередь"; break;
            case 2: columnName = "Обрабатывается"; break;
            case 3: columnName = "Поступило"; break;
            case 4: columnName = "Обработано"; break;
            case 5: columnName = "Потеряно"; break;
        }
        return columnName;
    }
    /**Метод отображения данных в ячейке таблицы.
     * @param rowIndex индекс строки, по которому определяется о какой из
     * трех категорий идет речь (технической, финансовой или консультационной).
     * @param columnIndex индекс столбца, по которому определяется то, какие данные
     * необходимо отобразить.
     * @return Значение, которое отображается в ячейке таблицы.*/
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        switch (columnIndex){
            case 0: value = CallType.values()[rowIndex]; break;
            case 1: value = getCountCalls(rowIndex); break;
            case 2: value = getValueAboutCall(0, rowIndex); break;
            case 3: value = getValueAboutCall(1, rowIndex); break;
            case 4: value = getValueAboutCall(2, rowIndex); break;
            case 5: value = getValueAboutCall(3, rowIndex); break;
        }
        return value;
    }
    /*Данный метод определяет из списка количество
    * звонков в очереди для определенной категории.*/
    private Object getCountCalls(int value) {
        int count = 0;
        CallType callType = CallType.values()[value];
        for (Call call : calls)
            if(call.getCallType() == callType)
                count++;
        return count;
    }
     /*Метод, который возвращает количество звонков разных состояний
     *(принятых, обработанных и пр.)  для определенной категории.
     * Первый параметр указывает какого вида звонка интересует его
     * количество, а второй указывает на категорию. Если список не
     * инициализирован, то для всех возвращающее значение - 0.*/
    private int getValueAboutCall(int infoIndex, int rowIndex) {
        return infoAboutCalls == null ? 0 : infoAboutCalls.get(infoIndex)[rowIndex];
    }
    /**Метод устанавливает новый (обновленный) список звонков.*/
    public void setCalls(List<Call> calls) {
        this.calls = calls;
    }
    /**Метод устанавливает новые значения списка с количеством звонков.*/
    public void setInfoAboutCalls(List<int[]> infoAboutCalls) {
        this.infoAboutCalls = infoAboutCalls;
    }
}