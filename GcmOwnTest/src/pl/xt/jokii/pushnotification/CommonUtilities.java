package pl.xt.jokii.pushnotification;

public final class CommonUtilities {
    
    // give your server registration url here
    static final String SERVER_URL = "http://pinnote.zz.mu/register.php";
 
    // Google project id
    static final String SENDER_ID = "1067864210918";
 
 
//    static final String DISPLAY_MESSAGE_ACTION =
//            "com.androidhive.pushnotifications.DISPLAY_MESSAGE";
// 
//    static final String EXTRA_MESSAGE = "message";
 
    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
//    static void displayMessage(Context context, String message) {
//        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
//        intent.putExtra(EXTRA_MESSAGE, message);
//        context.sendBroadcast(intent);
//    }
}
