package pl.xt.jokii.pushnotifications;


public final class CommonUtilities {
    
    // give your server registration url here
    public static final String SERVER_URL          = "http://locationer.comxa.com/";
    public static final String REGISTRATION_SITE   = "register.php";
    public static final String UNREGISTRATION_SITE = "unregister.php";
    
    public static final String LOCALIZER_PREFERENCES = "LOCALIZER_PREFERENCES";
    public static final String POOLING_ENABLED = "POOLING_ENABLED";    
 
    // Google project id
    public static final String SENDER_ID = "1067864210918";
 
    /**
     * Tag used on log messages.
     */
    static final String TAG = "AndroidHive GCM";
 
    public static final String EXTRA_TOKEN = "token";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_LOCATION_BUNDLE = "extra_location_bundle";
 
    public static final String TOKEN_LOCATION = "location";
    
	/**
	 * Data type: double
	 */
	public final static String KEY_LOCATION_LATITUDE = "location_lat";
	
	/**
	 * Data type: double
	 */
	public final static String KEY_LOCATION_LONGITUDE = "location_lon";
	
	/**
	 * Data type: double
	 */
	public final static String KEY_LOCATION_ACCURACY = "location_acc";
	
	/**
	 * Data type: double
	 */
	public final static String KEY_LOCATION_PROVIDER = "location_provider";
	
	/**
	 * Data type: long
	 */
	public final static String KEY_LOCATION_TIMESTAMP = "location_timestamp";	    
}
