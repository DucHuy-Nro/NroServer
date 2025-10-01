package nro.models.network;

import nro.models.interfaces.IServerClose;
import java.net.Socket;
import java.io.IOException;
import java.net.InetSocketAddress;
import nro.models.interfaces.ISession;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import nro.models.interfaces.ISessionAcceptHandler;
import nro.models.interfaces.INetwork;
import nro.models.utils.Logger;

public class Network implements INetwork, Runnable {

    private static Network instance;
    private int port;
    private ServerSocketChannel serverSocketChannel;
    private Class<? extends ISession> sessionClone;
    private volatile boolean start;
    private IServerClose serverClose;
    private ISessionAcceptHandler acceptHandler;
    private Thread loopServer;
    private Selector selector;

    // Dùng ThreadPoolExecutor thay cho cachedThreadPool để giới hạn tài nguyên
    private final ExecutorService threadPool = new ThreadPoolExecutor(
        10, // core pool size
        100, // max pool size
        60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000), // tránh quá tải
        Executors.defaultThreadFactory(),
        new ThreadPoolExecutor.AbortPolicy()
    );

    public static synchronized Network gI() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    private Network() {
        this.port = -1;
        this.sessionClone = Session.class;
    }

    @Override
    public INetwork init() {
        try {
            this.selector = Selector.open();
        } catch (IOException ex) {
            Logger.errorln("Failed to open selector: " + ex.getMessage());
        }
        this.loopServer = new Thread(this, "Network");
        return this;
    }

    @Override
    public INetwork start(final int port) throws Exception {
        if (port < 0) {
            throw new IllegalArgumentException("Please initialize the server port!");
        }
        if (this.acceptHandler == null) {
            throw new IllegalStateException("AcceptHandler has not been initialized!");
        }
        if (!ISession.class.isAssignableFrom(this.sessionClone)) {
            throw new IllegalStateException("The type 'sessionClone' is invalid!");
        }

        this.port = port;

        try {
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
            this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            Logger.error("Error initializing server at port " + port + ": " + ex.getMessage());
            throw new RuntimeException("Failed to bind server socket.", ex);
        }

        this.start = true;
        this.loopServer.start();
        Logger.success(Logger.PURPLE + "Active Port " + this.port + "\n");
        return this;
    }

    @Override
    public INetwork close() {
        this.start = false;

        if (loopServer != null) {
            loopServer.interrupt(); // ✔️ Dừng thread đang select
        }

        try {
            if (selector != null) {
                for (SelectionKey key : selector.keys()) {
                    key.cancel(); // ✔️ Hủy các key để tránh memory leak
                }
                selector.close();
            }
        } catch (IOException e) {
            Logger.errorln("Error closing selector: " + e.getMessage());
        }

        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
        } catch (IOException e) {
            Logger.errorln("Error closing server socket: " + e.getMessage());
        }

        threadPool.shutdownNow();

        if (serverClose != null) {
            serverClose.serverClose();
        }

        return this;
    }

    @Override
    public INetwork dispose() {
        this.acceptHandler = null;
        this.loopServer = null;
        this.serverSocketChannel = null;
        this.selector = null;
        this.sessionClone = null;
        return this;
    }

    @Override
    public INetwork setAcceptHandler(final ISessionAcceptHandler handler) {
        this.acceptHandler = handler;
        return this;
    }

    @Override
    public void run() {
        while (start) {
            try {
                selector.select(100); // ✔️ Timeout 100ms tránh kẹt thread

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove(); // ✔️ Dọn key đúng cách

                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        Socket socket = server.accept().socket();

                        // ✔️ Tối ưu socket config giảm lag
                        socket.setTcpNoDelay(true);
                        socket.setKeepAlive(true);

                        threadPool.submit(() -> {
                            try {
                                ISession session = SessionFactory.gI().cloneSession(this.sessionClone, socket);
                                acceptHandler.sessionInit(session);
                                SessionManager.gI().putSession(session);
                            } catch (Exception e) {
                                Logger.errorln("Error initializing session: " + e.getMessage());
                                try {
                                    socket.close();
                                } catch (IOException ex) {
                                    Logger.errorln("Error closing socket: " + ex.getMessage());
                                }
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Logger.errorln("Network IO error: " + e.getMessage());
            } catch (Exception ex) {
                Logger.errorln("Unhandled error: " + ex.toString());
            }
        }
    }

    @Override
    public INetwork setDoSomeThingWhenClose(final IServerClose serverClose) {
        this.serverClose = serverClose;
        return this;
    }

    @Override
    public INetwork setTypeSessionClone(final Class clazz) {
        if (!ISession.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Session class must implement ISession!");
        }
        this.sessionClone = clazz;
        return this;
    }

    @Override
    public ISessionAcceptHandler getAcceptHandler() throws Exception {
        if (this.acceptHandler == null) {
            throw new IllegalStateException("AcceptHandler has not been initialized!");
        }
        return this.acceptHandler;
    }

    @Override
    public void stopConnect() {
        this.start = false;
    }
}
