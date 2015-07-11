package tomasz.jokiel.sqlitebackup;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class Backup {

   public static void createBackupFile(Context context, String pathToBackupFile, Uri sourceTableContentUri)
            throws IOException {
        try {
            String backupString = Backup.createBackupString(context, sourceTableContentUri);
            FileUtils.writeToFileOnExternalStorage(pathToBackupFile, backupString);
        } catch (JSONException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public static void restoreFromBackupFile(Context context, String pathToBackupFile, Uri destinationTableContentUri)
                throws IOException {
            String backupString = FileUtils.readFromFileOnExternalStorage(pathToBackupFile);
            Backup.restoreFromBackupString(context, destinationTableContentUri, backupString);
        }

    private static String createBackupString(Context context, Uri tableContentUri) throws JSONException {
        JSONArray jArrayAll = new JSONArray();

        Cursor cursor = context.getContentResolver().query(tableContentUri, null, null, null, null);

        if(cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            int[] columnIndexes = new int[columnNames.length];
            int[] columnTypes = new int[columnNames.length];

            for(int i = 0; i < columnNames.length; i++) {
                columnIndexes[i] = cursor.getColumnIndex(columnNames[i]);
                columnTypes[i] = cursor.getType(columnIndexes[i]);
            }

            do {
                JSONObject jsonEntry = new JSONObject();

                for(int i = 0; i < columnNames.length; i++) {
                    JSONObject jsonMetaEntry = new JSONObject();

                    switch(columnTypes[i]) {
                        case Cursor.FIELD_TYPE_BLOB:
                            jsonMetaEntry.put(String.valueOf(columnTypes[i]), cursor.getBlob(columnIndexes[i]));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            jsonMetaEntry.put(String.valueOf(columnTypes[i]), cursor.getFloat(columnIndexes[i]));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            jsonMetaEntry.put(String.valueOf(columnTypes[i]), cursor.getInt(columnIndexes[i]));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            jsonMetaEntry.put(String.valueOf(columnTypes[i]), cursor.getString(columnIndexes[i]));
                            break;
                        case Cursor.FIELD_TYPE_NULL:
                        default:
                            break;
                    }

                    jsonEntry.put(columnNames[i], jsonMetaEntry);
                }
               
                jArrayAll.put(jsonEntry);
            }while(cursor.moveToNext());
        } else {
            cursor.close();
            return null;
        }

       cursor.close();

       return jArrayAll.toString();
    }

    private static void restoreFromBackupString(Context context, Uri tableContentUri, String backupString) {
        if(TextUtils.isEmpty(backupString)) {
            Log.e("restoreFromBackupString", "backup is empty");
            return;
        }

        Cursor cursor = null;

        try {
            JSONArray jArrayReaded = new JSONArray(backupString);
            int arrayLength = jArrayReaded.length();
            JSONObject entryElement;

            cursor = context.getContentResolver().query(tableContentUri, null, null, null, null);

            if(cursor != null) {
                String[] columnNames = cursor.getColumnNames();

                for(int i = 0; i < arrayLength; i++){
                    entryElement =  (JSONObject) jArrayReaded.get(i);

                    ContentValues args = new ContentValues();

                    for(int n = 0; n < columnNames.length; n++) {
                        if (BaseColumns._ID.equals(columnNames[n])) {
                            continue;
                        }
                        JSONObject entrySubElement = entryElement.getJSONObject(columnNames[n]);
                        Iterator it = entrySubElement.keys();
                        int columnTypeInt = 0;
                        String columnType = null;

                        if(it.hasNext()) {
                            columnType = it.next().toString();
                            columnTypeInt = Integer.valueOf(columnType);
                        } else {
                            continue;
                        }

                        switch (columnTypeInt) {
                            case Cursor.FIELD_TYPE_BLOB:
                                args.put(columnNames[n], (byte[])entrySubElement.get(columnType));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                args.put(columnNames[n], entrySubElement.getDouble(columnType));
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                args.put(columnNames[n], entrySubElement.getInt(columnType));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                args.put(columnNames[n], entrySubElement.getString (columnType));
                                break;
                            case Cursor.FIELD_TYPE_NULL:
                            default:
                                break;
                        }

                    }

                    context.getContentResolver().insert(tableContentUri, args);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            cursor.close();
        }
    }

}
