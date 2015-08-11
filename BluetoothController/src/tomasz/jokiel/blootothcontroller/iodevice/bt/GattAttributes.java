package tomasz.jokiel.blootothcontroller.iodevice.bt;

import java.util.HashMap;


public class GattAttributes {
    public  static final String SERVICE_HM_10                                 = "0000ffe0-0000-1000-8000-00805f9b34fb";
    // Other supported HM-10 services
    private static final String SERVICE_GENERIC_ACCESS                        = "00001800-0000-1000-8000-00805f9b34fb";
    private static final String SERVICE_GENERIC_ATTRIBUTE                     = "00001801-0000-1000-8000-00805f9b34fb";

    public  static final String CHARACTERISTIC_HM_10_RX_TX                    = "0000ffe1-0000-1000-8000-00805f9b34fb";
    // Other supported HM-10 characteristics
    private static final String CHARACTERISTIC_DEVICE_NAME                    = "00002a00-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_APPERANCE                      = "00002a01-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_PERIPHERIAL_PRIVACY_FLAG       = "00002a02-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_RECONNECTION_ADDRESS           = "00002a03-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS
                                                                              = "00002a04-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_SERVICE_CHANGED                = "00002a05-0000-1000-8000-00805f9b34fb";

    // below code not user but may be useful, so keep it for now
    @SuppressWarnings("serial")
    private static HashMap<String, String> mAttributes = new HashMap<String,  String>(){{
        // HM-10 Services definition
        put(SERVICE_HM_10,                    "HM 10 Serial");
        put(SERVICE_GENERIC_ACCESS,           "Generic Access Service");
        put(SERVICE_GENERIC_ATTRIBUTE,        "Generic Attribute Service");

        // HM-10 Characteristics
        put(CHARACTERISTIC_HM_10_RX_TX,       "HM-10 RX/TX data");
        put(CHARACTERISTIC_DEVICE_NAME,       "Device Name");
        put(CHARACTERISTIC_APPERANCE,         "Apperance");
        put(CHARACTERISTIC_PERIPHERIAL_PRIVACY_FLAG,   "Peripherial privacy flag");
        put(CHARACTERISTIC_RECONNECTION_ADDRESS,       "Reconnection Address");
        put(CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS, "Peripherial Preferred Connection Parameters");
        put(CHARACTERISTIC_SERVICE_CHANGED,                            "Service Chenges");
    }};

    public static String getUUIDFriendlyName(String uuid, String defaultName) {
        String name = mAttributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
