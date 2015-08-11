package tomasz.jokiel.blootothcontroller.iodevice.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import tomasz.jokiel.blootothcontroller.iodevice.ByteParser;
import tomasz.jokiel.blootothcontroller.iodevice.CommandByteParser;
import tomasz.jokiel.blootothcontroller.iodevice.EndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.IoDeviceListener;
import tomasz.jokiel.blootothcontroller.iodevice.MultiEndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.CommandByteParser.CommandReceivedListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BtIoDevice extends MultiEndpointDevice{
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket  mBluetoothSocket;
    private IoDeviceListener mIoDeviceListener;
    private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OutputStream mBluetoothOutputStream;
    private InputStream mBluetoothInputStream;
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    @Override
    public void init(Context context, IoDeviceListener listener) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mIoDeviceListener = listener;

        if (!isEnabled()) {
            Intent enableBtIntent = BtUtil.getRequestEnableBluetoothIntent();
            mIoDeviceListener.requestEnable(enableBtIntent);
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
                mIoDeviceListener.onDeviceFound(endpointDevice);
            }
        }
    };

    private DiscoveryBroadcastReceiver mDiscoveryBroadcastReceiver;

    // Register the BroadcastReceiver
    private IntentFilter mDiscoveryIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    @Override
    public boolean close() {
        isConnected.set(false);
        boolean outputStreamCloseResult = closeBluetoothOutputStream();
        boolean inputStreamCloseResult = closeBluetoothInputStream();

        return outputStreamCloseResult && inputStreamCloseResult;
    }

    private boolean closeBluetoothOutputStream() {
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

    private boolean closeBluetoothInputStream() {
        boolean result = false;

        if (mBluetoothInputStream != null) {
            try {
                mBluetoothInputStream.close();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                mBluetoothInputStream = null;
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
    public void readBytes(byte[] buffer) {}

    private void registerInputStreamListener(final InputStream inputStream) {
        if(inputStream != null) {
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    ByteParser byteParser = new CommandByteParser(new CommandReceivedListener() {
                        
                        @Override
                        public void onCommandReceived(String receivedCommand) {
                            mIoDeviceListener.dataArrived(receivedCommand.getBytes());
                        }
                    });
                    
                    try {
                        while (isConnected.get()) {
                            int data = inputStream.read();
                            
                            if (data != -1) {
                                byteParser.addByteToParse((byte)data);
                            }
                        }
                    } catch (IOException e) {
                        if(isConnected.get()) {
                            throw new RuntimeException(".\n\nCheck that the UUID: " + SPP_UUID.toString() + " exists on server.", e);
                        }
                    }
                }
            }).start();
        } else {
            Log.w("registerInputStreamListener", "inputStream == null");
        }
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
            mIoDeviceListener.registerForDiscovery(mDiscoveryBroadcastReceiver, mDiscoveryIntentFilter);
        }
    }

    @Override
    public void stopDiscovery() {
        if(mDiscoveryBroadcastReceiver != null) {
            mBluetoothAdapter.cancelDiscovery();
            mIoDeviceListener.unregisterFromDiscovery(mDiscoveryBroadcastReceiver);
            mDiscoveryBroadcastReceiver = null;
        }
    }

    @Override
    public boolean connect(EndpointDevice endpointDevice) {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(endpointDevice.getAddress());
        try {
            mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            mBluetoothSocket.connect();
            mBluetoothOutputStream = mBluetoothSocket.getOutputStream();
            mBluetoothInputStream = mBluetoothSocket.getInputStream();
            isConnected.set(true);
            registerInputStreamListener(mBluetoothInputStream);
            return true;
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

        return false;
    }

}
