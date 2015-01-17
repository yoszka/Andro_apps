package tomasz.jokiel.blootothcontroller;

import android.os.AsyncTask;
import tomasz.jokiel.blootothcontroller.iodevice.DiscoverableDevice;
import tomasz.jokiel.blootothcontroller.iodevice.EndpointDevice;

public class DeviceConnectPerformer {
    private final OnDeviceConnectedListener mOnDeviceConnectedListener;

    public DeviceConnectPerformer(OnDeviceConnectedListener onDeviceConnectedListener) {
        mOnDeviceConnectedListener = onDeviceConnectedListener;
    }

    public void connectDevice(DiscoverableDevice discoverableDevice, EndpointDevice endpointDevice) {
        new DeviceConnectingAsyncTask(discoverableDevice).execute(endpointDevice);
    }

    private class DeviceConnectingAsyncTask extends AsyncTask<EndpointDevice, EndpointDevice, EndpointDevice> {
        private final DiscoverableDevice mDiscoverableDevice;

        public DeviceConnectingAsyncTask(DiscoverableDevice discoverableDevice) {
            mDiscoverableDevice = discoverableDevice;
        }

        @Override
        protected EndpointDevice doInBackground(EndpointDevice... params) {
            EndpointDevice endpointDevice = params[0];
            mDiscoverableDevice.connect(endpointDevice);
            return endpointDevice;
        }

        @Override
        protected void onPostExecute(EndpointDevice endpointDevice) {
            mOnDeviceConnectedListener.onDeviceConnected(endpointDevice);
        }
    }

    public interface OnDeviceConnectedListener {
        public void onDeviceConnected(EndpointDevice endpointDevice);
    }
}
