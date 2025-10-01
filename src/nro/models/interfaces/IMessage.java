package nro.models.interfaces;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface IMessage {

    DataOutputStream writer();

    DataInputStream reader();

    byte[] getData();

    void cleanup();

    void dispose();
}
