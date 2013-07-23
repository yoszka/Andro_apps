package com.example.mytrace;

import java.util.Date;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MainActivity extends MapActivity  {
	protected static final String TAG = "MyTrace";
	private static final int MAX_HISTORY_SIZE = 60;
	private static final int MAX_ALPHA = 255;
//	private UiLogger mLogger;
	MapView mapView;
	private Drawable mPositionIcon; 
	private Drawable mSignalTypeIndicator; 
	
	private Point punkt = new Point();
	private Point punkt1 = new Point();
	private Point punkt2 = new Point();
	private GeoPoint mCurrentPos = new GeoPoint(52198000, 18923080);
	private Location mCurrentLocation = new Location(LocationManager.NETWORK_PROVIDER);
	private MapController mc;
	LimitedQueue<GeoPoint> locationHistory = new LimitedQueue<GeoPoint>(MAX_HISTORY_SIZE); 

	private LocationClient mLocationClient;

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
			setContentView(R.layout.activity_main);
			prepareMap();
		}
		
	}
	
	
	@Override
	protected void onResume() {
	    super.onResume();
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
	    	startLocationTracking();
	    }
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		stopLocationTracking();
	}
	
	void prepareMap(){

		// Prepare MapView
		mapView = (MapView) findViewById(R.id.mapView1);
		mapView.setEnabled(true);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getOverlays().add(new MapOverlay());

		mc = mapView.getController();
		mc.setZoom(7);
		mc.animateTo(mCurrentPos);

		mPositionIcon = getResources().getDrawable(R.drawable.ic_launcher);
		mPositionIcon.setBounds(0,0,30,30);
		
		mSignalTypeIndicator   = getResources().getDrawable(R.drawable.antenna);
		mSignalTypeIndicator.setBounds(0,0,17,17);
//		mSignalTypeIndicatorSatellite = getResources().getDrawable(R.drawable.satellite);
	}
	
	void navigateToPoint(Location location){
		mCurrentLocation = location;
		mCurrentPos = new GeoPoint(
				(int) (location.getLatitude() * 1E6),
				(int) (location.getLongitude() * 1E6));
		locationHistory.add(mCurrentPos);
		mc.animateTo(mCurrentPos);
	}
	
	class MapOverlay extends Overlay{

		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			if(shadow) return;
			Stack<Integer> saveCanvas = new Stack<Integer>();

			
			
			// Draw location history line
			saveCanvas.push(canvas.save());
			if(locationHistory.size() > 1){
				Paint pLine = new Paint();
				pLine.setColor(Color.BLUE);
				int alphaInterval = MAX_ALPHA / locationHistory.size();
				
				for(int i = 1; i < locationHistory.size(); i++){
					mapView.getProjection().toPixels(locationHistory.get(i-1), punkt1);
					mapView.getProjection().toPixels(locationHistory.get(i),   punkt2);
					
					// Calculate alpha for line (older point is more transparent)
					pLine.setAlpha(255 - (locationHistory.size()-(i+1))*alphaInterval);
					pLine.setStyle(Paint.Style.STROKE);
					pLine.setStrokeWidth(3.0f);
					pLine.setStrokeCap(Paint.Cap.ROUND);
					canvas.drawLine(punkt1.x, punkt1.y, punkt2.x, punkt2.y, pLine);
				}
			}
			canvas.restoreToCount(saveCanvas.pop());
			
			
			// Draw current location related stuffs
			mapView.getProjection().toPixels(mCurrentPos, punkt);

			saveCanvas.push(canvas.save());
			canvas.translate(punkt.x - (mPositionIcon.getBounds().width()/2), punkt.y - (mPositionIcon.getBounds().height()/2));

			// Draw circle of accuracy
			Paint p = new Paint();
			p.setColor(Color.RED);
			p.setAlpha(70);
			canvas.drawCircle((mPositionIcon.getBounds().width()/2), (mPositionIcon.getBounds().height()/2), (int)(mCurrentLocation.getAccuracy() * calculateZoomToPixelMetersRatio(mapView.getZoomLevel())), p);

			// Draw location icon
			mPositionIcon.draw(canvas);

			// Draw accuracy value
			if(mCurrentLocation.getAccuracy() > 0){
				p.setColor(Color.BLACK);
				p.setAlpha(255);
				canvas.drawText(String.format("%.1f", mCurrentLocation.getAccuracy()), mPositionIcon.getBounds().width(), 0, p);
			}
			
			// Draw signal type icon
			saveCanvas.push(canvas.save());
			mSignalTypeIndicator = null;
			if(mCurrentLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
				mSignalTypeIndicator   = getResources().getDrawable(R.drawable.antenna);
			}else if(mCurrentLocation.getProvider().equals(LocationManager.GPS_PROVIDER)){
				mSignalTypeIndicator   = getResources().getDrawable(R.drawable.satellite);
			}else if(mCurrentLocation.getProvider().equals("fused")){
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
			Date date = new Date(mCurrentLocation.getTime());
			canvas.drawText(String.format("%d:%02d", date.getHours(), date.getMinutes()), mPositionIcon.getBounds().width(), mPositionIcon.getBounds().height(), p);

			canvas.restoreToCount(saveCanvas.pop());
		}
	}
	
	double calculateZoomToPixelMetersRatio(int zoom){
		// At zoom level 1 (fully zoomed out), the equator of the earth is 256 pixels long
		double earthEquatorInPix = 256/2;
		double earthEquatorInMeters = 44000000; // 44Mm
		double ratio = (earthEquatorInPix * Math.pow(2, zoom)) / earthEquatorInMeters;
		return ratio;
	}	
	
	public void onClickTest1(View v){
		startLocationTracking();
	}
	
	public void onClickTest2(View v){
		mLocationClient.disconnect();
	}

	int startLocationTracking() {
		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (result == ConnectionResult.SUCCESS) {
	    	log("ConnectionResult.SUCCESS");
	        mLocationClient = new LocationClient(this, mConnectionCallbacks, mConnectionFailedListener);
	        mLocationClient.connect();
	    }else{
	    	log("ConnectionResult.FAIL");
	    }
	    return result;
	}
	
	void stopLocationTracking(){
		if(mLocationClient != null){
			mLocationClient.disconnect();
		}
	}
	
	private ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks() {

	    @Override
	    public void onDisconnected() {
	    	log("DISCONNECTED");
	    }

	    @Override
	    public void onConnected(Bundle arg0) {
	    	log("CONNECTED");
	        LocationRequest locationRequest = LocationRequest.create();
//	        locationRequest.setFastestInterval(0);
	        locationRequest.setInterval(10*1000) // 10s
	        			   .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	        mLocationClient.requestLocationUpdates(locationRequest, mLocationListener);
	    }
	};

	private OnConnectionFailedListener mConnectionFailedListener = new OnConnectionFailedListener() {

	    @Override
	    public void onConnectionFailed(ConnectionResult arg0) {
	        log("ConnectionFailed");
	    }
	};

	private LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			log("LAT: " +location.getLatitude() + ", LON: "+ location.getLongitude()+ "\n");
			navigateToPoint(location);
		}
	};
	
	
	
	
	
	// **************************************
    private class UiLogger{
        int windweResourceId;
        String mLogContent = "";
        TextView tv;
        Handler handler = new Handler();
        
        public UiLogger(Activity activity, int windowResourceId) {
            tv = (TextView) activity.findViewById(windowResourceId);        
        }
        
        public void clear(){
            setText("");
        }
        
        public String setText(final String text){
            handler.post(new Runnable() {
                
                @Override
                public void run() {
                    mLogContent = text;
                    tv.setText(mLogContent);
                }
            });

            return text;
        }
        
        /**
         * Add text in these same line
         * @param text
         * @return
         */
        public String addTextl(String text){
            mLogContent += text;
            setText(mLogContent);
            
            return mLogContent;
        }
        
        /**
         * Add text in new line bottom
         * @param text
         * @return
         */
        public String addTextBn(String text){           
            return addTextl("\n" +text);
        }
        
        /**
         * Add text in new line top
         * @param text
         * @return
         */
        public String addTextTn(String text){
            return setText(text + "\n" + mLogContent);
        }
        
    }
    
    
    void log(String text){
//        mLogger.addTextTn(text);
        Log.d(TAG, text);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}

}
