package pl.xt.jokii.inventory;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class EditOptionPreference extends PreferenceActivity {
	private static final String PREFERENCE_SCREEN   = "pref_screen";
    private static final String OPTION_NEW_CATEGORY = "new_category";

    
    private PreferenceScreen   mPreferenceScreen;
    private EditTextPreference mPreferenceOptionNewCategory;
    private Integer mOptionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.edit_preference);

//        Intent intent = getIntent();
//        Bundle extras = intent.getExtras();
//        mOptionIndex = extras.getInt(MainActivity.OPTION_INDEX);
        mPreferenceScreen 				= (PreferenceScreen)   findPreference(PREFERENCE_SCREEN);
        mPreferenceOptionNewCategory 	= (EditTextPreference) findPreference(OPTION_NEW_CATEGORY);

        mPreferenceOptionNewCategory.setText("");
//
//        mPreferenceOptionNewCategory.setSummary(mOptions.get(mOptionIndex).name);

//        mPreferenceOptionNewCategory.setOnPreferenceChangeListener(onPreferenceChangeListener);

    }
    
    public EditTextPreference getEditTextPreference(){
        return mPreferenceOptionNewCategory;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        showEditNewCategoryName();
    }

//    Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
//        @Override
//        public boolean onPreferenceChange(Preference preference, Object newValue) {
//            if(preference == mPreferenceOptionNewCategory){
//                mPreferenceOptionNewCategory.setSummary((String) newValue);
//            }
//            return true;
//        }
//    };
    
    public void showEditNewCategoryName(){
        // the position of your item inside the preference screen above
        int pos = findPreference(OPTION_NEW_CATEGORY).getOrder();
        // simulate a click / call it!!
        mPreferenceScreen.onItemClick( null, null, pos, 0 );
    }

}
