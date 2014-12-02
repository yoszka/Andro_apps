package com.example.twolibs;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application {
    private static MyApplication mInstance;

    public static MyApplication getInstance() {
        return mInstance;
    }

    public static Context getContext() {
        return mInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        mInstance = this;
        Log.v("##_TwoLibs", "MyApplication.onCreate() " + mInstance);
        super.onCreate();
    }
}
