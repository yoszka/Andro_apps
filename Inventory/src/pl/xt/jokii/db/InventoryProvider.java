package pl.xt.jokii.db;

import java.util.HashMap;

import pl.xt.jokii.db.InventoryProviderMetaData.InventoryTableMetaData;


//import android.database.sqlite.SQLiteOpenHelper;
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

public class InventoryProvider extends ContentProvider{
	private static final String TAG = "InventoryProvider";
	
	// Create projection map
	private static HashMap<String, String> sInventoryProjectionMap;
	
	static
	{
		sInventoryProjectionMap = new HashMap<String, String>();
		
		sInventoryProjectionMap.put(InventoryTableMetaData._ID, 				InventoryTableMetaData._ID);
		
		sInventoryProjectionMap.put(InventoryTableMetaData.INVENTORY_NAME, 		InventoryTableMetaData.INVENTORY_NAME);
		
		sInventoryProjectionMap.put(InventoryTableMetaData.INVENTORY_AMOUNT, 	InventoryTableMetaData.INVENTORY_AMOUNT);
		
		sInventoryProjectionMap.put(InventoryTableMetaData.INVENTORY_CATEGORY, 	InventoryTableMetaData.INVENTORY_CATEGORY);
		
	}
	
	// Mechanism allowing identify pattern for all incoming URI identifiers
	private static final UriMatcher sUriMatcher;
	public static final int INVENTORY_COLLECTION_URI_INDICATOR 	= 1;
	public static final int INVENTORY_SINGLE_URI_INDICATOR 		= 2;
	
	static
	{
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(InventoryProviderMetaData.AUTHORITY, InventoryTableMetaData.TABLE_NAME, 	 	 INVENTORY_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(InventoryProviderMetaData.AUTHORITY, InventoryTableMetaData.TABLE_NAME+ "/#", INVENTORY_SINGLE_URI_INDICATOR);
	}
	
	// Handle onCreate feedback
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
			super(context, InventoryProviderMetaData.DATABASE_NAME, null, InventoryProviderMetaData.DATABASE_VERSION);
			Log.v(TAG, "DatabaseHelper()");
			// TODO Auto-generated constructor stub
		}

		// Create data base
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "DatabaseHelper.onCreate()");
			db.execSQL("CREATE TABLE IF NOT EXISTS "+InventoryTableMetaData.TABLE_NAME+" ("
				+InventoryProviderMetaData.InventoryTableMetaData._ID+" INTEGER PRIMARY KEY, "
				+InventoryTableMetaData.INVENTORY_NAME		+" VARCHAR, "
				+InventoryTableMetaData.INVENTORY_CATEGORY	+" VARCHAR, "
				+InventoryTableMetaData.INVENTORY_AMOUNT	+" INTEGER "				
				+");");
			

			
		}

		// Update data base version
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.v("", "Updating data base version from "+ oldVersion + " to "+ newVersion + ". All old data will be trunctated");
			db.execSQL("DROP TABLE IF EXIST " + InventoryTableMetaData.TABLE_NAME);
			onCreate(db);
//			db.execSQL("ALTER TABLE "+InventoryProviderMetaData.Inventory_TABLE_NAME+" ADD "+InventoryTableMetaData.SERVICE_EXPIRED+" INTEGER");	// Cause SQLiteException Can't upgrade read-only data base
		}
		
	}

	
	
	
	
	@Override
	public String getType(Uri uri) {
		Log.v(TAG, "getType()");
		switch(sUriMatcher.match(uri))
		{
		case INVENTORY_COLLECTION_URI_INDICATOR:
			return InventoryTableMetaData.CONTENT_TYPE;
			
		case INVENTORY_SINGLE_URI_INDICATOR:
			return InventoryTableMetaData.CONTENT_ITEM_TYPE;
			
		default:
			throw new IllegalArgumentException("Nieznany ident. URI " + uri);
		}
	}	
	
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch(sUriMatcher.match(uri))
		{
		case INVENTORY_COLLECTION_URI_INDICATOR:
			qb.setTables(InventoryTableMetaData.TABLE_NAME);
			qb.setProjectionMap(sInventoryProjectionMap);
			break;
			
		case INVENTORY_SINGLE_URI_INDICATOR:
			qb.setTables(InventoryTableMetaData.TABLE_NAME);
			qb.setProjectionMap(sInventoryProjectionMap);
			qb.appendWhere(InventoryTableMetaData._ID + "=" + uri.getPathSegments().get(1));
			break;
			
		default:
			throw new IllegalArgumentException("Unknown ident. URI" + uri);
		}
		
		// Use default sort order if not given
		String orderBy;
		
		if(TextUtils.isEmpty(sortOrder))
		{
			orderBy = InventoryTableMetaData.DEFAULT_SORT_ORDER;
		}
		else
		{
			orderBy = sortOrder;
		}
		
		// open data base and run query
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		// Set notification uri for returning Uri
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}	
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		// Checking Uri
		if(sUriMatcher.match(uri) != INVENTORY_COLLECTION_URI_INDICATOR)
		{
			throw new IllegalArgumentException("Unknown ident. URI " + uri);
		}
		
		// Check input data fields - all have to be configured
		
		if(values.containsKey(InventoryTableMetaData.INVENTORY_NAME) == false)
		{
			throw new IllegalArgumentException("Insertion fail, missing field "+InventoryTableMetaData.INVENTORY_NAME+": " + uri);
		}	
		if(values.containsKey(InventoryTableMetaData.INVENTORY_AMOUNT) == false)
		{
			throw new IllegalArgumentException("Insertion fail, missing field "+InventoryTableMetaData.INVENTORY_AMOUNT+": " + uri);
		}
		if(values.containsKey(InventoryTableMetaData.INVENTORY_CATEGORY) == false)
		{
			throw new IllegalArgumentException("Insertion fail, missing field "+InventoryTableMetaData.INVENTORY_CATEGORY+": " + uri);
		}
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		//long rowId = db.insert(InventoryTableMetaData.TABLE_NAME, /*null*/InventoryTableMetaData.SERVICE_DATE, values);		// ?? InventoryTableMetaData.SERVICE_DATE maybe null instead
		long rowId = db.insert(InventoryTableMetaData.TABLE_NAME, null, values);		// ?? InventoryTableMetaData.SERVICE_DATE maybe null instead
		
		if(rowId > 0)
		{
			Uri insertedUri = ContentUris.withAppendedId(InventoryTableMetaData.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(insertedUri, null);		// notify that Uri has hanged
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
		case INVENTORY_COLLECTION_URI_INDICATOR:
			count = db.update(InventoryTableMetaData.TABLE_NAME, values, selection, selectionArgs);
			break;
		case INVENTORY_SINGLE_URI_INDICATOR:
			String rowId = uri.getPathSegments().get(1);
			count = db.update(InventoryTableMetaData.TABLE_NAME, values, 
					InventoryTableMetaData._ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
					selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown ident. URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);						// notify that Uri has hanged
		
		return count;
	}
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		
		switch(sUriMatcher.match(uri))
		{
		case INVENTORY_COLLECTION_URI_INDICATOR:
			count = db.delete(InventoryTableMetaData.TABLE_NAME, selection, selectionArgs);
			break;
			
		case INVENTORY_SINGLE_URI_INDICATOR:
			String rowId = uri.getPathSegments().get(1);
			count = db.delete(InventoryTableMetaData.TABLE_NAME, 					
					InventoryTableMetaData._ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
					selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Nieznany ident. URI " + uri);			
		}
		
		getContext().getContentResolver().notifyChange(uri, null);						// notify that Uri has hanged
		
		return count;
	}

}
