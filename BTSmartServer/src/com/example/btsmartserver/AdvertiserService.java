package com.example.btsmartserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class AdvertiserService extends Service {
    private static boolean sIsRunning = false;

    private BluetoothManager mManager;
    private BluetoothAdapter mAdapter;
    private BluetoothGattServer mGattServer;
    private BluetoothLeAdvertiser mLeAdvertiser;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    static final int MESSAGE_REGISTER_CLIENT = 1;
    static final int MESSAGE_UNREGISTER_CLIENT = 2;
    static final int MESSAGE_SET_INT_VALUE = 3;
    static final int MSGESSAGE_SET_STRING_VALUE = 4;

    ArrayList<Messenger> mConnectedClients = new ArrayList<Messenger>();

    final Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MESSAGE_REGISTER_CLIENT:
                mConnectedClients.add(msg.replyTo);
                break;
            case MESSAGE_UNREGISTER_CLIENT:
                mConnectedClients.remove(msg.replyTo);
                break;
            case MESSAGE_SET_INT_VALUE:
                setIntValueForCharacteristic(msg.arg1);
                break;
            default:
                super.handleMessage(msg);
            }
        };
    });

    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d("GATT server", "onConnectionStateChange, new state " + newState);
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d("GATT server", "onServiceAdded: " + service);
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                BluetoothGattCharacteristic characteristic) {
            Log.d("GATT server", "onCharacteristicReadRequest");
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
                byte[] value) {
            Log.d("GATT server", "onCharacteristicWriteRequest " + value);
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded,
                    offset, value);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            Log.d("GATT server", "onNotificationSent");
            super.onNotificationSent(device, status);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                BluetoothGattDescriptor descriptor) {
            Log.d("GATT server", "onDescriptorReadRequest: " + descriptor);
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);

        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor,
                boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d("GATT server", "onDescriptorWriteRequest: " + value);
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.d("GATT server", "Our gatt server on execute write.");
            super.onExecuteWrite(device, requestId, execute);
        }

    };

    private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {

        @Override
        public void onStartFailure(int errorCode) {
            Log.d("AdvertiseCallback", "onStartFailure");
            Toast.makeText(AdvertiserService.this, "advertise onStartFailure", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d("AdvertiseCallback", "onStartSuccess");
            Toast.makeText(AdvertiserService.this, "advertise onStartSuccess", Toast.LENGTH_LONG).show();
        };
    };

    @Override
    public void onCreate() {
        mManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (mManager == null) {
            Toast.makeText(this, "mManager is null", Toast.LENGTH_LONG).show();
            return;
        }

        mAdapter = mManager.getAdapter();

        if (!mAdapter.isMultipleAdvertisementSupported()) {
            Toast.makeText(this, "Advertisement not supported", Toast.LENGTH_LONG).show();
            return;
        } else {
            openGattServer();
            addDeviceInfoService();
            startAdvertise();
        }

        sIsRunning = true;

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAdvertise();
        mGattServer.close();
        sIsRunning = false;
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

    private void startAdvertise() {
        if (null == mAdapter)
            return;
    
        if (null == mLeAdvertiser) {
            mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
        }
    
        if (null == mLeAdvertiser)
            return;
    
        mLeAdvertiser.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData(), mAdvCallback);
    }

    private void setIntValueForCharacteristic(int arg1) {

        List<BluetoothDevice> connectedDevices = mManager.getConnectedDevices(BluetoothProfile.GATT);

        if (null != connectedDevices) {
            mNotifyCharacteristic.setValue(String.valueOf(arg1).getBytes());

            if (0 != connectedDevices.size())
                mGattServer.notifyCharacteristicChanged(connectedDevices.get(0), mNotifyCharacteristic, false);
        }
    }

    private void openGattServer() {
        mGattServer = mManager.openGattServer(this, mGattServerCallback);
    }

    private void stopAdvertise() {
        if (null != mLeAdvertiser) {
            mLeAdvertiser.stopAdvertising(mAdvCallback);
        }

        mLeAdvertiser = null;
    }

    private void addDeviceInfoService() {
        if (null == mGattServer) {
            return;
        }

        final String SERVICE_DEVICE_INFORMATION = "00004444-0000-1000-8000-00805f9b34fb";
        final String CHARACTERISTIC_SOFTWARE_REVISION = "00005555-0000-1000-8000-00805f9b34fb";

        final String CHARACTERISTIC_NOTIFY = "00006543-0000-1000-8000-00805f9b34fb";

        BluetoothGattService previousService = mGattServer.getService(UUID.fromString(SERVICE_DEVICE_INFORMATION));

        if (null != previousService) {
            mGattServer.removeService(previousService);
        }

        BluetoothGattCharacteristic softwareVersionCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHARACTERISTIC_SOFTWARE_REVISION), BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        mNotifyCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(CHARACTERISTIC_NOTIFY),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattService deviceInfoService = new BluetoothGattService(UUID.fromString(SERVICE_DEVICE_INFORMATION),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        softwareVersionCharacteristic.setValue(new String("0.0.0").getBytes());
        mNotifyCharacteristic.setValue(String.valueOf(0).getBytes());

        deviceInfoService.addCharacteristic(softwareVersionCharacteristic);
        deviceInfoService.addCharacteristic(mNotifyCharacteristic);
        mGattServer.addService(deviceInfoService);
    }

    private void sendIntegerToUI(int intvaluetosend) {
        for (int i = mConnectedClients.size() - 1; i >= 0; i--) {
            try {
                mConnectedClients.get(i).send(Message.obtain(null, MESSAGE_SET_INT_VALUE, intvaluetosend, 0));

                Bundle bundle = new Bundle();
                bundle.putString("str1", String.valueOf(intvaluetosend));
                Message msg = Message.obtain(null, MSGESSAGE_SET_STRING_VALUE);
                msg.setData(bundle);
                mConnectedClients.get(i).send(msg);

            } catch (RemoteException e) {
                e.printStackTrace();
                mConnectedClients.remove(i);
            }
        }
    }

    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setConnectable(true);
        return settingsBuilder.build();
    }

    private AdvertiseData buildAdvertiseData() {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        mAdapter.setName("BTServer");
        dataBuilder.setIncludeDeviceName(true);
        return dataBuilder.build();
    }

}