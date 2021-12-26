package com.geekbrains.io;

import com.geekbrains.model.*;
import com.sun.javafx.collections.ImmutableObservableList;
import dialogs.Dialogs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class ClientCommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private final StorageController controller;
    private Callback call;
    private ClientStatus clientStatus = new ClientStatus();

    public ClientCommandHandler(StorageController controller) {
        this.controller = controller;
        this.call = new Callback() {
            @Override
            public void callback() throws IOException {
                Platform.runLater(() -> {
                    try {
                        StorageController.INSTANCE.getRefreshFiles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        };
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand msg) throws Exception {
        log.info(msg.getType().toString());
        handleCommand(ctx, msg);
    }

    private void handleCommand(ChannelHandlerContext ctx, AbstractCommand msg) throws IOException {
        switch (msg.getType()) {
            case INFO:
                String message = ((InfoMessage) msg).getMessage();
                Platform.runLater(() -> Dialogs.showDialog(Alert.AlertType.ERROR, "Сообщение от сервера", "Внимание", message));
                break;
            case LS_FILES:
                List<FileInfo> list = ((LSFileCommand) msg).getFileList();

                Platform.runLater(() -> {
                    App.INSTANCE.getChangeNameStage().close();
                    StorageController.INSTANCE.serverListView.getItems().clear();
                    StorageController.INSTANCE.serverListView.getItems().addAll(list);
                });
                break;
            case UPLOAD:
                UploadCommand command = (UploadCommand) msg;
                Path path = Paths.get(controller.getFILE_ROOT_PATH() + "/" + command.getFileName());
                boolean result = FileUtils.touchFile(path, call);
                if (result) {
                    clientStatus.setCurrentFileName(path);
                    clientStatus.setFileSize(command.getFileSize());
                    clientStatus.setCurrentPart(0);
                    clientStatus.setPartsCount(((UploadCommand) msg).getPartsCount());
                    clientStatus.setCurrentAction(ActionType.DOWNLOAD);
                }
                break;
            case UPLOAD_DATA_COMMAND:
                UploadDataCommand data = (UploadDataCommand) msg;
                FileUtils.uploadPart(clientStatus, data, call);
                break;
            case AUTH_OK:
                Platform.runLater(() -> App.INSTANCE.switchToMainWindow());
        }
    }
}
