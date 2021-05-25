package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Controller {


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
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String strFromServer = in.readUTF();
                            if (strFromServer.startsWith("/authok")) {
                                break;
                            }
                            mainChatArea.appendText(strFromServer + "\n");
                        }
                        while (true) {
                            String strFromServer = in.readUTF();
                            if (strFromServer.equalsIgnoreCase("/end")) {
                                try {
                                    socket.close();
                                    in.close();
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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


//    public void start() {
//        try {
//            socket = new Socket("localhost", 8189);
//            socket.setSoTimeout(15000);
//            in = new DataInputStream(socket.getInputStream());
//            out = new DataOutputStream(socket.getOutputStream());
//            Thread t = new Thread(() -> {
//                try {
//                    while (true) {
//                        String str = in.readUTF();
//                        mainChatArea.appendText(str + "\n");
//                        if (str.startsWith("/authok")) {
//                            socket.setSoTimeout(0);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            t.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void onAuthClick() {
//        if (socket == null || socket.isClosed()) {
//            start();
//        }
//        try {
//            out.writeUTF("/auth" + loginField.getText() + " " + passField.getText());
//            loginField.clear();
//            passField.clear();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
