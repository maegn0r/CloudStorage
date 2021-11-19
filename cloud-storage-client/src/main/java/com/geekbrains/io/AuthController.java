package com.geekbrains.io;

import com.geekbrains.model.AuthCommandData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import dialogs.Dialogs;

public class AuthController {

    @FXML
    public CheckBox checkIsNew;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;


    @FXML
    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.length() == 0 || password == null || password.length() == 0) {
            Dialogs.AuthError.EMPTY_CREDENTIALS.show();
            return;
        }
        Network.getInstance().send(new AuthCommandData(login, password, !checkIsNew.isIndeterminate() && checkIsNew.isSelected()));
    }
}
