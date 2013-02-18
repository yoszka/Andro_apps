package com.example.listsynchroner;

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
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.HashMap;

public class ListDataProvider extends ContentProvider{
    public static final String AUTHORITY = "com.example.listsynchroner.ListDataProvider";
    public static final String TABLE_NAME = "list";    
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    
    
    /**
     * <P>Type: TEXT</P>
     */
    public static final String NAME = "NAME";
    
    /**
     * <P>Type: TEXT</P>
     */
    public static final String NUMBER = "NUMBER";
    
    /**
     * <P>Type: INTEGER (long)</P>
     */
    public static final String DATE = "DATE";
    
    /**
     * <P>Type: INTEGER (int)</P>
     */    
    public static final String TYPE = "TYPE";
    
    /**
     * <P>TYPE: Boolean (int)</P>
     */    
    public static final String IS_NEW = "IS_NEW";
    
    
    
    public static final String DATABASE_NAME = "listdata.db";
    public static final int DATABASE_VERSION = 1;
    
    // Projection map
    private static HashMap<String, String> sProjectionMap;  
    
    static
    {
        sProjectionMap = new HashMap<String, String>();
    
        sProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
        sProjectionMap.put(NAME,   NAME);
        sProjectionMap.put(NUMBER, NUMBER);
        sProjectionMap.put(DATE,   DATE);
        sProjectionMap.put(TYPE,   TYPE);
        sProjectionMap.put(IS_NEW, IS_NEW);
    }  
    
    // Set URI pattern identifier
    private static final UriMatcher sUriMatcher;
    public static final int COLLECTION_URI_INDICATOR    = 1;
    public static final int SINGLE_URI_INDICATOR        = 2;  
    
    static
    {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME,           COLLECTION_URI_INDICATOR);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#",    SINGLE_URI_INDICATOR);
    }    

    private DatabaseHelper mDbHelper;
    
    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper
    {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Create data base
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("
                +BaseColumns._ID+" INTEGER PRIMARY KEY, "
                +NAME+"     VARCHAR, "
                +NUMBER+"   VARCHAR, "
                +DATE+"     INTEGER, "
                +TYPE+"     INTEGER, "              
                +IS_NEW+"   INTEGER "
                +");");
        }

        // Change data base version
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXIST " + TABLE_NAME);
            onCreate(db);
        }
        
    }

    @Override
    public String getType(Uri uri) {
        
        switch(sUriMatcher.match(uri))
        {
        case COLLECTION_URI_INDICATOR:
            return "vnd.android.cursor.dir/vnd.androidexample.list";
            
        case SINGLE_URI_INDICATOR:
            return "vnd.android.cursor.item/vnd.androidexample.list";
            
        default:
            throw new IllegalArgumentException("Nieznany ident. URI " + uri);
        }
    }
    
    

    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        
        switch(sUriMatcher.match(uri))
        {
        case COLLECTION_URI_INDICATOR:
            qb.setTables(TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            break;
            
        case SINGLE_URI_INDICATOR:
            qb.setTables(TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            qb.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(1));
            break;
            
        default:
            throw new IllegalArgumentException("Undefinied ident. URI" + uri);
        }
        
        // Sort order
        String orderBy;
        
        if(TextUtils.isEmpty(sortOrder)){
            orderBy = "_id DESC";
        }else
        {
            orderBy = sortOrder;
        }
        
        // Open db anr run query
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }   
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        
        // Sprawdzenie ¿¹danego identyfikatora Uri
        if(sUriMatcher.match(uri) != COLLECTION_URI_INDICATOR)
        {
            throw new IllegalArgumentException("Undefinied ident. URI " + uri);
        }
        
        // Sprawdzenie pól danych wejœciowych - wszystkie musz¹ byæ skonfigurowane
        
        if(values.containsKey(NAME) == false)
        {
            throw new IllegalArgumentException("Missing field NAME: " + uri);
        }
        if(values.containsKey(NUMBER) == false)
        {
            throw new IllegalArgumentException("Missing field NUMBER: " + uri);
        }   
        if(values.containsKey(DATE) == false)
        {
            throw new IllegalArgumentException("Missing field DATE: " + uri);
        }
        if(values.containsKey(TYPE) == false)
        {
            throw new IllegalArgumentException("Missing field TYPE: " + uri);
        }
        if(values.containsKey(IS_NEW) == false)
        {
            throw new IllegalArgumentException("Missing field IS_NEW: " + uri);
        }       
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, null, values);
        
        if(rowId > 0)
        {
            Uri insertedUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(insertedUri, null);
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
            count = db.update(TABLE_NAME, values, selection, selectionArgs);
            break;
        case SINGLE_URI_INDICATOR:
            String rowId = uri.getPathSegments().get(1);
            count = db.update(TABLE_NAME, values, 
                    BaseColumns._ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
                    selectionArgs);
            break;
            
        default:
            throw new IllegalArgumentException("Nieznany ident. URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        
        return count;
    }
    
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        
        switch(sUriMatcher.match(uri))
        {
        case COLLECTION_URI_INDICATOR:
            count = db.delete(TABLE_NAME, selection, selectionArgs);
            break;
            
        case SINGLE_URI_INDICATOR:
            String rowId = uri.getPathSegments().get(1);
            count = db.delete(TABLE_NAME,                  
                    BaseColumns._ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
                    selectionArgs);
            break;
            
        default:
            throw new IllegalArgumentException("Undefinied ident. URI " + uri);           
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        
        return count;
    }    

}
