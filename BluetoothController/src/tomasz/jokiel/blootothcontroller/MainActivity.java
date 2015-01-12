package tomasz.jokiel.blootothcontroller;

import java.util.LinkedHashSet;

import tomasz.jokiel.blootothcontroller.iodevice.ApplicationStateListener;
import tomasz.jokiel.blootothcontroller.iodevice.BtIoDevice;
import tomasz.jokiel.blootothcontroller.iodevice.DeviceDiscoveryCallback;
import tomasz.jokiel.blootothcontroller.iodevice.DiscoverableDevice;
import tomasz.jokiel.blootothcontroller.iodevice.EndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.IoDevice;
import tomasz.jokiel.blootothcontroller.iodevice.IoDeviceListener;
import tomasz.jokiel.blootothcontroller.iodevice.MessageDisplayer;
import tomasz.jokiel.blootothcontroller.iodevice.MultiEndpointDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements IoDeviceListener, DeviceDiscoveryCallback {
    private static final String DEVICES_LIST = "DEVICES_LIST";

    private final int REQUEST_ENABLE_IO_DEVICE = 1;

    private IoDevice mIoDevice;
    private MessageDisplayer mMessageDisplayer;
    private ApplicationStateListener mApplicationStateListener;
    private LinkedHashSet<EndpointDevice> mEndpointDevices = new LinkedHashSet<EndpointDevice>();

    private boolean mIsEnableRequestInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            mIsEnableRequestInProgress = true;
        }

        initIoDevice();

        PlaceholderFragment placeholderFragment = new PlaceholderFragment();
        placeholderFragment.setDiscoverableDevice((DiscoverableDevice) mIoDevice);
        placeholderFragment.setDeviceDiscoveryCallback(this);
        mMessageDisplayer = placeholderFragment;
        mApplicationStateListener = placeholderFragment;
        

        getSupportFragmentManager().beginTransaction().replace(R.id.container, placeholderFragment).commit();
        
        if(mIoDevice.isEnabled()) {
            displayBoundedDevices();
        }
        if(savedInstanceState != null) {
            Parcelable[] devices = savedInstanceState.getParcelableArray(DEVICES_LIST);

            for(Parcelable device : devices) {
                mEndpointDevices.add((EndpointDevice) device);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArray(DEVICES_LIST, mEndpointDevices.toArray(new EndpointDevice[mEndpointDevices.size()]));
        super.onSaveInstanceState(outState);
    }

    private void initIoDevice() {
        mIoDevice = new BtIoDevice();
        mIoDevice.init(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void dataArrived(byte[] data) {
    }

    @Override
    public void requestEnable(Intent enableIntent) {
        if(!mIsEnableRequestInProgress) {
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
    public void onDeviceFound(EndpointDevice device) {
        Toast.makeText(this, device.getName(), Toast.LENGTH_LONG).show();
        mEndpointDevices.add(device);
        mApplicationStateListener.onDeviceListChanged(mEndpointDevices);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_IO_DEVICE) {
            mIsEnableRequestInProgress = false;
            if(resultCode == RESULT_CANCELED) {
                mMessageDisplayer.displayMessage("BT device OFF");
            } else if(resultCode == RESULT_OK) {
                mMessageDisplayer.displayMessage("BT device ON");
                displayBoundedDevices();
            }
        }
    }

    private void displayBoundedDevices() {
        try {
            EndpointDevice[] pairedDevices = getPairedDevices();

            if (pairedDevices.length > 0) {
                // Loop through paired devices
                for (EndpointDevice device : pairedDevices) {
                    mEndpointDevices.add(device);
//                    mMessageDisplayer.appendLineDisplayMessage(device.getName() + " [" + device.getAddress() + "]");
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
        if(mIoDevice.isEnabled()) {
            displayBoundedDevices();
        }
    }

}
