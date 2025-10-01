package nro.models.interfaces;

public interface ISessionAcceptHandler {

    void sessionInit(final ISession p0);

    void sessionDisconnect(final ISession p0);
}
