package com.example.listsynchroner;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.CallLog.Calls;
import android.widget.Toast;

/**
 * 
 * @author root
 * adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
 * adb shell am broadcast -a com.example.listsynchroner.SYNCHRONIZE_DATA
 * adb shell am broadcast -a com.example.listsynchroner.NOTIFY_CHANGE
 * adb shell am broadcast -a com.example.listsynchroner.ADD_ENTRY
 */
public class StartupBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "BR start", Toast.LENGTH_LONG).show();
        
        String action = intent.getAction();
        
        if(action != null){
            if(action.equals("android.intent.action.BOOT_COMPLETED")){              // initial synchronization, add dummy data
                
                // Init dummy data base
                initDummyDataBase(context);
                
                synchronizeCallLog(context);
            }
            
            if(action.equals("com.example.listsynchroner.SYNCHRONIZE_DATA")){          // data synchronization
                synchronizeCallLog(context);
            }
            
            if(action.equals("com.example.listsynchroner.NOTIFY_CHANGE")){             // Notify CallLog data change
                context.getContentResolver().notifyChange(Calls.CONTENT_URI, null);
            }
            
            if(action.equals("com.example.listsynchroner.ADD_ENTRY")){              // Add dummy entry to 
                ContentValues values = new ContentValues();
                
                values.put(ListDataProvider.NAME,    "New_"+((int)(Math.random()*1000)));
                values.put(ListDataProvider.NUMBER,  ""+((int)(Math.random()*100000)));
                values.put(ListDataProvider.DATE,    ""+((int)(Math.random()*1000000000)));
                values.put(ListDataProvider.TYPE,    (((int)(Math.random()*10))%3)+1);
                values.put(ListDataProvider.IS_NEW,  1);
                
                context.getContentResolver().insert(ListDataProvider.CONTENT_URI, values); 
            }            
        }
    }
    
    
    void synchronizeCallLog(Context ctx){
        // clear CallLog data before syncing
        CallLogSynchro.delleteAllCallLog(ctx);
        
        // Read dummy data base
        Cursor c = ctx.getContentResolver().query(ListDataProvider.CONTENT_URI, null, null, null, ListDataProvider.DATE + " ASC");
        
        // copy all dummy data base entries to Call Log
        while(c.moveToNext()){
            int     id          = c.getInt      (c.getColumnIndex(BaseColumns._ID));
            String  name        = c.getString   (c.getColumnIndex(ListDataProvider.NAME));
            String  number      = c.getString   (c.getColumnIndex(ListDataProvider.NUMBER));
            long    date        = c.getLong     (c.getColumnIndex(ListDataProvider.DATE));
            int     type        = c.getInt      (c.getColumnIndex(ListDataProvider.TYPE));
            int     isNew       = c.getInt      (c.getColumnIndex(ListDataProvider.IS_NEW));
            
            CallLogSynchro.addCall(ctx, id, name, "2", number, 1, type, date, 67, isNew);
        }   
        c.close();
    }
    
    
    void initDummyDataBase(Context ctx){
        ContentValues[] values = new ContentValues[4];
        
        values[0] = new ContentValues();
        values[1] = new ContentValues();
        values[2] = new ContentValues();
        values[3] = new ContentValues();
        
        values[0].put(ListDataProvider.NAME,    "Alan");
        values[0].put(ListDataProvider.NUMBER,  "123456");
        values[0].put(ListDataProvider.DATE,    13465464);
        values[0].put(ListDataProvider.TYPE,    Calls.MISSED_TYPE);
        values[0].put(ListDataProvider.IS_NEW,  1);
        
        values[1].put(ListDataProvider.NAME,    "Alan");
        values[1].put(ListDataProvider.NUMBER,  "123456");
        values[1].put(ListDataProvider.DATE,    134654640003L);
        values[1].put(ListDataProvider.TYPE,    Calls.MISSED_TYPE);
        values[1].put(ListDataProvider.IS_NEW,  1);
        
        values[2].put(ListDataProvider.NAME,    "George");
        values[2].put(ListDataProvider.NUMBER,  "007889");
        values[2].put(ListDataProvider.DATE,    1361216837000L);
        values[2].put(ListDataProvider.TYPE,    Calls.OUTGOING_TYPE);
        values[2].put(ListDataProvider.IS_NEW,  0);
        
        values[3].put(ListDataProvider.NAME,    "Nick");
        values[3].put(ListDataProvider.NUMBER,  "8899654");
        values[3].put(ListDataProvider.DATE,    1391216837000L);
        values[3].put(ListDataProvider.TYPE,    Calls.INCOMING_TYPE);
        values[3].put(ListDataProvider.IS_NEW,  1);
        
        ctx.getContentResolver().bulkInsert (ListDataProvider.CONTENT_URI, values);       
    }

}
