package com.example.twolibs;

import java.util.ArrayList;


public class FooClass {

	public FooClass() {
	}

	public static native int baarMethod();
	public static native boolean someNativMethod();
	public static final native int getMyPid();
	public static final native String getSystemSecureSetting(Object resolver);
	public static final native int getSystemSecureSettingInt(Object resolver);
	public static final native int getInt(Object cr, String name, int def);
	public static final native String[] getSomeStringArray();
	public static final native ArrayList<Object> getInstalledPackages(Object packageManager);
	public static final native Object getApplicationObject();
	public static final native Object getApplicationContext();

}