package pl.xt.jokii.eventannouncement;
 
import static pl.xt.jokii.pushnotifications.CommonUtilities.SENDER_ID;

import java.util.Set;

import pl.xt.jokii.eventannouncement.R;
import pl.xt.jokii.pushnotifications.CommonUtilities;
import pl.xt.jokii.pushnotifications.ServerUtilities;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
 
public class GCMIntentService extends GCMBaseIntentService {
 
	private static final String TAG = "GCMIntentService";
    public static OnUnregisteredListener onUnregisteredListener;
 
    public GCMIntentService() {
        super(SENDER_ID);
    }
 
    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        ServerUtilities.register(context, RegisterActivity.name, RegisterActivity.email, registrationId);
        if(RegisterActivity.onRegisterListener != null){
        	RegisterActivity.onRegisterListener.onRegistered();
        	RegisterActivity.onRegisterListener = null;
        }
    }
 
    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        if(onUnregisteredListener != null){
        	onUnregisteredListener.onUnregistered();
        	onUnregisteredListener = null;
        }else{
        	ServerUtilities.unregister(context, registrationId, null);
        }
    }
 
    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
//        String message = intent.getExtras().getString("price");
 
        // notifies user
        generateNotification(context, intent.getExtras());
    }
 
    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
//        String message = getString(R.string.gcm_deleted, total);
        // notifies user
//        generateNotification(context, message);
    }
 
    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }
 
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }
 
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, Bundle message) {
    	if(message != null){
    		String token = message.getString(CommonUtilities.EXTRA_TOKEN);
    		
    		if((token != null) && (token.equals(CommonUtilities.TOKEN_EVENT))){
//    		if(true){
    			int icon = R.drawable.ic_launcher_main;
    			long when = System.currentTimeMillis();
    			String msg   = message.getString(CommonUtilities.EXTRA_MESSAGE);
//    			String msg   = message.getString("price");
    			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    			Notification notification = new Notification(icon, "Event", when);
//    			
    			String title = context.getString(R.string.app_name);
//    			
//    			Intent notificationIntent = new Intent(context, MainActivity.class);
    			Intent notificationIntent = new Intent(context, MainScreen.class);
//    			
//    			
    			notificationIntent.putExtra(CommonUtilities.EXTRA_TOKEN,   CommonUtilities.TOKEN_EVENT);
    			notificationIntent.putExtra(CommonUtilities.EXTRA_MESSAGE, msg);
//    			notificationIntent.putExtra(CommonUtilities.EXTRA_LOCATION_BUNDLE, message);
//    			
//    			// set intent so it does not start a new activity
    			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    			PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    			notification.setLatestEventInfo(context, title, msg, intent);
    			notification.flags |= Notification.FLAG_AUTO_CANCEL;
//    			
//    			// Play default notification sound
//    			notification.defaults |= Notification.DEFAULT_SOUND;
    			playLocalAudio(context);
//    			
//    			// Vibrate if vibrate is enabled
    			notification.defaults |= Notification.DEFAULT_VIBRATE;
    			notificationManager.notify(0, notification);  
    			
    			
    	        
//    	        if(message != null){
//    	        	
////    	        	Set<String> keys = message.keySet();
////    	        	for(String key : keys){
////    	        		Log.v("KLUCZE", key+"");
////    	        	}
//    	        	
////    	        	StringBuilder sb = new StringBuilder();
////    	        	sb.append(message + "\n");
////    	        	sb.append( message.getString(CommonUtilities.KEY_LOCATION_LATITUDE)+ "\n");
////    	        	sb.append( message.getString(CommonUtilities.KEY_LOCATION_LONGITUDE)+ "\n");
////    	        	sb.append( message.getString(CommonUtilities.KEY_LOCATION_ACCURACY)+ "\n");
////    	        	sb.append( message.getString(CommonUtilities.KEY_LOCATION_PROVIDER)+ "\n");
////    	        	sb.append( message.getString(CommonUtilities.KEY_LOCATION_TIMESTAMP)+ "\n");
//    	        	
//    	        	Intent locationIntent = new Intent("pl.xt.jokii.locationreceiver.LOCATION");
//    	        	locationIntent.putExtra(CommonUtilities.EXTRA_MESSAGE, 	message.getString(CommonUtilities.EXTRA_MESSAGE) +"");
//    	        	context.sendBroadcast(locationIntent);
//    	        }
    			
    		}
    	}
 
    }
    
	private static void playLocalAudio(Context context) {
		final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
			
			// Read current levels
			final int volumeMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			final int volumeNotif = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);		
			
			// read max levels
			final int maxVolumeMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			final int maxVolumeNotif = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
			
			// set volume for stream music same as for notification
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volumeNotif * maxVolumeMusic) / maxVolumeNotif, 0);
			
//			Log.v("ALARM volumeMusic", ((volumeNotif * maxVolumeMusic) / maxVolumeNotif)+"");
//			Log.v("ALARM volumeNotif", volumeNotif+"");			
			
			MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.sdwin);
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				public void onCompletion(MediaPlayer mp) {
					// Get back to orginal volume level
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeMusic, 0);
//					Log.v("ALARM volumeMusic", volumeMusic+"");
//					Log.v("ALARM volumeNotif", volumeNotif+"");					
				}
			});
		}	
	}
 
}