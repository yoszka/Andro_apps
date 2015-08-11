package tomasz.jokiel.blootothcontroller.iodevice.bt;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import tomasz.jokiel.blootothcontroller.iodevice.EndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.IoDeviceListener;
import tomasz.jokiel.blootothcontroller.iodevice.MultiEndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.OnDeviceConnectStateListener;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BtSmartIoDevice extends MultiEndpointDevice {

    private static final long SCAN_PERIOD = 10000;
    private IoDeviceListener mIoDeviceListener;
    private BluetoothAdapter mBluetoothAdapter;
    private AtomicBoolean mIsConnected = new AtomicBoolean(false);
    private AtomicBoolean mScanning = new AtomicBoolean(false);
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler = new Handler();
    private EndpointDevice mEndpointDevice;
    private SoftReference<Context> mContext;
    private BluetoothGattCharacteristic HM10characteristicTX;
    private BluetoothGattCharacteristic HM10characteristicRX;
    private List<String> mDevicesFound = new ArrayList<String>();

    private LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            final String deviceAddress = device.getAddress();

            if (!mDevicesFound.contains(deviceAddress)) {
                Log.i("BtSmartIoDevice", "onLeScan, new device: " + device + ", rssi: " + rssi + ", scanRecord: " + new String(scanRecord));
                mDevicesFound.add(deviceAddress);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EndpointDevice endpointDevice = new EndpointDevice(device.getName(), deviceAddress);
                        endpointDevice.setIsConnectEventAcynchronous(true);
                        mIoDeviceListener.onDeviceFound(endpointDevice);
                    }
                });
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("onConnectionStateChange", "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i("onConnectionStateChange",
                        "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("onConnectionStateChange", "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.w("onServicesDiscovered", "onServicesDiscovered received: " + status);

            configureGattServicesAndCharacteristics(gatt.getServices());
            OnDeviceConnectStateListener deviceConnectStateListener = getDeviceConnectStateListener();
            if (deviceConnectStateListener != null) {
                deviceConnectStateListener.onDeviceConnected(mEndpointDevice);
            }
            mIsConnected.set(true);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mIoDeviceListener.dataArrived(characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mIoDeviceListener.dataArrived(characteristic.getValue());
        }
    };

    @Override
    public void init(Context context, IoDeviceListener listener) {
        if (!isBluetoothSmartSupported(context)) {
            throw new RuntimeException("Bluetooth Smart not supported");
        }

        mContext = new SoftReference<Context>(context);
        mIoDeviceListener = listener;
        initBluetootAdapter(context);

        if (!isEnabled()) {
            Intent enableBtIntent = BtUtil.getRequestEnableBluetoothIntent();
            mIoDeviceListener.requestEnable(enableBtIntent);
        }
    }

    @Override
    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    @Override
    public EndpointDevice[] getBondedDevices() {
        final Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice[] bluetoothDevices = devices.toArray(new BluetoothDevice[devices.size()]);
        EndpointDevice[] endpointDevices = new EndpointDevice[devices.size()];

        for (int i = 0; i < endpointDevices.length; i++) {
            endpointDevices[i] = new EndpointDevice(bluetoothDevices[i].getName(), bluetoothDevices[i].getAddress());
        }

        return endpointDevices;
    }

    @Override
    public void startDiscovery() {
        scanBluetoothSmartDevice(true);
    }

    @Override
    public void stopDiscovery() {
        scanBluetoothSmartDevice(false);
    }

    @Override
    public boolean connect(EndpointDevice endpointDevice) {
        if (mBluetoothAdapter == null || endpointDevice == null) {
            Log.w("connect", "BluetoothAdapter not initialized or unspecified endpointDevice.");
            return false;
        }

        if (!reconnect(endpointDevice)) {
            return makeNewDeviceConnect(endpointDevice);
        }

        return true;
    }

    @Override
    public boolean close() {
        mIsConnected.set(false);
        if (mBluetoothGatt == null) {
            return true;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        return true;
    }

    @Override
    public boolean writeBytes(byte[] bytes) {

        if (mIsConnected.get()) {
            HM10characteristicTX.setValue(bytes);
            return writeCharacteristic(HM10characteristicTX);
        }

        return false;
    }

    private void initBluetootAdapter(Context context) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            throw new RuntimeException("Bluetooth Smart service unavailable");
        }
    }

    private boolean isBluetoothSmartSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private void scanBluetoothSmartDevice(final boolean enable) {
        Log.d("BtSmartIoDevice", "scanBluetoothSmartDevice");
        if (enable) {
            mDevicesFound.clear();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning.set(false);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning.set(true);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning.set(false);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private boolean reconnect(EndpointDevice endpointDevice) {
        if (mEndpointDevice != null && endpointDevice.getAddress().equals(mEndpointDevice.getAddress())
                && mBluetoothGatt != null) {
            Log.d("reconnect", "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(endpointDevice.getAddress());
                mBluetoothGatt = device.connectGatt(mContext.get(), false, mGattCallback);
                mEndpointDevice = endpointDevice;
                return true;
            }
        }

        return false;
    }

    private boolean makeNewDeviceConnect(EndpointDevice endpointDevice) {
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(endpointDevice.getAddress());
        if (device == null) {
            Log.w("connect", "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(mContext.get(), false, mGattCallback);
        Log.d("connect", "Trying to create a new connection.");
        mEndpointDevice = endpointDevice;
        return true;
    }

    private void configureGattServicesAndCharacteristics(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

        boolean isHM10CharacteristicsInitialized = false;

        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            android.util.Log.d("##_displayGattServices", "serviceUUID= " + uuid);

            printCharacteristicsForService(gattService);

            if (!isHM10CharacteristicsInitialized) {
                if (uuid.equals(GattAttributes.SERVICE_HM_10)) {
                    initHM10characteristicTxRx(gattService);
                    isHM10CharacteristicsInitialized = true;
                }
            }
        }
    }

    private void initHM10characteristicTxRx(BluetoothGattService gattService) {
        UUID HM10CharacteristicRxTx = UUID.fromString(GattAttributes.CHARACTERISTIC_HM_10_RX_TX);
        HM10characteristicTX = gattService.getCharacteristic(HM10CharacteristicRxTx);
        HM10characteristicRX = gattService.getCharacteristic(HM10CharacteristicRxTx);
        setCharacteristicNotification(HM10characteristicRX, true);
    }

    private void printCharacteristicsForService(BluetoothGattService gattService) {
        List<BluetoothGattCharacteristic> serviceCharacteristics = gattService.getCharacteristics();

        for (BluetoothGattCharacteristic bluetoothGattCharacteristic : serviceCharacteristics) {
            String characteristicUUID = bluetoothGattCharacteristic.getUuid().toString();
            android.util.Log.d("##_displayGattServices", " - characteristicUUID= " + characteristicUUID);
            printDescriptorsForCharacteristic(bluetoothGattCharacteristic);
        }
    }

    private void printDescriptorsForCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        List<BluetoothGattDescriptor> characteristicDescriptors = bluetoothGattCharacteristic.getDescriptors();
        for (BluetoothGattDescriptor bluetoothGattDescriptor : characteristicDescriptors) {
            String descriptorUUID = bluetoothGattDescriptor.getUuid().toString();
            android.util.Log.d("##_displayGattServices", "    - descriptorUUID= " + descriptorUUID);
        }
    }

    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("setCharacteristicNotification", "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("writeCharacteristic", "BluetoothAdapter not initialized");
            return false;
        }

        return mBluetoothGatt.writeCharacteristic(characteristic);
    }
}
