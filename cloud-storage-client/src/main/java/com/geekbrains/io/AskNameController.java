package com.geekbrains.io;

import com.geekbrains.model.*;
import dialogs.Dialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AskNameController {
    @FXML
    public TextField newNameField;
    @FXML
    public Button doneNewNameButton;
    @FXML
    public Button iWantOldNameButton;
    @FXML
    private Label label;

    private String oldName;

    private CommandType commandType;

    private int activeNum;

    public void setActiveNum(int activeNum) {
        this.activeNum = activeNum;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public void setLabel(String label) {
        Platform.runLater(() -> this.label.setText(label));
    }


    @FXML
    public void closeChangeNameWindow(ActionEvent actionEvent) {
        App.INSTANCE.getChangeNameStage().close();
    }

    @FXML
    public void executeRenameTouchMakeDir(ActionEvent actionEvent) throws IOException {
        String newName = newNameField.getText();
        if (newName == null || newName.length() == 0) {
            Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание!", "Предупреждение", "Имя не может быть пустым!");
        } else {
            if (commandType == CommandType.CHANGE_NAME_COMMAND) {
                if (activeNum == 1) {

                    Path path = StorageController.INSTANCE.getCurClientDir().resolve(oldName).normalize();
                    Files.move(path, path.resolveSibling(newName));
                    StorageController.INSTANCE.getRefreshFiles();
                    App.INSTANCE.getChangeNameStage().close();

                } else if (activeNum == 2) {
                    Network.getInstance().send(new ChangeNameCommand(oldName, newName));
                    App.INSTANCE.getChangeNameStage().close();
                }
            } else if (commandType == CommandType.TOUCH) {
                if (activeNum == 1) {
                    Path path = StorageController.INSTANCE.getCurClientDir().resolve(newName).normalize();
                    if (Files.exists(path)) {
                        Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание!", "Предупреждение", "Файл с таким именем уже существует!");
                        newNameField.setText("");
                    } else {
                        Files.createFile(path);
                        StorageController.INSTANCE.getRefreshFiles();
                        App.INSTANCE.getChangeNameStage().close();
                    }
                } else if (activeNum == 2) {
                    Network.getInstance().send(new TouchCommand(newName));
                }
            } else if (commandType == CommandType.MK_DIR) {
                if (activeNum == 1) {
                    Path path = StorageController.INSTANCE.getCurClientDir().resolve(newName).normalize();
                    if (Files.exists(path)) {
                        Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание!", "Предупреждение", "Файл с таким именем уже существует!");
                        newNameField.setText("");
                    } else {
                        Files.createDirectory(path);
                        StorageController.INSTANCE.getRefreshFiles();
                        App.INSTANCE.getChangeNameStage().close();
                    }
                } else if (activeNum == 2) {
                    Network.getInstance().send(new MKDirCommand(newName));
                }
            }
        }
    }
}

