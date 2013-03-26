package pl.xt.jokii.pushnotification;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class GcmService extends Service{
	private static final String APP_SHARED_PREFS = "registrationID";
	private SharedPreferences sharedPrefs;
	private Editor prefsEditor;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		prefsEditor = sharedPrefs.edit();
	}
	
	private void setRegistratioId(String registrationId){
		prefsEditor.putString(APP_SHARED_PREFS, registrationId);
		prefsEditor.commit();
	}
	
	private String getRegistratioId(){
        return sharedPrefs.getString(APP_SHARED_PREFS, "");
    }

	private static PowerManager.WakeLock sWakeLock;
	private static final Object LOCK = GcmService.class;
	private static final String TAG = "GcmService";
	
	static void runIntentInService(Context context, Intent intent) {
        synchronized(LOCK) {
            if (sWakeLock == null) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "my_wakelock");
            }
        }
        sWakeLock.acquire();
        intent.setClassName(context, GcmService.class.getName());
        context.startService(intent);
    }
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
      try {
    	  String action = intent.getAction();
      if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
    	  Toast.makeText(getApplicationContext(), "Handle UN/REGISTRATION", Toast.LENGTH_LONG).show();
          handleRegistration(intent);
      } else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
//          handleMessage(intent);
    	  Toast.makeText(getApplicationContext(), "Handle RECEIVE", Toast.LENGTH_LONG).show();
      }
	  } finally {
	      synchronized(LOCK) {
	          sWakeLock.release();
	      }
	  }
		return Service.START_STICKY;
	}
	
	
	private void handleRegistration(Intent intent) {
	    String registrationId = intent.getStringExtra("registration_id");
	    String error = intent.getStringExtra("error");
	    String unregistered = intent.getStringExtra("unregistered");       
	    // registration succeeded
	    if (registrationId != null) {
	        // store registration ID on shared preferences
	        // notify 3rd-party server about the registered ID
	    	Toast.makeText(getApplicationContext(), "registration_id: " + registrationId, Toast.LENGTH_LONG).show();
	    	setRegistratioId(registrationId);
	    }
	        
	    // unregistration succeeded
	    if (unregistered != null) {
	        // get old registration ID from shared preferences
	        // notify 3rd-party server about the unregistered ID
	    	Toast.makeText(getApplicationContext(), "unregistered: " + unregistered, Toast.LENGTH_LONG).show();
	    } 
	        
	    // last operation (registration or unregistration) returned an error;
	    if (error != null) {
	    	Toast.makeText(getApplicationContext(), "error: " + error, Toast.LENGTH_LONG).show();
	        if ("SERVICE_NOT_AVAILABLE".equals(error)) {
	           // optionally retry using exponential back-off
	           // (see Advanced Topics)
	        } else {
	            // Unrecoverable error, log it
	            Log.i(TAG, "Received error: " + error);
	        }
	    }
	}
	
	private void handleMessage(Intent intent) {
	    // server sent 2 key-value pairs, score and time
	    String score = intent.getStringExtra("score");
	    String time = intent.getStringExtra("time");
	    // generates a system notification to display the score and time
	    Toast.makeText(getApplicationContext(), "score: " + score +  ", time: " + time, Toast.LENGTH_LONG).show();
	}
    	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
