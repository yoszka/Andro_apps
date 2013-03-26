package pl.xt.jokii.pushnotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	private static final String GOOGLE_API_KEY = "AIzaSyA3DeMKrdtb6gG4kYdnbHbm-JUvm1ouikg";
	private static final String CLIENT_2_REGISTRATION_ID_PREF = "CLIENT_2_REGISTRATION_ID_PREF";
	private static String mClient2RegistrationId = "APA91bHLDuh9uc2GPf81FbeNNuDiLhdbZ1R2izhPHnHfiWzXb62xvQea171qkAn9DqQWBzjD093ZAPFxj4PgNQPoAsEUlZDtzKibILQKppa2cGMPlxJS7sqy1pmhCYGdut2mqxVMdjY5p27MbCq1vOL2y5qsBXXwWPfDIKtFiiVVBYBEsleiQZs";
    private SharedPreferences sharedPrefs;
    private Editor prefsEditor;
	EditText tvMessageWindow;
	Button btnSend;
	Sender sender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvMessageWindow = (EditText) findViewById(R.id.editText1);
		btnSend = (Button) findViewById(R.id.button3);
		sender = new Sender(GOOGLE_API_KEY);
		
		sharedPrefs = getSharedPreferences(CLIENT_2_REGISTRATION_ID_PREF, Activity.MODE_PRIVATE);
		prefsEditor = sharedPrefs.edit();
		mClient2RegistrationId = sharedPrefs.getString(CLIENT_2_REGISTRATION_ID_PREF, mClient2RegistrationId);
	}
	
    public void updateClient2RegistrationId(String text) {
    	mClient2RegistrationId = text;
    	prefsEditor.putString(CLIENT_2_REGISTRATION_ID_PREF, text);
    	prefsEditor.commit();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClickRegister(View v){
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		// sets the app name in the intent
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		registrationIntent.putExtra("sender", CommonUtilities.SENDER_ID);
		startService(registrationIntent);
	}
	
	public void onClickUnregister(View v){
		Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
		unregIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		startService(unregIntent);
	}
	
	public void onClickSend(View v){
//		collapse_key=score_update&time_to_live=108&delay_while_idle=1&data.score=4x8&data.time=15:16.2342&registration_id=42
		tvMessageWindow.setEnabled(false);
		btnSend.setEnabled(false);
		new HttpRequestHelper().execute(tvMessageWindow.getText().toString());
	}
	
	private class HttpRequestHelper extends AsyncTask<String, Void, Void>{
		private static final String TAG = "PostHelper";

		@Override
		protected Void doInBackground(String... params) {
//			List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
//			httpParams.add(new BasicNameValuePair("regId", "APA91bHLDuh9uc2GPf81FbeNNuDiLhdbZ1R2izhPHnHfiWzXb62xvQea171qkAn9DqQWBzjD093ZAPFxj4PgNQPoAsEUlZDtzKibILQKppa2cGMPlxJS7sqy1pmhCYGdut2mqxVMdjY5p27MbCq1vOL2y5qsBXXwWPfDIKtFiiVVBYBEsleiQZs"));
//			httpParams.add(new BasicNameValuePair("message", params[0] + ""));
//			ServerUtilities.makeHttpRequest("http://pinnote.zz.mu/send_message.php", "GET" ,httpParams);
			
			// Use Sender
			Message message = new Message.Builder()
			    .delayWhileIdle(false)
			    .addData("price", params[0] + "")			// "price" is a key value used in target aps
			    .build();
			
//			Log.v(TAG, "mClient2RegistrationId: " + mClient2RegistrationId);
			
			try {
				Result result = sender.send(message, mClient2RegistrationId, 3);
//				Log.v(TAG, "result.getErrorCodeName(): " + result.getErrorCodeName());
				String newClient2RegistrationId = result.getCanonicalRegistrationId();
				if(!TextUtils.isEmpty(newClient2RegistrationId)){
					Log.v(TAG, "Canonical registration ID: " + result.getCanonicalRegistrationId());
					updateClient2RegistrationId(newClient2RegistrationId);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			tvMessageWindow.setEnabled(true);
			btnSend.setEnabled(true);
			Log.v(TAG, "HttpRequest executed");
		}

	}
		

}
