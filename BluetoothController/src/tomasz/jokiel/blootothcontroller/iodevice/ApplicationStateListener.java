package tomasz.jokiel.blootothcontroller.iodevice;

import java.util.LinkedHashSet;

public interface ApplicationStateListener {
    public void onDeviceListChanged(LinkedHashSet<EndpointDevice> endpointDevices);
}
