package com.geekbrains.io;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import com.sun.javafx.collections.ImmutableObservableList;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class StorageController implements Initializable {

    public ListView<String> listView;
    public TextField input;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final String FILE_ROOT_PATH = "StorageDir";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            initListFiles();
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = dis.readUTF();
                        Platform.runLater(() -> listView.getItems().add(message));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initListFiles() {
        File fileRootPath = new File(FILE_ROOT_PATH);
        if (!fileRootPath.exists()) {
            fileRootPath.mkdir();
        }
        String[] filesList = fileRootPath.list();
        listView.setItems(new ImmutableObservableList<>(filesList));
    }

    public void send(ActionEvent actionEvent) throws IOException {
        String message = input.getText();
        uploadFile(message);
        input.clear();
    }

    private void uploadFile(String msg) throws IOException {
        if (msg.startsWith("/upload")) {
            String filePath = msg.split(" ", 2)[1];
            File file = new File(filePath);
            if (file.exists()) {
                String fileName = file.getName();
                dos.writeUTF("/upload");
                dos.writeUTF(fileName);
                dos.writeLong(file.length());
                dos.flush();
                FileInputStream fis = new FileInputStream(file);
                byte[] buf = new byte[8192];
                int writeBytes;
                while ((writeBytes = fis.read(buf)) != -1) {
                    dos.write(buf, 0, writeBytes);
                }
                dos.flush();
            }
        }
    }

    public void moveToTextField(MouseEvent mouseEvent) {
        String fileName = listView.getSelectionModel().getSelectedItems().get(0);
        if (fileName != null) {
            input.setText("/upload " + FILE_ROOT_PATH + "\\" + fileName);
        }
    }
}