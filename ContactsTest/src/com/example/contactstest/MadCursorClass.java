package com.example.contactstest;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

import java.util.HashMap;
import java.util.Set;

public class MadCursorClass {
    private static final String TAG = "MadCursorClass";
    public static final String COLUMN_01 = "COLUMN_01"; 
    public static final String COLUMN_02 = "COLUMN_02"; 
    public static final String COLUMN_03 = "COLUMN_03"; 
    public static final String COLUMN_04 = "COLUMN_04"; 
    public static final String COLUMN_05 = "COLUMN_05"; 
    public static final String COLUMN_06 = "COLUMN_06"; 
    public static final String COLUMN_07 = "COLUMN_07"; 
    public static final String COLUMN_08 = "COLUMN_08"; 
    public static final String COLUMN_09 = "COLUMN_09"; 
    public static final String COLUMN_10 = "COLUMN_10"; 
    
    public Cursor getHugeCursor(String[] projection){
        Cursor c = createSourceCursor(60);
        MatrixCursor matrixCursor;
        String[] columnRow;
        
        HashMap<String, String> sFullProjection = new HashMap<String, String>();
        
        sFullProjection.put(COLUMN_01, null);
        sFullProjection.put(COLUMN_02, null);
        sFullProjection.put(COLUMN_03, null);
        sFullProjection.put(COLUMN_04, null);
        sFullProjection.put(COLUMN_05, null);
        sFullProjection.put(COLUMN_06, null);
        sFullProjection.put(COLUMN_07, null);
        sFullProjection.put(COLUMN_08, null);
        sFullProjection.put(COLUMN_09, null);
        sFullProjection.put(COLUMN_10, null);        
        
        // if null fulfill whole projection array
        if(projection == null){
            Set<String> keys = sFullProjection.keySet();
            projection = new String[keys.size()];
            int i = 0;
            loki("No column names, create some");
            for(String key : keys){
                projection[i++] = key;
                loki(key);
            }
        }
        
        // Create suitable matrix cursor
        matrixCursor = new MatrixCursor(projection, c.getCount());
        columnRow    = new String[projection.length];
        
        // Iterate through source cursor and fulfill new cursor
        while(c.moveToNext()){
            // Projection array have to contain all output cursor default columns, so some of then could be empty
            sFullProjection.put(COLUMN_01, c.getString(c.getColumnIndex("one")));
            sFullProjection.put(COLUMN_02, c.getString(c.getColumnIndex("two")));
            sFullProjection.put(COLUMN_03, c.getString(c.getColumnIndex("three")));
            sFullProjection.put(COLUMN_04, c.getString(c.getColumnIndex("four")));
            sFullProjection.put(COLUMN_05, null);
            sFullProjection.put(COLUMN_06, "always");
            sFullProjection.put(COLUMN_07, null);
            sFullProjection.put(COLUMN_08, null);
            sFullProjection.put(COLUMN_09, null);
            sFullProjection.put(COLUMN_10, null);
            
            // Prepare row for new cursor
            for(int i = 0; i < columnRow.length; i++){
                columnRow[i] = sFullProjection.get(projection[i]);
            }
            matrixCursor.addRow(columnRow);
        }
        c.close();
        
        return matrixCursor;
    }
    
    private void loki(String txt){
        Log.i(TAG, txt+"");
    }
    
//    private class Person{
//        String name;
//        public Person(String name) {
//            this.name = name;
//        }
//    }
    
    
    private Cursor createSourceCursor(int columnsCount){
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"one", "two", "three", "four"});
//        matrixCursor.addRow(new String[]{"1", "2", "3", "4"});
//        matrixCursor.addRow(new String[]{"aa", "bb", "cc", "dd"});
//        matrixCursor.addRow(new String[]{"qw", "er", "rt", "ty"});
        
        for(int i = 0; i < columnsCount; i++){
            matrixCursor.addRow(new String[]{Math.ceil(Math.random()*1000+1) + "", Math.ceil(Math.random()*1000+1) + "", Math.ceil(Math.random()*1000+1) + "", Math.ceil(Math.random()*1000+1) + ""});
        }
        
        return matrixCursor;
        
    }

}
