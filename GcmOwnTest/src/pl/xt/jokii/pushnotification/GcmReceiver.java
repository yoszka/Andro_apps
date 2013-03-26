package pl.xt.jokii.pushnotification;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class GcmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "Registered/Unregistered with GCM server", Toast.LENGTH_LONG).show();
		GcmService.runIntentInService(context, intent);
		setResult(Activity.RESULT_OK, null, null);
	}

}
