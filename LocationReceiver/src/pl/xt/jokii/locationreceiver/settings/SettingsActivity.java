/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.xt.jokii.locationreceiver.settings;

import com.google.android.gcm.GCMRegistrar;

import pl.xt.jokii.locationreceiver.OnUnregisteredListener;
import pl.xt.jokii.locationreceiver.R;
import pl.xt.jokii.locationreceiver.RegisterActivity;
import pl.xt.jokii.pushnotifications.ServerUtilities;
import pl.xt.jokii.pushnotifications.server.model.User;
import pl.xt.jokii.pushnotifications.server.util.OnUsersGetListener;
import pl.xt.jokii.pushnotifications.server.util.UsersGetter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnUsersGetListener {

	private static final CharSequence PREFERENCE_KEY_REGISTRATION 		= "register_singup";
	private static final CharSequence PREFERENCE_KEY_LOG_OUT 			= "log_out";
	private static final CharSequence PREFERENCE_KEY_LOCALIZATION_NBR 	= "localized_phone_number";
	private static final CharSequence PREFERENCE_KEY_LOCALIZATION_CNT 	= "locaization_count";
	private static final CharSequence PREFERENCE_KEY_START 				= "start";
    private static final CharSequence PREFERENCE_KEY_POOLING     		= "is_pooling_enabled";
    public  static final String       PREFERENCE_KEY_LAST_LOCATIONS_CNT = "last_locations_count";
    public  static final String       PREFERENCE_KEY_USER_TO_TRACK      = "preference_user_to_track";
    public static final String SENT_ACTION 		= "pl.xt.jokii.locationreceiver.TRACKING_SMS_SENT";
	public static final String DELIVERED_ACTION = "pl.xt.jokii.locationreceiver.TRACKING_SMS_DELIVERED";
	public static final int IDX_SMS_SENT_DELIVERED = 1684753;
	private static final String LOCALIZER_PREFERENCES = "LOCALIZER_PREFERENCES";
	private static final String POOLING_ENABLED = "POOLING_ENABLED";

    private PreferenceScreen mRegisterSingup;
    private PreferenceScreen mLogOut;
    private EditTextPreference mLocalizationPhoneNumber;
    private EditTextPreference mLocalizationCount;
    private EditTextPreference mLastLocationsCount;
    private EditTextPreference mUserToTrack;
    private CheckBoxPreference mPooling;
    private PreferenceScreen mStart;
    private PreferenceClckListener preferenceClickListener;
    private PreferenceChangeListener preferenceChangeListener;
    private Integer mMyID;
    private SmsDeliveredReceiver sentDeliveredReceiver = new SmsDeliveredReceiver();
    private boolean isSentDeliveredReceiverRegistered = false;
    
    private SharedPreferences sharedPrefs;
    private Editor prefsEditor;
    
    AlertDialog.Builder myAlertDialog;
    
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            updateSettingsAvaibility();
        };
    };

	@SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
		sharedPrefs = getSharedPreferences(LOCALIZER_PREFERENCES, Activity.MODE_WORLD_READABLE);
		prefsEditor = sharedPrefs.edit();
        
        preferenceClickListener = new PreferenceClckListener();
        preferenceChangeListener = new PreferenceChangeListener();
        
        // Category REGISTRATION
        mRegisterSingup 	= (PreferenceScreen) findPreference(PREFERENCE_KEY_REGISTRATION);
        mLogOut 			= (PreferenceScreen) findPreference(PREFERENCE_KEY_LOG_OUT);
        
        // Category LOCALIZER
        mPooling 					= (CheckBoxPreference) findPreference(PREFERENCE_KEY_POOLING);
        mLocalizationPhoneNumber 	= (EditTextPreference) findPreference(PREFERENCE_KEY_LOCALIZATION_NBR);
        mLocalizationCount 			= (EditTextPreference) findPreference(PREFERENCE_KEY_LOCALIZATION_CNT);
        mLastLocationsCount			= (EditTextPreference) findPreference(PREFERENCE_KEY_LAST_LOCATIONS_CNT);
        mUserToTrack                = (EditTextPreference) findPreference(PREFERENCE_KEY_USER_TO_TRACK);
        mStart 						= (PreferenceScreen)   findPreference(PREFERENCE_KEY_START);

        // Additional configuration
        mRegisterSingup.setOnPreferenceClickListener(preferenceClickListener);
        mLogOut.setOnPreferenceClickListener(preferenceClickListener);
        mPooling.setOnPreferenceChangeListener(preferenceChangeListener);
