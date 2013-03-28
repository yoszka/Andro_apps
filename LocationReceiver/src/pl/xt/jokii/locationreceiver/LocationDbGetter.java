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

public class LocationDbGetter extends IntentService{
	private static final String TAG = "LocationGetter";
	private static final String dBUsername = "jokii";
	private static final String dBPassword = "jostpr";
//	private Location mCurrentLocation = new Location(LocationManager.NETWORK_PROVIDER);

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
		if(Connectivity.isOnline(this)){
			Location loc = queryDB();
			
			Intent locationIntent = new Intent("pl.xt.jokii.locationreceiver.LOCATION");
			locationIntent.putExtra(KEY_LOCATION_LATITUDE, 	loc.getLatitude()+"");
			locationIntent.putExtra(KEY_LOCATION_LONGITUDE, loc.getLongitude()+"");
			locationIntent.putExtra(KEY_LOCATION_ACCURACY, 	loc.getAccuracy()+"");
			locationIntent.putExtra(KEY_LOCATION_PROVIDER,  loc.getProvider()+"");
			locationIntent.putExtra(KEY_LOCATION_TIMESTAMP, loc.getTime()+"");
			sendBroadcast(locationIntent);
		}else{
			// No internet connection available
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
	}
	
    
    
    Location queryDB(){
		Connection connection;
		Statement statement;
		ResultSet result;
		Location location = new Location(LocationManager.NETWORK_PROVIDER);

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
        		 location.setLatitude(result.getDouble(KEY_LOCATION_LATITUDE));
        		 location.setLongitude(result.getDouble(KEY_LOCATION_LONGITUDE));
        		 location.setAccuracy(result.getFloat(KEY_LOCATION_ACCURACY));
        		 location.setProvider(result.getString(KEY_LOCATION_PROVIDER));
        		 location.setTime(result.getLong(KEY_LOCATION_TIMESTAMP));
        	 }


        	result.close();
        	statement.close();
        	connection.close();	
		} catch (Exception e) {
			throw new RuntimeException("JDBC connection fail", e);
		}

		Log.v("JDBC result", "result: " + location.getTime() + ", provider: " + location.getProvider());
		
		return location;
	}

}
