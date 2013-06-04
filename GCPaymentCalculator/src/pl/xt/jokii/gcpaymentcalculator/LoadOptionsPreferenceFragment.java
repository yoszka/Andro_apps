package pl.xt.jokii.gcpaymentcalculator;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Tomek on 02.06.13.
 */
public class LoadOptionsPreferenceFragment extends PreferenceFragment {

    private static final String PREFERENCE_SCREEN = "pref_screen";
    private static final String LOAD_SAVED_OPTIONS = "load_saved_options";
    private static final String SAVE_TO_OPTIONS = "save_to_options_name";
    OptionManager optionManager;
    PreferenceScreen mPreferenceScreen;
    ListPreference loadListPreference;
    EditTextPreference saveOptionsEdittextPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.load_options);

        mPreferenceScreen = (PreferenceScreen) findPreference(PREFERENCE_SCREEN);
        // Getting the ListPreference and EditTextPreference from the Preference Resource
        loadListPreference            = (ListPreference )     getPreferenceManager().findPreference(LOAD_SAVED_OPTIONS);
        saveOptionsEdittextPreference = (EditTextPreference ) getPreferenceManager().findPreference(SAVE_TO_OPTIONS);

        optionManager = new OptionManager(getActivity());
    }

    public ListPreference getListPreference(){
        return loadListPreference;
    }
    public EditTextPreference getEditTextPreference(){
        return saveOptionsEdittextPreference;
    }

    public void showLoadOptionsList(){
        // the position of your item inside the preference screen above
        int pos = findPreference(LOAD_SAVED_OPTIONS).getOrder();

        // simulate a click / call it!!
        mPreferenceScreen.onItemClick( null, null, pos, 0 );
    }


    public void showEditSaveOptionsName(){
        // the position of your item inside the preference screen above
        int pos = findPreference(SAVE_TO_OPTIONS).getOrder();

        // simulate a click / call it!!
        mPreferenceScreen.onItemClick( null, null, pos, 0 );
    }

    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener listener){
//        // Getting the ListPreference from the Preference Resource
//        ListPreference loadListPreference = (ListPreference ) getPreferenceManager().findPreference("lp_android_choice");
        /** Setting Preference change listener for the ListPreference */
        loadListPreference.setOnPreferenceChangeListener(listener);
        saveOptionsEdittextPreference.setOnPreferenceChangeListener(listener);
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

        loadListPreference.setEntries(optionNames);
        loadListPreference.setEntryValues(optionNames);
        return true;
    }

//    private class CustomListPreference extends ListPreference{
//
//
//        public CustomListPreference(Context context) {
//            super(context);
//        }
//
//        @Override
//        protected View onCreateDialogView() {
//            View dialogView = super.onCreateDialogView();
//            ListView lv = (ListView) dialogView.findViewById(android.R.id.list);
//            lv.getAdapter()
//            return dialogView;
//        }
//    }
}