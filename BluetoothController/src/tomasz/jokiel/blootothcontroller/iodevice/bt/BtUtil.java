package tomasz.jokiel.blootothcontroller.iodevice.bt;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class BtUtil {
    public static Intent getRequestEnableBluetoothIntent() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }
}
