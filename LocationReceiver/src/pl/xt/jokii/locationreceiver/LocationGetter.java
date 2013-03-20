package pl.xt.jokii.locationreceiver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationGetter extends IntentService{
	private static final String TAG = "LocationGetter";
	private static final String dBUsername = "username";
	private static final String dBPassword = "password";
	private Location mCurrentLocation = new Location(LocationManager.NETWORK_PROVIDER);

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

	public LocationGetter() 
    { 
        super("LocationGetter"); 
    } 
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
		queryDB();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
	}
	
    
    
    void queryDB(){
		Connection connection;
		Statement statement;
		ResultSet result;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			throw new RuntimeException("JDBC driver fail");
		}


		try {
			//Connection connection = DriverManager.getConnection("jdbc:mysql://db_address:port/bd_name","user","passowrd");
        	connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/android971873", dBUsername, dBPassword);
        	statement   = connection.createStatement();
        	
//        	statement.execute("INSERT INTO `proba` (`ID`, `text`) VALUES (NULL, 'z eclipsa androida');");		// use statement.execute("....."); for INSERT, UPDATE ?
//        	statement.execute("INSERT INTO `android971873`.`locations` (`ID`, `user`, `location_lat`, `location_lon`, `location_acc`, `location_provider`, `location_timestamp`) VALUES (NULL, 'tomek', '51.1151300', '16.9506200', '100.0', 'network', 1363388511000);");
        	result = statement.executeQuery("SELECT * FROM `locations`"); 											// use statement.executeQuery("....."); for SELECT


        	 while (result.next()) {
        		 mCurrentLocation.setLatitude(result.getDouble(KEY_LOCATION_LATITUDE));
        		 mCurrentLocation.setLongitude(result.getDouble(KEY_LOCATION_LONGITUDE));
        		 mCurrentLocation.setAccuracy(result.getFloat(KEY_LOCATION_ACCURACY));
        		 mCurrentLocation.setProvider(result.getString(KEY_LOCATION_PROVIDER));
        		 mCurrentLocation.setTime(result.getLong(KEY_LOCATION_TIMESTAMP));
        	 }


        	result.close();
        	statement.close();
        	connection.close();	
		} catch (Exception e) {
			throw new RuntimeException("JDBC connection fail", e);
		}

		Log.v("JDBC result", "result: " + mCurrentLocation.getTime() + ", provider: " + mCurrentLocation.getProvider());
		
		Intent intent = new Intent("pl.xt.jokii.locationreceiver.LOCATION");
		intent.putExtra(KEY_LOCATION_LATITUDE, 	mCurrentLocation.getLatitude()+"");
		intent.putExtra(KEY_LOCATION_LONGITUDE, mCurrentLocation.getLongitude()+"");
		intent.putExtra(KEY_LOCATION_ACCURACY, 	mCurrentLocation.getAccuracy()+"");
		intent.putExtra(KEY_LOCATION_PROVIDER,  mCurrentLocation.getProvider()+"");
		intent.putExtra(KEY_LOCATION_TIMESTAMP, mCurrentLocation.getTime()+"");
		sendBroadcast(intent);
		
	}

}
