package pl.xt.jokii.mobiledataenabler;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener {
    private static final int MOBILE_CONNECTION = 0;
    private static final int WIFI_CONNECTION   = 1;
    ToggleButton toggleButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		gpsEnabler();
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        toggleButton.setOnClickListener(this);
        toggleButton.setChecked(isMobileDataEnabled());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    @Override
    public void onClick(View v) {
        boolean newState = !isMobileDataEnabled();
        toggleButton.setChecked(newState);
        try {
            setMobileDataEnabled(this, newState);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void setMobileDataEnabled(Context context, boolean enabled) throws ClassNotFoundException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled",
                Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
    }


    boolean isMobileDataEnabled() {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo == null) {
            return false;
        }

        int type = networkInfo.getType();
        String typeName = networkInfo.getTypeName();
        boolean connected = networkInfo.isConnected();
        Log.i("isMobileDataEnabled", "type: " + type);
        Log.i("isMobileDataEnabled", "typeName: " + typeName);
        Log.i("isMobileDataEnabled", "connected: " + connected);
        return type == MOBILE_CONNECTION;
    }
//	private void gpsEnabler(){
////		Settings.Secure.putString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, "network,gps");
//		final Intent poke = new Intent();
//	    poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
//	    poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
//	    poke.setData(Uri.parse("3"));
//	    sendBroadcast(poke);
//	}

}
