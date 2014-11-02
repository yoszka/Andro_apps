package pl.xt.jokii.pushnotifications.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import pl.xt.jokii.pushnotifications.server.model.LocationDb;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

public class LocationsGetter extends StreamTemplate {
    private static final String           LOCATIONS_PAGE_URL          = "http://locationer.comxa.com/list_locations.php";
    private static final String           LOCATIONS_USERNAME_TAG      = "user_name";
    private static final String           LOCATIONS_JSON_STRING_START = "[{";
    private LocationDb[]                  mLocationsDb;
    private static OnLocationsGetListener mListener;
    private String mRemoteUserNeme;

    public LocationsGetter() {}
    
    public LocationsGetter(String remoteUserNeme) {
        mRemoteUserNeme = remoteUserNeme;
    }
    
    /**
     * @hide
     */
    @Override
    public InputStream createInputStream() throws Exception {
        String urlToDownloadDataFrom = getUrlToDownloadDataFrom(); 
        return new URL(urlToDownloadDataFrom).openStream();
    }

    /**
     * @hide
     */
    @Override
    public void useInputStream(InputStream is) throws Exception {
        String jsonString = getJsonString(is);
        if (!TextUtils.isEmpty(jsonString)) {
            Gson gson = new Gson();
            try {
                mLocationsDb = gson.fromJson(jsonString, LocationDb[].class);
            } catch (IllegalStateException ise) {
                Log.e("LocationsGetter", "IllegalStateException on parsing locations");
                ise.printStackTrace();
            }
        }
    }

    private String getUrlToDownloadDataFrom() {
        String urlToDownloadDataFrom = (mRemoteUserNeme != null) ?
                (LOCATIONS_PAGE_URL + "?" + LOCATIONS_USERNAME_TAG + "=" + mRemoteUserNeme) :
                LOCATIONS_PAGE_URL;
        return urlToDownloadDataFrom;
    }

    /**
     * Method introduced since free hosting pages add after response additional
     * analytics data
     * 
     * @param is
     * @return
     * @throws IOException
     */
    private String getJsonString(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String jsonResult = br.readLine();

        while (jsonResult != null && !jsonResult.startsWith(LOCATIONS_JSON_STRING_START)) {
            jsonResult = br.readLine();
        }

        return jsonResult;
    }

    /**
     * Synchronous locations loader
     * 
     * @return
     */
    private void loadLocations() {
        execute();
    }

    /**
     * Locations getter
     * 
     * @return
     */
    private LocationDb[] getLocations() {
        return mLocationsDb;
    }

    /**
     * Asynchronous locations getter
     * 
     * @param listener
     */
    public static void getLocations(String remoteUserNeme, OnLocationsGetListener listener) {
        if (listener == null) {
            throw new NullPointerException("OnLocationsGetListener - listener cannot be null.");
        }
        mListener = listener;
        new LocationsAsyncGetter().execute(remoteUserNeme);
    }

    /**
     * Auxiliary class to perform background operation
     * 
     * @author Tomek
     * 
     */
    private static class LocationsAsyncGetter extends AsyncTask<String, Void, LocationDb[]> {
        @Override
        protected LocationDb[] doInBackground(String... params) {
            String remoteUserNeme = params[0];
            LocationsGetter ug = new LocationsGetter(remoteUserNeme);
            ug.loadLocations();
            return ug.getLocations();
        }

        @Override
        protected void onPostExecute(LocationDb[] locations) {
            super.onPostExecute(locations);
            if (mListener != null) {
                mListener.onLocationsGet(locations);
            }
        }
    }
}
