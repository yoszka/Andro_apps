package com.test.testcontentprovider.test;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;

public class Validator extends ActivityInstrumentationTestCase2<MainTestActivity> {

	private Context mContext;



    	@SuppressWarnings("deprecation")
    	public Validator() {
		super("com.test.testcontentprovider", MainTestActivity.class);
	}
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		mContext = this.getInstrumentation().getContext();
	}

	public void testQuery(){
	    Cursor c = mContext.getContentResolver().query(Uri.parse("content://com.test.testcontentprovider.TestContentProvider/TEST"), null, null, null, null);
	    assertNotNull("Cursor is null", c);
	    assertEquals(3, c.getCount());
	}
	
	
    
	
}
