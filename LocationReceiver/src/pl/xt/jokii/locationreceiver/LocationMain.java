package pl.xt.jokii.locationreceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import pl.xt.jokii.locationreceiver.settings.SettingsActivity;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * @author Tomek
 * adb shell am broadcast -a pl.xt.jokii.locationreceiver.LOCATION -e extras_key extras_string_value
 * adb shell am broadcast -a pl.xt.jokii.locationreceiver.LOCATION -e location_lat 51.1151300 -e location_lon 16.9506200 -e location_acc 100.0 -e location_provider network -e location_timestamp 1363388511000
 */
public class LocationMain extends MapActivity {
	private boolean mIsReceiverRegistered = false;
	private boolean BD_POOLING_ENABLED = true;
	private BroadcastReceiver mReceiver; 
//	private final static String RECEIVER_ACTION = "pl.xt.jokii.locationreceiver.LOCATION";
	MapView mapView;
	private Drawable mPositionIcon; 
	private Drawable mSignalTypeIndicator; 
//	private Drawable time2; 
//	private Drawable cancel; 
	private Point punkt = new Point();
	private GeoPoint mCurrentPos = new GeoPoint(52198000, 18923080);
//	private Location mCurrentLocation = new Location(LocationManager.NETWORK_PROVIDER);
	private MapController mc;
//	private StringBuilder mStrb;
//	private final static String KEY_FISRST_RUN = "first_run";
	private static final int POSITION_REFRESH_PERIOD_SECOND = 20;
	private Timer mTimer = new Timer();
	private SharedPreferences mDefaultSharedPrefs;
	private SharedPreferences mSharedPrefs;
	List listLocationsSynchr = Collections.synchronizedList(new ArrayList<Location>());
	private int mLastLocationsCount;
	
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
	
	
	private void startTask() {
        mTimer.schedule(new PeriodicTask(), 0);
    }
	
