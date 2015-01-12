package tomasz.jokiel.blootothcontroller.iodevice;

import android.content.Context;

public abstract class IoDevice implements IoDeviceable{

    @Override
    public void init(Context context, IoDeviceListener listener) {
    }

    @Override
    public abstract boolean close();

    @Override
    public abstract boolean writeBytes(byte[] bytes);

    @Override
    public void readBytes(byte[] b) {
    }

}
