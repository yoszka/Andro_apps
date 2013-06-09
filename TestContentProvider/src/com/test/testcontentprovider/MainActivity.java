
package com.test.testcontentprovider;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.testcontentprovider.ProviderMetaData.TableMetaData;

public class MainActivity extends Activity {
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        insertData("Tomasz", 27, "132456798");
        insertData("Paulina", 26, "999666333");
        insertData("Damian", 27, "00011122233");
        insertData("Basia", 7, "369");
        
        ll = (LinearLayout) findViewById(R.id.linear_layout_main);
        
        Cursor c = getContentResolver().query(TableMetaData.CONTENT_URI, null, null, null, null);
        
        if(c != null) {
            while(c.moveToNext()) {
                TextView tv = new TextView(this);
                tv.setText(c.getString(c.getColumnIndex(TableMetaData.NAME)));
                ll.addView(tv);
            }
            c.close();
        }
    }
    
    private void insertData(String name, int age, String phoneNumber) {
        ContentValues values = new ContentValues();
        values.put(TableMetaData.NAME, name);
        values.put(TableMetaData.AGE, age);
        values.put(TableMetaData.PHONE_NUMBER, phoneNumber);
        getContentResolver().insert(TableMetaData.CONTENT_URI, values);
        Toast.makeText(getApplicationContext(), "insertData() OK", 0).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
