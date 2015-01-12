package tomasz.jokiel.blootothcontroller;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import tomasz.jokiel.blootothcontroller.iodevice.ApplicationStateListener;
import tomasz.jokiel.blootothcontroller.iodevice.DeviceDiscoveryCallback;
import tomasz.jokiel.blootothcontroller.iodevice.DiscoverableDevice;
import tomasz.jokiel.blootothcontroller.iodevice.EndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.MessageDisplayer;
import tomasz.jokiel.tankcontroller.OnPositionChangedByDistanceListener;
import tomasz.jokiel.tankcontroller.TankControllerView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceholderFragment extends Fragment implements MessageDisplayer, OnClickListener, Callback, ApplicationStateListener, OnItemClickListener{
    private static final int DISCOVERY_TIMEOUT_MSG = 1;
    private static final int REPEATER_MSG = 2;
    private static final long DISCOVERY_DEVICE_MAX_PERIOD = 12000;
    private TextView mTextView;
    private TankControllerView mTankControllerView;
    private Button mStartDiscoveryButton;
    private Button mButton1;
    private Button mButton2;
    private Button buttonDisconnect;
    private ProgressBar mStartDiscoveryButtonProgressBar;
    private ListView mDiscoveredDevicesListView;
    private DiscoverableDevice mDiscoverableDevice;
    private LinkedHashSet<EndpointDevice> mEndpointDevices;
    private final Handler mHandler = new Handler(this);

    private ArrayAdapter<String> mDiscoveredDevicesArrayAdapter;
    private DeviceDiscoveryCallback mDeviceDiscoveryCallback;

    String mPositionFormatted;
    
    private OnPositionChangedByDistanceListener
                            mOnPositionChangedByDistanceListenerTank = new OnPositionChangedByDistanceListener(9) {
        @Override
        public void onPositionChanged() {
            String positionFormatted = mTankControllerView.getCurrentPositionFormatted();
            mPositionFormatted = "*:GO"+positionFormatted+":#";
            mTextView.setText(positionFormatted);
            if(mDiscoverableDevice != null) {
              boolean success = mDiscoverableDevice.writeBytes(mPositionFormatted.getBytes());
              if(!success) {
//                  Toast.makeText(getActivity(), "No connected?", Toast.LENGTH_SHORT).show();
              } else {
                  mHandler.removeMessages(REPEATER_MSG);
                  mHandler.sendEmptyMessageDelayed(REPEATER_MSG, 700);
              }
          }
        }
    };

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.text);
        mTankControllerView = (TankControllerView) rootView.findViewById(R.id.tank_controller_view);
        mStartDiscoveryButton = (Button) rootView.findViewById(R.id.startDiscoveryButton);
        mButton1 = (Button) rootView.findViewById(R.id.button1);
        mButton2 = (Button) rootView.findViewById(R.id.button2);
        buttonDisconnect = (Button) rootView.findViewById(R.id.buttonDisconnect);
        mStartDiscoveryButton.setOnClickListener(this);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        buttonDisconnect.setOnClickListener(this);
        mStartDiscoveryButtonProgressBar = (ProgressBar) rootView.findViewById(R.id.startDiscoveryButtonProgressBar);
        mDiscoveredDevicesListView = (ListView) rootView.findViewById(R.id.discoveredDevicesListView);
        mDiscoveredDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());
        mTankControllerView.setOnPositionChangedByDistanceListener(mOnPositionChangedByDistanceListenerTank);

        if(mEndpointDevices!= null) {
            addEntriesToAdapter();
        }

        mDiscoveredDevicesListView.setAdapter(mDiscoveredDevicesArrayAdapter);
        mDiscoveredDevicesListView.setOnItemClickListener(this);
        return rootView;
    }
    
    public void setDiscoverableDevice(DiscoverableDevice discoverableDevice) {
        mDiscoverableDevice = discoverableDevice;
    }

    public void setDeviceDiscoveryCallback(DeviceDiscoveryCallback deviceDiscoveryCallback) {
        mDeviceDiscoveryCallback = deviceDiscoveryCallback;
    }

    @Override
    public void displayMessage(String message) {
//        mTextView.setText(message);
    }

    @Override
    public void appendLineDisplayMessage(String message) {
//        mTextView.setText(mTextView.getText() + "\n" + message);
    }

    @Override
    public void onDeviceListChanged(LinkedHashSet<EndpointDevice> endpointDevices) {
        setDeviceList(endpointDevices);
    }

    @Override
    public void onClick(View v) {
        if(v == mStartDiscoveryButton) {
            startDiscovery();
        } else if(v == mButton1) {
            if(mDiscoverableDevice != null) {
//                boolean success = mDiscoverableDevice.writeBytes("*:P200H002R100:#".getBytes());
                boolean success = mDiscoverableDevice.writeBytes("*:GO+200&+200:#".getBytes());
                if(!success) {
                    Toast.makeText(getActivity(), "No connected?", Toast.LENGTH_SHORT).show();
                }
            }
        } else if(v == mButton2) {
            if(mDiscoverableDevice != null) {
//                boolean success = mDiscoverableDevice.writeBytes("*:P200H019R100:#".getBytes());
                boolean success = mDiscoverableDevice.writeBytes("*:GO-200&-200:#".getBytes());
                if(!success) {
                    Toast.makeText(getActivity(), "No connected?", Toast.LENGTH_SHORT).show();
                }
            }
        } else if(v == buttonDisconnect) {
            if(mDiscoverableDevice != null) {
                boolean success = mDiscoverableDevice.close();
                if(!success) {
                    Toast.makeText(getActivity(), "No connected?", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EndpointDevice[] device = mEndpointDevices.toArray(new EndpointDevice[mEndpointDevices.size()]);
        Toast.makeText(getActivity(), device[position].toString(), Toast.LENGTH_SHORT).show();
        
        initateConnection(device[position]);
        
    }

    private void initateConnection(EndpointDevice endpointDevice) {
        if(mDiscoverableDevice != null) {
            mDiscoverableDevice.connect(endpointDevice);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopDiscovery();
        if(mDiscoverableDevice != null) {
            mDiscoverableDevice.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mStartDiscoveryButton.setEnabled(mDiscoverableDevice != null && mDiscoverableDevice.isEnabled());
    }
    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what){
        case DISCOVERY_TIMEOUT_MSG:
            stopDiscovery();
            return true;

        case REPEATER_MSG:
            if(mDiscoverableDevice != null) {
              boolean success = mDiscoverableDevice.writeBytes(mPositionFormatted.getBytes());
              if(!success) {
//                  Toast.makeText(getActivity(), "No connected?", Toast.LENGTH_SHORT).show();
              } else {
                  if(!mPositionFormatted.equals("*:GO+000&+000:#")){
                      mHandler.sendEmptyMessageDelayed(REPEATER_MSG, 700);
                  }
              }
          }
            return true;

        default:
            return false;
        }
    }

    private void startDiscovery() {
        if(mDiscoverableDevice != null) {
            if(mDeviceDiscoveryCallback != null) {
                mDeviceDiscoveryCallback.onStartDiscovery();
            }

            mDiscoveredDevicesArrayAdapter.clear();
            mStartDiscoveryButtonProgressBar.setVisibility(View.VISIBLE);
            mDiscoverableDevice.startDiscovery();
            mHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT_MSG, DISCOVERY_DEVICE_MAX_PERIOD);
            mStartDiscoveryButton.setEnabled(false);
        }
    }

    private void stopDiscovery() {
        if(mDiscoverableDevice != null) {
            mDiscoverableDevice.stopDiscovery();
        }
        mStartDiscoveryButtonProgressBar.setVisibility(View.GONE);
        mStartDiscoveryButton.setEnabled(true);
    }

    private void setDeviceList(LinkedHashSet<EndpointDevice> endpointDevices){
        mEndpointDevices = endpointDevices;

        if(mDiscoveredDevicesArrayAdapter != null && endpointDevices.size() > 0) {
            mDiscoveredDevicesArrayAdapter.clear();
            addEntriesToAdapter();
        }
    }

    private void addEntriesToAdapter() {
        EndpointDevice[] endpointDevicesArray = mEndpointDevices.toArray(new EndpointDevice[mEndpointDevices.size()]);

        for(short i = 0; i < mEndpointDevices.size(); i++) {
            StringBuilder sb = new StringBuilder();
            mDiscoveredDevicesArrayAdapter.add(sb.append(endpointDevicesArray[i].getName()).append("\n").append(endpointDevicesArray[i].getAddress()).toString());
        }
    }

}