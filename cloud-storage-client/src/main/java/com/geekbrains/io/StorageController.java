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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import lombok.Getter;
import lombok.SneakyThrows;

public class StorageController implements Initializable {

    public ListView<FileInfo> listView;   //num 1
    public ListView<FileInfo> serverListView; //num 2
    public Button upload,download, renameBtn, createBtn, createDirBtn, refreshBtn, upBtn, deleteBtn;
//    public Button download;
    @Getter
    private final String FILE_ROOT_PATH = "StorageDir";
//    public Button renameBtn;
//    public Button createBtn;
//    public Button createDirBtn;
    @Getter
    private Path curClientDir;
    public static StorageController INSTANCE;


    public StorageController(){
        INSTANCE = this;
    }


    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.curClientDir = Paths.get(FILE_ROOT_PATH);
        initListFiles();
        listView.setCellFactory(new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
            @Override
            public ListCell<FileInfo> call(ListView<FileInfo> param) {
                return new ListCell<FileInfo>() {
                    @Override
                    protected void updateItem (FileInfo item, boolean empty){
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            String formattedFileName = String.format("%-30s", item.getFileName());
                            String formattedFileLength = String.format("%, d bytes", item.getFileSize());
                            if (item.getFileSize() == -1L) {
                                formattedFileLength = String.format("%s", "[ DIR ]");
                            }
                            String text = String.format("%s %20s", formattedFileName, formattedFileLength);
                            setText(text);
                        }
                    }

                };
            }
        });
        serverListView.setCellFactory(new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
            @Override
            public ListCell<FileInfo> call(ListView<FileInfo> param) {
                return new ListCell<FileInfo>() {
                    @Override
                    protected void updateItem (FileInfo item, boolean empty){
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            String formattedFileName = String.format("%-30s", item.getFileName());
                            String formattedFileLength = String.format("%, d bytes", item.getFileSize());
                            if (item.getFileSize() == -1L) {
                                formattedFileLength = String.format("%s", "[ DIR ]");
                            }
                            String text = String.format("%s %20s", formattedFileName, formattedFileLength);
                            setText(text);
                        }
                    }

                };
            }
        });
        download.setFocusTraversable(false);
        setFocusOff(upload,download, renameBtn, createBtn, createDirBtn, refreshBtn, upBtn, deleteBtn);
    }

    private void setFocusOff(Button ... buttons) {
        for (Button button : buttons) {
            button.setFocusTraversable(false);
        }
    }

    private void initListFiles() throws IOException {
        File fileRootPath = new File(FILE_ROOT_PATH);
        if (!fileRootPath.exists()) {
            fileRootPath.mkdir();
        }
        Path path = Paths.get(FILE_ROOT_PATH);
        List<FileInfo> list = Files.list(path).map(item -> new FileInfo(item)).collect(Collectors.toList());
        listView.getItems().addAll(list);
    }

    private void refreshListFiles() throws IOException {
           List<FileInfo> list = Files.list(curClientDir).map(item -> new FileInfo(item)).collect(Collectors.toList());
           listView.getItems().clear();
           listView.getItems().addAll(list);
    }


    private void filePrepare(FileInfo s) {
        File file = new File(FILE_ROOT_PATH + "/" + s.getFileName());
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
        FileInfo selectedItem = listView.getSelectionModel().getSelectedItem();
        if (serverListView.getItems().stream().map(FileInfo::getFileName).anyMatch(item -> item.equals(selectedItem.getFileName())))  {
            Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание", "Предупреждение", "Файл с таким именем уже существует! Переименуйте или удалите этот файл.");
        } else {
            if (selectedItem != null) {
                filePrepare(selectedItem);
            }
            Network.getInstance().send(new LSCommand());
        }
    }

    @FXML
    private void doDownload() throws IOException {
        FileInfo selectedItem = serverListView.getSelectionModel().getSelectedItem();
        if (Files.list(curClientDir).map(item -> item.getFileName().toString()).anyMatch(item -> item.equals(selectedItem.getFileName()))){
            Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание", "Предупреждение", "Файл с таким именем уже существует! Переименуйте или удалите этот файл.");
        } else {
            if (selectedItem != null) {
                Network.getInstance().send(new DownloadRequestCommand(selectedItem.getFileName()));
            }
            refreshListFiles();
        }
    }

    public void doRefresh(MouseEvent mouseEvent) throws IOException {
        if (listView.isFocused()) {
            refreshListFiles();
        } else if (serverListView.isFocused()) {
            Network.getInstance().send(new LSCommand());
        }
    }

    public void listClientClick(MouseEvent mouseEvent) throws IOException {
        if(mouseEvent.getClickCount() == 2){
            FileInfo selected = listView.getSelectionModel().getSelectedItem();
            if(selected != null && Files.isDirectory(curClientDir.resolve(selected.getFileName()).normalize())){
                doClientCD(selected.getFileName());
            }
        }
    }

    private void doClientCD(String selected) throws IOException {
        curClientDir = curClientDir.resolve(selected).normalize();
        refreshListFiles();
    }

    public void listServerClick(MouseEvent mouseEvent) throws IOException {
        if(mouseEvent.getClickCount() == 2){
            FileInfo selected = serverListView.getSelectionModel().getSelectedItem();
            if(selected != null && selected.getFileSize() == -1){
                Network.getInstance().send(new ChangeDirCommand(selected.getFileName()));
            }
        }
    }

    public void doCDUp(MouseEvent mouseEvent) throws IOException {
        if(listView.isFocused()){
            curClientDir = curClientDir.resolve("..").normalize();
            refreshListFiles();
        }else if(serverListView.isFocused()){
            Network.getInstance().send(new ChangeDirCommand(".."));
            refreshListFiles();
        }
    }

    public void doDelete(MouseEvent mouseEvent) throws IOException {
        if(listView.isFocused()){
            FileInfo selected = listView.getSelectionModel().getSelectedItem();
            if(selected != null){
                Path path = curClientDir.resolve(selected.getFileName()).normalize();
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
        }else if(serverListView.isFocused()){
            FileInfo selected = serverListView.getSelectionModel().getSelectedItem();
            if(selected != null){
                Network.getInstance().send(new DeleteCommand(selected.getFileName()));
            }
        }
    }

    public void renameFile(MouseEvent mouseEvent) {
        if (listView.isFocused()) {
            FileInfo fis = listView.getSelectionModel().getSelectedItem();
            if (fis == null){
                return;
            }
            App.INSTANCE.getChangeNameController().setOldName(fis.getFileName());
        } else {
            FileInfo fis = serverListView.getSelectionModel().getSelectedItem();
            if (fis == null){
                return;
            }
            App.INSTANCE.getChangeNameController().setOldName(fis.getFileName());
        }
        App instance = App.INSTANCE;
        instance.getChangeNameController().setCommandType(CommandType.CHANGE_NAME_COMMAND);
        instance.getChangeNameController().setLabel("Введите новое имя:");
        instance.getChangeNameController().setActiveNum(listView.isFocused() ? 1 : 2);
        instance.getChangeNameController().newNameField.setText("");
        instance.getChangeNameStage().show();
    }
    public void getRefreshFiles() throws IOException {
        refreshListFiles();
    }

    public void createFile(MouseEvent mouseEvent) {
        App instance = App.INSTANCE;
        instance.getChangeNameController().setCommandType(CommandType.TOUCH);
        instance.getChangeNameController().setActiveNum(listView.isFocused() ? 1 : 2);
        instance.getChangeNameController().setLabel("Введите имя для создаваемого файла:");
        instance.getChangeNameController().newNameField.setText("");
        instance.getChangeNameStage().show();
    }

    public void createDir(MouseEvent mouseEvent) {
        App instance = App.INSTANCE;
        instance.getChangeNameController().setCommandType(CommandType.MK_DIR);
        instance.getChangeNameController().setActiveNum(listView.isFocused() ? 1 : 2);
        instance.getChangeNameController().setLabel("Введите имя для создаваемой папки:");
        instance.getChangeNameController().newNameField.setText("");
        instance.getChangeNameStage().show();

    }
}

