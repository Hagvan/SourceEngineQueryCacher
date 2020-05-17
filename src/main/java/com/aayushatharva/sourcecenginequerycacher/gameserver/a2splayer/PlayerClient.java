package com.aayushatharva.sourcecenginequerycacher.gameserver.a2splayer;

import com.aayushatharva.sourcecenginequerycacher.Main;
import com.aayushatharva.sourcecenginequerycacher.gameserver.a2sinfo.InfoClient;
import com.aayushatharva.sourcecenginequerycacher.utils.Config;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PlayerClient extends Thread {

    private static final Logger logger = LogManager.getLogger(InfoClient.class);
    private boolean keepRunning = true;

    public PlayerClient(String name) {
        super(name);
    }

    @SuppressWarnings("BusyWait")
    public void run() {

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(Main.eventLoopGroup)
                    .channelFactory(() -> {
                        if (Config.Transport.equalsIgnoreCase("epoll") && Epoll.isAvailable()) {
                            return new EpollDatagramChannel(InternetProtocolFamily.IPv4);
                        } else {
                            return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                        }
                    })
                    .option(ChannelOption.ALLOCATOR, Main.BYTE_BUF_ALLOCATOR)
                    .option(ChannelOption.SO_SNDBUF, Config.SendBufferSize)
                    .option(ChannelOption.SO_RCVBUF, Config.ReceiveBufferSize)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(Config.FixedReceiveAllocatorBufferSize))
                    .handler(new PlayerHandler());


            Channel channel = bootstrap.bind(0).sync().channel();

            while (keepRunning) {
                channel.writeAndFlush(new DatagramPacket(Packets.A2S_PLAYER_CHALLENGE_REQUEST_2.copy(), Config.GameServer)).sync();
                sleep(Config.GameUpdateInterval);
            }

            channel.closeFuture().sync();
        } catch (Exception ex) {
            logger.atError().withThrowable(ex).log("Error occurred");
        }
    }

    public void shutdown() {
        this.interrupt();
        keepRunning = false;
    }
}