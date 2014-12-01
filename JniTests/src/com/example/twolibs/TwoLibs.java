/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.twolibs;

import android.app.Activity;
import android.content.ContentResolver;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.os.Build;
import android.os.Bundle;

public class TwoLibs extends Activity
{
	TextView  tv;
	private FooClass mFoo;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tv = (TextView) findViewById(R.id.textView1);
        
        int       x  = 1000;
        int       y  = 42;

        // here, we dynamically load the library at runtime
        // before calling the native method.
        //
        System.loadLibrary("twolib-second");

        int  z = add(x, y);

        tv.setText( "The sum of " + x + " and " + y + " is " + z );
        mFoo = new FooClass();
    }

    public native int add(int  x, int  y);
    public final native String getSystemSecureSetting();
    
    public static int isSELinuxEnforced2(){
    	Log.v("##_TwoLibs", "Called");
//    	Build.getRadioVersion();
    	return 128;
    };
    
    public void onClickMe(View v) {
    	int a = add(1, 2);
    	int b = FooClass.baarMethod();
    	int myPid = FooClass.getMyPid();
//    	String systsemSecureSettingNative = getSystemSecureSetting();
    	String systsemSecureSettingNative = FooClass.getSystemSecureSetting(getContentResolver());
    	tv.setText("Wynik= " + a + ", b=" + b + ", MyPid: " + myPid + "S.S.S = " + systsemSecureSettingNative);
    }
}
