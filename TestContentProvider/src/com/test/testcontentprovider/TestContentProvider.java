package com.test.testcontentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.test.testcontentprovider.ProviderMetaData.TableMetaData;

import java.util.HashMap;

public class TestContentProvider extends ContentProvider{

    private static final String TAG = "TestContentProvider";
    
    // Create projection map
    private static HashMap<String, String> sProjectionMap;
    
    static
    {
        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(TableMetaData._ID,           TableMetaData._ID);
        sProjectionMap.put(TableMetaData.NAME,          TableMetaData.NAME);
        sProjectionMap.put(TableMetaData.PHONE_NUMBER,  TableMetaData.PHONE_NUMBER);
        sProjectionMap.put(TableMetaData.AGE,           TableMetaData.AGE);
        
    }
    
    // Mechanism for identify pattern for all incoming URI identifiers
    private static final UriMatcher sUriMatcher;
    public static final int COLLECTION_URI_INDICATOR    = 1;
    public static final int SINGLE_URI_INDICATOR        = 2;
    
    static
    {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ProviderMetaData.AUTHORITY, TableMetaData.TABLE_NAME,      COLLECTION_URI_INDICATOR);
        sUriMatcher.addURI(ProviderMetaData.AUTHORITY, TableMetaData.TABLE_NAME+"/#", SINGLE_URI_INDICATOR);
    }
    
    // Handle call onCreate
    private DatabaseHelper mDbHelper;
    
    @Override
    public boolean onCreate() {
        Log.v(TAG, "onCreate");
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper
    {

        public DatabaseHelper(Context context) {
            super(context, ProviderMetaData.DATABASE_NAME, null, ProviderMetaData.DATABASE_VERSION);
            Log.v(TAG, "DatabaseHelper()");
        }

        // Create data base
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(TAG, "DatabaseHelper.onCreate()");
            db.execSQL("CREATE TABLE IF NOT EXISTS "+TableMetaData.TABLE_NAME+" ("
                +ProviderMetaData.TableMetaData._ID+" INTEGER PRIMARY KEY, "
                +TableMetaData.NAME+" VARCHAR, "
                +TableMetaData.PHONE_NUMBER+" VARCHAR, "              
                +TableMetaData.AGE+" INTEGER"
                +");");
            

            
        }

        // Change version of data base
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v("", "Update data base from version "+ oldVersion + " to version "+ newVersion + ". All data will be dropped");
            db.execSQL("DROP TABLE IF EXIST " + TableMetaData.TABLE_NAME);
            onCreate(db);
//          db.execSQL("ALTER TABLE "+CarServProviderMetaData.CARSERV_TABLE_NAME+" ADD "+CarServTableMetaData.SERVICE_EXPIRED+" INTEGER"); // will cause exception, need to create new data base
        }
        
    }

    
    
    
    
    @Override
    public String getType(Uri uri) {
        Log.v(TAG, "getType()");
        switch(sUriMatcher.match(uri))
        {
        case COLLECTION_URI_INDICATOR:
            return TableMetaData.CONTENT_TYPE;
            
        case SINGLE_URI_INDICATOR:
            return TableMetaData.CONTENT_ITEM_TYPE;
            
        default:
            throw new IllegalArgumentException("Unknown URI ident. URI " + uri);
        }
    }   
    
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        
        switch(sUriMatcher.match(uri))
        {
        case COLLECTION_URI_INDICATOR:
            qb.setTables(TableMetaData.TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            break;
            
        case SINGLE_URI_INDICATOR:
            qb.setTables(TableMetaData.TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            qb.appendWhere(TableMetaData._ID + "=" + uri.getPathSegments().get(1));
            break;
            
        default:
            throw new IllegalArgumentException("Unknown URI ident. URI " + uri);
        }
        
        // If sort order isn't provided then use default sort order value
        String orderBy;
        
        if(TextUtils.isEmpty(sortOrder))
        {
            orderBy = TableMetaData.DEFAULT_SORT_ORDER;
        }
        else
        {
            orderBy = sortOrder;
        }
        
        // Opening data base and run query
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        
        // Set notfication uri for cursor for notification
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }   
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        
        // Check URI type
        if(sUriMatcher.match(uri) != COLLECTION_URI_INDICATOR)
        {
            throw new IllegalArgumentException("Unknown URI ident. URI " + uri);
        }
        
        // Checking input data fields if create complete set
        if(values.containsKey(TableMetaData.NAME) == false)
        {
            throw new IllegalArgumentException("Insertion fail, missing field NAME: " + uri);
        }   
        if(values.containsKey(TableMetaData.PHONE_NUMBER) == false)
        {
            throw new IllegalArgumentException("Insertion fail, missing field PHONE_NUMBER: " + uri);
        }
        if(values.containsKey(TableMetaData.AGE) == false)
        {
            throw new IllegalArgumentException("Insertion fail, missing field AGE: " + uri);
        }
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //long rowId = db.insert(CarServTableMetaData.TABLE_NAME, /*null*/CarServTableMetaData.SERVICE_DATE, values);       // ?? CarServTableMetaData.SERVICE_DATE maybe null instead
        long rowId = db.insert(TableMetaData.TABLE_NAME, null, values);      // ?? CarServTableMetaData.SERVICE_DATE maybe null instead
        
        if(rowId > 0)
        {
            Uri insertedUri = ContentUris.withAppendedId(TableMetaData.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(insertedUri, null);                      // notify that data base has changed
            return insertedUri;
        }
        
        throw new IllegalArgumentException("Insertion fail in " + uri);
    }   
    
    
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        
        switch(sUriMatcher.match(uri))
        {
        case COLLECTION_URI_INDICATOR:
            count = db.update(TableMetaData.TABLE_NAME, values, selection, selectionArgs);
            break;
        case SINGLE_URI_INDICATOR:
            String rowId = uri.getPathSegments().get(1);
            count = db.update(TableMetaData.TABLE_NAME, values, 
                    TableMetaData._ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
                    selectionArgs);
            break;
            
        default:
            throw new IllegalArgumentException("Unknown URI ident. URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);                                  // notify that data base has changed
        
        return count;
    }
    
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        
        switch(sUriMatcher.match(uri))
        {
        case COLLECTION_URI_INDICATOR:
            count = db.delete(TableMetaData.TABLE_NAME, selection, selectionArgs);
            break;
            
        case SINGLE_URI_INDICATOR:
            String rowId = uri.getPathSegments().get(1);
            count = db.delete(TableMetaData.TABLE_NAME,                  
                    TableMetaData._ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
                    selectionArgs);
            break;
            
        default:
            throw new IllegalArgumentException("Unknown URI ident. URI " + uri);           
        }
        
        getContext().getContentResolver().notifyChange(uri, null);                                  // notify that data base has changed
        
        return count;
    }


}
