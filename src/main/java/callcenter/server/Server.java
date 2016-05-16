package callcenter.server;

import callcenter.CallCenter;
import callcenter.Operator;
import callcenter.connection.Connection;
import callcenter.connection.Data;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**<p>Класс Server реализует обмен данных с клиентом.</p>
 * <p>Сервер организует могопоточный обмен, так как с каждым новым подлючением
 * создается новый отдельный поток, в котором и происходит обработка запросов.</p>
 * @author Роман
 * @version 1.0*/
public class Server {
    private static CallCenter callCenter;
    private static Logger logger;
    /*Инициализация объекта протоколирования.*/
    private static void initLogger(){
        try {
            logger = Logger.getLogger(Server.class.getName());
            FileHandler fileHandler = new FileHandler("logs/server");
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create the log file.", e);
        }
    }
    /**Главная нить (поток), которая создает серверный сокет и
     * в бесконечном цикле ожидаются подключения нового клиента. */
    /* При подключении клиента отдельной нитью запускается обработчик, которому
     передаем сокет и сервер снова ожидает подключения по указаному порту.*/
    public static void main(String[] args) throws IOException {
        try {
            initLogger();
            ServerSocket serverSocket = new ServerSocket(5000);
            callCenter = new CallCenter();
            logger.log(Level.INFO, "Server is running.");
            while (true){
                Socket socket = serverSocket.accept();
                logger.log(Level.INFO, "New client is connected.");

                new Handler(socket).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Connection error.", e);
            logger.log(Level.WARNING, "Check that no other active connections on this port.");
        }
    }
    /*Обработчик наследуется от класса Thread и переопределяет метод run.*/
    private static class Handler extends Thread{
        private Socket socket;
        private Operator clientOperator;
        Handler(Socket socket) {
            this.socket = socket;
        }
        /*Создаем соединение с сервером, получаем запрос данных от него,
        * в ответ на который отправляем запрос логина и id, возвращающим
        * результатом будет новый клиент-оператор. Затем, в бесконечном
        * цикле получаем запросы, если необходимо производим манипуляции со
        * списками операторов и звонков, и отправляем данные процесса работы
        * колл-центра клиенту. Все это возможно благодоря объекту callCenter,
        * в классе которого содержится вся необходимая реализация функционала
        * работы колл-цента. Если клиент покинул сеанс, то удаляем его. */
        @Override
        public void run() {
            try (Connection connection = new Connection(socket)) {
                if(connection.receive().getDataType() == Data.DataType.DATA_REQUEST)
                    clientOperator = loginAndIdRequest(connection);
                callCenter.addNewOperator(clientOperator);
                while (true){
                    switch (connection.receive().getDataType()){
                        case OPERATION_HOLD:
                            callCenter.addToHoldList(clientOperator);
                            break;
                        case OPERATION_RESUME:
                            callCenter.addToBusyList(clientOperator);
                            break;
                        case OPERATION_RESET:
                            callCenter.addToReadyList(clientOperator);
                            break;
                        case OPERATION_RETURN_TO_LINE:
                            callCenter.addToReadyListFromAbsent(clientOperator);
                            break;
                        case OPERATION_OUT_TO_THE_BREAK:
                            callCenter.addToAbsentList(clientOperator);
                            break;
                        default: break;
                    }
                    connection.send(new Data(callCenter.getAllLists()));
                }
            } catch (IOException e) {
                logger.log(Level.INFO,
                        String.format("Agent [%s] left the system.", clientOperator.getId()));
            } catch (ClassNotFoundException ignored) { }
            callCenter.remove(clientOperator);
        }
        /*Метод отправляет клиенту запрос логина и id
         и возвращает авторизированного оператора.*/
        private Operator loginAndIdRequest(Connection connection) throws IOException, ClassNotFoundException {
            connection.send(new Data(Data.DataType.REQUEST_LOGIN_AND_ID));
            return connection.receive().getOperator();
        }
    }
}