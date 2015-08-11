package tomasz.jokiel.blootothcontroller;

import tomasz.jokiel.blootothcontroller.iodevice.DiscoverableDevice;
import tomasz.jokiel.blootothcontroller.iodevice.EndpointDevice;
import tomasz.jokiel.blootothcontroller.iodevice.OnDeviceConnectStateListener;
import android.os.AsyncTask;

public class DeviceConnectPerformer {
    private final OnDeviceConnectStateListener mDeviceConnectStateListener;

    public DeviceConnectPerformer(OnDeviceConnectStateListener onDeviceConnectStateListener) {
        mDeviceConnectStateListener = onDeviceConnectStateListener;
    }

    public void connectDevice(DiscoverableDevice discoverableDevice, EndpointDevice endpointDevice) {
        new DeviceConnectingAsyncTask(discoverableDevice).execute(endpointDevice);
    }

    private class DeviceConnectingAsyncTask extends AsyncTask<EndpointDevice, EndpointDevice, EndpointDevice> {
        private final DiscoverableDevice mDiscoverableDevice;

        public DeviceConnectingAsyncTask(DiscoverableDevice discoverableDevice) {
            mDiscoverableDevice = discoverableDevice;
            mDiscoverableDevice.setDeviceConnectStateListener(mDeviceConnectStateListener);
        }

        @Override
        protected EndpointDevice doInBackground(EndpointDevice... params) {
            EndpointDevice endpointDevice = params[0];
            mDiscoverableDevice.connect(endpointDevice);
            return endpointDevice;
        }

        @Override
        protected void onPostExecute(EndpointDevice endpointDevice) {
            if (!endpointDevice.isIsConnectEventAcynchronous()) {
                mDeviceConnectStateListener.onDeviceConnected(endpointDevice);
            }
        }
    }
}
