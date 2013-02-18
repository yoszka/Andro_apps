package com.example.listsynchroner;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

public class StartApp extends Application {
//    private BroadcastReceiver receiver;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Toast.makeText(this, "Started", Toast.LENGTH_LONG).show();
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.BOOT_COMPLETED");
//
//        receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Toast.makeText(getApplicationContext(), "Started on boot", Toast.LENGTH_LONG).show();
//            }
//        };
//
//        registerReceiver(receiver, filter);		
//    }
//
//    @Override
//    public void onTerminate() {
//        super.onTerminate();
//        unregisterReceiver(receiver);
//    }
}
