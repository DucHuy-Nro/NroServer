package nro.models.interfaces;

import nro.models.network.Message;
import nro.models.network.QueueHandler;
import nro.models.consts.SocketType;

public interface ISession {

    ISession setSendCollect(final IMessageSendCollect p0);

    ISession setMessageHandler(final IMessageHandler p0);

    ISession setKeyHandler(final IKeySessionHandler p0);

    ISession startSend();

    ISession startCollect();
    
    ISession startQueueHandler();
    
    ISession start();

    String getIP();

    boolean isConnected();

    long getID();

    void sendMessage(final Message p0);

    void doSendMessage(final Message p0) throws Exception;

    void disconnect();

    void dispose();

    void sendKey() throws Exception;

    void setKey(Message msg) throws Exception;

    void setKey(byte[] keys);

    byte[] getKey();

    boolean sentKey();

    void setSentKey(final boolean p0);

    SocketType getSocketType();
    
    QueueHandler getQueueHandler();
}
