package pl.xt.jokii.pushnotification;
 

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
 
public final class ServerUtilities {
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();
	private static final String TAG = null;
 
//    /**
//     * Register this account/device pair within the server.
//     *
//     */
//    static void register(final Context context, String name, String email, final String regId) {
//        Log.i(TAG, "registering device (regId = " + regId + ")");
//        String serverUrl = SERVER_URL;
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("regId", regId);
//        params.put("name", name);
//        params.put("email", email);
// 
//        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
//        // Once GCM returns a registration id, we need to register on our server
//        // As the server might be down, we will retry it a couple
//        // times.
//        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
//            Log.d(TAG, "Attempt #" + i + " to register");
//            try {
//                displayMessage(context, context.getString(
//                        R.string.server_registering, i, MAX_ATTEMPTS));
//                post(serverUrl, params);
//                GCMRegistrar.setRegisteredOnServer(context, true);
//                String message = context.getString(R.string.server_registered);
//                CommonUtilities.displayMessage(context, message);
//                return;
//            } catch (IOException e) {
//                // Here we are simplifying and retrying on any error; in a real
//                // application, it should retry only on unrecoverable errors
//                // (like HTTP error code 503).
//                Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
//                if (i == MAX_ATTEMPTS) {
//                    break;
//                }
//                try {
//                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
//                    Thread.sleep(backoff);
//                } catch (InterruptedException e1) {
//                    // Activity finished before we complete - exit.
//                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
//                    Thread.currentThread().interrupt();
//                    return;
//                }
//                // increase backoff exponentially
//                backoff *= 2;
//            }
//        }
//        String message = context.getString(R.string.server_register_error, MAX_ATTEMPTS);
//        CommonUtilities.displayMessage(context, message);
//    }
// 
//    /**
//     * Unregister this account/device pair within the server.
//     */
//    static void unregister(final Context context, final String regId) {
//        Log.i(TAG, "unregistering device (regId = " + regId + ")");
//        String serverUrl = SERVER_URL + "/unregister";
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("regId", regId);
//        try {
//            post(serverUrl, params);
//            GCMRegistrar.setRegisteredOnServer(context, false);
//            String message = context.getString(R.string.server_unregistered);
//            CommonUtilities.displayMessage(context, message);
//        } catch (IOException e) {
//            // At this point the device is unregistered from GCM, but still
//            // registered in the server.
//            // We could try to unregister again, but it is not necessary:
//            // if the server tries to send a message to the device, it will get
//            // a "NotRegistered" error message and should unregister the device.
//            String message = context.getString(R.string.server_unregister_error,
//                    e.getMessage());
//            CommonUtilities.displayMessage(context, message);
//        }
//    }
 
    /**
     * Issue a POST request to the server.<br>
     * Example of use:<br><br>
     * {@code
        Map<String, String> params = new HashMap<String, String>();}<br>{@code
        params.put("regId", regId);}<br>{@code
        params.put("name", name);}<br>{@code
        params.put("email", email);}<br>{@code
        post("http://exampledomain.com/register.php", params);}
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    static void post(String endpoint, Map<String, String> params)
            throws IOException {   
 
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            
            bodyBuilder
            	.append(param.getKey())
            	.append('=')
             	.append(param.getValue());
            
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        
        try {
            Log.e("URL", "> " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }
    
    
    /**
     * Issue a POST/GET request to the server.<br>
     * Example of use:<br><br>
     * {@code
        List<NameValuePair> params = new ArrayList<NameValuePair>();}<br>{@code
        params.add(new BasicNameValuePair("email", email));}<br>{@code
        makeHttpRequest("http://exampledomain.com/register.php", "GET" ,params);}
     *
     * @param url http addres
     * @param method "GET" or "POST"
     * @param params request parameters.
     * @return
     */
    public static String makeHttpRequest(String url, String method, List<NameValuePair> params) {
    	InputStream is = null;
    	StringBuilder sb = new StringBuilder();
    	
        // Making HTTP request
        try {
 
            // check for request method
            if(method.equals("POST")){
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
 
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
 
            }else if(method.equals("GET")){
                // request method is GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);
 
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }else{
            	throw new IllegalArgumentException("Bad method: " + method);
            }          
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
 
        return sb.toString();
 
    }    
}