package pl.xt.jokii;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MyLocation {
	private static final int MEASURES_TO_ESTABLISH = 2; 
	private static final long TIME_TO_WAIT_IN_SEC = 30; 
	private static final String TAG = "MyLocation";
    Timer timer1;
    LocationManager lm;
    LocationResultListener locationResultListener;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    int locationGotCounterGps;
    int locationGotCounterNetwork;
    Location currentBestLocation;

    public boolean getLocation(Context context, LocationResultListener locationListener)
    {
    	locationGotCounterGps = 0;
    	locationGotCounterNetwork = 0 ;
    	currentBestLocation = null;
//    	private static 
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResultListener = locationListener;
        if(lm == null){
        	lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        //exceptions will be thrown if provider is not permitted.
        try{
        	gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
        	Log.e(TAG, "GPS_PROVIDER isn't enabled");
        }
        try{
        	network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){
        	Log.e(TAG, "NETWORK_PROVIDER isn't enabled");
        }

        //don't start listeners if no provider is enabled
        if(!gps_enabled && !network_enabled){
        	Log.e(TAG, "No providers enabled");
        	return false;
        }

        if(gps_enabled){
        	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        	Log.v(TAG, "Start listen for GPS location");
        }
        if(network_enabled){
        	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        	Log.v(TAG, "Start listen for NETWORK location");
        }
        timer1 = new Timer();
        timer1.schedule(new GetLastLocation(), TIME_TO_WAIT_IN_SEC * 1000);
        
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
        	if(isBetterLocation(location, currentBestLocation)){
        		currentBestLocation = location;
        	}
        	locationGotCounterGps++;
        	if((locationGotCounterGps >= MEASURES_TO_ESTABLISH) && (!network_enabled)){
        		timer1.cancel();
        		Log.v(TAG, "gotLocation GPS");
        		locationResultListener.gotLocation(location);
        		lm.removeUpdates(this);
        		lm.removeUpdates(locationListenerNetwork);
        	}
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
        	if(isBetterLocation(location, currentBestLocation)){
        		currentBestLocation = location;
        	}
        	locationGotCounterNetwork++;
        	if((locationGotCounterNetwork >= MEASURES_TO_ESTABLISH) && (!gps_enabled)){
        		timer1.cancel();
        		Log.v(TAG, "gotLocation NETWORK");
        		locationResultListener.gotLocation(location);
        		lm.removeUpdates(this);
//            lm.removeUpdates(locationListenerGps);	// don,t remove GPS location before if found only NETWORK location
        	}
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
             lm.removeUpdates(locationListenerGps);
             lm.removeUpdates(locationListenerNetwork);

             Location net_loc = null, gps_loc = null;
             
             if(gps_enabled){
            	 gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             }
             if(network_enabled){
            	 net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
             }

             //if there are both values use the latest one
             if((gps_loc != null) && (net_loc != null)){
             	if(isBetterLocation(gps_loc, net_loc)){
             		currentBestLocation = gps_loc;
            	}else{
            		currentBestLocation = net_loc;
            	}
             	locationResultListener.gotLocation(currentBestLocation);
                return;
             }

             if(gps_loc != null){
            	 Log.v(TAG, "gotLocation LAST KNOWN GPS");
                 locationResultListener.gotLocation(gps_loc);
                 return;
             }
             if(net_loc != null){
            	 Log.v(TAG, "gotLocation LAST KNOWN NETWORK");
                 locationResultListener.gotLocation(net_loc);
                 return;
             }
             Log.v(TAG, "gotLocation LAST KNOWN null");
             locationResultListener.gotLocation(null);
        }
    }
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }    

    public static abstract class LocationResultListener{
        public abstract void gotLocation(Location location);
    }
}
