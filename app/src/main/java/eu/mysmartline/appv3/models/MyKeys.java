package eu.mysmartline.appv3.models;


public class MyKeys {
	/**
	 * MY_SMARTLINE_PREFS is used to store shared preferences
	 */
	public static final String MY_SMARTLINE_PREFS = "MY_SMARTLINE_PREFS";
	
	/**
	 * This is the google cloud message registration id
	 */
	public static final String PROPERTY_REG_ID = "PROPERTY_REG_ID";
	
	public static final String PROPERTY_DEVICE_ACTIVATED = "PROPERTY_DEVICE_ACTIVATED";
	public static final String PROPERTY_TEST_MESSAGE_VALUE = "PROPERTY_TEST_MESSAGE_VALUE";
	public static final String PROPERTY_CLOUD_ACTIVATION_ID = "PROPERTY_CLOUD_ACTIVATION_ID";
	public static final String PROPERTY_NAVIGATETO_INTENTION = "PROPERTY_NAVIGATETO_INTENTION";
	public static final String PROPERTY_NAVIGATETO_VALUE = "PROPERTY_NAVIGATETO_VALUE";
	public static final String PROPERTY_NAVIGATETO_SHOW_LINES = "PROPERTY_NAVIGATETO_SHOW_LINES";
	public static final String PROPERTY_NAVIGATETO_SHOW_DISPLAY_PANEL = "PROPERTY_NAVIGATETO_SHOW_DISPLAY_PANEL";
	public static final String PROPERTY_CURRENT_NUMBER = "PROPERTY_CURRENT_NUMBER";
	public static final String PROPERTY_CURRENT_NUMBER_B = "PROPERTY_CURRENT_NUMBER_B";
	public static final String PROPERTY_CURRENT_NUMBER_C = "PROPERTY_CURRENT_NUMBER_C";
	public static final String PROPERTY_LOGTAG = "mysmartlineV3";
	
	/**
	 * value = my-smartline-gcm-v3
	 * this is the the project from the google development console who is 
	 * providing the google cloud messaging services. The android application
	 * does not use this. It is only hear for clarification purpose 
	 */
	public static final String PROPERTY_GCM_PROJECT_ID = "my-smartline-gcm-v3";
	/**value = 288823265606
	 * this is the project number related to the GCM project id. This number
	 * is referred my the GCM registration services inside the APP in
	 * MainActivity as local variable SENDER_ID
	 */
	public static final String PROPERTY_GCM_PROJECT_NUMBER ="288823265606";
	
	/**
	 * this is the root url of the app.
	 * http://mysmartline-web.appspot.com/
	 * http://www.mysmartline.eu/
	 * http://mysmartline.eu/
	 */
	public static final String PROPERTY_HOME = "http://mysmartline.eu/";
	public static final String PROPERTY_PRINTER_SERVICE = "PROPERY_PRINTER_SERVICE";

	public static final String PROPERTY_PRINTER_NAME = "PROPERTY_PRINTER_NAME";

	public static final String PROPERTY_PRINTER_IP = "PROPERTY_PRINTER_IP";
	
	
	/**
	 * The property that this Key points to is a boolean and at the
	 * beginning of the validation process will be set to false.
	 */
	public static final String PROPERTY_RECEVED_WARM_UP_MESSAGE_FROM_SERVER = "PROPERTY_RECEVED_WARM_UP_MESSAGE_FROM_SERVER";
}
