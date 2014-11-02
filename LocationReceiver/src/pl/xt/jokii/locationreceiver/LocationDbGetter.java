package pl.xt.jokii.locationreceiver;

import java.util.ArrayList;

import pl.xt.jokii.locationreceiver.settings.SettingsActivity;
import pl.xt.jokii.pushnotifications.server.model.LocationDb;
import pl.xt.jokii.pushnotifications.server.util.LocationsGetter;
import pl.xt.jokii.pushnotifications.server.util.OnLocationsGetListener;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

/*
 --
 -- Table structure for table `locations`
 --

 CREATE TABLE `locations` (
 `ID` int(11) NOT NULL AUTO_INCREMENT,
 `user` varchar(50) NOT NULL,
 `location_lat` double NOT NULL,
 `location_lon` double NOT NULL,
 `location_acc` float NOT NULL,
 `location_provider` varchar(50) NOT NULL,
 `location_timestamp` bigint(15) NOT NULL,
 PRIMARY KEY (`ID`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;
 */
public class LocationDbGetter extends IntentService implements OnLocationsGetListener {
    private static final String TAG                    = "LocationGetter";
    private static final String GENERIC_USER_NAME      = "user_generic";
    private SharedPreferences   mDefaultSharedPrefs;
    private String              mRemoteUserName;

    public LocationDbGetter() {
        super("LocationGetter");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");
        mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mRemoteUserName = mDefaultSharedPrefs.getString(SettingsActivity.PREFERENCE_KEY_USER_TO_TRACK, GENERIC_USER_NAME);

        if (Connectivity.isOnline(this)) {
            LocationsGetter.getLocations(mRemoteUserName, this);
        } else {
            // No internet connection available
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Override
    public void onLocationsGet(LocationDb[] locations) {
        if (locations != null) {
            ArrayList<Location> locationArray = createLocationsArray(locations);

            if ((locationArray) != null && (!locationArray.isEmpty())) {
                Intent locationIntent = new Intent(Const.ACTION_LOCATION);
                locationIntent.putExtra(Const.EXTRA_LOCATION_ARRAY, locationArray);
                sendBroadcast(locationIntent);
            } else {
                sendBroadcast(new Intent(Const.ACTION_DATABASE_UNAVAILABLE));
            }
        }
    }

    private ArrayList<Location> createLocationsArray(LocationDb[] locations) {
        ArrayList<Location> locationArray = new ArrayList<Location>(locations.length);
        Location location;

        for (LocationDb loc : locations) {
            location = new Location(loc.getLocation_provider());
            location.setLatitude(loc.getLocation_lat());
            location.setLongitude(loc.getLocation_lon());
            location.setAccuracy(loc.getLocation_acc());
            location.setTime(loc.getLocation_timestamp());

            locationArray.add(location);
        }

        return locationArray;
    }

}
