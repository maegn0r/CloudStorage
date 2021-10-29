package com.geekbrains.io;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

import com.geekbrains.model.*;
import com.sun.javafx.collections.ImmutableObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.Getter;

public class StorageController implements Initializable {

    public ListView<String> listView;
    public TextField input;
    public ListView<String> serverListView;
    public Label label;
    @Getter
    private final String FILE_ROOT_PATH = "StorageDir";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(() -> Network.getInstance().start(this)).start();
        initListFiles();
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
        parseMessage(message);
        input.clear();
    }

    private void parseMessage(String message) {
        String[] arr = message.split(" ");

        if (arr[0].equals("ls")) {
            Network.getInstance().sendLS(new LSCommand());
        } else if (arr[0].equals("mkdir")) {
            Network.getInstance().sendMKDir(new MKDirCommand(arr[1]));
        } else if (arr[0].equals("touch")) {
            Network.getInstance().sendTouch(new TouchCommand(arr[1]));
        } else if (arr[0].equals("cd")) {
            Network.getInstance().sendChangeDir(new ChangeDirCommand(arr[1]));
        } else if (arr[0].equals("upload")) {
            filePrepare(arr[1]);
        } else if (arr[0].equals("download")) {
            Network.getInstance().send(new DownloadRequestCommand(arr[1]));
        }
    }

    private void filePrepare(String s) {
        File file = new File(FILE_ROOT_PATH + "/" + s);
        if (file.exists()){
            long size = file.length();
            String fileName = file.getName();
            int partCount = (int) ((size/1024) + 1);
            Network.getInstance().send(new UploadCommand(fileName,size, partCount));
            if (size>0){
                try (FileInputStream fis = new FileInputStream(file)){
                    byte[] bytebuf = new byte[1024];
                    int readBytes;
                    for (int i = 0; i < partCount; i++) {
                        readBytes = fis.read(bytebuf);
                        Network.getInstance().send(new UploadDataCommand(fileName, i+1,readBytes,bytebuf));
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}

