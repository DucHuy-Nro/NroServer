package nro.models.network;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import nro.models.interfaces.IMessageSendCollect;
import nro.models.interfaces.ISession;

public final class Sender implements Runnable {

    private ISession session;
    private BlockingDeque<Message> messages;
    private DataOutputStream dos;
    private IMessageSendCollect sendCollect;

    public Sender(@NonNull ISession session, @NonNull Socket socket) {
        try {
            if (session == null || socket == null) {
                throw new IllegalArgumentException("Session and Socket must not be null");
            }
            this.session = session;
            this.messages = new LinkedBlockingDeque<>();
            this.setSocket(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Sender setSocket(@NonNull Socket socket) {
        try {
            if (socket == null) {
                throw new IllegalArgumentException("Socket must not be null");
            }
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public void run() {
        try {
            if (session == null || !session.isConnected()) {
                System.out.println("Session is not connected or null. Exiting Sender run loop.");
                return;
            }

            while (session.isConnected()) {
                while (!messages.isEmpty()) {
                    Message message = messages.poll(8, TimeUnit.SECONDS);
                    if (message != null) {
                        doSendMessage(message);
                        message.cleanup();
                    }
                }
                TimeUnit.MILLISECONDS.sleep(120);
            }
        } catch (Exception e) {
            //   e.printStackTrace();
        }
    }

    public synchronized void doSendMessage(Message message) {
        try {
            if (session != null && session.isConnected()) {
                this.sendCollect.doSendMessage(this.session, this.dos, message);
            } else {

            }
        } catch (IOException e) {
            if (session != null) {
                session.disconnect();
            }
        } catch (Exception e) {
        }
    }

    public void sendMessage(Message msg) {
        try {
            if (session != null && session.isConnected()) {
                messages.add(msg);
            } else {
                System.out.println("Session is not connected or null. Cannot add message.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSend(IMessageSendCollect sendCollect) {
        this.sendCollect = sendCollect;
    }

    public void close() {
        this.messages.clear();
        if (this.dos != null) {
            try {
                this.dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispose() {
        this.session = null;
        this.messages = null;
        this.sendCollect = null;
        this.dos = null;
    }
}
