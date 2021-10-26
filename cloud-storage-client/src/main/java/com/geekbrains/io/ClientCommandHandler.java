package com.geekbrains.io;

import com.geekbrains.model.AbstractCommand;
import com.geekbrains.model.InfoMessage;
import com.geekbrains.model.LSFileCommand;
import com.sun.javafx.collections.ImmutableObservableList;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientCommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private final StorageController controller;

    public ClientCommandHandler(StorageController controller) {
        this.controller = controller;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand msg) throws Exception {
        log.info(msg.getType().toString());
        handleCommand(ctx, msg);
    }

    private void handleCommand(ChannelHandlerContext ctx, AbstractCommand msg) {
        switch (msg.getType()){
            case INFO:
                String message = ((InfoMessage)msg).getMessage();
                Platform.runLater(() -> controller.label.setText(message));
                    break;
            case LS_FILES:
                String [] arr = ((LSFileCommand) msg).getFileList().toArray(new String[0]);
                Platform.runLater(()-> controller.serverListView.setItems(new ImmutableObservableList<>(arr)));
                break;
        }
    }
}
