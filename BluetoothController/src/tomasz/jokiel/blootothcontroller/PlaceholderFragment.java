package tomasz.jokiel.blootothcontroller;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import tomasz.jokiel.blootothcontroller.iodevice.ApplicationStateListener;
import tomasz.jokiel.blootothcontroller.iodevice.DeviceDiscoveryCallback;
import tomasz.jokiel.blootothcontroller.iodevice.DiscoverableDevice;
import tomasz.jokiel.blootothcontroller.iodevice.EndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.MessageDisplayer;
import tomasz.jokiel.blootothcontroller.iodevice.OnDeviceConnectStateListener;
import tomasz.jokiel.tankcontroller.OnPositionChangedByDistanceListener;
import tomasz.jokiel.tankcontroller.TankControllerView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class PlaceholderFragment extends Fragment implements MessageDisplayer, OnClickListener, Callback, ApplicationStateListener, OnItemClickListener, OnDeviceConnectStateListener{
    private static final int REPEATER_PERIOD_MS = 1400;
    private static final int DISCOVERY_TIMEOUT_MSG = 1;
    private static final int REPEATER_MSG = 2;
    private static final long DISCOVERY_DEVICE_MAX_PERIOD = 12000;
    private TextView mTextView;
    private TextView mBatteryTextView;
    private TankControllerView mTankControllerView;
    private Button mStartDiscoveryButton;
    private Button mButtonDisconnect;
    private Button mBatteryButton;
    private ProgressBar mStartDiscoveryButtonProgressBar;
    private ListView mDiscoveredDevicesListView;
    private DiscoverableDevice mDiscoverableDevice;
    private LinkedHashSet<EndpointDevice> mEndpointDevices;
    private final Handler mHandler = new Handler(this);
    private DeviceConnectPerformer mDeviceConnectPerformer = new DeviceConnectPerformer(this);
    private View mConnectedToLlistItemView;
    
    private EditText mStreamPathEditText;
    private Button mStartButton;
    private VideoView mVideoView;
    private MediaController mMediaController;
    
    private final static String BATTERY_LEVEL_MESSAGE = "*:SC002:#"; 

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
                  mHandler.sendEmptyMessageDelayed(REPEATER_MSG, REPEATER_PERIOD_MS);
              }
          }
        }
    };

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.infoText);
        mBatteryTextView = (TextView) rootView.findViewById(R.id.batteryTextView);
        mTankControllerView = (TankControllerView) rootView.findViewById(R.id.tank_controller_view);
        mStartDiscoveryButton = (Button) rootView.findViewById(R.id.startDiscoveryButton);
        mButtonDisconnect = (Button) rootView.findViewById(R.id.buttonDisconnect);
        mBatteryButton = (Button) rootView.findViewById(R.id.batteryButton);
        mStartDiscoveryButton.setOnClickListener(this);
        mButtonDisconnect.setOnClickListener(this);
        mBatteryButton.setOnClickListener(this);
        mStartDiscoveryButtonProgressBar = (ProgressBar) rootView.findViewById(R.id.startDiscoveryButtonProgressBar);
        mDiscoveredDevicesListView = (ListView) rootView.findViewById(R.id.discoveredDevicesListView);
        mDiscoveredDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());
        mTankControllerView.setOnPositionChangedByDistanceListener(mOnPositionChangedByDistanceListenerTank);
        
        mStreamPathEditText = (EditText) rootView.findViewById(R.id.stream_path);
        mStartButton = (Button) rootView.findViewById(R.id.start_button);
        mVideoView = (VideoView)rootView.findViewById(R.id.video_view);

        mMediaController = new MediaController(getActivity());
        mMediaController.setAnchorView(mVideoView);
        mVideoView.setMediaController(mMediaController);

        mStartButton.setOnClickListener(this);

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
    public void batteryLevel(int batteryLevel) {
        mBatteryTextView.setText(String.valueOf(batteryLevel));
    }

    @Override
    public void onDeviceListChanged(LinkedHashSet<EndpointDevice> endpointDevices) {
        setDeviceList(endpointDevices);
    }

    @Override
    public void onClick(View v) {
        if(v == mStartDiscoveryButton) {
            startDiscovery();
        } else if(v == mButtonDisconnect) {
            disconnectDevice();
        } else if(v == mBatteryButton) {
            if(mDiscoverableDevice != null) {
                requestBatteryLevel();
            } else {
                Toast.makeText(getActivity(), "No connected?", Toast.LENGTH_SHORT).show();
            }
        } else if (v == mStartButton) {
            configureVideoView();
        }
    }

    private void disconnectDevice() {
        if(mDiscoverableDevice != null) {
            boolean success = mDiscoverableDevice.close();

            enableDevicesList();

            if(!success) {
                Toast.makeText(getActivity(), "Not connected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestBatteryLevel() {
        mDiscoverableDevice.writeBytes(BATTERY_LEVEL_MESSAGE.getBytes());
    }

    private void enableDevicesList() {
        mDiscoveredDevicesListView.setEnabled(true);

        if(mConnectedToLlistItemView != null) {
            int positionOnList = (int) mConnectedToLlistItemView.getTag();
            String viewText = ((TextView)mConnectedToLlistItemView).getText().toString();

            if(mDiscoveredDevicesArrayAdapter.getCount() > positionOnList) {
                String listItemTextOnTheList = mDiscoveredDevicesArrayAdapter.getItem(positionOnList);
                boolean isViewPositionStillSame = viewText.equals(listItemTextOnTheList);
                
                if(isViewPositionStillSame) {
                    mConnectedToLlistItemView.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setListItemChosenInactive(view, position);
        EndpointDevice[] device = mEndpointDevices.toArray(new EndpointDevice[mEndpointDevices.size()]);
        initateConnection(device[position]);
    }

    private void setListItemChosenInactive(View view, int position) {
        view.setEnabled(false);
        mDiscoveredDevicesListView.setEnabled(false);
        view.setTag(position);
        mConnectedToLlistItemView = view;
    }

    private void initateConnection(EndpointDevice endpointDevice) {
        if(mDiscoverableDevice != null) {
            mDeviceConnectPerformer.connectDevice(mDiscoverableDevice, endpointDevice);
        }
    }

    @Override
    public void onDeviceConnected(final EndpointDevice endpointDevice) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), "Connected:\n" + endpointDevice.toString(), Toast.LENGTH_SHORT).show();
            }
        });
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
        disconnectDevice();

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

    private void configureVideoView() {
      mVideoView.setVideoPath(mStreamPathEditText.getText().toString());
      mVideoView.start();
  }
}