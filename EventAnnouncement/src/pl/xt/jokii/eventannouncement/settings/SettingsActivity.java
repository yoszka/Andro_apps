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

package pl.xt.jokii.eventannouncement.settings;

import com.google.android.gcm.GCMRegistrar;

import pl.xt.jokii.eventannouncement.OnUnregisteredListener;
import pl.xt.jokii.eventannouncement.RegisterActivity;
import pl.xt.jokii.eventannouncement.R;
import pl.xt.jokii.pushnotifications.ServerUtilities;
import pl.xt.jokii.pushnotifications.server.model.User;
import pl.xt.jokii.pushnotifications.server.util.OnUsersGetListener;
import pl.xt.jokii.pushnotifications.server.util.UsersGetter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnUsersGetListener {

	private static final CharSequence PREFERENCE_KEY_REGISTRATION 		= "register_singup";
	private static final CharSequence PREFERENCE_KEY_LOG_OUT 			= "log_out";
//	private static final CharSequence PREFERENCE_KEY_LOCALIZATION_NBR 	= "localized_phone_number";
//	private static final CharSequence PREFERENCE_KEY_LOCALIZATION_CNT 	= "locaization_count";
//    private static final CharSequence PREFERENCE_KEY_START 				= "start";
//    public static final String SENT_ACTION 		= "pl.xt.jokii.locationreceiver.TRACKING_SMS_SENT";
//	public static final String DELIVERED_ACTION = "pl.xt.jokii.locationreceiver.TRACKING_SMS_DELIVERED";
//	public static final int IDX_SMS_SENT_DELIVERED = 1684753;

    private PreferenceScreen mRegisterSingup;
    private PreferenceScreen mLogOut;
//    private EditTextPreference mLocalizationPhoneNumber;
//    private EditTextPreference mLocalizationCount;
//    private PreferenceScreen mStart;
    private PreferenceClckListener preferenceClickListener;
    private PreferenceChangeListener preferenceChangeListener;
    private Integer mMyID;
//    private SmsDeliveredReceiver sentDeliveredReceiver = new SmsDeliveredReceiver();
//    private boolean isSentDeliveredReceiverRegistered = false;
    
    AlertDialog.Builder myAlertDialog;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        preferenceClickListener = new PreferenceClckListener();
        preferenceChangeListener = new PreferenceChangeListener();
        
        // Category REGISTRATION
        mRegisterSingup 	= (PreferenceScreen) findPreference(PREFERENCE_KEY_REGISTRATION);
        mLogOut 			= (PreferenceScreen) findPreference(PREFERENCE_KEY_LOG_OUT);
        
        // Category LOCALIZER
//        mLocalizationPhoneNumber 	= (EditTextPreference) findPreference(PREFERENCE_KEY_LOCALIZATION_NBR);
//        mLocalizationCount 			= (EditTextPreference) findPreference(PREFERENCE_KEY_LOCALIZATION_CNT);
//        mStart 						= (PreferenceScreen) findPreference(PREFERENCE_KEY_START);

        // Additional configuration
        mRegisterSingup.setOnPreferenceClickListener(preferenceClickListener);
        mLogOut.setOnPreferenceClickListener(preferenceClickListener);
//        mLocalizationPhoneNumber.setOnPreferenceChangeListener(preferenceChangeListener);
//        mLocalizationCount.setOnPreferenceChangeListener(preferenceChangeListener);
//        mStart.setOnPreferenceClickListener(preferenceClickListener);
//        mLocalizationPhoneNumber.setSummary(mLocalizationPhoneNumber.getText()+"");
//        mLocalizationCount.setSummary(getResources().getString(R.string.locaization_count_summary) + "\n("+mLocalizationCount.getText()+")");

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
//			if(preference == mStart){
//				Intent sentIntent = new Intent();
//				sentIntent.setAction(SENT_ACTION);
//				PendingIntent smsSentPeIntent = PendingIntent.getBroadcast(getApplicationContext(), IDX_SMS_SENT_DELIVERED, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//				getApplicationContext().registerReceiver(sentDeliveredReceiver, new IntentFilter(SENT_ACTION));
//				
//				Intent deliveredIntent = new Intent();
//				deliveredIntent.setAction(DELIVERED_ACTION);
//				PendingIntent smsDeliveredPeIntent = PendingIntent.getBroadcast(getApplicationContext(), IDX_SMS_SENT_DELIVERED, deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//				getApplicationContext().registerReceiver(sentDeliveredReceiver, new IntentFilter(DELIVERED_ACTION));
//				
//				isSentDeliveredReceiverRegistered = true;
//				 
//				SmsManager sm = SmsManager.getDefault();
//				sm.sendTextMessage(mLocalizationPhoneNumber.getText(), "5556", "##ptr" + mLocalizationCount.getText() + "##id" + mMyID, smsSentPeIntent, smsDeliveredPeIntent);
//				return true;
//			}
			return false;
		}
		
	}
	
	
	private class PreferenceChangeListener implements OnPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			
//			if(preference == mLocalizationPhoneNumber){
//				mLocalizationPhoneNumber.setSummary((String)value);
////				Toast.makeText(getApplicationContext(), "mLocalizationCount", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//			if(preference == mLocalizationCount){
//				mLocalizationCount.setSummary(getResources().getString(R.string.locaization_count_summary) + "\n("+(String)value+")");
////				Toast.makeText(getApplicationContext(), "mLocalizationCount", Toast.LENGTH_SHORT).show();
//				return true;
//			}
			return false;
		}
		
	}
	
	
	private void updateSettingsAvaibility(){
        String registrationId = GCMRegistrar.getRegistrationId(this);
        if (!registrationId.equals("")) {
        	mLogOut.setEnabled(true);
//        	mLocalizationPhoneNumber.setEnabled(true);
//        	mLocalizationCount.setEnabled(true);
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
//	        			mStart.setEnabled(true);								// Enable "Start" option
//	        			mStart.setSummary(getResources().getString(R.string.start_summary) + "\n(ID: "+mMyID+")");
	        			mRegisterSingup.setSummary(getResources().getString(R.string.register_summary_on_already_registered) + " (ID: "+mMyID+")");
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
					updateSettingsAvaibility();
				}
			});
			return null;
		}
	}
	
//	private class SmsDeliveredReceiver extends BroadcastReceiver{
//		@Override
//		public void onReceive(Context ctx, Intent intent) {
//			if((intent.getAction() != null) && (intent.getAction().equals(SENT_ACTION))){
//				Toast.makeText(ctx, "SMS sent", Toast.LENGTH_SHORT).show();
//			}
//			if((intent.getAction() != null) && (intent.getAction().equals(DELIVERED_ACTION))){
//				Toast.makeText(ctx, "SMS delivered", Toast.LENGTH_SHORT).show();
//				ctx.unregisterReceiver(sentDeliveredReceiver);
//				isSentDeliveredReceiverRegistered = false;
//				SettingsActivity.this.finish();
//			}
//		}
//	}
	
	@Override
	protected void onStart() {
		super.onStart();
		updateSettingsAvaibility();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
//		if(isSentDeliveredReceiverRegistered){
//			getApplicationContext().unregisterReceiver(sentDeliveredReceiver);
//			isSentDeliveredReceiverRegistered = false;
//		}
	}

}