//        mPooling.setChecked(sharedPrefs.getBoolean(POOLING_ENABLED, false));
        mLocalizationPhoneNumber.setOnPreferenceChangeListener(preferenceChangeListener);
        mLocalizationCount.setOnPreferenceChangeListener(preferenceChangeListener);
        mLastLocationsCount.setOnPreferenceChangeListener(preferenceChangeListener);
        mUserToTrack.setOnPreferenceChangeListener(preferenceChangeListener);
        mStart.setOnPreferenceClickListener(preferenceClickListener);
        mLocalizationPhoneNumber.setSummary(mLocalizationPhoneNumber.getText()+"");
        mLocalizationCount.setSummary(getResources().getString(R.string.locaization_count_summary) + "\n("+mLocalizationCount.getText()+")");
        mLastLocationsCount.setSummary( String.format(getString(R.string.last_locations_count_summary), mLastLocationsCount.getText()));
        mUserToTrack.setSummary( mUserToTrack.getText());

        myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Log Out");
        myAlertDialog.setMessage("Are you sure");
        myAlertDialog.setNegativeButton(android.R.string.cancel, null);
        myAlertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface arg0, int arg1) {
 	        String registrationId = GCMRegistrar.getRegistrationId(SettingsActivity.this);		// Get GCM registration ID
 	        if (!registrationId.equals("")) {
 	        	Toast.makeText(getApplicationContext(), "Logging out", Toast.LENGTH_SHORT).show();
 	        	new UnregisterAsyncTask().execute(registrationId);
 	        }
         }});
        

        updateSettingsAvaibility();
    }
	
	private class PreferenceClckListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			
			if(preference == mRegisterSingup){
				getApplicationContext().startActivity(new Intent(getApplicationContext(), RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				return true;
			}
			if(preference == mLogOut){
				myAlertDialog.show();
				return true;
			}
			if(preference == mStart){
				Intent sentIntent = new Intent();
				sentIntent.setAction(SENT_ACTION);
				PendingIntent smsSentPeIntent = PendingIntent.getBroadcast(getApplicationContext(), IDX_SMS_SENT_DELIVERED, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				getApplicationContext().registerReceiver(sentDeliveredReceiver, new IntentFilter(SENT_ACTION));
				
				Intent deliveredIntent = new Intent();
				deliveredIntent.setAction(DELIVERED_ACTION);
				PendingIntent smsDeliveredPeIntent = PendingIntent.getBroadcast(getApplicationContext(), IDX_SMS_SENT_DELIVERED, deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				getApplicationContext().registerReceiver(sentDeliveredReceiver, new IntentFilter(DELIVERED_ACTION));
				
				isSentDeliveredReceiverRegistered = true;
				 
				SmsManager sm = SmsManager.getDefault();
				sm.sendTextMessage(mLocalizationPhoneNumber.getText(), "5556", "##ptr" + mLocalizationCount.getText() + "##id" + mMyID, smsSentPeIntent, smsDeliveredPeIntent);
				return true;
			}
			return false;
		}
		
	}
	
	
	private class PreferenceChangeListener implements OnPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			
			if(preference == mLocalizationPhoneNumber){
				mLocalizationPhoneNumber.setSummary((String)value);
//				Toast.makeText(getApplicationContext(), "mLocalizationCount", Toast.LENGTH_SHORT).show();
				return true;
			}
			if(preference == mLocalizationCount){
			    mLocalizationCount.setSummary(getResources().getString(R.string.locaization_count_summary) + "\n("+(String)value+")");
//				Toast.makeText(getApplicationContext(), "mLocalizationCount", Toast.LENGTH_SHORT).show();
			    return true;
			}
			if(preference == mLastLocationsCount){
			    if (!TextUtils.isEmpty((String)value) && (Integer.valueOf((String)value) > 0)) {
			        mLastLocationsCount.setText((String)value);
			        mLastLocationsCount.setSummary( String.format(getString(R.string.last_locations_count_summary), (String)value));
			    } else {
			        Toast.makeText(getApplicationContext(), "Value have to be grater than 0", Toast.LENGTH_SHORT).show();
			        return false;
			    }
			    return true;
			}
			if(preference == mUserToTrack){
			    if (TextUtils.isEmpty((String)value)) {
			        Toast.makeText(getApplicationContext(), "Cannot be empty", Toast.LENGTH_SHORT).show();
			        return false;
			    } 
			    mUserToTrack.setSummary((String)value);
				return true;
			}
			if(preference == mPooling){
//				Toast.makeText(getApplicationContext(), "mPooling: " + ((Boolean) value), Toast.LENGTH_SHORT).show();
				setPoolingEnabled((Boolean) value);
				return true;
			}
			return false;
		}
		
	}
	
	
	private void updateSettingsAvaibility(){
        String registrationId = GCMRegistrar.getRegistrationId(this);
        if (!registrationId.equals("")) {
        	mLogOut.setEnabled(true);
        	mLocalizationPhoneNumber.setEnabled(true);
        	mLocalizationCount.setEnabled(true);
//        	mStart.setEnabled(true);					// Updated in onUsersGet
        	mRegisterSingup.setEnabled(false);
        	mRegisterSingup.setSummary(R.string.register_summary_on_already_registered);
        }
        // Get user id from server
        UsersGetter.getUsers(this);
	}


	@Override
	public void onUsersGet(User[] users) {
		if(users != null){
	        String registrationId = GCMRegistrar.getRegistrationId(this);		// Get GCM registration ID
	        if (!registrationId.equals("")) {
	        	for(User user : users){											// Find registered user which has same GCM registration ID  
	        		if(registrationId.equals(user.getGcm_regid())){
	        			mMyID = user.getId();
	        			mStart.setEnabled(true);								// Enable "Start" option
	        			mStart.setSummary(getResources().getString(R.string.start_summary) + "\n(ID: "+mMyID+")");
	        		}
	        	}
	        }
		}
	}
	
	private class UnregisterAsyncTask extends AsyncTask<String, Void, Void>{
		@Override
		protected Void doInBackground(String... param) {
	        	ServerUtilities.unregister(SettingsActivity.this, param[0], new OnUnregisteredListener() {
				@Override
				public void onUnregistered() {
				    mHandler.sendEmptyMessage(0);
				}
			});
			return null;
		}
	}
	
	private class SmsDeliveredReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context ctx, Intent intent) {
			if((intent.getAction() != null) && (intent.getAction().equals(SENT_ACTION))){
				Toast.makeText(ctx, "SMS sent", Toast.LENGTH_SHORT).show();
			}
			if((intent.getAction() != null) && (intent.getAction().equals(DELIVERED_ACTION))){
				Toast.makeText(ctx, "SMS delivered", Toast.LENGTH_SHORT).show();
				ctx.unregisterReceiver(sentDeliveredReceiver);
				isSentDeliveredReceiverRegistered = false;
				SettingsActivity.this.finish();
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		updateSettingsAvaibility();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(isSentDeliveredReceiverRegistered){
			getApplicationContext().unregisterReceiver(sentDeliveredReceiver);
			isSentDeliveredReceiverRegistered = false;
		}
	}
	
	
	/**
	 * Set whether DB pooling should be enabled
	 * @param enabled
	 */
    public void setPoolingEnabled(boolean enabled) {
    	prefsEditor.putBoolean(POOLING_ENABLED, enabled);
    	prefsEditor.commit();
    }

}
