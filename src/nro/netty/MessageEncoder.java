package nro.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import nro.models.network.Message;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        Session session = ctx.channel().attr(SessionManager.SESSION_KEY).get();
        if (session != null) {
            // Logic to encode messages will be added later
        }
    }
}