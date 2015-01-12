package tomasz.jokiel.blootothcontroller.iodevice;

public abstract class MultiEndpointDevice extends DiscoverableDevice{
    public abstract EndpointDevice[] getBondedDevices();
}
