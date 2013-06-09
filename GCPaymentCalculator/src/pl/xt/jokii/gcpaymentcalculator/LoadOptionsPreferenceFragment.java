package pl.xt.jokii.gcpaymentcalculator;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

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
    private static final String REMOVE_OPTIONS = "remove_options";
    OptionManager             mOptionManager;
    PreferenceScreen          mPreferenceScreen;
    ListPreference            mLoadListPreference;
    EditTextPreference        mSaveOptionsEdittextPreference;
    MultiSelectListPreference mRemoveOptionsMultiSelectListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.manage_options_storage);

        mPreferenceScreen = (PreferenceScreen) findPreference(PREFERENCE_SCREEN);
        // Getting the ListPreference and EditTextPreference from the Preference Resource
        mLoadListPreference = (ListPreference )     getPreferenceManager().findPreference(LOAD_SAVED_OPTIONS);
        mSaveOptionsEdittextPreference = (EditTextPreference ) getPreferenceManager().findPreference(SAVE_TO_OPTIONS);
        mRemoveOptionsMultiSelectListPreference = (MultiSelectListPreference ) getPreferenceManager().findPreference(REMOVE_OPTIONS);

        mOptionManager = new OptionManager(getActivity());
    }

    public ListPreference getListPreference(){
        return mLoadListPreference;
    }
    public EditTextPreference getEditTextPreference(){
        return mSaveOptionsEdittextPreference;
    }
    public MultiSelectListPreference getMultiSelectPreference(){
        return mRemoveOptionsMultiSelectListPreference;
    }

    public void showLoadOptionsList(){
        // the position of your item inside the preference screen above
        int pos = findPreference(LOAD_SAVED_OPTIONS).getOrder();

        // simulate a click / call it!!
        mPreferenceScreen.onItemClick( null, null, pos, 0 );
    }


    public void showRemoveOptionsList(){
        // the position of your item inside the preference screen above
        int pos = findPreference(REMOVE_OPTIONS).getOrder();

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
//        ListPreference mLoadListPreference = (ListPreference ) getPreferenceManager().findPreference("lp_android_choice");
        /** Setting Preference change listener for the ListPreference */
        mLoadListPreference.setOnPreferenceChangeListener(listener);
        mSaveOptionsEdittextPreference.setOnPreferenceChangeListener(listener);
        mRemoveOptionsMultiSelectListPreference.setOnPreferenceChangeListener(listener);
    }

    public boolean loadStoredOptions(){
        HashMap<String, ArrayList<OptionStore> > options = mOptionManager.loadOptions();
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

        // Set entries and values for load options list
        mLoadListPreference.setEntries(optionNames);
        mLoadListPreference.setEntryValues(optionNames);

        // Set entries and values also for remove options list
        mRemoveOptionsMultiSelectListPreference.setEntries(optionNames);
        mRemoveOptionsMultiSelectListPreference.setEntryValues(optionNames);


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