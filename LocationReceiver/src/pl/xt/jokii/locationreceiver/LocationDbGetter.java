package pl.xt.jokii.locationreceiver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import pl.xt.jokii.locationreceiver.settings.SettingsActivity;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
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
public class LocationDbGetter extends IntentService{
	private static final String TAG = "LocationGetter";
	private static final String DB_USERNAME  = "user";
	private static final String DB_PASSWORD  = "password";
	private static final String GENERIC_USER_NAME      = "user_generic";	
//	private Location mCurrentLocation = new Location(LocationManager.NETWORK_PROVIDER);
	private SharedPreferences mDefaultSharedPrefs;
    private String mRemoteUserName;

	/**
	 * Data type: double
	 */
	private final static String KEY_LOCATION_LATITUDE = "location_lat";
	
	/**
	 * Data type: double
	 */
	private final static String KEY_LOCATION_LONGITUDE = "location_lon";
	
	/**
	 * Data type: double
	 */
	private final static String KEY_LOCATION_ACCURACY = "location_acc";
	
	/**
	 * Data type: double
	 */
	private final static String KEY_LOCATION_PROVIDER = "location_provider";
	
	/**
	 * Data type: long
	 */
	private final static String KEY_LOCATION_TIMESTAMP = "location_timestamp";

	public LocationDbGetter() 
    { 
        super("LocationGetter"); 
    } 
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
	      mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	      mRemoteUserName = mDefaultSharedPrefs.getString(SettingsActivity.PREFERENCE_KEY_USER_TO_TRACK, GENERIC_USER_NAME);
	        
		if(Connectivity.isOnline(this)){
		    ArrayList<Location> locationArray = queryDB();
			
			if ((locationArray) != null && (locationArray.size() > 0)) {
				Intent locationIntent = new Intent(Const.ACTION_LOCATION);
				locationIntent.putExtra(Const.EXTRA_LOCATION_ARRAY, locationArray);
//				locationIntent.putExtra(KEY_LOCATION_LATITUDE, 	loc.getLatitude()+"");
//				locationIntent.putExtra(KEY_LOCATION_LONGITUDE, loc.getLongitude()+"");
//				locationIntent.putExtra(KEY_LOCATION_ACCURACY, 	loc.getAccuracy()+"");
//				locationIntent.putExtra(KEY_LOCATION_PROVIDER,  loc.getProvider()+"");
//				locationIntent.putExtra(KEY_LOCATION_TIMESTAMP, loc.getTime()+"");
				sendBroadcast(locationIntent);
			}
			else{
				sendBroadcast(new Intent(Const.ACTION_DATABASE_UNAVAILABLE));
			}
		}else{
			// No internet connection available
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
	}
	
    
    
    ArrayList<Location> queryDB(){
		Connection connection;
		Statement statement;
		ResultSet result;
		ArrayList<Location> locationArray = new ArrayList<Location>();
		Location location;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			throw new RuntimeException("JDBC driver fail");
		}


		try {
			//Connection connection = DriverManager.getConnection("jdbc:mysql://db_address:port/bd_name","user","passowrd");
        	connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/android971873", DB_USERNAME, DB_PASSWORD);
        	statement   = connection.createStatement();
        	
//        	statement.execute("INSERT INTO `proba` (`ID`, `text`) VALUES (NULL, 'z eclipsa androida');");		// use statement.execute("....."); for INSERT, UPDATE ?
//        	statement.execute("INSERT INTO `android971873`.`locations` (`ID`, `user`, `location_lat`, `location_lon`, `location_acc`, `location_provider`, `location_timestamp`) VALUES (NULL, 'tomek', '51.1151300', '16.9506200', '100.0', 'network', 1363388511000);");
//        	result = statement.executeQuery("SELECT * FROM `locations`"); 											// use statement.executeQuery("....."); for SELECT
        	result = statement.executeQuery("SELECT * FROM `locations` WHERE `user` = '"+mRemoteUserName+"' ORDER BY `location_timestamp` DESC");


        	 while (result.next()) {
        	     location = new Location(result.getString(KEY_LOCATION_PROVIDER));
        		 location.setLatitude(result.getDouble(KEY_LOCATION_LATITUDE));
        		 location.setLongitude(result.getDouble(KEY_LOCATION_LONGITUDE));
        		 location.setAccuracy(result.getFloat(KEY_LOCATION_ACCURACY));
        		 location.setTime(result.getLong(KEY_LOCATION_TIMESTAMP));

        		 locationArray.add(location);
        	 }


        	result.close();
        	statement.close();
        	connection.close();	
        	Log.v("JDBC result", "result: " + locationArray.size() + ((locationArray.size() > 0) ? ", last provider: " + locationArray.get(0).getProvider() : ""));
		}catch(SQLException e){
			e.printStackTrace();
			locationArray = null;
		} catch (Exception e) {
			throw new RuntimeException("JDBC connection fail", e);
		}

		
		return locationArray;
	}

}
