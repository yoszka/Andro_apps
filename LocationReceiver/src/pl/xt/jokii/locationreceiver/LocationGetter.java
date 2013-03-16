package pl.xt.jokii.locationreceiver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class LocationGetter extends IntentService{
	private static final String TAG = "LocationGetter";
	public static final int IDX_ALARM_MESSAGE = 19991999;
	private static int NUMBER_OF_RUNS = 5;
	private static int triggerCounter = 0;
	private Object synchr = new Object();
	private StringBuilder mStrb;
	private Location mCurrentLocation = new Location(LocationManager.NETWORK_PROVIDER);

	/**
	 * Data type: boolean
	 */
	private final static String KEY_FISRST_RUN = "first_run";
	
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
		
		Bundle extras = intent.getExtras();
		if((extras != null) && (extras.containsKey(KEY_FISRST_RUN))){
			triggerCounter = 0;
		}
		
		triggerCounter++;
		
//		startJDBC();
		queryDB();
		
//		synchronized (synchr) {
//			try {
//				synchr.wait(20000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}	
//		}
		
        
//		synchronized (synchr) {
//			MyLocation myLocation = new MyLocation();
//			boolean triggerSuccess = myLocation.getLocation(getApplicationContext(), mLocationResult);
//			
//			if(!triggerSuccess){
//				Log.e(TAG, "Unsuccessful trigger getLocation");
//			}else{
//				try {
//					synchr.wait(30000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
	}
	
