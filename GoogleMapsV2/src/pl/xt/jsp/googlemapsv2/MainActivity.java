package pl.xt.jsp.googlemapsv2;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity  {
    private final LatLng LOCATION = new LatLng(52.2514800, 21.0356000);
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        map  = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        map.addMarker(new MarkerOptions().position(LOCATION).title("I'm here"));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION, 10);
        map.animateCamera(update);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int mapType = GoogleMap.MAP_TYPE_NONE;

        switch (item.getItemId()) {
        case R.id.map_normal:
            mapType = GoogleMap.MAP_TYPE_NORMAL;
            break;
        case R.id.map_satellite:
            mapType = GoogleMap.MAP_TYPE_SATELLITE;
            break;
        case R.id.map_hybrid:
            mapType = GoogleMap.MAP_TYPE_HYBRID;
            break;
        case R.id.map_terrain:
            mapType = GoogleMap.MAP_TYPE_TERRAIN;
            break;
        default:
            return super.onOptionsItemSelected(item);
        
        }
        map.setMapType(mapType);
        return true;
        
        
    }

}
