package com.geekbrains.netty;

import com.geekbrains.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private Path currentDir = Server.ROOT_DIR;

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
                break;
            case MK_DIR: doMKDir(ctx,(MKDirCommand)msg);
                break;
            case TOUCH: doTouch(ctx, (TouchCommand)msg);
                break;
            default: ctx.writeAndFlush(new InfoMessage("Unknown command!"));
        }
    }

    private void doTouch(ChannelHandlerContext ctx, TouchCommand msg) throws IOException {
        Path path = currentDir.resolve(msg.getNewFile()).normalize();
        if (Files.exists(path)){
            ctx.writeAndFlush(new InfoMessage("File with same name already exists"));
        } else Files.createFile(path);
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
            if (path.startsWith(Server.ROOT_DIR)) {
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
}
