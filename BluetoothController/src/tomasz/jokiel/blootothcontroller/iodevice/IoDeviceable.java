package tomasz.jokiel.blootothcontroller.iodevice;

import android.content.Context;

public interface IoDeviceable {
    public void init(Context context, IoDeviceListener listener);
    public boolean close();
    public boolean isEnabled();
    public boolean writeBytes(byte[] b);
    public void readBytes(byte[] b);
}
