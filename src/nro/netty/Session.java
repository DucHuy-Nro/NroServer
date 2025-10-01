package nro.netty;

import io.netty.channel.Channel;
import nro.models.network.Message;
import nro.models.player.Player;

public class Session {

    private static int NEXT_ID = 1;

    public int id;
    private Channel channel;
    public Player player;

    public boolean isLogin;
    private byte curR, curW;
    private byte[] sendKey, recvKey;
    private final byte[] key = "j>i&b<@V".getBytes();

    public Session(Channel channel) {
        this.id = NEXT_ID++;
        this.channel = channel;
    }

    public void setCurR(byte curR) {
        this.curR = curR;
    }

    public void setCurW(byte curW) {
        this.curW = curW;
    }

    public void setSendKey(byte[] sendKey) {
        this.sendKey = sendKey;
    }

    public void setRecvKey(byte[] recvKey) {
        this.recvKey = recvKey;
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isActive();
    }


    public void sendMessage(Message ms) {
        if (isConnected()) {
            channel.writeAndFlush(ms);
        }
    }

    public void close() {
        if (isConnected()) {
            channel.close();
        }
    }

    public String getIpAddress() {
        return channel.remoteAddress().toString().split(":")[0].replace("/", "");
    }
}