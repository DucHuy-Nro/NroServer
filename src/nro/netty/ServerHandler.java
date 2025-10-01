package nro.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import nro.models.network.Message;
import nro.server.Controller;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Session session = new Session(ctx.channel());
        ctx.channel().attr(SessionManager.SESSION_KEY).set(session);
        SessionManager.getInstance().add(session);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Session session = ctx.channel().attr(SessionManager.SESSION_KEY).get();
        if (session != null) {
            SessionManager.getInstance().remove(session);
            session.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Session session = ctx.channel().attr(SessionManager.SESSION_KEY).get();
        if (session != null && msg != null) {
            Controller.getInstance().onMessage(session, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Log the exception, and close the connection to prevent memory leaks.
        // cause.printStackTrace();
        ctx.close();
    }
}