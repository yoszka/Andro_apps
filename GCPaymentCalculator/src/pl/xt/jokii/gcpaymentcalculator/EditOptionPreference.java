package pl.xt.jokii.gcpaymentcalculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import java.util.ArrayList;

/**
 * Created by Tomek on 30.05.13.
 */
public class EditOptionPreference extends PreferenceActivity {
    private static final String OPTION_NAME = "option_name";
    private static final String PLACI_FIRMA_KEY = "option_palci_firma";
    private static final String PLACI_PRACOWNIK_KEY = "option_palci_pracownik";
    private static final String REMOVE_OPTION_KEY = "remove_option";

    private EditTextPreference mPreferenceOptionName;
    private EditTextPreference mPreferenceOptionPlaciFirma;
    private EditTextPreference mPreferenceOptionPlaciPracownik;
    private PreferenceScreen   mPreferenceRemoveoption;
    private Integer mOptionIndex;
    private OptionManager mOptionManager;
    ArrayList<OptionStore> mOptions;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.edit_preference);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mOptionIndex = extras.getInt(MainActivity.OPTION_INDEX);

        mPreferenceOptionName = (EditTextPreference) findPreference(OPTION_NAME);
        mPreferenceOptionPlaciFirma = (EditTextPreference) findPreference(PLACI_FIRMA_KEY);
        mPreferenceOptionPlaciPracownik = (EditTextPreference) findPreference(PLACI_PRACOWNIK_KEY);
        mPreferenceRemoveoption = (PreferenceScreen) findPreference(REMOVE_OPTION_KEY);

        mOptionManager = new OptionManager(getApplication());
        mOptions = mOptionManager.getCurrentOptions();

        mPreferenceOptionName.setText(mOptions.get(mOptionIndex).name);
        mPreferenceOptionPlaciFirma.setText(String.valueOf(mOptions.get(mOptionIndex).payCompany));
        mPreferenceOptionPlaciPracownik.setText(String.valueOf(mOptions.get(mOptionIndex).payEmployee));

        mPreferenceOptionName.setSummary(mOptions.get(mOptionIndex).name);
        mPreferenceOptionPlaciFirma.setSummary(String.valueOf(mOptions.get(mOptionIndex).payCompany));
        mPreferenceOptionPlaciPracownik.setSummary(String.valueOf(mOptions.get(mOptionIndex).payEmployee));

        mPreferenceOptionName.setOnPreferenceChangeListener(onPreferenceChangeListener);
        mPreferenceOptionPlaciFirma.setOnPreferenceChangeListener(onPreferenceChangeListener);
        mPreferenceOptionPlaciPracownik.setOnPreferenceChangeListener(onPreferenceChangeListener);
        mPreferenceRemoveoption.setOnPreferenceClickListener(onPreferenceClickListener);

        // Delete alert dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Usunąć?");
        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mOptions.remove((int)mOptionIndex);
                mOptionManager.saveCurrentOptions(mOptions);
                setResult(Activity.RESULT_OK, null);
                EditOptionPreference.this.finish();
            }
        });
        alertDialogBuilder.setNegativeButton(android.R.string.no, null);
        alertDialog = alertDialogBuilder.create();
    }

    Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            OptionStore optionStore = mOptions.get(mOptionIndex);
            if(preference == mPreferenceOptionName){
                optionStore.name = (String) newValue;
                mPreferenceOptionName.setSummary((String) newValue);
            }else if(preference == mPreferenceOptionPlaciFirma){
                optionStore.payCompany = Double.parseDouble((String) newValue);
                mPreferenceOptionPlaciFirma.setSummary(String.format("%.2f", optionStore.payCompany));
            }else if(preference == mPreferenceOptionPlaciPracownik){
                optionStore.payEmployee = Double.parseDouble((String) newValue);
                mPreferenceOptionPlaciPracownik.setSummary(String.format("%.2f", optionStore.payEmployee));
            }
            mOptionManager.saveCurrentOptions(mOptions);
            return true;
        }
    };

    Preference.OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            alertDialog.show();
            return false;
        }
    };
}
