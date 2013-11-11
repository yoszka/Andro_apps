package pl.xt.jokii.inventory;

import android.util.Log;

public class Debug {

    public static final void log(String method){
        log(method, null);
    }

    public static final void log(String method, Object message){
        if(message == null){
            message = new String();
        }
        if(BuildConfig.DEBUG){
            Log.d("D: "+method, message.toString());
        }
    }
}
