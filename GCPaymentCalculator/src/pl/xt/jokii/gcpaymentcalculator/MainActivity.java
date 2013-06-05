
package pl.xt.jokii.gcpaymentcalculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

    public static final String OPTION_INDEX = "OPTION_INDEX";
    EditText brutto;
    EditText bony;
    EditText przychod;
    EditText rozchod;
    EditText naReke;
    LinearLayout listaOpcji;
    OptionManager optionManager;
    
    private static double firmaLuxMed;
    private static double firmaMultisport;
    private static double firmaAviva;
    private static double pracownikLuxMed;
    private static double pracownikMultisport;
    private static double podatekZusProcent;
    private static double podatekZdrowProcent;
    private static double zaliczka_podatku;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        brutto      = (EditText) findViewById(R.id.editTextBrutto);
        bony        = (EditText) findViewById(R.id.editTextBony);
        przychod    = (EditText) findViewById(R.id.editTextPrzychod);
        rozchod     = (EditText) findViewById(R.id.editTextRozchod);
        naReke      = (EditText) findViewById(R.id.editTextNaReke);
        listaOpcji  = (LinearLayout) findViewById(R.id.linearLayoutListaOpcji);

        brutto.addTextChangedListener(mValueChangeListener);
        bony.addTextChangedListener(mValueChangeListener);
        
        firmaLuxMed         = getResources().getInteger(R.integer.lux_med_company)        / 100.0;
        firmaMultisport     = getResources().getInteger(R.integer.multisport_company)     / 100.0;
        firmaAviva          = getResources().getInteger(R.integer.aviva_company)          / 100.0;
        pracownikLuxMed     = getResources().getInteger(R.integer.lux_med_employee)    / 100.0;
        pracownikMultisport = getResources().getInteger(R.integer.multisport_employee) / 100.0;
        podatekZusProcent   = getResources().getInteger(R.integer.podatekZus)           / 10000.0;
        podatekZdrowProcent = getResources().getInteger(R.integer.podatekZdrowotne)     / 10000.0;
        zaliczka_podatku    = getResources().getInteger(R.integer.zaliczka_podatku)     / 100.0;


        optionManager = new OptionManager(getApplication());

        loadCurrentOptions();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    private class Option{
        final double placiFirma;
        final double placiPracownik;
        final CheckBox chbx;
        final Button btn;

        Option(CheckBox chbx, Button btn, double placiFirma, double placiPracownik){
            this.chbx = chbx;
            this.placiFirma = placiFirma;
            this.placiPracownik = placiPracownik;
            this.btn = btn;
        }
    }

    /**
     * List of additional options
     */
    private ArrayList<Option> optionList = new ArrayList<Option>();

    /**
     * Wraper to {@link #addOption(String, double, double)}
     * @param textResourceID resource id with option name
     * @param placiFirma how many pay company for this option
     * @param placiPracownik how many pay employee for this option
     * @return index of option
     */
    private int addOption(int textResourceID, double placiFirma, double placiPracownik){
        return addOption(getApplicationContext().getResources().getString(textResourceID), placiFirma, placiPracownik);
    }


    /**
     * Add special option
     * @param text  option name
     * @param placiFirma how many pay company for this option
     * @param placiPracownik how many pay employee for this option
     * @return index of option
     */
    private int addOption(String text, double placiFirma, double placiPracownik){
        RelativeLayout  opcjaRow = (RelativeLayout) getLayoutInflater().inflate(R.layout.list_item, null);
        CheckBox chbx = (CheckBox) opcjaRow.findViewById(R.id.checkBox);
        Button btn = (Button) opcjaRow.findViewById(R.id.button);
        chbx.setText(text);
        chbx.setOnCheckedChangeListener(checkBoxCheckedChangeListener);
        listaOpcji.addView(opcjaRow);
        Option opcja = new Option(chbx, btn, placiFirma, placiPracownik);
        optionList.add(opcja);
        return (optionList.size() - 1);
    }

    
    
    private TextWatcher mValueChangeListener = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateCalculation();
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {}
    };
    
    
    private OnCheckedChangeListener checkBoxCheckedChangeListener = new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          updateCalculation();
      }
  };

    /**
     * Load/reLoad current options from application data
     */
    private void loadCurrentOptions(){
        // first remove all previous options
        listaOpcji.removeAllViewsInLayout();
        optionList.clear();
        ArrayList<OptionStore> options = optionManager.getCurrentOptions();
        for(OptionStore option : options){
            addOption(option.name, option.payCompany, option.payEmployee);
        }
    }


    /**
     * Update calculation and showLoadOptionsList in View proper values
     */
    private void  updateCalculation() {
        String valueTmp = null;
        double valueBrutto              = 0.0;
        double valueBony                = 0.0;
        double przychodySuma;
        double przychodySumaOptions     = 0.0;
        double placiPracownikSumaOptions= 0.0;
        double rozchodySuma;
        double naRekeValue;
        double sumaSkladekZus;
        
        // Obliczanie podstawy (przychodów) do policzenia wysokości podatku
        valueTmp = brutto.getText().toString();
        if(!TextUtils.isEmpty(valueTmp)) {
            valueBrutto = Double.parseDouble(valueTmp);
        }
        
        valueTmp = bony.getText().toString();
        if(!TextUtils.isEmpty(valueTmp)) {
            valueBony = Double.parseDouble(valueTmp);
        }

        for(Option o : optionList){
            if(o.chbx.isChecked()){
                przychodySumaOptions += o.placiFirma;
                placiPracownikSumaOptions += o.placiPracownik;
            }
        }

        przychodySuma =   valueBrutto
                        + valueBony
                        + przychodySumaOptions;
        
        sumaSkladekZus = przychodySuma * podatekZusProcent;
        
        rozchodySuma =    valueBony 
                        + przychodySumaOptions
                        + zaliczka_podatku
                        + placiPracownikSumaOptions
                        + sumaSkladekZus
                        + ((przychodySuma - sumaSkladekZus) * podatekZdrowProcent);
        
        
        naRekeValue = (przychodySuma - rozchodySuma) + valueBony;
        

        przychod.setText(String.format("%.2f", przychodySuma));
        rozchod.setText(String.format("%.2f", rozchodySuma));
        naReke.setText(String.format("%.2f", naRekeValue));
    }

    /**
     * Button "Add"
     * @param v
     */
    public void onClickButtonAdd(View v){
        final String defaultOptionName = getResources().getString(R.string.default_option_name);
        int optionIndex = addOption(defaultOptionName, 0, 0);
        Option option = optionList.get(optionIndex);
        optionManager.addCurrentOption(new OptionStore(defaultOptionName, option.placiFirma, option.placiPracownik));
        Intent intent = new Intent(this, EditOptionPreference.class);
        intent.putExtra(OPTION_INDEX, optionIndex);
        startActivityForResult(intent, R.id.edit_option_preference);
        updateCalculation();
    }

    /**
     * Button "Edit"
     * @param v
     */
    public void onClickButtonEdit(View v){
        Intent intent = new Intent(this, EditOptionPreference.class);
        intent.putExtra(OPTION_INDEX, getOptionIndexByButton((Button) v));
        startActivityForResult(intent, R.id.edit_option_preference);
    }

    /**
     * Menu "Load options"
     * @param mi
     */
    public void onClickMenuLoadOptions(MenuItem mi){
        showLoadOptionsDialog();
    }

    /**
     * Menu "Save options"
     * @param mi
     */
    public void onClickMenuSaveOptions(MenuItem mi){
        showSaveOptionsDialog();
    }

    /**
     * Menu "Remove options"
     * @param mi
     */
    public void onClickMenuRemoveOptions(MenuItem mi){
        showRemoveOptionsDialog();
    }

    /**
     * Get option index from options array by id of contained "Edit" button
     * @param button
     * @return
     */
    private Integer getOptionIndexByButton(Button button){
        int index = 0;
        for(Option o : optionList){
            if(o.btn == button){
                return index;
            }
            index++;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == R.id.edit_option_preference)){
            // reload options after change
            loadCurrentOptions();
            updateCalculation();
        }
    }

    private void showLoadOptionsDialog(){
        final LoadOptionsPreferenceFragment fragment = (LoadOptionsPreferenceFragment) getFragmentManager().findFragmentById(R.id.embeded_preference);
        fragment.getListPreference().setDialogTitle(R.string.load_options);
        fragment.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.loaded)+" \""+((String) o)+"\"", 0).show();
                    optionManager.restoreCurrentOptions((String) o);
                    loadCurrentOptions();
                    updateCalculation();
                    return true;
            }});
        if(fragment.loadStoredOptions()){
            fragment.showLoadOptionsList();
        }else{
            Toast.makeText(getApplicationContext(), R.string.no_options_found, 0).show();
        }
    }


    private void showRemoveOptionsDialog(){
        final LoadOptionsPreferenceFragment fragment = (LoadOptionsPreferenceFragment) getFragmentManager().findFragmentById(R.id.embeded_preference);
        fragment.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Set<String> values = (Set<String>) o;
                for(String value : values){
                    optionManager.removeFromOptionsStorage(value);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.removed)+" \""+value+"\"", 0).show();
                }
            return true;
        }});
        if(fragment.loadStoredOptions()){
            fragment.getMultiSelectPreference().setValues(new HashSet<String>());                   // Unselect all options by pass empty set of options to select
            fragment.showRemoveOptionsList();
        }else{
            Toast.makeText(getApplicationContext(), R.string.no_options_found, 0).show();
        }
    }


    private void showSaveOptionsDialog(){
        final LoadOptionsPreferenceFragment fragment = (LoadOptionsPreferenceFragment) getFragmentManager().findFragmentById(R.id.embeded_preference);
        fragment.getEditTextPreference().setText("");
        fragment.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if(!optionManager.storeCurrentOptions((String) o)){
                    // if this name already exist
                    // (ask if overwrite?)
                    optionManager.updateCurentOptionsStorage((String) o);
                }
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.options_saved)+" \""+((String) o)+"\"", 0).show();
                return true;
            }});
            fragment.showEditSaveOptionsName();
    }

}