//    LocationResultListener mLocationResult = new LocationResultListener(){
//        @Override
//        public void gotLocation(Location loc){
//        	if(loc != null){
////			Toast.makeText(getBaseContext(),
////					"Location changed : Lat: " + loc.getLatitude() +
////					" Lng: " + loc.getLongitude() + ", Acc: " + loc.getAccuracy(),
////					Toast.LENGTH_SHORT).show();
//        		
//        		
//        		Log.v(TAG, "onLocationChanged. LAT: [" + loc.getLatitude() + "], LON: [" + loc.getLongitude() + "], accuracy: [" + loc.getAccuracy() + "]");
//        		Intent intent = new Intent("pl.xt.jokii.locationreceiver.LOCATION");
//        		intent.putExtra(KEY_LOCATION_LATITUDE, 	loc.getLatitude()+"");
//        		intent.putExtra(KEY_LOCATION_LONGITUDE, loc.getLongitude()+"");
//        		intent.putExtra(KEY_LOCATION_ACCURACY, 	loc.getAccuracy()+"");
//        		intent.putExtra(KEY_LOCATION_PROVIDER, loc.getProvider()+"");
//        		intent.putExtra(KEY_LOCATION_TIMESTAMP, loc.getTime()+"");
//        		sendBroadcast(intent);
//        		
//        		//adb shell am broadcast -a pl.xt.jokii.locationreceiver.LOCATION -e location_lat 51.1151300 -e location_lon 16.9506200 -e location_acc 100.0 -e location_provider network -e location_timestamp 1363388511000
//        		// send intent or whatever
////        		final double LAT = loc.getLatitude();
////        		final double LON = loc.getLongitude();
////        		final float ACC = loc.getAccuracy();
//        	}else{
//        		Log.v(TAG, "No location found");
//        	}
//        	
//        	if(triggerCounter < NUMBER_OF_RUNS){
//        		setScheduleTask();
//        	}
//        	synchronized (synchr) {
//        		synchr.notify();
//			}
//        }
//    };
    
	
    /**
     * Execute remote DB operations
     */
    public void startJDBC()
    {
    	new RefreshDbData().execute();        
    }
    
    
    /**
     * Connecting to remote MySQL data base, perform INSERT and SELECT operation
     * source http://developer.android.com/guide/components/processes-and-threads.html
     * @author Tomasz Jokiel
     *
     */
    private class RefreshDbData extends AsyncTask<Void, Void, Location>
    {

    	/** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
		@Override
		protected Location doInBackground(Void... arg0) {
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
	        	connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/android971873","username","pasword");
	        	statement   = connection.createStatement();
	        	
//	        	statement.execute("INSERT INTO `proba` (`ID`, `text`) VALUES (NULL, 'z eclipsa androida');");		// use statement.execute("....."); for INSERT, UPDATE ?
//	        	statement.execute("INSERT INTO `android971873`.`locations` (`ID`, `user`, `location_lat`, `location_lon`, `location_acc`, `location_provider`, `location_timestamp`) VALUES (NULL, 'tomek', '51.1151300', '16.9506200', '100.0', 'network', 1363388511000);");
	        	result = statement.executeQuery("SELECT * FROM `locations`"); 											// use statement.executeQuery("....."); for SELECT


	        	mStrb = new StringBuilder();
	        	 while (result.next()) {
	        		 mStrb.append("location_lat: " + result.getString(result.findColumn("location_lat")) + "\n");
	        		 mStrb.append("location_lon: " + result.getDouble(result.findColumn("location_lon")) + "\n");
	        		 mStrb.append("location_timestamp: " + result.getLong("location_timestamp") + "\n");
	        		 
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

//			return mStrb.toString();
			return mCurrentLocation;
		}

	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */		
		@Override
		protected void onPostExecute(Location loc) {
//			tv.setText(result);
//			Log.v("JDBC result", "result: " + result);
			Log.v("JDBC result", "result: " + loc.getTime() + ", provider: " + loc.getProvider());
			
    		Intent intent = new Intent("pl.xt.jokii.locationreceiver.LOCATION");
    		intent.putExtra(KEY_LOCATION_LATITUDE, 	loc.getLatitude()+"");
    		intent.putExtra(KEY_LOCATION_LONGITUDE, loc.getLongitude()+"");
    		intent.putExtra(KEY_LOCATION_ACCURACY, 	loc.getAccuracy()+"");
    		intent.putExtra(KEY_LOCATION_PROVIDER, loc.getProvider()+"");
    		intent.putExtra(KEY_LOCATION_TIMESTAMP, loc.getTime()+"");
    		sendBroadcast(intent);
    		
    		if(triggerCounter < NUMBER_OF_RUNS){
    			setScheduleTask();
    		}
    		
    		synchronized (synchr) {
    			synchr.notify();	
    		}
		}
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
        	connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/android971873","username","password");
        	statement   = connection.createStatement();
        	
//        	statement.execute("INSERT INTO `proba` (`ID`, `text`) VALUES (NULL, 'z eclipsa androida');");		// use statement.execute("....."); for INSERT, UPDATE ?
//        	statement.execute("INSERT INTO `android971873`.`locations` (`ID`, `user`, `location_lat`, `location_lon`, `location_acc`, `location_provider`, `location_timestamp`) VALUES (NULL, 'tomek', '51.1151300', '16.9506200', '100.0', 'network', 1363388511000);");
        	result = statement.executeQuery("SELECT * FROM `locations`"); 											// use statement.executeQuery("....."); for SELECT


        	mStrb = new StringBuilder();
        	 while (result.next()) {
        		 mStrb.append("location_lat: " + result.getString(result.findColumn("location_lat")) + "\n");
        		 mStrb.append("location_lon: " + result.getDouble(result.findColumn("location_lon")) + "\n");
        		 mStrb.append("location_timestamp: " + result.getLong("location_timestamp") + "\n");
        		 
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

//		return mStrb.toString();
//		return mCurrentLocation;
		
		Log.v("JDBC result", "result: " + mCurrentLocation.getTime() + ", provider: " + mCurrentLocation.getProvider());
		
		Intent intent = new Intent("pl.xt.jokii.locationreceiver.LOCATION");
		intent.putExtra(KEY_LOCATION_LATITUDE, 	mCurrentLocation.getLatitude()+"");
		intent.putExtra(KEY_LOCATION_LONGITUDE, mCurrentLocation.getLongitude()+"");
		intent.putExtra(KEY_LOCATION_ACCURACY, 	mCurrentLocation.getAccuracy()+"");
		intent.putExtra(KEY_LOCATION_PROVIDER,  mCurrentLocation.getProvider()+"");
		intent.putExtra(KEY_LOCATION_TIMESTAMP, mCurrentLocation.getTime()+"");
		sendBroadcast(intent);
		
//		if(triggerCounter < NUMBER_OF_RUNS){
//			setScheduleTask();
//		}
		
	}
	
	
    /**
	 * Configure scheduled task
	 */
	private void setScheduleTask()
	{
		 Log.v(TAG, "setScheduleTask");
		 
		// get a Calendar object with current time
		 Calendar cal = Calendar.getInstance();
		 // add 30 second to the calendar object
		 cal.add(Calendar.SECOND, 30);

		 Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);

		 PendingIntent sender = PendingIntent.getBroadcast(this, IDX_ALARM_MESSAGE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		 // Get the AlarmManager service
		 AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		 am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
		 
		 //am.setRepeating(AlarmManager.RTC_WAKEUP, 5*1000, 5*1000, sender);
		 //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 5*1000, 5*1000, sender);
//		 Toast.makeText(getApplicationContext(), "Task scheduled", Toast.LENGTH_SHORT).show();
	}

}
