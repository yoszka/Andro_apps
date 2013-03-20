package pl.xt.jokii;

import java.util.List;
import pl.xt.jokii.MyLocation.LocationResultListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/*

Certificate fingerprint (MD5): F2:CD:AD:5F:2C:20:46:00:88:77:E2:90:41:C3:64:C9
MAP_KEY v1: 0ykAwHWMyyAgz7PGdpNZIfrZhP1GR5ZSD74p7jg

*/

public class MapaActivity extends MapActivity {
	
	int[] colors = new int[]{Color.RED, Color.BLUE};
	int colorIndex = 0;
    
	Object synchronizer = new Object();
	MapView mapView;
	GeoPoint geoPoint = new GeoPoint(51110000, 17000000);
	GeoPoint mCurrentPos = new GeoPoint(51115130, 16950620);
	float positionAccuracy;
//	GeoPoint mCurrentPos = new GeoPoint(51125130, 16950620);
	MapController mc;
	GeoPoint p;
	List<GeoPoint> trasa;
	TextView text;
	Handler h = new Handler();
	private enum LocationFindProgress{
		IDLE, SEARCHING, SEARCHING_FAIL, SEARCHING_SUCCESS
	}
	private LocationFindProgress mState = LocationFindProgress.IDLE;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	
    	
    	text = (TextView) findViewById(R.id.textView);
//    	mapView = new MapView(this, "0ykAwHWMyyAgz7PGdpNZIfrZhP1GR5ZSD74p7jg");
    	mapView = (MapView) findViewById(R.id.mapView1);
    	mapView.setEnabled(true);
    	mapView.setClickable(true);
    	mapView.setBuiltInZoomControls(true);
    	
    	mc = mapView.getController();
    	
    	//---navigate to a point first---        
    	String coordinates[] = {"51.1151300", "16.9506200"};
    	double lat = Double.parseDouble(coordinates[0]);
    	double lng = Double.parseDouble(coordinates[1]);
    	p = new GeoPoint(
				    	(int) (lat * 1E6),
				    	(int) (lng * 1E6));
    	mc.animateTo(p);
    	mc.setZoom(14);
    	
    	final Drawable ikona = getResources().getDrawable(R.drawable.ic_launcher);
    	final Drawable time2 = getResources().getDrawable(R.drawable.time2);
    	final Drawable cancel = getResources().getDrawable(R.drawable.cancel);
    	
    	final Point punkt = new Point();
    	ikona.setBounds(0,0,30,30);

    	time2.setBounds(0,0,time2.getIntrinsicWidth(),time2.getIntrinsicHeight());
    	cancel.setBounds(0,0,cancel.getIntrinsicWidth(),cancel.getIntrinsicHeight());
    	
    	
    	
    	// Get self location
//    	lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//    	locationListener = new MyLocationListener();
//    	lm.requestLocationUpdates(
//    			LocationManager.GPS_PROVIDER,
//    			2000,
//    			1,
//    			locationListener);
    	
//		lm.requestLocationUpdates(
//				LocationManager.NETWORK_PROVIDER,
//				2000,
//				1,
//				locationListener);
		
//		Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		if(loc != null){
//			int latitide = (int)(loc.getLatitude()* 1E6);
//			int longitude = (int)(loc.getLongitude() * 1E6);
//			mCurrentPos = new GeoPoint(latitide, longitude);
//			Log.v("MAP", "getLastKnownLocation. LAT: [" + latitide + "], LON: [" + longitude + "], accuracy: [" + loc.getAccuracy() + "]");
//		}
    	
    	
    	
