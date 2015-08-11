package tomasz.jokiel.blootothcontroller.iodevice;


public abstract class DiscoverableDevice extends IoDevice {
    private OnDeviceConnectStateListener mDeviceConnectStateListener;

    public abstract void startDiscovery();

    public abstract void stopDiscovery();

    public abstract boolean connect(EndpointDevice endpointDevice);
    
    public void setDeviceConnectStateListener(OnDeviceConnectStateListener onDeviceConnectStateListener) {
        mDeviceConnectStateListener = onDeviceConnectStateListener;
    }

    public OnDeviceConnectStateListener getDeviceConnectStateListener() {
        return mDeviceConnectStateListener;
    }
}
