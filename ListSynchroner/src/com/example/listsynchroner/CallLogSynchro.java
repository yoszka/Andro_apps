package com.example.listsynchroner;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CallLog.Calls;

public class CallLogSynchro {
    private static int mCounter = 0;

    public static Uri addCall(Context context, int id, String name, String numberType, String number,
            int presentation, int callType, long startTime, int duration, int isNew) {
        final ContentResolver resolver = context.getContentResolver();



        ContentValues values = new ContentValues(5);

        values.put(Calls.NUMBER,    number);
        values.put(Calls.TYPE,      Integer.valueOf(callType));
        values.put(Calls.DATE,      Long.valueOf(startTime));
        values.put(Calls.DURATION,  Long.valueOf(duration));
        values.put(Calls.NEW,       Integer.valueOf(isNew));
        
        if (callType == Calls.MISSED_TYPE) {
            values.put(Calls.IS_READ, Integer.valueOf(0));
        }
        
        values.put(Calls.CACHED_NAME, name);
        values.put(Calls.CACHED_NUMBER_TYPE, numberType);
        values.put(Calls.CACHED_NUMBER_TYPE, numberType);
        values.put(Calls._ID, Integer.valueOf(id));
        values.putNull("SYNCING");


        return resolver.insert(Calls.CONTENT_URI, values);

    }
    
    public static int delleteAllCallLog(Context context){
        return context.getContentResolver().delete(Calls.CONTENT_URI, "SYNCING", null);
    }
}
