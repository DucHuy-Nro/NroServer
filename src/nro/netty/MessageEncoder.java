package nro.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import nro.models.network.Message;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        Session session = ctx.channel().attr(SessionManager.SESSION_KEY).get();
        if (session != null && msg != null) {
            try {
                byte[] data = msg.getData();
                byte command = msg.getCommand();
                int size = data.length;

                if (size <= 32767) {
                    if (size > 255) {
                        out.writeByte(command);
                        out.writeShort(size);
                    } else {
                        out.writeByte(command);
                        out.writeByte(size);
                    }
                } else {
                    out.writeByte(command);
                    out.writeInt(size);
                }
                out.writeBytes(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}