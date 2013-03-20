package pl.xt.jokii.locationreceiver;

import java.util.Date;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

/**
 * @author Tomek
 * adb shell am broadcast -a pl.xt.jokii.locationreceiver.LOCATION -e extras_key extras_string_value
 * adb shell am broadcast -a pl.xt.jokii.locationreceiver.LOCATION -e location_lat 51.1151300 -e location_lon 16.9506200 -e location_acc 100.0 -e location_provider network -e location_timestamp 1363388511000
 */
public class LocationReceiver extends MapActivity {
	private boolean mIsReceiverRegistered = false;
	private BroadcastReceiver mReceiver; 
	private final static String RECEIVER_ACTION = "pl.xt.jokii.locationreceiver.LOCATION";
	private Drawable mPositionIcon; 
	private Drawable time2; 
	private Drawable cancel; 
	private Point punkt = new Point();
	private GeoPoint mCurrentPos = new GeoPoint(52198000, 18923080);
	private Location mCurrentLocation = new Location(LocationManager.NETWORK_PROVIDER);
	private MapController mc;
	
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
	
	MapView mapView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
    	
    	time2 = getResources().getDrawable(R.drawable.time2);
    	cancel = getResources().getDrawable(R.drawable.cancel);
    	
    	time2.setBounds(0,0,time2.getIntrinsicWidth(),time2.getIntrinsicHeight());
    	cancel.setBounds(0,0,cancel.getIntrinsicWidth(),cancel.getIntrinsicHeight());    	
    	
	  mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			String tmp = null;
			
			double latitude = 0.0;
			if(extras.containsKey(KEY_LOCATION_LATITUDE)){
				tmp = extras.getString(KEY_LOCATION_LATITUDE);
				latitude = Double.parseDouble(tmp);
			}
			
			double longitude = 0.0;
			if(extras.containsKey(KEY_LOCATION_LONGITUDE)){
				tmp = extras.getString(KEY_LOCATION_LONGITUDE);
				longitude = Double.parseDouble(tmp);
			}
			
			float accuracy = 0;
			if(extras.containsKey(KEY_LOCATION_ACCURACY)){
				tmp = extras.getString(KEY_LOCATION_ACCURACY);
				accuracy = Float.parseFloat(tmp);
			}
			
			String provider = null;
			if(extras.containsKey(KEY_LOCATION_PROVIDER)){
				provider = extras.getString(KEY_LOCATION_PROVIDER);
			}
			
			long timestamp = 0;
			if(extras.containsKey(KEY_LOCATION_TIMESTAMP)){
				tmp = extras.getString(KEY_LOCATION_TIMESTAMP);
				timestamp = Long.parseLong(tmp);
			}
			
			Toast.makeText(getApplicationContext(), "RECEIVE LAT: " + latitude + ", LON: " + longitude + ", ACC: " + accuracy + ", PROV: " + provider + ", T: " + timestamp + ", t: " + new Date(timestamp).toString(), Toast.LENGTH_SHORT).show();
			
			mCurrentLocation.setLatitude(latitude);
			mCurrentLocation.setLongitude(longitude);
			mCurrentLocation.setAccuracy(accuracy);
			mCurrentLocation.setProvider(provider);
			mCurrentLocation.setTime(timestamp);
			
			mCurrentPos = new GeoPoint(
								    	(int) (latitude * 1E6),
								    	(int) (longitude * 1E6));
    		mc.animateTo(mCurrentPos);
		}
	  };

	  registerReceiver(mReceiver, new IntentFilter(RECEIVER_ACTION)); 
	  mIsReceiverRegistered = true;
	}
	
	class MapOverlay extends Overlay{
    	
    	public void draw(Canvas canvas, MapView mapView, boolean shadow) {        		
    		super.draw(canvas, mapView, shadow);
    		if(shadow) return;
    		
			mapView.getProjection().toPixels(mCurrentPos, punkt);
			
			canvas.save();
			canvas.translate(punkt.x - (mPositionIcon.getBounds().width()/2), punkt.y - (mPositionIcon.getBounds().height()/2));
			
			Paint p = new Paint();
			p.setColor(Color.RED);
			p.setAlpha(70);
			canvas.drawCircle((mPositionIcon.getBounds().width()/2), (mPositionIcon.getBounds().height()/2), (int)(mCurrentLocation.getAccuracy() * calculateZoomToPixelMetersRatio(mapView.getZoomLevel())), p);
			
			mPositionIcon.draw(canvas);
			
			if(mCurrentLocation.getAccuracy() > 0){
				p.setColor(Color.BLACK);
				p.setAlpha(255);
				canvas.drawText(String.format("%.1f", mCurrentLocation.getAccuracy()), mPositionIcon.getBounds().width(), 0, p);
				
				p.setColor(Color.RED);
				Date date = new Date(mCurrentLocation.getTime());
				canvas.drawText(String.format("%d:%02d", date.getHours(), date.getMinutes()), mPositionIcon.getBounds().width(), mPositionIcon.getBounds().height(), p);
			}
			
//			switch(mState){
//			case IDLE:
//			case SEARCHING:
//				canvas.translate(ikona.getBounds().width(), -time2.getBounds().height());
//				time2.draw(canvas);
//				break;
//				
//			case SEARCHING_FAIL:
//				canvas.translate(ikona.getBounds().width(), -cancel.getBounds().height());
//				cancel.draw(canvas);
//				break;
//				
//			case SEARCHING_SUCCESS:
//    			if(positionAccuracy > 0){
//    				p.setColor(Color.BLACK);
//    				p.setAlpha(255);
//    				canvas.drawText(String.format("%.1f", positionAccuracy), ikona.getBounds().width(), 0, p);
//    			}
////        			else{
////        				canvas.translate(ikona.getBounds().width(), -time2.getBounds().height());
////        				time2.draw(canvas);
////        			}
//    			break;
//			}

			
			canvas.restore();
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
			unregisterReceiver(mReceiver);
			mIsReceiverRegistered = false;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(!mIsReceiverRegistered){
			  registerReceiver(mReceiver, new IntentFilter(RECEIVER_ACTION)); 
			  mIsReceiverRegistered = true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location_receiver, menu);
		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}

}
