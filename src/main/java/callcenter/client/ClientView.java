package callcenter.client;

import callcenter.Operator;
import callcenter.client.tablemodule.CallsTableModule;
import callcenter.client.tablemodule.ProcessOrHoldTableModule;
import callcenter.client.tablemodule.ReadyOrAbsentTableModule;
import callcenter.connection.Connection;
import callcenter.connection.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**Класс ориентированный на графическое отображение данных, поступающих от сервера.
 * @author Роман
 * @version 1.0*/
public class ClientView {
    private JPanel panel1;
    private JButton resetButton;
    private JButton holdButton;
    private JComboBox modeWorking;
    private JLabel infoStatus;
    private JTable readyTable;
    private JTable absentTable;
    private JTable processTable;
    private JTable holdTable;
    private JTable callsTable;
    private JLabel infoCallType;
    private JLabel infoTelephone;
    private JLabel infoTime;
    private ReadyOrAbsentTableModule readyTableModel = new ReadyOrAbsentTableModule();
    private ReadyOrAbsentTableModule absentTableModel = new ReadyOrAbsentTableModule();
    private ProcessOrHoldTableModule processTableModel = new ProcessOrHoldTableModule();
    private ProcessOrHoldTableModule holdTableModel = new ProcessOrHoldTableModule();
    private CallsTableModule callsModel = new CallsTableModule();
    private static Logger logger;
    private final ClientController controller;
    /**Конструктор инициализирует контроллер и объект протоколирования.*/
    ClientView(ClientController controller) {
        this.controller = controller;
        initLogger();
    }
    /*Инициализация объекта протоколирования.*/
    private void initLogger(){
        try {
            logger = Logger.getLogger(ClientView.class.getName());
            logger.addHandler(new FileHandler("logs/clientView"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create the log file.", e);
        }
    }
    /**Метод инициализации компонентов окна (таблиц, кнопок, всплывающего списка),
    * добавляет слушателей событий и настраивает параметры отображения данных в окне.*/
    void buildGui(){
        JFrame frame = new JFrame("Call center");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        holdButton.addActionListener(new buttonsActionListener());
        resetButton.addActionListener(new buttonsActionListener());
        modeWorking.addActionListener(new comboBoxActionListener());
        callsTable.setModel(callsModel);
        readyTable.setModel(readyTableModel);
        absentTable.setModel(absentTableModel);
        processTable.setModel(processTableModel);
        holdTable.setModel(holdTableModel);
        frame.getContentPane().add(panel1);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    }
    /**Метод получает у контроллера модель и отображает данные.*/
    void show() {
        ClientModel model = controller.getModel();
        showTablesAndUpdateInfoAboutOperators(model);
        showStateButtonsAndInfoAboutCall(model);
    }
    /*Моделям таблиц передаем соответсвующие списки и вызываем
    * метод, который в окне обновляет содержимое таблиц.*/
    private void showTablesAndUpdateInfoAboutOperators(ClientModel model){
        readyTableModel.setOperators(model.getReadyOperators());
        readyTableModel.fireTableDataChanged();
        processTableModel.setOperators(model.getBusyOperators());
        processTableModel.fireTableDataChanged();
        holdTableModel.setOperators(model.getHoldOperators());
        holdTableModel.fireTableDataChanged();
        absentTableModel.setOperators(model.getAbsentOperators());
        absentTableModel.fireTableDataChanged();
        callsModel.setCalls(model.getCalls());
        callsModel.setInfoAboutCalls(model.getInfoAboutCalls());
        callsModel.fireTableDataChanged();
    }
    /*Получаем у модели клиента-оператора, в зависимости от статуса которого,
    * устанавливаем доступными/недоступными кнопки, а также отображаем его
    * текущий статус и процесс обработки звонка.*/
    private void showStateButtonsAndInfoAboutCall(ClientModel model) {
        Operator clientOperator = model.getUpdateClientOperator();
        switch (clientOperator.getStatus()){
            case READY:
                setButtonsEnable(false);
                setHoldButtonText("Удержать");
                setTextOnInfoLabel("Линия свободна", clientOperator.getTime());
                break;
            case BUSY:
                setButtonsEnable(true);
                setHoldButtonText("Удержать");
                setTextOnInfoLabel("В обработке звонка", clientOperator.getCall().getCallType().toString(),
                        String.valueOf(clientOperator.getCall().getTelephone()), clientOperator.getTime());
                break;
            case HOLD:
                setButtonsEnable(true);
                setHoldButtonText("Возобновить");
                setTextOnInfoLabel("На удержании", clientOperator.getCall().getCallType().toString(),
                        String.valueOf(clientOperator.getCall().getTelephone()), clientOperator.getTime());
                break;
            case ABSENT:
                setButtonsEnable(false);
                setHoldButtonText("Удержать");
                setTextOnInfoLabel("Перерыв", clientOperator.getTime());
                break;
        }
    }
    /*Один из перегруженных методов, отображает
    * текущий статус и процесс обработки звонка.*/
    private void setTextOnInfoLabel(String status, String callType, String telephone, String time){
        infoStatus.setText(status);
        infoCallType.setText(callType);
        infoCallType.setVisible(true);
        infoTelephone.setText(telephone);
        infoTelephone.setVisible(true);
        infoTime.setText(time);
    }
    /*Один из перегруженных методов, вызывается если оператор
    * находится не в обработке звонка. Отображает текущий статус.*/
    private void setTextOnInfoLabel(String status, String time){
        infoStatus.setText(status);
        infoTime.setText(time);
        infoCallType.setVisible(false);
        infoTelephone.setVisible(false);
    }
    /*Устанавливает название "Удержать"/"Возодновить" на кнопке*/
    private void setHoldButtonText(String text){
        holdButton.setText(text);
    }
    private void setButtonsEnable(boolean state){
        holdButton.setEnabled(state);
        resetButton.setEnabled(state);
    }
    /*Обработчик событий нажатия кнопки. Получает у контроллера соединение
    * и в зависимости от нажатой кнопки отправляет серверу соответствующий запрос.*/
    private class buttonsActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Connection connection = controller.getConnection();
                switch (e.getActionCommand()) {
                    case "Удержать":
                        connection.send(new Data(Data.DataType.OPERATION_HOLD));
                        break;
                    case "Возобновить":
                        connection.send(new Data(Data.DataType.OPERATION_RESUME));
                        break;
                    case "Завершить":
                        connection.send(new Data(Data.DataType.OPERATION_RESET));
                        break;
                }
            }
            catch (IOException ex) {
                logger.log(Level.SEVERE, "Connection error.");
            }
        }
    }
    /*Обработчик событий всплывающего списка. Получает у контроллера соединение
    * и в зависимости от выбранного элемента отправляет серверу соответствующий запрос.*/
    private class comboBoxActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Connection connection = controller.getConnection();
                JComboBox box = (JComboBox) e.getSource();
                String selectedItem = (String) box.getSelectedItem();
                switch (selectedItem){
                    case "Линия":
                        connection.send(new Data(Data.DataType.OPERATION_RETURN_TO_LINE));
                        break;
                    case "Перерыв":
                        connection.send(new Data(Data.DataType.OPERATION_OUT_TO_THE_BREAK));
                        break;
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Connection error.");
            }
        }
    }
}