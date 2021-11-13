package com.geekbrains.io;

import com.geekbrains.model.ChangeNameCommand;
import com.geekbrains.model.CommandType;
import com.geekbrains.model.InfoMessage;
import com.geekbrains.model.TouchCommand;
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

public class ChangeNameController {
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

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public void setLabel(String label) {
        Platform.runLater(() -> this.label.setText(label));
//        this.label.setText(label);
    }


    @FXML
    public void closeChangeNameWindow(ActionEvent actionEvent) {
        App.INSTANCE.getChangeNameStage().close();
    }

    @FXML
    public void executeChangeName(ActionEvent actionEvent) throws IOException {
        String newName = newNameField.getText();
        if (commandType == CommandType.CHANGE_NAME_COMMAND) {
            if (StorageController.INSTANCE.getActiveNum() == 1) {
                String selected = StorageController.INSTANCE.listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Path path = StorageController.INSTANCE.getCurClientDir().resolve(selected).normalize();
                    Files.move(path, path.resolveSibling(newName));
                    StorageController.INSTANCE.getRefreshFiles();
                    newNameField.setText("");
                    App.INSTANCE.getChangeNameStage().close();
                }
            } else if (StorageController.INSTANCE.getActiveNum() == 2) {
                if (newName == null || newName.length() == 0 || oldName == null || oldName.length() == 0) {
                    Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание!", "Предупреждение", "Имя не может быть пустым!");
                }
                Network.getInstance().send(new ChangeNameCommand(oldName, newName));
                newNameField.setText("");
                App.INSTANCE.getChangeNameStage().close();
            }
        } else if (commandType == CommandType.TOUCH) {
                if (StorageController.INSTANCE.getActiveNum() == 1) {
                    if (newName != null) {
                        Path path = StorageController.INSTANCE.getCurClientDir().resolve(newName).normalize();
                        if (Files.exists(path)){
                            Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание!", "Предупреждение", "Файл с таким именем уже существует!");
                        } else {
                            Files.createFile(path);
                            StorageController.INSTANCE.getRefreshFiles();
                            newNameField.setText("");
                            App.INSTANCE.getChangeNameStage().close();
                        }
                    }
                } else if (StorageController.INSTANCE.getActiveNum() == 2){
                    if (newName == null || newName.length() == 0) {
                        Dialogs.showDialog(Alert.AlertType.ERROR, "Внимание!", "Предупреждение", "Имя не может быть пустым!");
                    } else {
                        Network.getInstance().send(new TouchCommand(newName));
                        newNameField.setText("");
//                        App.INSTANCE.getChangeNameStage().close();
                    }
            }
        }
    }
}

