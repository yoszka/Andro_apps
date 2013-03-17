package com.android.jsp.smsinterceptor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsInterceptor extends BroadcastReceiver{
	static String TAG = "SmsInterceptor";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG,  "onReceive");
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);  
        
        
        Log.v(TAG,  messages.getMessageBody());
            if(messages.getMessageBody().contains("specialSentenceToDetect")) {
                abortBroadcast();
            }
	}

}