        mapView.getOverlays().add(new Overlay() {
        	@Override
        	public boolean onTap(GeoPoint p, MapView mapView) {
        		geoPoint = p;
				return true;
        	};
        	
        	@Override
        	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
        			long when) {
        		super.draw(canvas, mapView, shadow, when);
        		return true;
        	}
        	
        	public void draw(Canvas canvas, MapView mapView, boolean shadow) {        		
        		super.draw(canvas, mapView, shadow);
        		if(shadow) return;
        		
        		
        		
        		/*T
        		// Wyswietlenie listy punktow (mozna udoskonalis laczac poszczegolne punkty aby stworzyly ciagla trase)
        		//for(int i = 0; i < trasa.size(); i++)
        		for(GeoPoint gp : trasa)
        		{
        			//GeoPoint from = trasa.get(i-1);
        			//GeoPoint to = trasa.get(i);
        			
        			//mapa.getProjection().toPixels(from, punkt);
        			mapa.getProjection().toPixels(gp, punkt);
//        			Log.v("MAP", "gp. Latitude: [" + gp.getLatitudeE6() + "], Longitude: [" + gp.getLongitudeE6() + "]");
        			canvas.save();
        			canvas.translate(punkt.x, punkt.y);
//        			Log.v("MAP", "punkt. x: [" + punkt.x + "], y: [" + punkt.y + "]");
        			ikona.draw(canvas);
        			canvas.restore();
        			
        		}
        		*/
        		
        		
        		synchronized (synchronizer) {

        			mapView.getProjection().toPixels(mCurrentPos, punkt);
        			canvas.save();
        			canvas.translate(punkt.x - (ikona.getBounds().width()/2), punkt.y - (ikona.getBounds().height()/2));
        			
//        			calculateZoomToPixelMetersRatio(mapView.getZoomLevel());
//        			positionAccuracy = 50; // 100 m
        			
        			Paint p = new Paint();
        			p.setColor(Color.RED);
        			p.setAlpha(70);
        			canvas.drawCircle((ikona.getBounds().width()/2), (ikona.getBounds().height()/2), (int)(positionAccuracy * calculateZoomToPixelMetersRatio(mapView.getZoomLevel())), p);

        			ikona.draw(canvas);
        			
        			switch(mState){
        			case IDLE:
        			case SEARCHING:
        				canvas.translate(ikona.getBounds().width(), -time2.getBounds().height());
        				time2.draw(canvas);
        				break;
        				
        			case SEARCHING_FAIL:
        				canvas.translate(ikona.getBounds().width(), -cancel.getBounds().height());
        				cancel.draw(canvas);
        				break;
        				
        			case SEARCHING_SUCCESS:
            			if(positionAccuracy > 0){
            				p.setColor(Color.BLACK);
            				p.setAlpha(255);
            				canvas.drawText(String.format("%.1f", positionAccuracy), ikona.getBounds().width(), 0, p);
            			}
//            			else{
//            				canvas.translate(ikona.getBounds().width(), -time2.getBounds().height());
//            				time2.draw(canvas);
//            			}
            			break;
        			}

        			
        			canvas.restore();
				}
        		
//        		51.1151300°N, 16.9506200°E
        		        		
        		// wyswietlanie ikony w danych wspolrzednych geograficznych
        		//mapa.getProjection().toPixels(geoPoint, punkt);
        		//canvas.translate(punkt.x, punkt.y);
        		//ikona.draw(canvas);
        	}
		});
        

        
        LocationResultListener locationResult = new LocationResultListener(){
            @Override
            public void gotLocation(Location loc){
            	if(loc != null){
            		mState = LocationFindProgress.SEARCHING_SUCCESS;
//    			Toast.makeText(getBaseContext(),
//    					"Location changed : Lat: " + loc.getLatitude() +
//    					" Lng: " + loc.getLongitude() + ", Acc: " + loc.getAccuracy(),
//    					Toast.LENGTH_SHORT).show();
            		
            		p = new GeoPoint(
            				(int) (loc.getLatitude() * 1E6),
            				(int) (loc.getLongitude() * 1E6));
            		synchronized (synchronizer) {
            			mCurrentPos = p;
            		}
            		mc.animateTo(p);
            		
            		Log.v("MAP", "onLocationChanged. LAT: [" + loc.getLatitude() + "], LON: [" + loc.getLongitude() + "], accuracy: [" + loc.getAccuracy() + "]");
            		final double LAT = loc.getLatitude();
            		final double LON = loc.getLongitude();
            		final float ACC = loc.getAccuracy();
            		positionAccuracy = ACC;
            		h.post(new Runnable() {
						@Override
						public void run() {
							text.setTextColor(colors[(colorIndex++)%(colors.length)]);
							text.setText("LAT:[" + LAT + "],\nLON:[" + LON + "],\nacc:[" + ACC + "]");
						}
					});
//    		mc.setZoom(13);
//    		mapView.invalidate();
            	}else{
            		mState = LocationFindProgress.SEARCHING_FAIL;
            		mapView.invalidate();
            		Log.v("MAP", "No location found");
            	}
            }
        };
        
        MyLocation myLocation = new MyLocation();
        mState = LocationFindProgress.SEARCHING;
//        mState = LocationFindProgress.SEARCHING_FAIL;
        boolean triggerSuccess = myLocation.getLocation(this, locationResult);
        
        if(!triggerSuccess){
        	new AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("None of location provider enabled")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                	MapaActivity.this.finish();
                }
             })
             .show();
        }
        
    	
        
    }
    
    
    double calculateZoomToPixelMetersRatio(int zoom){
    	// At zoom level 1 (fully zoomed out), the equator of the earth is 256 pixels long
    	double earthEquatorInPix = 256/2;
    	double earthEquatorInMeters = 44000000; // 44Mm
    	double ratio = (earthEquatorInPix * Math.pow(2, zoom)) / earthEquatorInMeters;
		return ratio;
    }
    
    
    
//    public class MyLocationListener implements LocationListener {
//
//    	@Override
//    	public void onLocationChanged(Location loc) {
//    		if (loc != null) {
//    			Toast.makeText(getBaseContext(),
//    					"Location changed : Lat: " + loc.getLatitude() +
//    					" Lng: " + loc.getLongitude() + ", Acc: " + loc.getAccuracy(),
//    					Toast.LENGTH_SHORT).show();
//    			
//    			p = new GeoPoint(
//    					(int) (loc.getLatitude() * 1E6),
//    					(int) (loc.getLongitude() * 1E6));
//    			synchronized (synchronizer) {
//    				mCurrentPos = p;
//    				positionAccuracy = loc.getAccuracy();
//    			}
//    			mc.animateTo(p);
//    			Log.v("MAP", "onLocationChanged. LAT: [" + loc.getLatitude() + "], LON: [" + loc.getLongitude() + "], accuracy: [" + positionAccuracy + "]");
//    			
//    		}
////    		mc.setZoom(13);
////    		mapView.invalidate();
//    	}
//    	@Override
//    	public void onProviderDisabled(String provider) {
//    	}
//    	@Override
//    	public void onProviderEnabled(String provider) {
//    	}
//    	@Override
//    	public void onStatusChanged(String provider, int status,
//    			Bundle extras) {
//    	}
//
//    }    
    
    

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean isLocationDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}