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
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

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
    
    
    public void addContactClick(View v){
        addContact("Alan", "666");
    }
    
    public void deleteAllContactsClick(View v){
      deleteAllContacts();       
    }    
    
}
