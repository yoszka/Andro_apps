package com.test.testcontentprovider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ProviderMetaData {
    public static final String AUTHORITY = "com.test.testcontentprovider.TestContentProvider";
    
    public static final String  DATABASE_NAME = "test.db";
    public static final int     DATABASE_VERSION = 1;
//    public static final String  TABLE_NAME = "TEST";
    
    private ProviderMetaData(){};
    
    public static final class TableMetaData implements BaseColumns 
    {
        private TableMetaData(){};
        public static final String TABLE_NAME = "TEST";
        
        // definicja identyfikatora URI dla typu MIME
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.testcontentprovider";
        
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.testcontentprovider";
        
        public static final String DEFAULT_SORT_ORDER = "_id DESC";
        
        // Dodatkowe kolumny
        //public static final String _ID                = "_ID";        // long     - dostarczane automatyczenie poprzez implementacjê klasy BaseColumns
        
        public static final String NAME          = "name";           // String
        
        public static final String AGE           = "age";            // int
            
        public static final String PHONE_NUMBER  = "phone_number";   // String
        
    }
}
