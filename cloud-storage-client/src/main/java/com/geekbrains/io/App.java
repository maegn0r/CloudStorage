package com.geekbrains.io;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public static App INSTANCE;

    private static final String STORAGE_WINDOW_FXML = "storageWindow.fxml";
    private static final String AUTH_DIALOG_FXML = "authDialog.fxml";
    private static final String CHANGE_NAME_FXML = "changeName.fxml";

    private Stage primaryStage;
    private Stage authStage;
    private Stage changeNameStage;
    private FXMLLoader storageWindowLoader;
    private FXMLLoader authLoader;
    private FXMLLoader changeNameLoader;


    @Override
    public void init() throws Exception {
        INSTANCE = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        initViews();
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        authStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        getStorageWindowStage().show();
        getAuthStage().show();
        new Thread(() -> Network.getInstance().start(getStorageController())).start();
    }

    public Stage getChangeNameStage() {
        return changeNameStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getAuthStage() {
        return authStage;
    }

    private AuthController getAuthController() {
        return authLoader.getController();
    }

    public StorageController getStorageController() {
        return storageWindowLoader.getController();
    }

    public AskNameController getChangeNameController() {
        return changeNameLoader.getController();
    }

    private void initViews() throws IOException {
        initChatWindow();
        initAuthDialog();
        initChangeNameDialog();
    }


    void initChangeNameDialog() throws IOException {
        changeNameLoader = new FXMLLoader();
        changeNameLoader.setLocation(App.class.getResource(CHANGE_NAME_FXML));
        Parent changeNamePanel = changeNameLoader.load();

        changeNameStage = new Stage();
        changeNameStage.initOwner(primaryStage);
        changeNameStage.initModality(Modality.WINDOW_MODAL);
        changeNameStage.setScene(new Scene(changeNamePanel));

    }

    private void initChatWindow() throws IOException {
        storageWindowLoader = new FXMLLoader();
        storageWindowLoader.setLocation(App.class.getResource(STORAGE_WINDOW_FXML));

        Parent root = storageWindowLoader.load();
        this.primaryStage.setScene(new Scene(root));

        setStageForSecondScreen(primaryStage);
    }


    private void initAuthDialog() throws java.io.IOException {
        authLoader = new FXMLLoader();
        authLoader.setLocation(App.class.getResource(AUTH_DIALOG_FXML));
        Parent authDialogPanel = authLoader.load();

        authStage = new Stage();
        authStage.initOwner(primaryStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(authDialogPanel));
    }


    private void setStageForSecondScreen(Stage primaryStage) {
        Screen secondScreen = getSecondScreen();
        Rectangle2D bounds = secondScreen.getBounds();
        primaryStage.setX(bounds.getMinX() + (bounds.getWidth() - 300) / 2);
        primaryStage.setY(bounds.getMinY() + (bounds.getHeight() - 200) / 2);
    }

    private Screen getSecondScreen() {
        for (Screen screen : Screen.getScreens()) {
            if (!screen.equals(Screen.getPrimary())) {
                return screen;
            }
        }
        return Screen.getPrimary();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getStorageWindowStage() {
        return primaryStage;
    }

    public void switchToMainWindow() {
        getAuthStage().close();
    }
}