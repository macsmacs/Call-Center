package callcenter.connection;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**Класс, организующий соединение между сервером и клиентом.
 * Выступает в роли моста для сервера и клиента, содержит два
 * потока для приема/передачи сериализованных данных.
 * @author Роман
 * @version 1.0*/
public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    /*В контруктор передаем сокет, инициализируем его и потоки*/
    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }
    /**Метод отправки сериализованных данных.
     * @serialData data Данные для отправки.*/
    public void send(Data data) throws IOException {
        //Чтобы объект out не был использован несколькими потоками сразу, блокируем его.
        synchronized(out){
            out.writeObject(data);
            out.flush();
            out.reset();
        }
    }
    /**Метод приема десериализованных данных.
     * @return Десериализованные данные.*/
    public Data receive() throws IOException, ClassNotFoundException {
        //Чтобы объект in не был использован несколькими потоками сразу, блокируем его.
        synchronized (in){
            return (Data) in.readObject();
        }
    }
    /**Переопределенный метод интерфейса Closeable.
    * Закрываются сокет и потоки ввода/вывода.*/
    @Override
    public void close() throws IOException {
            socket.close();
            in.close();
            out.close();
    }
}