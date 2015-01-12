package tomasz.jokiel.blootothcontroller.iodevice;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

public interface IoDeviceListener {
    public void dataArrived(byte[] data);
    public void requestEnable(Intent enableIntent);
    public void registerForDiscovery(BroadcastReceiver receiver, IntentFilter intentFilter);
    public void unregisterFromDiscovery(BroadcastReceiver receiver);
    public void onDeviceFound(EndpointDevice device);
}
