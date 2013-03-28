package pl.xt.jokii.locationreceiver;
 
import static pl.xt.jokii.pushnotifications.CommonUtilities.SENDER_ID;
import static pl.xt.jokii.pushnotifications.CommonUtilities.SERVER_URL;

import com.google.android.gcm.GCMRegistrar;

import pl.xt.jokii.locationreceiver.R;
import pl.xt.jokii.pushnotifications.ServerUtilities;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
 
public class RegisterActivity extends Activity {
    // alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();
    
    // Asyntask
    AsyncTask<Void, Void, Void> mRegisterTask;
    
    // Progres dialog
    ProgressDialog progresDialog;
 
    // Internet detector
    ConnectionDetector cd;
 
    // UI elements
    EditText txtName;
    EditText txtEmail;
 
    // Register button
    Button btnRegister;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
 
        cd = new ConnectionDetector(getApplicationContext());
 
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(RegisterActivity.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
 
        // Check if GCM configuration is set
        if (SERVER_URL == null || SENDER_ID == null || SERVER_URL.length() == 0
                || SENDER_ID.length() == 0) {
            // GCM sernder id / server url is missing
            alert.showAlertDialog(RegisterActivity.this, "Configuration Error!",
                    "Please set your Server URL and GCM Sender ID", false);
            // stop executing code by return
             return;
        }
 
        txtName = (EditText) findViewById(R.id.txtName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        btnRegister = (Button) findViewById(R.id.btnRegister);
 
        /*
         * Click event on Register button
         * */
        btnRegister.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // Read EditText dat
                String name = txtName.getText().toString();
                String email = txtEmail.getText().toString();
 
                // Check if user filled the form
                if(name.trim().length() > 0 && email.trim().length() > 0){
//                    // Launch Main Activity
//                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
// 
//                    // Registering user on our server
//                    // Sending registraiton details to MainActivity
//                    i.putExtra("name", name);
//                    i.putExtra("email", email);
//                    startActivity(i);
//                    finish();
                	String registrationId = registerToGcm(name, email, new OnRegisteredListener() {
						@Override
						public void onRegistered() {
							progresDialog.dismiss();
							Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_LONG).show();
							finish();
						}
					});
                	if(registrationId != null){
                		finish();
                	}else{
                		progresDialog = ProgressDialog.show(getApplicationContext(), "", "Registering...");
                	}
                }else{
                    // user doen't filled that data
                    // ask him to fill the form
                    alert.showAlertDialog(RegisterActivity.this, "Registration Error!", "Please enter your details", false);
                }
            }
        });
    }
    
    
    /**
     * Register to GCM 
     * @param name registration name
     * @param email registration email
     * @param listener listener to listen when registration is done
     * @return registration ID if already registered to GCM, null if not yet registered 
     */
    private String registerToGcm(final String name, final String email, final OnRegisteredListener listener){
    	String registrationId = null;
        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(this);
        Log.v("RegisterActivity", "RegistrationID: " + regId);
 
        // Check if regid already presents
        if (regId.equals("")) {
            // Registration is not present, register now with GCM
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            // Device is already registered on GCM
            if (GCMRegistrar.isRegisteredOnServer(this)) {
                // Skips registration.
                Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_LONG).show();
                registrationId = regId;
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {
 
                    @Override
                    protected Void doInBackground(Void... params) {
                        // Register on server
                        // On server creates a new user
                        ServerUtilities.register(context, name, email, regId);
                        return null;
                    }
 
                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                        listener.onRegistered();
                    }
 
                };
                mRegisterTask.execute();
            }
        }
        
		return registrationId;
    }
    

	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		if (progresDialog != null) {
			progresDialog.dismiss();
		}
		try {
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}
 
}