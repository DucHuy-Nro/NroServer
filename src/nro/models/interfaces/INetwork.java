package nro.models.interfaces;

public interface INetwork extends Runnable {

    INetwork init();

    INetwork start(int p0) throws Exception;

    INetwork setAcceptHandler(ISessionAcceptHandler p0);

    INetwork close();

    INetwork dispose();
    
    INetwork setDoSomeThingWhenClose(IServerClose serverClose);

    INetwork setTypeSessionClone(Class p0) throws Exception;

    ISessionAcceptHandler getAcceptHandler() throws Exception;

    void stopConnect();
}
