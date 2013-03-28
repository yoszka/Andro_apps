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

import pl.xt.jokii.locationreceiver.R;
import pl.xt.jokii.locationreceiver.RegisterActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	private static final CharSequence PREFERENCE_KEY_REGISTRATION 		= "register_singup";
	private static final CharSequence PREFERENCE_KEY_LOG_OUT 			= "log_out";
	private static final CharSequence PREFERENCE_KEY_LOCALIZATION_CNT 	= "locaization_count";
    private static final CharSequence PREFERENCE_KEY_START 				= "start";
    
    
    private PreferenceScreen mRegisterSingup;
    private PreferenceScreen mLogOut;
    private EditTextPreference mLocalizationCount;
    private PreferenceScreen mStart;
    private PreferenceClckListener preferenceClickListener;
    private PreferenceChangeListener preferenceChangeListener;
    
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
        mLocalizationCount 	= (EditTextPreference) findPreference(PREFERENCE_KEY_LOCALIZATION_CNT);
        mStart 				= (PreferenceScreen) findPreference(PREFERENCE_KEY_START);

        // Additional configuration
        mRegisterSingup.setOnPreferenceClickListener(preferenceClickListener);
        mLogOut.setOnPreferenceClickListener(preferenceClickListener);
        mLocalizationCount.setOnPreferenceChangeListener(preferenceChangeListener);
        mStart.setOnPreferenceClickListener(preferenceClickListener);

        updateSettingsAvaibility();
        myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Log Out");
        myAlertDialog.setMessage("Are you sure");
        myAlertDialog.setNegativeButton(android.R.string.cancel, null);
        myAlertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface arg0, int arg1) {
        	 Toast.makeText(getApplicationContext(), "Loged out", Toast.LENGTH_SHORT).show();
         }});
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
//				Toast.makeText(getApplicationContext(), "mStart", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
		
	}
	
	
	private class PreferenceChangeListener implements OnPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			
			if(preference == mLocalizationCount){
//				Toast.makeText(getApplicationContext(), "mLocalizationCount", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
		
	}
	
	
	private void updateSettingsAvaibility(){
        String registrationId = GCMRegistrar.getRegistrationId(this);
        if (!registrationId.equals("")) {
        	mLogOut.setEnabled(true);
        	mLocalizationCount.setEnabled(true);
        	mStart.setEnabled(true);
        	mRegisterSingup.setEnabled(false);
        	mRegisterSingup.setSummary(R.string.register_summary_on_already_registered);
        }
	}

}
