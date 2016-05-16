package callcenter.client.tablemodule;

import callcenter.Operator;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**<p>Класс, необходимый для создания модели таблицы, которая отображает список
 * операторов, готовых к обработке звонка или тех, которые ушли на перерыв.</p>
 * <p>С этой целью данный класс наследуется от абстрактного класса AbstractTableModel
 * и переопределяет его абстрактные методы, необходимые для отображения данных.</p>
 * @author Роман
 * @version 1.0*/
public class ReadyOrAbsentTableModule extends AbstractTableModel{
    private List<Operator> operators;
    /**Конструктор инициализирует список операторов для отображения.*/
    public ReadyOrAbsentTableModule() {
        operators = new ArrayList<>();
    }
    /**В этом методе указывется количество строк.*/
    @Override
    public int getRowCount() {
        return operators.size();
    }
    /**В этом методе указывется количество столбцов.*/
    @Override
    public int getColumnCount() {
        return 3;
    }
    /**В этом методе задаются названия стобцов таблицы.*/
    @Override
    public String getColumnName(int column) {
        String columnName = "";
        switch (column) {
            case 0: columnName = "Оператор"; break;
            case 1: columnName = "ID"; break;
            case 2: columnName = "Время"; break;
        }
        return columnName;
    }
    /**Метод отображения данных в ячейке таблицы.
     * @param rowIndex индекс строки, по которому извлекается оператор из списка.
     * @param columnIndex индекс столбца, по которому определяется то, какие данные
     * необходимо отобразить.
     * @return Значение, которое отображается в ячейке таблицы.*/
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Operator operator = operators.get(rowIndex);
        Object value = null;
        switch (columnIndex){
            case 0: value = operator.getLogin(); break;
            case 1: value = operator.getId(); break;
            case 2: value = operator.getTime(); break;
        }
        return value;
    }
    /**Метод устанавливает новый (обновленный) список операторов.*/
    public void setOperators(List<Operator> operators) {
        this.operators = operators;
    }
}