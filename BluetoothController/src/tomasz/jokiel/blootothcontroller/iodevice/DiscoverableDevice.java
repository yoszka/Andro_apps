package tomasz.jokiel.blootothcontroller.iodevice;

public abstract class DiscoverableDevice  extends IoDevice{
    public abstract void startDiscovery();
    public abstract void stopDiscovery();
    public abstract void connect(EndpointDevice endpointDevice);
}
