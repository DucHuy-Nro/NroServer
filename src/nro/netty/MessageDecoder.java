package nro.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import nro.models.network.Message;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Session session = ctx.channel().attr(SessionManager.SESSION_KEY).get();
        if (session != null) {
            // Logic to decode messages will be added later
        }
    }
}