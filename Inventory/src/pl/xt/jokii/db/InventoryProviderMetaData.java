package pl.xt.jokii.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryProviderMetaData {
	public static final String AUTHORITY = "pl.xt.jokii.db.InventoryProvider";
	
	public static final String 	DATABASE_NAME = "inventory.db";
	public static final int 	DATABASE_VERSION = 2;
	public static final String 	INVENTORY_TABLE_NAME = "Inventories_";
	
	private InventoryProviderMetaData(){};
	
	public static final class InventoryTableMetaData implements BaseColumns 
	{
		private InventoryTableMetaData(){};
		public static final String TABLE_NAME = INVENTORY_TABLE_NAME;
		
		// URI identifiers for MIME type
		public static final Uri    CONTENT_URI 			= Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		
		public static final String CONTENT_TYPE 		= "vnd.android.cursor.dir/vnd.androidInventory.supply";
		
		public static final String CONTENT_ITEM_TYPE 	= "vnd.android.cursor.item/vnd.androidInventory.supply";
		
		public static final String DEFAULT_SORT_ORDER 	= "_id DESC";
		
		// Additional columns
		public static final String INVENTORY_NAME 		= "Name";		// String
		
		public static final String INVENTORY_CATEGORY  = "Category";	// String
			
		public static final String INVENTORY_AMOUNT 	= "Amount";		// int
		
	}
}
