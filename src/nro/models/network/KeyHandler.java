package nro.models.network;

import java.io.IOException;
import nro.models.interfaces.ISession;
import nro.models.interfaces.IKeySessionHandler;
import nro.models.consts.Cmd_message;

public class KeyHandler implements IKeySessionHandler {

    @Override
    public void sendKey(ISession session) {
        final Message msg = new Message(Cmd_message.GET_SESSION_ID);
        try {
            final byte[] KEYS = session.getKey();
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            session.doSendMessage(msg);
            msg.cleanup();
            session.setSentKey(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setKey(ISession session, Message message) throws Exception {
        try {
            int b = message.reader().readByte();
            byte[] KEYS = new byte[b];
            for (int i = 0; i < b; ++i) {
                KEYS[i] = message.reader().readByte();
            }
            for (int j = 0; j < KEYS.length - 1; j++) {
                KEYS[j + 1] = (byte) (KEYS[j + 1] ^ KEYS[j]);
            }
            session.setKey(KEYS);
            session.setSentKey(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
