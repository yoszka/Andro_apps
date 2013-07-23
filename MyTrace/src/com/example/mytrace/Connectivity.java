package com.example.mytrace;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Class with connectivity utilities
 * @author Tomasz Jokiel
 */
public class Connectivity {

	/**
	 * Indicates whether the device is online (has an Internet connection)
	 * To use this function need following permissions:
	 * &lt;uses-permission android:name=&quot;android.permission.ACCESS_NETWORK_STATE&quot; /&gt;
	 * @param activity - activity against which Internet access is checked 
	 * @return true if connection is available, false otherwise
	 */	
	public static boolean isOnline(Context ctx)
	{
	    boolean var = false;
	    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    if ( cm.getActiveNetworkInfo() != null ) 
	    {
	        var = true;
	    }
	    return var;		
	}


}