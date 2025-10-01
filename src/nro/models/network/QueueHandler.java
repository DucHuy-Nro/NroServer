package nro.models.network;

import lombok.NonNull;
import lombok.Setter;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import nro.models.interfaces.IMessageHandler;
import nro.models.interfaces.ISession;

public class QueueHandler implements Runnable {

    private ISession session;
    private BlockingDeque<Message> messages;
    @Setter
    private IMessageHandler messageHandler;

    public QueueHandler(@NonNull ISession session) {
        try {
            this.session = session;
            this.messages = new LinkedBlockingDeque<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (session != null && session.isConnected()) {
                if (messages != null) {
                    Message message = messages.poll(5, TimeUnit.SECONDS);
                    if (message != null) {
                        this.messageHandler.onMessage(this.session, message);
                        message.cleanup();
                    }
                } else {
                    System.err.println("WARNING: QueueHandler.messages is null");
                    break;
                }

                TimeUnit.MILLISECONDS.sleep(33); // ~30 FPS
            }
        } catch (Exception e) {
        }
    }

    public void addMessage(Message msg) {
        try {
            if (session.isConnected() && messages.size() < 500) {
                messages.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (messages != null) {
            messages.clear();
        }
    }

    public void dispose() {
        this.session = null;
        this.messages = null;
    }
}
