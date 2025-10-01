package nro.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import nro.models.network.Message;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        Session session = ctx.channel().attr(SessionManager.SESSION_KEY).get();
        if (session != null) {
            while(in.isReadable()) {
                byte b = in.readByte();
                if (session.isLogin) {
                    // Đã login thì xử lý luồng dữ liệu bình thường
                } else {
                    // Chưa login, xử lý handshake
                    if (b == 20) {
                        handshake(session, in);
                        session.isLogin = true; // Giả định handshake thành công
                    }
                }
            }
            // Phần logic đọc message chính sẽ được chuyển vào đây
            // Tạm thời để trống để đảm bảo cấu trúc
        }
    }

    private void handshake(Session session, ByteBuf dis) {
        try {
            byte key = dis.readByte();
            session.setCurR(key);
            session.setCurW(key);

            byte[] sendKey = new byte[key];
            for (int i = 0; i < key; i++) {
                sendKey[i] = dis.readByte();
            }
            session.setSendKey(sendKey);

            byte[] recvKey = new byte[key];
            for (int i = 0; i < key; i++) {
                recvKey[i] = dis.readByte();
            }
            session.setRecvKey(recvKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}