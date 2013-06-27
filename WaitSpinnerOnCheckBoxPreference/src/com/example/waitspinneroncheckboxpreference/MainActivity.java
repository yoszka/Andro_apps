package com.example.waitspinneroncheckboxpreference;

import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity  implements OnPreferenceChangeListener{

	private CheckBoxPreference checkBoxPref;
    private CheckBoxPreference checkBoxPrefCustom;
    private EditTextPreference editTextPref;
    private int orginalCheckBoxLayoutResource;
	private int orginalCheckBoxCustomLayoutResource;
	private int orginalEditTextLayoutResource;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        
        editTextPref = (EditTextPreference) findPreference("edit_text");
        checkBoxPref = (CheckBoxPreference) findPreference("wlacznik");
        checkBoxPrefCustom = (CheckBoxPreference) findPreference("wlacznik_custom");
        
        editTextPref.setOnPreferenceChangeListener(this);
        checkBoxPref.setOnPreferenceChangeListener(this);
        checkBoxPrefCustom.setOnPreferenceChangeListener(this);
        
        orginalEditTextLayoutResource = editTextPref.getWidgetLayoutResource();
        orginalCheckBoxLayoutResource = checkBoxPref.getWidgetLayoutResource();
        orginalCheckBoxCustomLayoutResource = checkBoxPrefCustom.getWidgetLayoutResource();
        
    }

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference == checkBoxPref){
			checkBoxPref.setEnabled(false);
			checkBoxPref.setWidgetLayoutResource(R.layout.progr);
			delay_s(2);
			return true;
			
		}else if(preference == editTextPref){
			editTextPref.setSummary((String)newValue);
			editTextPref.setEnabled(false);
			editTextPref.setWidgetLayoutResource(R.layout.progr);
			delay2_s(2);			
			return true;
			
		}else if(preference == checkBoxPrefCustom){
			checkBoxPrefCustom.setEnabled(false);
			checkBoxPrefCustom.setWidgetLayoutResource(R.layout.custom_chexbox);
			delay3_s(2);
			return true;
		}
		
		return false;
	}
	
	
	
	
	
	// *****************************************************************************************************
	/**
	 * MOCK delayed operation for checkbox
	 * @param time_second
	 */
	void delay_s(final int time_second){
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				try {
					Thread.sleep(time_second * 1000);
					handle_action();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		t.start();
	}
	
	
	void handle_action(){
		Handler h = new Handler(getMainLooper());
		
		h.post(new Runnable() {
			
			public void run() {
				checkBoxPref.setEnabled(true);
				checkBoxPref.setWidgetLayoutResource(orginalCheckBoxLayoutResource);
			}
		});
	}
	
	// -----------------------------------------------------------------------------------
	
	/**
	 * MOCK delayed operation for EditText
	 * @param time_second
	 */
	void delay2_s(final int time_second){
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				try {
					Thread.sleep(time_second * 1000);
					handle_action2();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		t.start();
	}
	
	
	void handle_action2(){
		Handler h = new Handler(getMainLooper());
		
		h.post(new Runnable() {
			
			public void run() {
				editTextPref.setEnabled(true);
				editTextPref.setWidgetLayoutResource(orginalEditTextLayoutResource);
			}
		});
	}
	
	// *****************************************************************************************************
	/**
	 * MOCK delayed operation for custom checkbox
	 * @param time_second
	 */
	void delay3_s(final int time_second){
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				try {
					Thread.sleep(time_second * 1000);
					handle_action3();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		t.start();
	}
	
	
	void handle_action3(){
		Handler h = new Handler(getMainLooper());
		
		h.post(new Runnable() {
			
			public void run() {
				checkBoxPrefCustom.setEnabled(true);
				checkBoxPrefCustom.setWidgetLayoutResource(orginalCheckBoxCustomLayoutResource);
			}
		});
	}
	
}

