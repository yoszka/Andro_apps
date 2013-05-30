
package pl.xt.jokii.gcpaymentcalculator;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class MainActivity extends Activity {
    
    EditText brutto;
    EditText bony;
    EditText przychod;
    EditText rozchod;
    EditText naReke;
    LinearLayout listaOpcji;
    
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
        
        firmaLuxMed         = getResources().getInteger(R.integer.lux_med_firma)        / 100.0;
        firmaMultisport     = getResources().getInteger(R.integer.multisport_firma)     / 100.0;
        firmaAviva          = getResources().getInteger(R.integer.aviva_firma)          / 100.0;
        pracownikLuxMed     = getResources().getInteger(R.integer.lux_med_pracownik)    / 100.0;
        pracownikMultisport = getResources().getInteger(R.integer.multisport_pracownik) / 100.0;
        podatekZusProcent   = getResources().getInteger(R.integer.podatekZus)           / 10000.0;
        podatekZdrowProcent = getResources().getInteger(R.integer.podatekZdrowotne)     / 10000.0;
        zaliczka_podatku    = getResources().getInteger(R.integer.zaliczka_podatku)     / 100.0;

        addOption(R.string.multisport, firmaMultisport, pracownikMultisport);
        addOption(R.string.aviva, firmaAviva, 0);
        addOption(R.string.lux_med, firmaLuxMed, pracownikLuxMed);

    }


    private class Option{
        final double placiFirma;
        final double placiPracownik;
        final CheckBox chbx;

        Option(CheckBox chbx, double placiFirma, double placiPracownik){
            this.chbx = chbx;
            this.placiFirma = placiFirma;
            this.placiPracownik = placiPracownik;
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
     * @return
     */
    private Option addOption(int textResourceID, double placiFirma, double placiPracownik){
        return addOption(getApplicationContext().getResources().getString(textResourceID), placiFirma, placiPracownik);
    }


    /**
     * Add special option
     * @param text  option name
     * @param placiFirma how many pay company for this option
     * @param placiPracownik how many pay employee for this option
     * @return
     */
    private Option addOption(String text, double placiFirma, double placiPracownik){
        RelativeLayout  opcjaRow = (RelativeLayout) getLayoutInflater().inflate(R.layout.list_item, null);
        CheckBox chbx = (CheckBox) opcjaRow.findViewById(R.id.checkBox);
        chbx.setText(text);
        chbx.setOnCheckedChangeListener(checkBoxCheckedChangeListener);
        listaOpcji.addView(opcjaRow);
        Option opcja = new Option(chbx, placiFirma, placiPracownik);
        optionList.add(opcja);
        return opcja;
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
    
    private void  updateCalculation() {
        String valueTmp = null;
        double valueBrutto              = 0.0;
        double valueBony                = 0.0;
        double przychodySuma            = 0.0;
        double przychodySumaOptions     = 0.0;
        double placiPracownikSumaOptions= 0.0;
        double rozchodySuma             = 0.0;
        double naRekeValue              = 0.0;
        double sumaSkladekZus           = 0.0;
        
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

}
