package com.geekbrains.io;

import com.geekbrains.model.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;

public class Network {

    private static Network currentInstance = new Network();

    private Channel currentChannel;

    public static Network getInstance() {
        return currentInstance;
    }

    private Network() {

    }

    public void start(StorageController controller) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 8189));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    currentChannel = socketChannel;
                    socketChannel.pipeline().addLast
                            (
                                    new ObjectDecoder(100 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                             new ClientCommandHandler(controller));
                }
            });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLS(LSCommand command) {
        currentChannel.writeAndFlush(command);
    }

    public void sendTouch(TouchCommand command) {
        currentChannel.writeAndFlush(command);
    }

    public void sendChangeDir(ChangeDirCommand command) {
        currentChannel.writeAndFlush(command);
    }

    public void sendMKDir(MKDirCommand command) {
        currentChannel.writeAndFlush(command);
    }

    public void send(AbstractCommand command){
        currentChannel.writeAndFlush(command);
    }
}
