package nro.models.network;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import nro.models.interfaces.IKeySessionHandler;
import nro.models.interfaces.IMessageHandler;
import nro.models.interfaces.IMessageSendCollect;
import nro.models.interfaces.ISession;
import nro.models.consts.SocketType;

public class Session implements ISession {

    private static ISession instance;
    private static int ID_INIT;
    private SocketType socketType = SocketType.SERVER;
    private byte[] KEYS;
    private boolean sentKey;
    public int id;
    private Socket socket;
    private boolean connected;
    private Sender sender;
    private Collector collector;
    private QueueHandler queueHandler;
    private Thread tSender;
    private Thread tCollector;
    private Thread tQueueHandler;
    private IKeySessionHandler keyHandler;
    private String ip;

    public static ISession gI() throws Exception {
        if (instance == null) {
            throw new Exception("Instance has not been initialized!");
        }
        return instance;
    }

    public Session(String host, int port) throws IOException {
        this.id = 31072002;
        this.socket = new Socket(host, port);
        this.socket.setSendBufferSize(0x100000);
        this.socket.setReceiveBufferSize(0x100000);
        this.socketType = SocketType.CLIENT;
        this.connected = true;
        this.ip = this.socket.getLocalAddress().getHostAddress();
        this.KEYS = "NguyenDucVuEntertainment".getBytes();
        initComponents();
    }

    public Session(Socket socket) {
        this.KEYS = "NguyenDucVuEntertainment".getBytes();
        this.id = ID_INIT++;
        this.socket = socket;
        try {
            this.socket.setSendBufferSize(0x100000);
            this.socket.setReceiveBufferSize(0x100000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.socketType = SocketType.SERVER;
        this.connected = true;
        this.ip = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
        initComponents();
    }

    private void initComponents() {
        this.sender = new Sender(this, this.socket);
        this.collector = new Collector(this, this.socket);
        this.queueHandler = new QueueHandler(this);

        this.tSender = new Thread(this.sender, "Sender - IP: " + this.ip);
        this.tCollector = new Thread(this.collector, "Collector - IP: " + this.ip);
        this.tQueueHandler = new Thread(this.queueHandler, "QueueHandler - IP: " + this.ip);
    }

    @Override
    public void sendMessage(Message msg) {
        if (isConnected() && msg != null && sender != null) {
            sender.sendMessage(msg);
        }
    }

    @Override
    public ISession setSendCollect(IMessageSendCollect collect) {
        if (sender != null) {
            sender.setSend(collect);
        }
        if (collector != null) {
            collector.setCollect(collect);
        }
        return this;
    }

    @Override
    public ISession setMessageHandler(IMessageHandler handler) {
        if (queueHandler != null) {
            queueHandler.setMessageHandler(handler);
        }
        return this;
    }

    @Override
    public ISession setKeyHandler(IKeySessionHandler handler) {
        this.keyHandler = handler;
        return this;
    }

    @Override
    public ISession startSend() {
        if (!tSender.isAlive()) {
            tSender.start();
        }
        return this;
    }

    @Override
    public ISession startCollect() {
        if (!tCollector.isAlive()) {
            tCollector.start();
        }
        return this;
    }

    @Override
    public ISession startQueueHandler() {
        if (!tQueueHandler.isAlive()) {
            tQueueHandler.start();
        }
        return this;
    }

    @Override
    public ISession start() {
        startSend();
        startCollect();
        startQueueHandler();
        return this;
    }

    @Override
    public String getIP() {
        return ip;
    }

    @Override
    public long getID() {
        return id;
    }

    @Override
    public void disconnect() {
        connected = false;
        sentKey = false;

        if (sender != null) {
            sender.close();
        }
        if (collector != null) {
            collector.close();
        }
        if (queueHandler != null) {
            queueHandler.close();
        }

        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dispose();
    }

    @Override
    public void dispose() {
        if (sender != null) {
            sender.dispose();
        }
        if (collector != null) {
            collector.dispose();
        }
        if (queueHandler != null) {
            queueHandler.dispose();
        }

        socket = null;
        sender = null;
        collector = null;
        queueHandler = null;
        ip = null;

        SessionManager.gI().removeSession(this);
    }

    @Override
    public void sendKey() throws Exception {
        if (keyHandler == null) {
            throw new Exception("Key handler has not been initialized!");
        }
        keyHandler.sendKey(this);
    }

    @Override
    public void setKey(Message message) throws Exception {
        if (keyHandler == null) {
            throw new Exception("Key handler has not been initialized!");
        }
        keyHandler.setKey(this, message);
    }

    @Override
    public void setKey(byte[] key) {
        this.KEYS = key;
    }

    @Override
    public boolean sentKey() {
        return sentKey;
    }

    @Override
    public void setSentKey(boolean sent) {
        this.sentKey = sent;
    }

    @Override
    public void doSendMessage(Message msg) throws Exception {
        if (sender != null) {
            sender.doSendMessage(msg);
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public byte[] getKey() {
        return KEYS;
    }

    @Override
    public SocketType getSocketType() {
        return socketType;
    }

    @Override
    public QueueHandler getQueueHandler() {
        return queueHandler;
    }
}
