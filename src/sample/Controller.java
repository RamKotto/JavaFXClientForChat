package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Controller {

    private AtomicBoolean timeForAuth = new AtomicBoolean(true);

    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    @FXML
    TextField messageField;
    @FXML
    TextArea mainChatArea;
    @FXML
    TextField loginField;
    @FXML
    TextField passField;

    public void sendMessage(ActionEvent actionEvent) {
        if (!messageField.getText().trim().isEmpty()) {
            try {
                out.writeUTF(messageField.getText().trim());
                messageField.clear();
                messageField.requestFocus();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } else {
            messageField.clear();
            messageField.requestFocus();
        }
    }

    public void onAuthClick() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            timer();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (timeForAuth.get()) {
                            String strFromServer = in.readUTF();
                            if (strFromServer.startsWith("/authok")) {
                                break;
                            }
                            mainChatArea.appendText(strFromServer + "\n");
                        }
                        if (!timeForAuth.get()) {
                            try {
                                socket.close();
                                in.close();
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        while (true) {
                            String strFromServer = in.readUTF();
                            if (strFromServer.equalsIgnoreCase("/end")) {
                                break;
                            }
                            mainChatArea.appendText(strFromServer);
                            mainChatArea.appendText("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void timer() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(() ->
                        timeForAuth.set(false)
                , 10, TimeUnit.SECONDS);
        if (!timeForAuth.get()) {
            System.out.println("Время на авторизацию вышло!");
            service.shutdown();
        }
    }
}
