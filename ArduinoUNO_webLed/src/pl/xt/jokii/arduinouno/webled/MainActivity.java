package pl.xt.jokii.arduinouno.webled;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String HOST_DEFAULT            = "192.168.0.109";
    private static final String APPLICATION_PREFERENCES = "application_reference";
    private static final String HOST                    = "host";
    private EditText          mEditTextHost;
    private SharedPreferences mSharedPreference;
    private Editor            mSharedPreferenceEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mEditTextHost = (EditText) findViewById(R.id.editTextHost);
        mSharedPreference = getSharedPreferences(APPLICATION_PREFERENCES, MODE_PRIVATE);
        mSharedPreferenceEditor = mSharedPreference.edit();
        
        mEditTextHost.setText(getStoredHost());
    }

    public void onClickOn(View v){
        String host = mEditTextHost.getText().toString();
        if(TextUtils.isEmpty(host)){
            host = HOST_DEFAULT;
        }
        Toast.makeText(getApplicationContext(), host, Toast.LENGTH_LONG).show();
        sendGetRequest(host, "1");
    }
    
    public void onClickOff(View v){
        String host = mEditTextHost.getText().toString();
        if(TextUtils.isEmpty(host)){
            host = HOST_DEFAULT;
        }
        Toast.makeText(getApplicationContext(), host, Toast.LENGTH_LONG).show();
        sendGetRequest(host, "0");
    }
    
    public void nClickStoreHost(View v){
        String host = mEditTextHost.getText().toString();
        if(!TextUtils.isEmpty(host)){
            storeHost(host);
            Toast.makeText(getApplicationContext(), host + "\nSaved", Toast.LENGTH_LONG).show();
        }
    }


    private void sendGetRequest(final String host, final String parameterValue) {
        if (Connectivity.isOnline(getApplicationContext())) {
            
            AsyncTask<Void, Void, Void> mRegisterTask;
            mRegisterTask = new AsyncTask<Void, Void, Void>() {
                
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        HttpUtil.httpGet(host, parameterValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    return null;
                }

            };
            mRegisterTask.execute();
            

        } else {
            Toast.makeText(this, "Not online", Toast.LENGTH_LONG).show();
        }

    }
    
    private String getStoredHost() {
        return mSharedPreference.getString(HOST, HOST_DEFAULT);
    }

    private void storeHost(String host) {
        mSharedPreferenceEditor.putString(HOST, host);
        mSharedPreferenceEditor.commit();
    }
}
