package com.geekbrains.io;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.geekbrains.model.*;
import com.sun.javafx.collections.ImmutableObservableList;
import dialogs.Dialogs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;

public class StorageController implements Initializable {

    public ListView<String> listView;   //num 1
    public ListView<String> serverListView; //num 2
    public Button upload;
    public Button download;
    @Getter
    private final String FILE_ROOT_PATH = "StorageDir";
    public Button renameBtn;
    public Button createBtn;
    @Getter
    private Path curClientDir;
    public static StorageController INSTANCE;

    private int activeNum = -1;

    public int getActiveNum() {
        return activeNum;
    }

    public StorageController(){
        INSTANCE = this;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.curClientDir = Paths.get(FILE_ROOT_PATH);
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

    private void refreshListFiles() throws IOException {
        List<String> list = Files.list(curClientDir).map(item -> item.getFileName().toString()).collect(Collectors.toList());

        listView.setItems(new ImmutableObservableList<>(list.toArray(new String[list.size()])));
    }

    private void filePrepare(String s) {
        File file = new File(FILE_ROOT_PATH + "/" + s);
        if (file.exists()) {
            long size = file.length();
            String fileName = file.getName();
            int partCount = (int) ((size / 1024) + 1);
            Network.getInstance().send(new UploadCommand(fileName, size, partCount));
            if (size > 0) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytebuf = new byte[1024];
                    int readBytes;
                    for (int i = 0; i < partCount; i++) {
                        readBytes = fis.read(bytebuf);
                        Network.getInstance().send(new UploadDataCommand(fileName, i + 1, readBytes, bytebuf));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void doUpload() {
        String selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            filePrepare(selectedItem);
        }
        Network.getInstance().sendLS(new LSCommand());
    }

    @FXML
    private void doDownload() throws IOException {
        String selectedItem = serverListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Network.getInstance().send(new DownloadRequestCommand(selectedItem));
        }
        Path path = Paths.get(FILE_ROOT_PATH);
        List<String> result = Files.list(path).map(item -> item.getFileName().toString()).collect(Collectors.toList());
        String[] arr = result.toArray(new String[0]);
        listView.setItems(new ImmutableObservableList<>(arr));
    }

    public void doRefresh(MouseEvent mouseEvent) throws IOException {
        if (activeNum == 1) {
            refreshListFiles();
        } else if (activeNum == 2) {
            Network.getInstance().send(new LSCommand());
        }
    }

    public void listClientClick(MouseEvent mouseEvent) throws IOException {
        activeNum = 1;
        if(mouseEvent.getClickCount() == 2){
            String selected = listView.getSelectionModel().getSelectedItem();
            if(selected != null && Files.isDirectory(curClientDir.resolve(selected).normalize())){
                doClientCD(selected);
            }
        }
    }

    private void doClientCD(String selected) throws IOException {
        curClientDir = curClientDir.resolve(selected).normalize();
        refreshListFiles();
    }

    public void listServerClick(MouseEvent mouseEvent) {
        activeNum = 2;
        if(mouseEvent.getClickCount() == 2){
            String selected = serverListView.getSelectionModel().getSelectedItem();
            if(selected != null && selected.split("\\.").length == 1){
                Network.getInstance().send(new ChangeDirCommand(selected));
            }
        }
    }

    public void doCDUp(MouseEvent mouseEvent) throws IOException {
        if(activeNum == 1){
            curClientDir = curClientDir.resolve("..").normalize();
            refreshListFiles();
        }else if(activeNum == 2){
            Network.getInstance().send(new ChangeDirCommand(".."));
        }
    }

    public void doDelete(MouseEvent mouseEvent) throws IOException {
        if(activeNum == 1){
            String selected = listView.getSelectionModel().getSelectedItem();
            if(selected != null){
                Path path = curClientDir.resolve(selected).normalize();
                if(Files.isRegularFile(path)){
                    Files.delete(path);
                }else {
                    if(Files.list(path).collect(Collectors.toList()).size() > 0){
                        Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание", "Предупреждение", "Папка не пуста! Проверьте содержимое папки.");
                    }else {
                        Files.delete(path);
                    }
                }
                refreshListFiles();
            }
        }else if(activeNum == 2){
            String selected = serverListView.getSelectionModel().getSelectedItem();
            if(selected != null){
                Network.getInstance().send(new DeleteCommand(selected));
            }
        }
    }

    public void openChangeNameWindow(MouseEvent mouseEvent) {
        App instance = App.INSTANCE;
        instance.getChangeNameController().setCommandType(CommandType.CHANGE_NAME_COMMAND);
        instance.getChangeNameController().setLabel("Введите новое имя:");
        instance.getChangeNameController().setOldName(serverListView.getSelectionModel().getSelectedItem());
        instance.getChangeNameStage().show();
    }
    public void getRefreshFiles() throws IOException {
        refreshListFiles();
    }

    public void createFile(MouseEvent mouseEvent) {
        App instance = App.INSTANCE;
        instance.getChangeNameController().setCommandType(CommandType.TOUCH);
        instance.getChangeNameController().setLabel("Введите имя для создаваемого файла:");
        instance.getChangeNameStage().show();
    }
}

