package com.example.contactstest;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private long mMsTime = 0; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

    }
    

    // Button handlers
    
    public void addContactClick(View v){
        addContact("Alan", "666");
    }
    
    public void deleteAllContactsClick(View v){
      deleteAllContacts();       
    } 
    
    public void onClickUseMadCursor(View v){
        startTimeCount();
        MadCursorClass madCursorClass = new MadCursorClass();
//        Cursor c = madCursorClass.getHugeCursor(null);
        Cursor c = madCursorClass.getHugeCursor(new String[]{MadCursorClass.COLUMN_01, MadCursorClass.COLUMN_03, MadCursorClass.COLUMN_06, MadCursorClass.COLUMN_02, MadCursorClass.COLUMN_05});
        long difference = stopTimeCount();
        
        dumpCursorContent(c);
        c.close();
        Log.d(TAG, "Time spend: " + difference + " ms");
    }    
    

    // Functions
    
    public void addContact(String name, String number){
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, name) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, number) // Number of the person
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build()); // Type of mobile number                    
        try
        {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (RemoteException e)
        { 
            throw new RuntimeException();
        }
        catch (OperationApplicationException e) 
        {
            throw new RuntimeException();
        }  
    }
    
    public void deleteAllContacts(){
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI).build());
        ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI).build());
         
        try
        {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (RemoteException e)
        { 
            throw new RuntimeException("Cant remove contact", e);
        }
        catch (OperationApplicationException e) 
        {
            throw new RuntimeException("Cant remove contact", e);
        }  
    } 
 
    private void dumpCursorContent(Cursor c){
        Log.v(TAG, "############################################");
        while(c.moveToNext()){
            for(String columnName : c.getColumnNames()){
                Log.v(TAG, columnName + ": [" + c.getString(c.getColumnIndex(columnName)) +"]");
            }
            Log.v(TAG, "--------------------------------------------");
        }        
    }
    
    private void startTimeCount(){
        mMsTime = System.currentTimeMillis();
    }
    
    private long stopTimeCount(){
        return System.currentTimeMillis() - mMsTime;
    }
    
    
    
    
    
    
}
