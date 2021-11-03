package com.geekbrains.netty;

import com.geekbrains.model.*;
import com.geekbrains.netty.auth.IAuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private Path currentDir;
    private Path mainUserDir;
    private ClientStatus clientStatus = new ClientStatus();
    private IAuthService authService;

    public CommandHandler (IAuthService authService1){
        this.authService = authService1;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractCommand msg) throws Exception {
        handleCommand(ctx, msg);
    }


    private void handleCommand(ChannelHandlerContext ctx, AbstractCommand msg) throws IOException {
        switch (msg.getType()) {
            case LS: doLS(ctx);
                break;
            case CHANGE_DIR:
                doChangeDir(ctx,(ChangeDirCommand)msg);
                doLS(ctx);
                break;
            case MK_DIR: doMKDir(ctx,(MKDirCommand)msg);
                doLS(ctx);
                break;
            case TOUCH: doTouch(ctx, (TouchCommand)msg);
                doLS(ctx);
                break;
            case UPLOAD: doUpload(ctx,(UploadCommand)msg);
                break;
            case UPLOAD_DATA_COMMAND: doUploadData((UploadDataCommand) msg);
                doLS(ctx);
                break;
            case DOWNLOAD: doDownload(ctx,(DownloadRequestCommand) msg);
                break;
            case AUTH:
                checkAuth(ctx, (AuthCommandData) msg);
                break;
            default: ctx.writeAndFlush(new InfoMessage("Unknown command!"));
        }
    }

    private void checkAuth(ChannelHandlerContext ctx, AuthCommandData msg) throws IOException {
        if(authService.checkLoginAndPassword(msg.getLogin(), msg.getPassword())){
            ctx.writeAndFlush(new AuthOkCommand());
            clientStatus.setLogIn(true);
            checkUserDir(ctx, msg);
            doLS(ctx);
        } else {
            ctx.writeAndFlush(new InfoMessage("Неверные логин и пароль"));
        }
    }

    private void doDownload(ChannelHandlerContext ctx, DownloadRequestCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getFileName()).normalize();
        if (Files.exists(path)){
            clientStatus.setCurrentAction(ActionType.DOWNLOAD);
            clientStatus.setFileSize(Files.size(path));
            clientStatus.setCurrentPart(0);
            clientStatus.setCurrentFileName(currentDir.resolve(msg.getFileName()).normalize());
            FileUtils.sendFile(ctx,clientStatus);
        } else ctx.writeAndFlush(new InfoMessage("File not found"));
    }

    private void doUploadData(UploadDataCommand msg) throws IOException {
        FileUtils.uploadPart(clientStatus, msg);
    }

    private void doUpload(ChannelHandlerContext ctx, UploadCommand msg) throws IOException {
        boolean result = doTouch(ctx, new TouchCommand(msg.getFileName()));
            if (result){
                clientStatus.setCurrentAction(ActionType.UPLOAD);
                clientStatus.setFileSize(msg.getFileSize());
                clientStatus.setCurrentPart(0);
                clientStatus.setCurrentFileName(currentDir.resolve(msg.getFileName()).normalize());
        } else {
            ctx.writeAndFlush(new InfoMessage("File with same name already exists"));
        }
    }

    private boolean doTouch(ChannelHandlerContext ctx, TouchCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getNewFile()).normalize();
        if (Files.exists(path)){
            ctx.writeAndFlush(new InfoMessage("File with same name already exists"));
        } else Files.createFile(path);
        return true;
    }

    private void doMKDir(ChannelHandlerContext ctx, MKDirCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getNewDir()).normalize();
        if (Files.exists(path)){
            ctx.writeAndFlush(new InfoMessage("Directory with same name already exists"));
        } else Files.createDirectory(path);
    }

    private void doChangeDir(ChannelHandlerContext ctx, ChangeDirCommand msg) {
        Path path = currentDir.resolve(msg.getDestinationDir()).normalize();
        if (Files.isDirectory(path) && Files.exists(path)) {
            if (path.startsWith(mainUserDir)) {
                currentDir = path;
            }
            ctx.flush();
        } else {
            String errMessage = "Directory not found!";
            ctx.writeAndFlush(new InfoMessage(errMessage));
        }
    }

    private void doLS(ChannelHandlerContext ctx) throws IOException {
        List<String> result = Files.list(currentDir).map(item -> item.getFileName().toString()).collect(Collectors.toList());
        ctx.writeAndFlush(new LSFileCommand(result));
    }
    private void checkUserDir (ChannelHandlerContext ctx, AuthCommandData msg) throws IOException {
        Path path = Server.ROOT_DIR.resolve(msg.getLogin()).normalize();
        if (clientStatus.isLogIn()){
            if (!Files.exists(path)){
                Files.createDirectory(path);
            }
           mainUserDir = path;
            currentDir = path;
        }
    }
}
