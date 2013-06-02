package pl.xt.jokii.gcpaymentcalculator;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tomek on 02.06.13.
 */
public class LoadOptionsPreferenceFragment extends PreferenceFragment {
    OptionManager optionManager;
    ListPreference p;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.load_options);

        optionManager = new OptionManager(getActivity());
        // Getting the ListPreference from the Preference Resource
        p = (ListPreference ) getPreferenceManager().findPreference("lp_android_choice");

//        // the preference screen your item is in must be known
//        PreferenceScreen screen = (PreferenceScreen) findPreference("pref_screen");
//
//        // the position of your item inside the preference screen above
//        int pos = findPreference("lp_android_choice").getOrder();
//
//        // simulate a click / call it!!
//        screen.onItemClick( null, null, pos, 0 );


//        /** Defining PreferenceChangeListener */
//        Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
//
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                Preference.OnPreferenceChangeListener listener = (Preference.OnPreferenceChangeListener) getActivity();
//                listener.onPreferenceChange(preference, newValue);
//                return true;
//            }
//        };
//
//        /** Getting the ListPreference from the Preference Resource */
//        ListPreference p = (ListPreference ) getPreferenceManager().findPreference("lp_android_choice");
//        /** Setting Preference change listener for the ListPreference */
//        p.setOnPreferenceChangeListener(onPreferenceChangeListener);
    }

    public void show(){
        // the preference screen your item is in must be known
        PreferenceScreen screen = (PreferenceScreen) findPreference("pref_screen");

        // the position of your item inside the preference screen above
        int pos = findPreference("lp_android_choice").getOrder();

        // simulate a click / call it!!
        screen.onItemClick( null, null, pos, 0 );
    }

    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener listener){
//        // Getting the ListPreference from the Preference Resource
//        ListPreference p = (ListPreference ) getPreferenceManager().findPreference("lp_android_choice");
        /** Setting Preference change listener for the ListPreference */
        p.setOnPreferenceChangeListener(listener);
    }

    public boolean loadStoredOptions(){
        HashMap<String, ArrayList<OptionStore> > options = optionManager.loadOptions();
        if(options.size() == 0){
            return false;   // no options loaded
        }
        String[] optionNames = new String[options.size()];
        int index = 0;
        Set<HashMap.Entry<String, ArrayList<OptionStore> >> optSet =  options.entrySet();
        HashMap.Entry<String, ArrayList<OptionStore> > entry;

        for(HashMap.Entry<String, ArrayList<OptionStore> > currentOption: optSet){
            optionNames[index++] = (String)currentOption.getKey();
        }

        p.setEntries(optionNames);
        p.setEntryValues(optionNames);
        return true;
    }
}