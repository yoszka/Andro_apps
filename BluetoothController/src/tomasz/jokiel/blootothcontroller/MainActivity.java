package tomasz.jokiel.blootothcontroller;

import java.util.LinkedHashSet;

import tomasz.jokiel.blootothcontroller.iodevice.ApplicationStateListener;
import tomasz.jokiel.blootothcontroller.iodevice.DeviceDiscoveryCallback;
import tomasz.jokiel.blootothcontroller.iodevice.DiscoverableDevice;
import tomasz.jokiel.blootothcontroller.iodevice.EndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.IoDevice;
import tomasz.jokiel.blootothcontroller.iodevice.IoDeviceListener;
import tomasz.jokiel.blootothcontroller.iodevice.MessageDisplayer;
import tomasz.jokiel.blootothcontroller.iodevice.MultiEndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.bt.BtIoDevice;
import tomasz.jokiel.blootothcontroller.iodevice.bt.BtSmartIoDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements IoDeviceListener, DeviceDiscoveryCallback, Callback {
    private static final String DEVICES_LIST = "DEVICES_LIST";
    private static final int IO_DEVICE_BLUETOOTH = 0;
    private static final int IO_DEVICE_BLUETOOTH_SMART = 1;
    private static final int DEFAULT_IO_DEVICE_TYPE = IO_DEVICE_BLUETOOTH;
    private final int mIoDeviceType = DEFAULT_IO_DEVICE_TYPE;

    private final int REQUEST_ENABLE_IO_DEVICE = 1;

    private final int MESSAGE_BATTERY_LEVEL = 1;

    private IoDevice mIoDevice;
    private MessageDisplayer mMessageDisplayer;
    private ApplicationStateListener mApplicationStateListener;
    private LinkedHashSet<EndpointDevice> mEndpointDevices = new LinkedHashSet<EndpointDevice>();
    private Handler mHandler = new Handler(this);

    private boolean mIsEnableRequestInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mIsEnableRequestInProgress = true;
        }

        mIoDevice = getIoDevice();

        PlaceholderFragment placeholderFragment = new PlaceholderFragment();
        placeholderFragment.setDiscoverableDevice((DiscoverableDevice) mIoDevice);
        placeholderFragment.setDeviceDiscoveryCallback(this);
        mMessageDisplayer = placeholderFragment;
        mApplicationStateListener = placeholderFragment;

        getSupportFragmentManager().beginTransaction().replace(R.id.container, placeholderFragment).commit();

        if (mIoDevice.isEnabled()) {
            displayBoundedDevices();
        }

        if (savedInstanceState != null) {
            Parcelable[] devices = savedInstanceState.getParcelableArray(DEVICES_LIST);

            for (Parcelable device : devices) {
                mEndpointDevices.add((EndpointDevice) device);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArray(DEVICES_LIST, mEndpointDevices.toArray(new EndpointDevice[mEndpointDevices.size()]));
        super.onSaveInstanceState(outState);
    }

    private IoDevice getIoDevice() {
        IoDevice ioDevice = null;

        switch (mIoDeviceType) {
        case IO_DEVICE_BLUETOOTH:
            ioDevice = new BtIoDevice();
            break;
        case IO_DEVICE_BLUETOOTH_SMART:
            ioDevice = new BtSmartIoDevice();
            break;
        default:
            throw new RuntimeException("No such device");
        }

        ioDevice.init(this, this);
        return ioDevice;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void dataArrived(byte[] data) {
        String dataString = new String(data);
        Log.v("##_BTC", "dataArrived: " + dataString);

        if (dataString.contains("&ADC&")) {
            Message msg = mHandler.obtainMessage(MESSAGE_BATTERY_LEVEL);
            msg.obj = dataString;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void requestEnable(Intent enableIntent) {
        if (!mIsEnableRequestInProgress) {
            startActivityForResult(enableIntent, REQUEST_ENABLE_IO_DEVICE);
            mIsEnableRequestInProgress = true;
        }
    }

    @Override
    public void registerForDiscovery(BroadcastReceiver receiver, IntentFilter intentFilter) {
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void unregisterFromDiscovery(BroadcastReceiver receiver) {
        unregisterReceiver(receiver);
    }

    @Override
    public void onDeviceFound(final EndpointDevice device) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, device.getName(), Toast.LENGTH_LONG).show();
                mEndpointDevices.add(device);
                mApplicationStateListener.onDeviceListChanged(mEndpointDevices);
                Log.d("MainActivity", "onDeviceFound");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_IO_DEVICE) {
            mIsEnableRequestInProgress = false;
            if (resultCode == RESULT_OK) {
                displayBoundedDevices();
            }
        }
    }

    private void displayBoundedDevices() {
        try {
            EndpointDevice[] pairedDevices = getPairedDevices();

            if (pairedDevices.length > 0) {
                for (EndpointDevice device : pairedDevices) {
                    mEndpointDevices.add(device);
                }
                mApplicationStateListener.onDeviceListChanged(mEndpointDevices);
            }
        } catch (ClassCastException classCastException) {
            classCastException.printStackTrace();
        }
    }

    private EndpointDevice[] getPairedDevices() {
        MultiEndpointDevice multiEndpointDevice = (MultiEndpointDevice) mIoDevice;
        EndpointDevice[] pairedDevices = multiEndpointDevice.getBondedDevices();
        return pairedDevices;
    }

    @Override
    public void onStartDiscovery() {
        mEndpointDevices.clear();
        if (mIoDevice.isEnabled()) {
            displayBoundedDevices();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_BATTERY_LEVEL:
            String dataString = (String) msg.obj;
            dataString = dataString.replaceAll("\\*\\:ACK&ADC&", ""); // *:ACK&ADC&165:#
            String value = dataString.replaceAll("\\:\\#", ""); // *:ACK&ADC&165:#
            Log.v("##_BTC", "MESSAGE_BATTERY_LEVEL: " + value);
            mMessageDisplayer.batteryLevel(Integer.valueOf(value));
            return true;
        }
        return false;
    }

}
