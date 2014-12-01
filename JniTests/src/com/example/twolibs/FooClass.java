package com.example.twolibs;

public class FooClass {

	public FooClass() {
	}

	public static native int baarMethod();
	public static native boolean someNativMethod();
	public static final native int getMyPid();
	public static final native String getSystemSecureSetting(Object resolver);
}
