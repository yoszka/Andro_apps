package tomasz.jokiel.blootothcontroller.iodevice;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class BtIoDevice extends MultiEndpointDevice{
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket  mBluetoothSocket;
    private IoDeviceListener mListener;
    private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OutputStream mBluetoothOutputStream;

    @Override
    public void init(Context context, IoDeviceListener listener) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mListener = listener;

        if (!isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mListener.requestEnable(enableBtIntent);
        }
    }

    private class DiscoveryBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("BT#" + getClass().getSimpleName(), "onReceive, action: " + action);
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                EndpointDevice endpointDevice = new EndpointDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                Log.v("BT#" + getClass().getSimpleName(), "DEVICE: " + endpointDevice);
                mListener.onDeviceFound(endpointDevice);
            }
        }
    };

    private DiscoveryBroadcastReceiver mDiscoveryBroadcastReceiver;

    // Register the BroadcastReceiver
    private IntentFilter mDiscoveryIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    @Override
    public boolean close() {
        boolean result = false;

        if (mBluetoothOutputStream != null) {
            try {
                mBluetoothOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mBluetoothOutputStream.close();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                mBluetoothOutputStream = null;
            }
        }

        return result;
    }

    @Override
    public boolean writeBytes(byte[] buffer) {

        if(mBluetoothOutputStream != null) {
            try {
                mBluetoothOutputStream.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(".\n\nCheck that the UUID: " + SPP_UUID.toString() + " exists on server.", e);
            }
        } else {
            Log.w("writeBytes", "mBluetoothOutputStream == null");
            return false;
        }
        return true;
    }

    @Override
    public void readBytes(byte[] b) {
    }

    @Override
    public boolean isEnabled() {
        return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled());
    }
    @Override
    public EndpointDevice[] getBondedDevices() {
        final Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice[] bluetoothDevices = devices.toArray(new BluetoothDevice[devices.size()]);
        EndpointDevice[] endpointDevices = new EndpointDevice[devices.size()];

        for(int i = 0; i < endpointDevices.length; i++) {
            endpointDevices[i] = new EndpointDevice(bluetoothDevices[i].getName(), bluetoothDevices[i].getAddress());
        }

        return endpointDevices;
    }

    @Override
    public void startDiscovery() {
        if(mDiscoveryBroadcastReceiver == null) {
            mBluetoothAdapter.startDiscovery();
            mDiscoveryBroadcastReceiver = new DiscoveryBroadcastReceiver();
            mListener.registerForDiscovery(mDiscoveryBroadcastReceiver, mDiscoveryIntentFilter);
        }
    }

    @Override
    public void stopDiscovery() {
        if(mDiscoveryBroadcastReceiver != null) {
            mBluetoothAdapter.cancelDiscovery();
            mListener.unregisterFromDiscovery(mDiscoveryBroadcastReceiver);
            mDiscoveryBroadcastReceiver = null;
        }
    }

    @Override
    public void connect(EndpointDevice endpointDevice) {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(endpointDevice.getAddress());
        try {
            mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            mBluetoothSocket.connect();
            mBluetoothOutputStream = mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("connect", e.getMessage());
            e.printStackTrace();
            try {
                mBluetoothSocket.close();
            } catch (IOException e1) {
                Log.e("connect", e1.getMessage());
                e1.printStackTrace();
            }
        }
    }

}
