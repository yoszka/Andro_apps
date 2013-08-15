package pl.xt.jokii.db;

import pl.xt.jokii.db.InventoryProviderMetaData.InventoryTableMetaData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public abstract class DbUtils {
	private static boolean DBG = false;
	
	/**
	 * Update given entry id in data base with data from carServEntry
	 * @param _ID			- id of entry in data base
	 * @param inventoryEntry	- new values for update
	 */
	public static void updateEntryDB(Context ctx, InventoryEntry inventoryEntry)
	{	
		//String strFilter = "_ID = "+id;
		ContentValues args = new ContentValues();
		
		args.put(InventoryTableMetaData.INVENTORY_NAME, inventoryEntry.getName());
		args.put(InventoryTableMetaData.INVENTORY_AMOUNT, 	inventoryEntry.getAmount());
		args.put(InventoryTableMetaData.INVENTORY_CATEGORY,  inventoryEntry.getCategory());
		
		ctx.getContentResolver().update(
				Uri.withAppendedPath(InventoryProviderMetaData.InventoryTableMetaData.CONTENT_URI, String.valueOf(inventoryEntry.getId())), 
				args, null, null);			
	}
	
	
	
	
	/**
	 * Insert new entry to data base
	 * @param ctx
	 * @param inventoryEntry
	 */
	public static void insertEntryDB(Context ctx, InventoryEntry inventoryEntry)
	{	
		ContentValues args = new ContentValues();
		args.put(InventoryTableMetaData.INVENTORY_NAME, inventoryEntry.getName());
		args.put(InventoryTableMetaData.INVENTORY_AMOUNT, 	inventoryEntry.getAmount());
		args.put(InventoryTableMetaData.INVENTORY_CATEGORY,  inventoryEntry.getCategory());
		
		ctx.getContentResolver().insert(InventoryProviderMetaData.InventoryTableMetaData.CONTENT_URI, args);		
	}	
	
	
	
	
	/**
	* Get complete entry from bata base
	* @param _ID			 - elemnet id from data base
	* @return InventoryEntry - entry from DB
	*/
	public static InventoryEntry getEntryFromDB(Context ctx, long id)
	{
		InventoryEntry 	inventoryEntry 	= new InventoryEntry();

	  	Cursor cursor = ctx.getContentResolver().query(
	  			Uri.withAppendedPath(InventoryProviderMetaData.InventoryTableMetaData.CONTENT_URI, String.valueOf(id)), 
	  			null, null, null, null);

	    if(cursor.moveToFirst())			//Metoda zwraca FALSE jesli cursor jest pusty
	    { 
	    	inventoryEntry.setId		(cursor.getLong		(cursor.getColumnIndex(InventoryTableMetaData._ID)));
	    	inventoryEntry.setName		(cursor.getString	(cursor.getColumnIndex(InventoryTableMetaData.INVENTORY_NAME)));
	    	inventoryEntry.setCategory	(cursor.getString	(cursor.getColumnIndex(InventoryTableMetaData.INVENTORY_CATEGORY)));
	    	inventoryEntry.setAmount	(cursor.getInt		(cursor.getColumnIndex(InventoryTableMetaData.INVENTORY_AMOUNT)));
	    }
	    else
	    {		      
	      Log.e("ERROR getEntryFromDB", "cursor is empty");         	 
	    }

	    cursor.close();		

	    return inventoryEntry;
	}	
	
	/**
	 * Get entries from data base and put to InventoryResultsSet
	 * @return InventoryResultsSet filed with entries from data base, null if none entry was retrieved 
	 */
	public static InventoryResultsSet retrieveResultSet(Context ctx){
		InventoryResultsSet resultsSet = new InventoryResultsSet();
		
	   	 Cursor cursor = ctx.getContentResolver().query(InventoryProviderMetaData.InventoryTableMetaData.CONTENT_URI, null, null, null, null);
	   	 
	   	 while(cursor.moveToNext()){
	   		InventoryEntry inventoryEntryTmp = new InventoryEntry();
	   		
	   		inventoryEntryTmp.setId			(cursor.getLong		(cursor.getColumnIndex(InventoryTableMetaData._ID)));
	   		inventoryEntryTmp.setName		(cursor.getString	(cursor.getColumnIndex(InventoryTableMetaData.INVENTORY_NAME)));
	   		inventoryEntryTmp.setCategory	(cursor.getString	(cursor.getColumnIndex(InventoryTableMetaData.INVENTORY_CATEGORY)));
	   		inventoryEntryTmp.setAmount		(cursor.getInt		(cursor.getColumnIndex(InventoryTableMetaData.INVENTORY_AMOUNT)));
	   		// type is added automatically inner addEntry() method
	   		
	   		resultsSet.addEntry(inventoryEntryTmp);
	   	 }
	   	cursor.close();
		return resultsSet;
	}
	
	
//	/**
//	 * Send message to external server
//	 * @param mesage
//	 */
//	public static void sendViaPOST(String mesage)
//	{
//		if(mesage.length() > 0)
//		{
//			PostDevice postDevice = new PostDevice("http://www.testeruploadu.w8w.pl/note/index.php?action=new");
//			
//			postDevice.addParameter("tekst"		, mesage);
//			postDevice.addParameter("pas"  		, "carserv");
//			postDevice.addParameter("nots_add"	, "tak");
//			
//			postDevice.send();
//		}
//	}	

}
