package callcenter.client.authentication;

import callcenter.Operator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**Используется для авторизации оператора.
 * @author Роман
 * @version 1.0*/
public class Authorization {
    private JFrame frame;
    private JTextField loginTextField;
    private JPanel panel;
    private JTextField idTextField;
    private JButton enterButton;
    private JLabel infoLabel;
    private JLabel logo;
    private Operator operator;
    /*В данном методе инициализируются все необходимые компоненты окна.
    * Кнопке ввода добавляется слушатель, для того чтобы при ее нажатии
    * запускался обработчик события, который запускает метод авторизации.*/
    /** Используется для инициализации компонентов окна и его запуска.*/
    public void go(){
        frame = new JFrame("Call center");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        logo.setIcon(new ImageIcon("logo.png"));
        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authorization();
            }
        });
        frame.getContentPane().add(panel);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    /*Собственно, сам метод авторизации, который следит за тем чтобы
    * были введены все два параметра(логин и id) и введены корректно.
    * В результате будет создан объект operator, на основе введенных параметров.*/
    private void authorization(){
            String login = loginTextField.getText();
            String id = idTextField.getText();
            if(!login.isEmpty() && !id.isEmpty())
                if(!login.matches("^[A-Za-z]+$"))
                    infoLabel.setText("Не верный Login. Login должен содержать только символы.");
                else if(!id.matches("^[0-9]{4}$"))
                    infoLabel.setText("Не верный ID. ID должен содержать только 4 цифры.");
                else {
                    operator = new Operator(login, id);
                    frame.dispose();
                }
            else infoLabel.setText("Необходимо заполнить все поля.");
    }
    /**Метод, возвращающий авторизированного в системе оператора.
     * @return Возвращает авторизированного в системе оператора.*/
    public Operator getOperator() {
        return operator;
    }
}