package nro.models.interfaces;

import nro.models.network.Message;

public interface IMessageHandler {

    void onMessage(final ISession p0, final Message p1) throws Exception;
}
