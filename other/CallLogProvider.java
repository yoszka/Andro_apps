/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.providers.contacts;

import static com.android.providers.contacts.util.DbQueryUtils.checkForSupportedColumns;
import static com.android.providers.contacts.util.DbQueryUtils.getEqualityClause;
import static com.android.providers.contacts.util.DbQueryUtils.getInequalityClause;

import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import com.android.providers.contacts.util.SelectionBuilder;
import com.google.common.annotations.VisibleForTesting;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.Log;

import java.util.HashMap;
import java.util.Set;

/**
 * Call log content provider.
 */
public class CallLogProvider extends ContentProvider {
    /** Selection clause to use to exclude voicemail records.  */
    private static final String EXCLUDE_VOICEMAIL_SELECTION = getInequalityClause(
            Calls.TYPE, Integer.toString(Calls.VOICEMAIL_TYPE));

    private static final int CALLS = 1;

    private static final int CALLS_ID = 2;

    private static final int CALLS_FILTER = 3;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(CallLog.AUTHORITY, "calls", CALLS);
        sURIMatcher.addURI(CallLog.AUTHORITY, "calls/#", CALLS_ID);
        sURIMatcher.addURI(CallLog.AUTHORITY, "calls/filter/*", CALLS_FILTER);
    }

    private static final HashMap<String, String> sCallsProjectionMap;
    static {

        // Calls projection map
        sCallsProjectionMap = new HashMap<String, String>();
        sCallsProjectionMap.put(Calls._ID, Calls._ID);
        sCallsProjectionMap.put(Calls.NUMBER, Calls.NUMBER);
        sCallsProjectionMap.put(Calls.DATE, Calls.DATE);
        sCallsProjectionMap.put(Calls.DURATION, Calls.DURATION);
        sCallsProjectionMap.put(Calls.TYPE, Calls.TYPE);
        sCallsProjectionMap.put(Calls.NEW, Calls.NEW);
        sCallsProjectionMap.put(Calls.VOICEMAIL_URI, Calls.VOICEMAIL_URI);
        sCallsProjectionMap.put(Calls.IS_READ, Calls.IS_READ);
        sCallsProjectionMap.put(Calls.CACHED_NAME, Calls.CACHED_NAME);
        sCallsProjectionMap.put(Calls.CACHED_NUMBER_TYPE, Calls.CACHED_NUMBER_TYPE);
        sCallsProjectionMap.put(Calls.CACHED_NUMBER_LABEL, Calls.CACHED_NUMBER_LABEL);
        sCallsProjectionMap.put(Calls.COUNTRY_ISO, Calls.COUNTRY_ISO);
        sCallsProjectionMap.put(Calls.GEOCODED_LOCATION, Calls.GEOCODED_LOCATION);
        sCallsProjectionMap.put(Calls.CACHED_LOOKUP_URI, Calls.CACHED_LOOKUP_URI);
        sCallsProjectionMap.put(Calls.CACHED_MATCHED_NUMBER, Calls.CACHED_MATCHED_NUMBER);
        sCallsProjectionMap.put(Calls.CACHED_NORMALIZED_NUMBER, Calls.CACHED_NORMALIZED_NUMBER);
        sCallsProjectionMap.put(Calls.CACHED_PHOTO_ID, Calls.CACHED_PHOTO_ID);
        sCallsProjectionMap.put(Calls.CACHED_FORMATTED_NUMBER, Calls.CACHED_FORMATTED_NUMBER);
    }

    private ContactsDatabaseHelper mDbHelper;
    private DatabaseUtils.InsertHelper mCallsInserter;
    private boolean mUseStrictPhoneNumberComparation;
    private VoicemailPermissions mVoicemailPermissions;
    private CallLogInsertionHelper mCallLogInsertionHelper;

    @Override
    public boolean onCreate() {
        if (Log.isLoggable(Constants.PERFORMANCE_TAG, Log.DEBUG)) {
            Log.d(Constants.PERFORMANCE_TAG, "CallLogProvider.onCreate start");
        }
        final Context context = getContext();
        mDbHelper = getDatabaseHelper(context);
        mUseStrictPhoneNumberComparation =
            context.getResources().getBoolean(
                    com.android.internal.R.bool.config_use_strict_phone_number_comparation);
        mVoicemailPermissions = new VoicemailPermissions(context);
        mCallLogInsertionHelper = createCallLogInsertionHelper(context);
        if (Log.isLoggable(Constants.PERFORMANCE_TAG, Log.DEBUG)) {
            Log.d(Constants.PERFORMANCE_TAG, "CallLogProvider.onCreate finish");
        }
        return true;
    }

    @VisibleForTesting
    protected CallLogInsertionHelper createCallLogInsertionHelper(final Context context) {
        return DefaultCallLogInsertionHelper.getInstance(context);
    }

    @VisibleForTesting
    protected ContactsDatabaseHelper getDatabaseHelper(final Context context) {
        return ContactsDatabaseHelper.getInstance(context);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Tables.CALLS);
        qb.setProjectionMap(sCallsProjectionMap);
        qb.setStrict(true);

        SelectionBuilder selectionBuilder = new SelectionBuilder(selection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);

        int match = sURIMatcher.match(uri);
        switch (match) {
            case CALLS:
                break;

            case CALLS_ID: {
                selectionBuilder.addClause(getEqualityClause(Calls._ID,
                        parseCallIdFromUri(uri)));
                break;
            }

            case CALLS_FILTER: {
                String phoneNumber = uri.getPathSegments().get(2);
                qb.appendWhere("PHONE_NUMBERS_EQUAL(number, ");
                qb.appendWhereEscapeString(phoneNumber);
                qb.appendWhere(mUseStrictPhoneNumberComparation ? ", 1)" : ", 0)");
                break;
            }

            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selectionBuilder.build(), selectionArgs, null, null,
                sortOrder, null);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), CallLog.CONTENT_URI);
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case CALLS:
                return Calls.CONTENT_TYPE;
            case CALLS_ID:
                return Calls.CONTENT_ITEM_TYPE;
            case CALLS_FILTER:
                return Calls.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        boolean isSyncing = values.containsKey("SYNCING");          // Check for SYNCING column and ...
        values.remove("SYNCING");                                   // ... remove it
        checkForSupportedColumns(sCallsProjectionMap, values);
        if(isSyncing){
            // Inserting a voicemail record through call_log requires the voicemail
            // permission and also requires the additional voicemail param set.
            if (hasVoicemailValue(values)) {
                checkIsAllowVoicemailRequest(uri);
                mVoicemailPermissions.checkCallerHasFullAccess();
            }
            if (mCallsInserter == null) {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                mCallsInserter = new DatabaseUtils.InsertHelper(db, Tables.CALLS);
            }
            
            ContentValues copiedValues = new ContentValues(values);
            
            // Add the computed fields to the copied values.
            mCallLogInsertionHelper.addComputedValues(copiedValues);
            
            long rowId = getDatabaseModifier(mCallsInserter).insert(copiedValues);
            if (rowId > 0) {
                return ContentUris.withAppendedId(uri, rowId);
            }
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        checkForSupportedColumns(sCallsProjectionMap, values);
        // Request that involves changing record type to voicemail requires the
        // voicemail param set in the uri.
        if (hasVoicemailValue(values)) {
            checkIsAllowVoicemailRequest(uri);
        }

        SelectionBuilder selectionBuilder = new SelectionBuilder(selection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);
        int updatedRowsOnList = 0;
        
        {
            ContentResolver resolver = getContext().getContentResolver();
            Uri url = Uri.parse("content://com.example.listsynchroner.ListDataProvider/list");
            StringBuffer sb = new StringBuffer();
            if(selectionArgs != null){
                for(String str : selectionArgs){sb.append("[" + str + "]");}
            }
            
            
            Set<String> keys = values.keySet();
            StringBuilder sbValues = new StringBuilder();
            if(keys != null){
                for(String key : keys){
                    sbValues.append(key+"->["+values.get(key)+"]");
                }
            }
            
            Log.v("CallLogProvider", "Update: uri: " + uri.toString() + ", selection: " + selection + ", selectionArgs: " + sb.toString() + ", values: " + sbValues.toString());
            
            Cursor c  = query(uri, null, selection, selectionArgs, null);
            int upd = 0;
            
            while(c.moveToNext()){
                Log.v("CallLogProvider", "ID to update: " + c.getString(c.getColumnIndex(Calls._ID)));
                // here make update from ListDataProvider
                
                String date = c.getString(c.getColumnIndex(Calls.DATE));
                
                try{
                    date = (Long.parseLong(date)*1000)+"";
                }catch(NumberFormatException e){
                    date = "";
                }
    
                ContentValues newValuesList = new ContentValues();
                
                if(values.containsKey(Calls.CACHED_NAME)){
                    newValuesList.put("NAME",    values.getAsString(Calls.CACHED_NAME));
                }
                if(values.containsKey(Calls.NUMBER)){
                    newValuesList.put("NUMBER",    values.getAsString(Calls.NUMBER));
                }
                if(values.containsKey(Calls.DATE)){
                    newValuesList.put("DATE",    values.getAsString(Calls.DATE));
                }
                if(values.containsKey(Calls.TYPE)){
                    newValuesList.put("TYPE",    values.getAsString(Calls.TYPE));
                }
                if(values.containsKey(Calls.NEW)){
                    newValuesList.put("IS_NEW",    values.getAsString(Calls.NEW));
                }
                // ...
                
                // FIXME below is a bug, new values should be retrieve from variable "values"
//                valuesList.put("NAME",    c.getString(c.getColumnIndex(Calls.CACHED_NAME)));
//                valuesList.put("NUMBER",  c.getString(c.getColumnIndex(Calls.NUMBER)));
//                valuesList.put("DATE",    date);
//                valuesList.put("TYPE",    c.getInt(c.getColumnIndex(Calls.TYPE)));
//                valuesList.put("IS_NEW",  c.getInt(c.getColumnIndex(Calls.NEW)));            
                
                if(newValuesList.size() > 0){
                    upd = resolver.update(url, newValuesList, "_ID = ?", new String[]{c.getString(c.getColumnIndex(Calls._ID))});
                    
                    if(upd == 1){
                        updatedRowsOnList++;
                    }
                }
            }
            c.close();        
        }
        
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int matchedUriId = sURIMatcher.match(uri);
        int updatedRows = 0;
        switch (matchedUriId) {
            case CALLS:
                break;

            case CALLS_ID:
                selectionBuilder.addClause(getEqualityClause(Calls._ID, parseCallIdFromUri(uri)));
                break;

            default:
                throw new UnsupportedOperationException("Cannot update URL: " + uri);
        }

        updatedRows = getDatabaseModifier(db).update(Tables.CALLS, values, selectionBuilder.build(),
                selectionArgs);
        
        // TODO after update lists should be synchronized again (ID change ;/ )
        if(updatedRowsOnList != updatedRows){                                                          // If on list different count of entries were updated than in CallLogProvider
            getContext().sendBroadcast(new Intent("com.example.listsynchroner.SYNCHRONIZE_DATA"));         // Notify that lists should be synchronized again to get real state
        }
        
        return updatedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        boolean isSyncing = false;
        if(selection != null){
            isSyncing = selection.equals("SYNCING");
        }
        
        if(isSyncing){
            selection = null;                    // on syncing only all entries from CallLogProvider are cleared
            selectionArgs = null;                // so we don't need selection and selectionArgs
        }
        
        SelectionBuilder selectionBuilder = new SelectionBuilder(selection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);
        int deletedRowsOnList = 0;
        
        if(!isSyncing){
            ContentResolver resolver = getContext().getContentResolver();
            Uri url = Uri.parse("content://com.example.listsynchroner.ListDataProvider/list");
            StringBuffer sb = new StringBuffer();
            if(selectionArgs != null){
                for(String str : selectionArgs){sb.append("[" + str + "]");}
            }
            
            Log.v("CallLogProvider", "uri: " + uri.toString() + ", selection: " + selection + ", selectionArgs: " + sb.toString());
            
            Cursor c  = query(uri, new String[]{Calls._ID}, selection, selectionArgs, null);
            int del = 0;
            
            while(c.moveToNext()){
                Log.v("CallLogProvider", "ID to delete: " + c.getString(c.getColumnIndex(Calls._ID)));
                // here make deletion from ListDataProvider
                del = resolver.delete(url, "_ID = ?", new String[]{c.getString(c.getColumnIndex(Calls._ID))});
                
                if(del == 1){
                    deletedRowsOnList++;
                }
            }
            c.close();
        }
        // and additional make same deletion in CallLogProvider, but after that operation CallLogProvider and ListDataProvider should be synced again, in case of failed deleting on ListDataProvider 

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int matchedUriId = sURIMatcher.match(uri);
        int deletedRows = 0;
        switch (matchedUriId) {
            case CALLS:
                deletedRows = getDatabaseModifier(db).delete(Tables.CALLS,
                        selectionBuilder.build(), selectionArgs);
                break;
                
            default:
                throw new UnsupportedOperationException("Cannot delete that URL: " + uri);
        }
        
        if((!isSyncing) && (deletedRowsOnList != deletedRows)){                                         // If on list different count of entries were deleted than in CallLogProvider
            getContext().sendBroadcast(new Intent("com.example.listsynchroner.SYNCHRONIZE_DATA"));         // Notify that lists should be synchronized again
        }
        
        return deletedRows;
        
    }

    // Work around to let the test code override the context. getContext() is final so cannot be
    // overridden.
    protected Context context() {
        return getContext();
    }

    /**
     * Returns a {@link DatabaseModifier} that takes care of sending necessary notifications
     * after the operation is performed.
     */
    private DatabaseModifier getDatabaseModifier(SQLiteDatabase db) {
        return new DbModifierWithNotification(Tables.CALLS, db, context());
    }

    /**
     * Same as {@link #getDatabaseModifier(SQLiteDatabase)} but used for insert helper operations
     * only.
     */
    private DatabaseModifier getDatabaseModifier(DatabaseUtils.InsertHelper insertHelper) {
        return new DbModifierWithNotification(Tables.CALLS, insertHelper, context());
    }

    private boolean hasVoicemailValue(ContentValues values) {
        return values.containsKey(Calls.TYPE) &&
                values.getAsInteger(Calls.TYPE).equals(Calls.VOICEMAIL_TYPE);
    }

    /**
     * Checks if the supplied uri requests to include voicemails and take appropriate
     * action.
     * <p> If voicemail is requested, then check for voicemail permissions. Otherwise
     * modify the selection to restrict to non-voicemail entries only.
     */
    private void checkVoicemailPermissionAndAddRestriction(Uri uri,
            SelectionBuilder selectionBuilder) {
        if (isAllowVoicemailRequest(uri)) {
            mVoicemailPermissions.checkCallerHasFullAccess();
        } else {
            selectionBuilder.addClause(EXCLUDE_VOICEMAIL_SELECTION);
        }
    }

    /**
     * Determines if the supplied uri has the request to allow voicemails to be
     * included.
     */
    private boolean isAllowVoicemailRequest(Uri uri) {
        return uri.getBooleanQueryParameter(Calls.ALLOW_VOICEMAILS_PARAM_KEY, false);
    }

    /**
     * Checks to ensure that the given uri has allow_voicemail set. Used by
     * insert and update operations to check that ContentValues with voicemail
     * call type must use the voicemail uri.
     * @throws IllegalArgumentException if allow_voicemail is not set.
     */
    private void checkIsAllowVoicemailRequest(Uri uri) {
        if (!isAllowVoicemailRequest(uri)) {
            throw new IllegalArgumentException(
                    String.format("Uri %s cannot be used for voicemail record." +
                            " Please set '%s=true' in the uri.", uri,
                            Calls.ALLOW_VOICEMAILS_PARAM_KEY));
        }
    }

   /**
    * Parses the call Id from the given uri, assuming that this is a uri that
    * matches CALLS_ID. For other uri types the behaviour is undefined.
    * @throws IllegalArgumentException if the id included in the Uri is not a valid long value.
    */
    private String parseCallIdFromUri(Uri uri) {
        try {
            Long id = Long.valueOf(uri.getPathSegments().get(1));
            return id.toString();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid call id in uri: " + uri, e);
        }
    }
}
