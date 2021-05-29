package sampleOne;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


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
                                loadHistory();
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
                            mainChatArea.appendText(strFromServer + "\n");
                            saveHistory();
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

    private void saveHistory() {
        try {
            File history = new File("history.txt");
            PrintWriter fileWriter = new PrintWriter(new FileWriter(history, false));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(mainChatArea.getText());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory() throws IOException {
        int lineHistory = 100;
        File history = new File("history.txt");
        if (!history.exists()) {
            System.out.println("Создание файла истории");
            history.createNewFile();
        }
        List<String> historyList = new ArrayList<>();
        FileInputStream in = new FileInputStream(history);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        String temp;
        while ((temp = bufferedReader.readLine()) != null) {
            historyList.add(temp);
        }

        if (historyList.size() > lineHistory) {
            for (int i = historyList.size() - lineHistory; i <= (historyList.size() - 1); i++) {
                mainChatArea.appendText(historyList.get(i) + "\n");
            }
        } else {
            for (int i = 0; i < historyList.size() ; i++) {
                mainChatArea.appendText(historyList.get(i) + "\n");
            }
        }
    }
}