	private class PeriodicTask extends TimerTask {
        @Override
        public void run() {
    		Intent intent = new Intent(getApplicationContext(), LocationDbGetter.class);
    		getApplicationContext().startService(intent);
    		
            mTimer.schedule(new PeriodicTask(), POSITION_REFRESH_PERIOD_SECOND * 1000);
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(!Connectivity.isOnline(this)){
        	new AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("No active Internet connection available")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                	finish();
                }
             })
             .show();
		}else{
			setContentView(R.layout.main);
			
			// Prepare MapView
			mapView = (MapView) findViewById(R.id.mapView1);
			mapView.setEnabled(true);
			mapView.setClickable(true);
			mapView.setBuiltInZoomControls(true);
			mapView.getOverlays().add(new MapOverlay());
			
			mc = mapView.getController();
			mc.setZoom(7);
			mc.animateTo(mCurrentPos);
			
			mPositionIcon = getResources().getDrawable(R.drawable.ic_android);
			mPositionIcon.setBounds(0,0,30,30);
			
			mReceiver = new BroadcastReceiver() {
				@SuppressWarnings("unchecked")
                @Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if ((action != null) && (action.equals(Const.ACTION_LOCATION))) {
//					    ArrayList<Location> list;
						Bundle extras = intent.getExtras();
						
						double latitude  = 0.0;
						double longitude = 0.0;
						float accuracy   = 0;
						String provider  = null;
						long timestamp   = 0;

						if ((extras != null) && (extras.containsKey(Const.EXTRA_LOCATION_ARRAY))) {
						    ArrayList<Location> list = (ArrayList<Location>) extras.getSerializable(Const.EXTRA_LOCATION_ARRAY);

						    synchronized(listLocationsSynchr)
						    {
						        listLocationsSynchr.clear();
    						    listLocationsSynchr.addAll(list);
    						    
    						    if ((listLocationsSynchr != null) && (listLocationsSynchr.size() > 0)) {
    						        latitude  = ((Location)listLocationsSynchr.get(0)).getLatitude();
    						        longitude = ((Location)listLocationsSynchr.get(0)).getLongitude();
    						        accuracy  = ((Location)listLocationsSynchr.get(0)).getAccuracy();
    						        provider  = ((Location)listLocationsSynchr.get(0)).getProvider();
    						        timestamp = ((Location)listLocationsSynchr.get(0)).getTime();
    
    						        Toast.makeText(getApplicationContext(), "SIZE:"+listLocationsSynchr.size()+", RECEIVE LAT: " + latitude + ", LON: " + longitude + ", ACC: " + accuracy + ", PROV: " + provider + ", T: " + timestamp + ", t: " + new Date(timestamp).toString(), Toast.LENGTH_SHORT).show();
    						        
//    						        mCurrentLocation.setLatitude(latitude);
//    						        mCurrentLocation.setLongitude(longitude);
//    						        mCurrentLocation.setAccuracy(accuracy);
//    						        mCurrentLocation.setProvider(provider);
//    						        mCurrentLocation.setTime(timestamp);
    						        
    						        mCurrentPos = new GeoPoint(
    						                (int) (latitude * 1E6),
    						                (int) (longitude * 1E6));
    						        mc.animateTo(mCurrentPos);
    						    } else {
    						        Toast.makeText(getApplicationContext(), "No input data", Toast.LENGTH_LONG).show();
    						    }
						    }
						}
						
//						String tmp = null;
//						
//						if(extras.containsKey(KEY_LOCATION_LATITUDE)){
//							tmp = extras.getString(KEY_LOCATION_LATITUDE);
//							latitude = Double.parseDouble(tmp);
//						}
//						
//						if(extras.containsKey(KEY_LOCATION_LONGITUDE)){
//							tmp = extras.getString(KEY_LOCATION_LONGITUDE);
//							longitude = Double.parseDouble(tmp);
//						}
//						
//						if(extras.containsKey(KEY_LOCATION_ACCURACY)){
//							tmp = extras.getString(KEY_LOCATION_ACCURACY);
//							accuracy = Float.parseFloat(tmp);
//						}
//						
//						if(extras.containsKey(KEY_LOCATION_PROVIDER)){
//							provider = extras.getString(KEY_LOCATION_PROVIDER);
//						}
//						
//						if(extras.containsKey(KEY_LOCATION_TIMESTAMP)){
//							tmp = extras.getString(KEY_LOCATION_TIMESTAMP);
//							timestamp = Long.parseLong(tmp);
//						}
						

					}else if(intent.getAction().equals(Const.ACTION_DATABASE_UNAVAILABLE)){
						// Show dialog on SQL connection problem
			        	new AlertDialog.Builder(LocationMain.this)
			            .setTitle("Error")
			            .setMessage("Database Connection Error")
			            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int which) { 
			                	finish();
			                }
			             })
			             .show();
					}
				}
			};
			
			registerReceiver(mReceiver, new IntentFilter(Const.ACTION_LOCATION));
			registerReceiver(mReceiver, new IntentFilter(Const.ACTION_DATABASE_UNAVAILABLE));
			mIsReceiverRegistered = true;
			
			mSharedPrefs = getSharedPreferences(Const.LOCALIZER_PREFERENCES, Activity.MODE_WORLD_READABLE);
			
			if(BD_POOLING_ENABLED && mSharedPrefs.getBoolean(Const.POOLING_ENABLED, false)){
				startTask();
			}
		}		
		
		mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	class MapOverlay extends Overlay{
    	
    	public void draw(Canvas canvas, MapView mapView, boolean shadow) {        		
    		super.draw(canvas, mapView, shadow);
    		if(shadow) return;

    		synchronized(listLocationsSynchr)
    		{
    		    if ((listLocationsSynchr != null) && (listLocationsSynchr.size() > 0)) {
    		        Location lastLocation = ((Location)listLocationsSynchr.get(0));
    		        
    		        Stack<Integer> saveCanvas = new Stack<Integer>();
    		        
    		        mapView.getProjection().toPixels(mCurrentPos, punkt);
    		        
    		        saveCanvas.push(canvas.save());
    		        canvas.translate(punkt.x - (mPositionIcon.getBounds().width()/2), punkt.y - (mPositionIcon.getBounds().height()/2));
    		        
    		        // Draw circle of accuracy
    		        Paint p = new Paint();
    		        p.setColor(Color.RED);
    		        p.setAlpha(70);
    		        canvas.drawCircle((mPositionIcon.getBounds().width()/2), (mPositionIcon.getBounds().height()/2), (int)(lastLocation.getAccuracy() * calculateZoomToPixelMetersRatio(mapView.getZoomLevel())), p);
    		        
    		        // Draw location icon
    		        mPositionIcon.draw(canvas);
    		        
    		        // Draw accuracy value
    		        if(lastLocation.getAccuracy() > 0){
    		            p.setColor(Color.BLACK);
    		            p.setAlpha(255);
    		            canvas.drawText(String.format("%.1f", lastLocation.getAccuracy()), mPositionIcon.getBounds().width(), 0, p);
    		        }
    		        
    		        // Draw signal type icon
    		        saveCanvas.push(canvas.save());
    		        mSignalTypeIndicator = null;
    		        if(lastLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
    		            mSignalTypeIndicator   = getResources().getDrawable(R.drawable.antenna);
    		        }else if(lastLocation.getProvider().equals(LocationManager.GPS_PROVIDER)){
    		            mSignalTypeIndicator   = getResources().getDrawable(R.drawable.satellite);
    		        }else if(lastLocation.getProvider().equals("fused")){
    		            mSignalTypeIndicator   = getResources().getDrawable(R.drawable.mobile);
    		        }
    		        
    		        if(mSignalTypeIndicator != null){
    		            mSignalTypeIndicator.setBounds(0,0,17,17);
    		            canvas.translate(-(mSignalTypeIndicator.getBounds().width()), 0);
    		            mSignalTypeIndicator.draw(canvas);
    		        }
    		        canvas.restoreToCount(saveCanvas.pop());
    		        
    		        // Draw time of measurement
    		        p.setColor(Color.RED);
    		        Date date = new Date(lastLocation.getTime());
    		        canvas.drawText(String.format("%d:%02d", date.getHours(), date.getMinutes()), mPositionIcon.getBounds().width(), mPositionIcon.getBounds().height(), p);
    		        
    		        canvas.restoreToCount(saveCanvas.pop());


    		        // Draw positions lines and past positions accuracy circles
    		        if((mLastLocationsCount > 1) && (listLocationsSynchr.size() > 1)){
    		            Point punktTmp0 = new Point();
    		            Point punktTmp = new Point();
    		            Location locationTmp0;
    		            Location locationTmp;
    		            GeoPoint positionTmp0;
    		            GeoPoint positionTmp;
    		            for(int i = 1; i < listLocationsSynchr.size(); i++){
    		                if(i >= mLastLocationsCount) break;

    		                locationTmp0 = ((Location)listLocationsSynchr.get(i-1));
    		                locationTmp  = ((Location)listLocationsSynchr.get(i));
    		                positionTmp0 = new GeoPoint(
    		                        (int) (locationTmp0.getLatitude() * 1E6),
    		                        (int) (locationTmp0.getLongitude() * 1E6));
    		                positionTmp = new GeoPoint(
    		                        (int) (locationTmp.getLatitude() * 1E6),
    		                        (int) (locationTmp.getLongitude() * 1E6));

    		                mapView.getProjection().toPixels(positionTmp0, punktTmp0);
    		                mapView.getProjection().toPixels(positionTmp,  punktTmp);
    		                
    		                Paint p2 = new Paint();
    		                p2.setColor(Color.BLUE);
    		                p2.setAlpha(20);

    		                Paint p3 = new Paint();
    		                p3.setColor(Color.BLUE);
    		                
    		                saveCanvas.push(canvas.save());
    		                
    		                // past positions line
    		                canvas.drawLine(punktTmp.x, punktTmp.y, punktTmp0.x, punktTmp0.y, p3);
    		                
    		                canvas.translate(punktTmp.x, punktTmp.y);
    		                
    		                // Accuracy circle
    		                canvas.drawCircle(0, 0, 
    		                        (int)(locationTmp.getAccuracy() * calculateZoomToPixelMetersRatio(mapView.getZoomLevel())), 
    		                        p2);
    		                // middle point
    		                canvas.drawCircle(0, 0, 2, p3);
    		                
    	                    // Draw time of measurement
    	                    Date pastDate = new Date(locationTmp.getTime());
    	                    canvas.drawText(String.format("%d:%02d", pastDate.getHours(), pastDate.getMinutes()), 4, 0, p3);
    	                    
    		                
    		                canvas.restoreToCount(saveCanvas.pop());
    		            }
    		        }
    		    }
    		}
    	}
	}
	
    double calculateZoomToPixelMetersRatio(int zoom){
    	// At zoom level 1 (fully zoomed out), the equator of the earth is 256 pixels long
    	double earthEquatorInPix = 256/2;
    	double earthEquatorInMeters = 44000000; // 44Mm
    	double ratio = (earthEquatorInPix * Math.pow(2, zoom)) / earthEquatorInMeters;
		return ratio;
    }	
    
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mIsReceiverRegistered){
			mTimer.cancel();
			unregisterReceiver(mReceiver);
			mIsReceiverRegistered = false;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mLastLocationsCount = Integer.valueOf(
		        mDefaultSharedPrefs.getString(SettingsActivity.PREFERENCE_KEY_LAST_LOCATIONS_CNT, "1"));

		if(!Connectivity.isOnline(this)){
//			mTimer.cancel();
//			unregisterReceiver(mReceiver);
//			mIsReceiverRegistered = false;
			
        	new AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("No active Internet connection available")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                	finish();
                }
             })
             .show();
		}else{
			if(!mIsReceiverRegistered){
				registerReceiver(mReceiver, new IntentFilter(Const.ACTION_LOCATION));
				
				if(BD_POOLING_ENABLED && mSharedPrefs.getBoolean(Const.POOLING_ENABLED, false)){
					mTimer = new Timer();
					mTimer.schedule(new PeriodicTask(), 0);
				}
				mIsReceiverRegistered = true;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location_receiver, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		action_settings
//		startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
		startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    	
    	return true;
	}   
	

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}

}
