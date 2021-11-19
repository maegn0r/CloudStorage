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

    public CommandHandler(IAuthService authService1) {
        this.authService = authService1;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractCommand msg) throws Exception {
        handleCommand(ctx, msg);
    }


    private void handleCommand(ChannelHandlerContext ctx, AbstractCommand msg) throws IOException {
        switch (msg.getType()) {
            case LS:
                doLS(ctx);
                break;
            case CHANGE_DIR:
                doChangeDir(ctx, (ChangeDirCommand) msg);
                doLS(ctx);
                break;
            case MK_DIR:
                doMKDir(ctx, (MKDirCommand) msg);
                doLS(ctx);
                break;
            case TOUCH:
                doTouch(ctx, (TouchCommand) msg);
                break;
            case UPLOAD:
                doUpload(ctx, (UploadCommand) msg);
                break;
            case UPLOAD_DATA_COMMAND:
                doUploadData(ctx, (UploadDataCommand) msg);
                doLS(ctx);
                break;
            case DOWNLOAD:
                doDownload(ctx, (DownloadRequestCommand) msg);
                break;
            case AUTH:
                checkAuth(ctx, (AuthCommandData) msg);
                break;
            case DELETE:
                doDelete(ctx, (DeleteCommand) msg);
                break;
            case CHANGE_NAME_COMMAND:
                doChangeName(ctx, (ChangeNameCommand) msg);
                break;
            default:
                ctx.writeAndFlush(new InfoMessage("Неизвестная команда!"));
        }
    }

    private void doDelete(ChannelHandlerContext ctx, DeleteCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getFileName()).normalize();
        if (Files.isRegularFile(path)) {
            Files.delete(path);
            doLS(ctx);
        } else {
            if (Files.list(path).collect(Collectors.toList()).size() > 0) {
                ctx.writeAndFlush(new InfoMessage("Папка не пуста! Проверьте содержимое папки."));
            } else {
                Files.delete(path);
                doLS(ctx);
            }
        }
    }

    private void checkAuth(ChannelHandlerContext ctx, AuthCommandData msg) throws IOException {
        if (msg.isNewUser()) {
            if (authService.createNewUser(msg.getLogin(), msg.getPassword())) {
                ctx.writeAndFlush(new AuthOkCommand());
                clientStatus.setLogIn(true);
                checkUserDir(ctx, msg);
                doLS(ctx);
            } else {
                ctx.writeAndFlush(new InfoMessage("Пользователь с таким логином уже существует"));
            }
        } else {
            if (authService.checkLoginAndPassword(msg.getLogin(), msg.getPassword())) {
                ctx.writeAndFlush(new AuthOkCommand());
                clientStatus.setLogIn(true);
                checkUserDir(ctx, msg);
                doLS(ctx);
            } else {
                ctx.writeAndFlush(new InfoMessage("Неверные логин и пароль"));
            }
        }
    }

    private void doDownload(ChannelHandlerContext ctx, DownloadRequestCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getFileName()).normalize();
        if (Files.exists(path) && !Files.isDirectory(path)) {
            clientStatus.setCurrentAction(ActionType.DOWNLOAD);
            clientStatus.setFileSize(Files.size(path));
            clientStatus.setCurrentPart(0);
            clientStatus.setCurrentFileName(currentDir.resolve(msg.getFileName()).normalize());
            FileUtils.sendFile(ctx, clientStatus);
        } else ctx.writeAndFlush(new InfoMessage("Папку скачать пока нельзя"));
    }

    private void doUploadData(ChannelHandlerContext ctx, UploadDataCommand msg) throws IOException {
        FileUtils.uploadPart(clientStatus, msg, new Callback() {
            @Override
            public void callback() throws IOException {
                doLS(ctx);
            }
        });
    }

    private void doUpload(ChannelHandlerContext ctx, UploadCommand msg) throws IOException {
        boolean result = doTouch(ctx, new TouchCommand(msg.getFileName()));
        if (result) {
            clientStatus.setCurrentAction(ActionType.UPLOAD);
            clientStatus.setFileSize(msg.getFileSize());
            clientStatus.setCurrentPart(0);
            clientStatus.setCurrentFileName(currentDir.resolve(msg.getFileName()).normalize());
        } else {
            ctx.writeAndFlush(new InfoMessage("Файл с таким именем уже существует"));
        }
    }

    private boolean doTouch(ChannelHandlerContext ctx, TouchCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getNewName()).normalize();
        if (Files.exists(path)) {
            ctx.writeAndFlush(new InfoMessage("Файл с таким именем уже существует"));
        } else {
            Files.createFile(path);
            doLS(ctx);
        }
        return true;
    }

    private void doMKDir(ChannelHandlerContext ctx, MKDirCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getNewDir()).normalize();
        if (Files.exists(path)) {
            ctx.writeAndFlush(new InfoMessage("Папка с таким именем уже существует"));
        } else Files.createDirectory(path);
    }

    private void doChangeDir(ChannelHandlerContext ctx, ChangeDirCommand msg) throws IOException {
        System.out.println("1");
        Path path = currentDir.resolve(msg.getDestinationDir()).normalize();
        if (Files.isDirectory(path) && Files.exists(path)) {
            System.out.println("2");
            if (path.startsWith(mainUserDir)) {
                currentDir = path;
            }
        } else {
            String errMessage = "Папка не найдена!";
            ctx.writeAndFlush(new InfoMessage(errMessage));
        }
    }

    private void doLS(ChannelHandlerContext ctx) throws IOException {
        List<FileInfo> result = Files.list(currentDir).map(item -> new FileInfo(item)).collect(Collectors.toList());
        ctx.writeAndFlush(new LSFileCommand(result));
    }

    private void checkUserDir(ChannelHandlerContext ctx, AuthCommandData msg) throws IOException {
        Path path = Server.ROOT_DIR.resolve(msg.getLogin()).normalize();
        if (clientStatus.isLogIn()) {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            mainUserDir = path;
            currentDir = path;
        }
    }

    private void doChangeName(ChannelHandlerContext ctx, ChangeNameCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getOldName()).normalize();
        Files.move(path, path.resolveSibling(msg.getNewName()));
        doLS(ctx);
    }
}
