package callcenter.client;
import callcenter.client.authentication.Authorization;
import callcenter.connection.Connection;
import callcenter.connection.Data;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
/**<p>Один из классов клиента. Отделен от данных и от реализации графического отображения.</p>
 * <p>Содержит метод, который реализует обмен данными клиента с сервером.</p>
 * @author Роман
 * @version 1.0*/
public class ClientController{
    private ClientModel model = new ClientModel();
    private ClientView view = new ClientView(this);
    private Connection connection;
    private static Logger logger;
    /**Метод, возвращающий модель.*/
    ClientModel getModel() {
        return model;
    }
    /**Метод, возвращающий клиентское соединение.*/
    public Connection getConnection() {
        return connection;
    }
    /*Инициализация объекта протоколирования.*/
    private static void initLogger(){
        try {
            logger = Logger.getLogger(ClientController.class.getName());
            logger.addHandler(new FileHandler("logs/clientController"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create the log file.", e);
        }
    }
    /**Главный метод, запускающий инициализацию объекта
     *  протоколирования и процесс обмена данными.
     * @param args не содержит аргументов.*/
    public static void main(String[] args) {
        initLogger();
        new ClientController().go();
    }
    /*Главный метод обмена данными клиента с сервером. Здесь создается сокетное соединение,
    * (для примера, в качестве ip используется localhost, а качестве номера порта тот, который находится
    * вне диапозона зарезервированных [1024 - 65536]). Затем в бесконечном цике клиент отправляет
    * запрос данных серверу и принимает ответ на запрос. Если клиент получил запрос логина и id,
    * то он запускает процесс авторизации, и затем отправляет нового подключенного оператора
    * серверу. А если клиет получил данные, отображающие процесс работы колл-центра, то он
    * переотправляет их модели и задействует представление (view).*/
    private void go(){
        try (Socket socket = new Socket("127.0.0.1", 5000)) {
            connection = new Connection(socket);
            while (true) {
                connection.send(new Data(Data.DataType.DATA_REQUEST));
                Data data = connection.receive();
                switch (data.getDataType()) {
                    case REQUEST_LOGIN_AND_ID:
                        Authorization authorization = new Authorization();
                        authorization.go();
                        while (authorization.getOperator() == null) {}
                            model.setClientOperator(authorization.getOperator());
                            logger.log(Level.INFO, "Successful authentication.");
                            connection.send(new Data(model.getClientOperator()));
                            view.buildGui();
                        break;
                    case DATA_LISTS:
                        model.setData(data.getData());
                        view.show();
                        break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Connection error.", e);
            e.printStackTrace();
        } catch (ClassNotFoundException ignored) { }
    }
}