
package pl.xt.jokii.gcpaymentcalculator;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class MainActivity extends Activity {
    
    EditText brutto;
    EditText bony;
    EditText przychod;
    EditText rozchod;
    EditText naReke;
    CheckBox multisport;
    CheckBox aviva;
    CheckBox luxMed;
    
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
        
        brutto     = (EditText) findViewById(R.id.editTextBrutto);
        bony       = (EditText) findViewById(R.id.editTextBony);
        przychod   = (EditText) findViewById(R.id.editTextPrzychod);
        rozchod    = (EditText) findViewById(R.id.editTextRozchod);
        naReke     = (EditText) findViewById(R.id.editTextNaReke);
        multisport = (CheckBox) findViewById(R.id.checkBoxMultisport);
        aviva      = (CheckBox) findViewById(R.id.checkBoxAviva);
        luxMed     = (CheckBox) findViewById(R.id.checkBoxLuxMed);
        
        brutto.addTextChangedListener(mValueChangeListener);
        bony.addTextChangedListener(mValueChangeListener);
        
        multisport.setOnCheckedChangeListener(checkBoxCheckedChangeListener);
        aviva.setOnCheckedChangeListener(checkBoxCheckedChangeListener);
        luxMed.setOnCheckedChangeListener(checkBoxCheckedChangeListener);
        
        firmaLuxMed         = getResources().getInteger(R.integer.lux_med_firma)        / 100.0;
        firmaMultisport     = getResources().getInteger(R.integer.multisport_firma)     / 100.0;
        firmaAviva          = getResources().getInteger(R.integer.aviva_firma)          / 100.0;
        pracownikLuxMed     = getResources().getInteger(R.integer.lux_med_pracownik)    / 100.0;
        pracownikMultisport = getResources().getInteger(R.integer.multisport_pracownik) / 100.0;
        podatekZusProcent   = getResources().getInteger(R.integer.podatekZus)           / 10000.0;
        podatekZdrowProcent = getResources().getInteger(R.integer.podatekZdrowotne)     / 10000.0;
        zaliczka_podatku    = getResources().getInteger(R.integer.zaliczka_podatku)     / 100.0;
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
        double valuePrzychodyMultisport = 0.0;
        double valuePrzychodyLuxMed     = 0.0;
        double valuePrzychodyAviva      = 0.0;
        double przychodySuma            = 0.0;
        double valueRozchodyMultisport  = 0.0;
        double valueRozchodyLuxMed      = 0.0;
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
        
        if(multisport.isChecked()) {
            valuePrzychodyMultisport = firmaMultisport;
            valueRozchodyMultisport  = pracownikMultisport;
        }
        
        if(luxMed.isChecked()) {
            valuePrzychodyLuxMed = firmaLuxMed;
            valueRozchodyLuxMed  = pracownikLuxMed;
        }
        
        if(aviva.isChecked()) {
            valuePrzychodyAviva = firmaAviva;
        }
        
                
        przychodySuma =   valueBrutto 
                        + valueBony 
                        + valuePrzychodyMultisport 
                        + valuePrzychodyLuxMed 
                        + valuePrzychodyAviva;
        
        sumaSkladekZus = przychodySuma * podatekZusProcent;
        
        rozchodySuma =    valueBony 
                        + valuePrzychodyMultisport 
                        + valuePrzychodyLuxMed 
                        + valuePrzychodyAviva
                        + zaliczka_podatku
                        + valueRozchodyLuxMed
                        + valueRozchodyMultisport
                        + sumaSkladekZus
                        + ((przychodySuma - sumaSkladekZus) * podatekZdrowProcent);
        
        
        naRekeValue = (przychodySuma - rozchodySuma) + valueBony;
        
        Log.i("update",   "valueBrutto              = " + valueBrutto);
        Log.i("update",   "valueBony                = " + valueBony);
        Log.i("update",   "valuePrzychodyMultisport = " + valuePrzychodyMultisport);
        Log.i("update",   "valuePrzychodyLuxMed     = " + valuePrzychodyLuxMed);
        Log.i("update",   "valuePrzychodyAviva      = " + valuePrzychodyAviva);
        Log.i("update",   "zaliczka_podatku         = " + zaliczka_podatku);
        Log.i("update",   "valueRozchodyLuxMed      = " + valueRozchodyLuxMed);
        Log.i("update",   "valueRozchodyMultisport  = " + valueRozchodyMultisport);
        Log.i("update",   "podatekProcent           = " + podatekZusProcent);
        Log.i("update",   "(przychodySuma*podatekProcent) = " + (przychodySuma*podatekZusProcent));
        Log.i("update",   "naRekeValue = " + naRekeValue);
        
        przychod.setText(String.format("%.2f", przychodySuma));
        rozchod.setText(String.format("%.2f", rozchodySuma));
        naReke.setText(String.format("%.2f", naRekeValue));
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

}
