package com.lukekorth.httpebble;

public class Constants {

	public static final String HTTPEBBLE = "httpebble";
	public static final String URL = "https://ofkorth.net/pebble/";

	public static final String GCM_ID = "444472151507";

	public static final String PEBBLE_ADDRESS = "address";

	public static final String HTTPEBBLE_UUID_PREFIX = "9141B628-BC89-498E-B147";
	public static final int HTTP_URL_KEY = 0xFFFF; // a URL to request
	public static final int HTTP_STATUS_KEY = 0xFFFE; // the HTTP status code
	public static final int HTTP_REQUEST_ID_KEY = 0xFFFC; // the request ID specified by the user to return (HTTP requests only)
	public static final int HTTP_CONNECT_KEY = 0xFFFB; // indicates that the watch has (re)connected to the phone app
	public static final int HTTP_APP_ID_KEY = 0xFFF2; // specifies the application’s app ID
	public static final int HTTP_COOKIE_STORE_KEY = 0xFFF0; // Request storing key-value data
	public static final int HTTP_COOKIE_LOAD_KEY = 0xFFF1; // Request loading key-value data
	public static final int HTTP_COOKIE_FSYNC_KEY = 0xFFF3; // Request syncing key-value data
	public static final int HTTP_COOKIE_DELETE_KEY = 0xFFF4; // Request deleting key-value data
	public static final int HTTP_TIME_KEY = 0xFFF5; // Request timezone information
	public static final int HTTP_UTC_OFFSET_KEY = 0xFFF6; // User’s UTC offset
	public static final int HTTP_IS_DST_KEY = 0xFFF7; // Whether DST is in effect
	public static final int HTTP_TZ_NAME_KEY = 0xFFF8; // User’s timezone name
	public static final int HTTP_LOCATION_KEY = 0xFFE0; // Request location information
	public static final int HTTP_LATITUDE_KEY = 0xFFE1; // User’s latitude
	public static final int HTTP_LONGITUDE_KEY = 0xFFE2; // User’s longitude
	public static final int HTTP_ALTITUDE_KEY = 0xFFE3; // User’s altitude

}