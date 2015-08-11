package tomasz.jokiel.blootothcontroller.iodevice;

import android.os.Parcel;
import android.os.Parcelable;

public class EndpointDevice implements Parcelable{
    private String mName;
    private String mAddress;
    private boolean mIsConnectEventAcynchronous;
    
    public EndpointDevice(String name, String address){
        mName = name;
        mAddress = address;
    }

    public EndpointDevice(Parcel in) {
        mName = in.readString();
        mAddress = in.readString();
    }

    public String getName(){
        return mName;
    }
    public String getAddress() {
        return mAddress;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[NAME:");
        sb.append(mName);
        sb.append("][ADDRESS:");
        sb.append(mAddress);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mAddress == null) ? 0 : mAddress.hashCode());
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EndpointDevice other = (EndpointDevice) obj;
        if (mAddress == null) {
            if (other.mAddress != null)
                return false;
        } else if (!mAddress.equals(other.mAddress))
            return false;
        if (mName == null) {
            if (other.mName != null)
                return false;
        } else if (!mName.equals(other.mName))
            return false;
        return true;
    }

    public static final Parcelable.Creator<EndpointDevice> CREATOR
    = new Parcelable.Creator<EndpointDevice>() {
        public EndpointDevice createFromParcel(Parcel in) {
            return new EndpointDevice(in);
        }

        public EndpointDevice[] newArray(int size) {
            return new EndpointDevice[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mAddress);
    }

    public boolean isIsConnectEventAcynchronous() {
        return mIsConnectEventAcynchronous;
    }

    public void setIsConnectEventAcynchronous(boolean isConnectEventAcynchronous) {
        mIsConnectEventAcynchronous = isConnectEventAcynchronous;
    }
    
    
}
