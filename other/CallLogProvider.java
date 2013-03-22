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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        printParametersToLog("QUERY", uri, projection, selection, selectionArgs, sortOrder, null);
        SelectionResolver(selection, selectionArgs);

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
        checkForSupportedColumns(sCallsProjectionMap, values);
        // Inserting a voicemail record through call_log requires the voicemail
        // permission and also requires the additional voicemail param set.
        
        printParametersToLog("INSERT", uri, null, null, null, null, values);
        
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
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        checkForSupportedColumns(sCallsProjectionMap, values);
        // Request that involves changing record type to voicemail requires the
        // voicemail param set in the uri.
        
        printParametersToLog("UPDATE", uri, null, selection, selectionArgs, null, values);
        SelectionResolver(selection, selectionArgs);
        
        if (hasVoicemailValue(values)) {
            checkIsAllowVoicemailRequest(uri);
        }

        SelectionBuilder selectionBuilder = new SelectionBuilder(selection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int matchedUriId = sURIMatcher.match(uri);
        switch (matchedUriId) {
            case CALLS:
                break;

            case CALLS_ID:
                selectionBuilder.addClause(getEqualityClause(Calls._ID, parseCallIdFromUri(uri)));
                break;

            default:
                throw new UnsupportedOperationException("Cannot update URL: " + uri);
        }

        return getDatabaseModifier(db).update(Tables.CALLS, values, selectionBuilder.build(),
                selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder selectionBuilder = new SelectionBuilder(selection);
        checkVoicemailPermissionAndAddRestriction(uri, selectionBuilder);
        
        printParametersToLog("DELETE", uri, null, selection, selectionArgs, null, null);
        SelectionResolver(selection, selectionArgs);

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int matchedUriId = sURIMatcher.match(uri);
        switch (matchedUriId) {
            case CALLS:
                return getDatabaseModifier(db).delete(Tables.CALLS,
                        selectionBuilder.build(), selectionArgs);
            default:
                throw new UnsupportedOperationException("Cannot delete that URL: " + uri);
        }
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
    
    private void printParametersToLog(String text, Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder, ContentValues values) {
        
        StringBuilder sbProjection = new StringBuilder();
        StringBuilder sbSelectionArgs = new StringBuilder();
        StringBuilder sbValues = new StringBuilder();
        
        if(projection != null) {
            for(String proj : projection) {
                sbProjection.append("[" + proj + "]");
            }
        }
        
        if(selectionArgs != null) {
            for(String sel : selectionArgs) {
                sbSelectionArgs.append("[" + sel + "]");
            }
        }
        
        if(values != null) {
            Set<String> keySet = values.keySet();
            Iterator<String> it = keySet.iterator();
            
            while(it.hasNext()) {
                String key = it.next();
                sbValues.append(key + "->["+values.get(key) + "]");
            }                
        }
        
        Log.v("_CallLogProvider", text 
                + ": uri=" + uri.toString() 
//                + ", projection=" + sbProjection.toString() 
                + ", selection=" + selection 
                + ", selectionArgs=" + sbSelectionArgs.toString() 
                + ", sortOrder" + sortOrder 
                + ", values=" + sbValues.toString());
    }
    
    
    
    // ********************************************************************************************************************************
    // ********************************************************************************************************************************
    // ********************************************************************************************************************************
    // ********************************************************************************************************************************
    private static void SelectionResolver(String selection, String[] selectionArgs){
        Map.Entry<Boolean, String> entry;
        ParameterPair parameterPair = null;
        boolean exist_is_read = false;
        String value_is_read = null;
        boolean exist_new = false;
        String value_new = null;
        boolean exist_type = false;
        String value_type = null;
        boolean exist_date = false;
        String value_date = null;
        String parameter_date = null;
        String[] IDsArray = null;
        
        // Merge selection and selectionArgs
        if((selection != null) && (selectionArgs != null)){
            selection = selection.replaceAll("\\?", "%s");
            
            switch(selectionArgs.length){
            case 1:
                selection = String.format(selection, selectionArgs[0]);
                break;
            case 2:
                selection = String.format(selection, selectionArgs[0], selectionArgs[1]);
                break;
            case 3:
                selection = String.format(selection, selectionArgs[0], selectionArgs[1], selectionArgs[2]);
                break;
            case 4:
                selection = String.format(selection, selectionArgs[0], selectionArgs[1], selectionArgs[2], selectionArgs[3]);
                break;
            }
        }
        System.out.println("MERGED: "+selection);
        
        IDsArray = extractIDsArray(selection);
        
        parameterPair = getValuePairParameter(selection, "is_read");
        exist_is_read = parameterPair.isKey_exist();
        value_is_read = parameterPair.getValue();
        
        parameterPair = getValuePairParameter(selection, "new");
        exist_new = parameterPair.isKey_exist();
        value_new = parameterPair.getValue();   
        
        parameterPair = getValuePairParameter(selection, "type");
        exist_type = parameterPair.isKey_exist();
        value_type = parameterPair.getValue();
        
        parameterPair = getValuePairParameter(selection, "date");
        exist_date = parameterPair.isKey_exist();
        value_date = parameterPair.getValue();
        parameter_date = parameterPair.getParameter();
        
        StringBuilder sbISs = new StringBuilder();
        if(IDsArray != null) {
            for(String str : IDsArray) {
                sbISs.append("["+str+"]");
            }
        }
        
//        System.out.println("PARSED: " + ((exist_is_read)? "READ" 
        Log.i("_CallLogProvider","PARSED: " + ((exist_is_read)? "READ" 
                + ", value_is_read = " + value_is_read 
                + ", " + (isParameterNegated(selection, "is_read")?"NEGATED ### ":" ### "): "")
                
                + ((exist_new)? " NEW" 
                + ", value_new = " + value_new 
                + ", " + (isParameterNegated(selection, "new")?"NEGATED ### ":" ### "): "")
                
                + ((exist_type)? " TYPE" 
                + ", value_type = " + value_type 
                + ", " + (isParameterNegated(selection, "type")?"NEGATED ### ":" ### "): "")
                
                + ((exist_date)? " DATE" 
                + ", value_date = " + value_date 
                + ", parameter_date: " + parameter_date
                + ", "+ (isParameterNegated(selection, "date")?"NEGATED ### ":" ### "): "")
                
                + " IDs = " + sbISs.toString());
    }
    
    static class ParameterPair{
        final boolean key_exist;
        final String value;
        final String parameter;
        
        public ParameterPair(boolean key_exist, String value, String parameter) {
            this.key_exist = key_exist;
            this.value = value;
            this.parameter = parameter;
        }
        
        public boolean isKey_exist() {
            return key_exist;
        }

        public String getValue() {
            return value;
        }

        public String getParameter() {
            return parameter;
        }
    }
    
    
//    static Map.Entry<Boolean, String> getValuePairEqual(String inputText, String key){
//        boolean exist_key = false;
//        String value = null;
//        
//        if(inputText != null){
//            final String pattern = ".*(("+key+")\\s*[=]\\s*\\d+).*";
//            Pattern p = Pattern.compile(pattern);
//            Matcher m = p.matcher(inputText);
//            
//            // Check if pattern matched at all, then parse it
//            if(m.find()){
//                exist_key = true;
//                String substringIsRead = (String) inputText.replaceAll(pattern, "$1");
//                value = (String) substringIsRead.replaceAll("[^0-9]", "");
//            }
//        }
//        
////      Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>("Not Unique key1","1");
//        return new AbstractMap.SimpleEntry<>(exist_key, value);
//    }
    
    static ParameterPair getValuePairParameter(String inputText, String key){
        boolean exist_key = false;
        String value = null;
        String parameter = null;
        
        if(inputText != null){
            final String pattern = ".*(("+key+")\\s*[<=>]\\s*\\d+).*";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(inputText);
            
            // Check if pattern matched at all, then parse it
            if(m.find()){
                exist_key = true;
                String substringIsRead = (String) inputText.replaceAll(pattern, "$1");
                value = (String) substringIsRead.replaceAll("[^0-9]", "");
                parameter = (String) substringIsRead.replaceAll("\\w", "");
            }
        }
        
//      Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>("Not Unique key1","1");
        return new ParameterPair(exist_key, value, parameter);
    }
    
    static boolean isParameterNegated(String inputText, String key){
        // NOT (is_read IS NOT NULL AND is_read = 0 AND date > ?)
        
        if(inputText != null){
            final String pattern = ".*(NOT)\\s*(\\()(.*("+key+")\\s*[<=>]\\s*\\d+).*\\).*";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(inputText);
            
            return m.find();
        }
        
        return false;
    }
    
    static String[] extractIDsArray(String inputString){
        if(inputString != null) {
            final String pattern = ".*((_id)\\s*(IN)\\s*\\({1})(.*)(\\)).*";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(inputString);
            
            // Check if pattern matched at all, then parse it
            if(m.find()){
                String subStringWithIds = (String) inputString.replaceAll(pattern, "$4");
                return subStringWithIds.split(",");
            }
        }

        return null;
    }
    
    
}
