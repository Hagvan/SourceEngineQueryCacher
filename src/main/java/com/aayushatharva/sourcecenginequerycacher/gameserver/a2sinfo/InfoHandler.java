package com.aayushatharva.sourcecenginequerycacher.gameserver.a2sinfo;

import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import com.aayushatharva.sourcecenginequerycacher.utils.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class InfoHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(InfoHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        // Set new Packet Data
        if (ByteBufUtil.equals(Packets.A2S_GENERIC_CHALLENGE_RESPONSE, datagramPacket.content().slice(0, 5))) {
            ByteBuf responseBuf = ctx.alloc().buffer()
                    .writeBytes(Packets.A2S_INFO_REQUEST.retainedDuplicate())
                    .writeBytes(datagramPacket.content().slice(5, 4));

            ctx.writeAndFlush(responseBuf, ctx.voidPromise());
        } else if (ByteBufUtil.equals(Packets.A2S_INFO_RESPONSE_HEADER, datagramPacket.content().slice(0, 5))) {
            // Set new Packet Data
            CacheHub.A2S_INFO.clear().writeBytes(datagramPacket.content());

            logger.debug("New A2SInfo Update Cached Successfully");
        } else {
            logger.error("Received unsupported A2S Info Response from Game Server: {}", ByteBufUtil.hexDump(datagramPacket.content()));
        }
    }
}
