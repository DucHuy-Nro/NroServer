package nro.models.network;

import java.net.Socket;

import lombok.Setter;
import java.io.DataInputStream;
import java.io.IOException;
import nro.models.interfaces.IMessageSendCollect;
import nro.models.interfaces.ISession;
import nro.models.consts.Cmd_message;
import nro.models.consts.SocketType;

public final class Collector implements Runnable {

    private ISession session;
    private DataInputStream dis;
    @Setter
    private IMessageSendCollect collect;

    public Collector(ISession session, Socket socket) {
        this.session = session;
        this.setSocket(socket);
    }

    public Collector setSocket(Socket socket) {
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public void run() {
        try {
            while (session != null && session.isConnected()) {
                final Message msg = this.collect.readMessage(this.session, this.dis);
                if (msg.command == Cmd_message.GET_SESSION_ID) {
                    if (session.getSocketType() == SocketType.SERVER) {
                        this.session.sendKey();
                    } else {
                        this.session.setKey(msg);
                    }
                    msg.cleanup();
                } else {
//                    this.messageHandler.onMessage(this.session, msg);
                    this.session.getQueueHandler().addMessage(msg);
                }
//                msg.cleanup();
            }
        } catch (Exception ignored) {
        }
        try {
            Network.gI().getAcceptHandler().sessionDisconnect(session);
        } catch (Exception ignored) {
        }
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    public void close() {
        if (dis != null) {
            try {
                dis.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void dispose() {
        session = null;
        dis = null;
        collect = null;
    }
}
