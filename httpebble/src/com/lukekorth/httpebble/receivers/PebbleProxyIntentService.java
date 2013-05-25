package com.lukekorth.httpebble.receivers;

import static com.getpebble.android.kit.Constants.APP_UUID;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.util.PebbleDictionary.KEY;
import static com.getpebble.android.kit.util.PebbleDictionary.LENGTH;
import static com.getpebble.android.kit.util.PebbleDictionary.TYPE;
import static com.getpebble.android.kit.util.PebbleDictionary.VALUE;
import static com.lukekorth.httpebble.Constants.HTTPEBBLE;
import static com.lukekorth.httpebble.Constants.HTTP_ALTITUDE_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_APP_ID_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_COOKIE_DELETE_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_COOKIE_LOAD_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_COOKIE_STORE_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_IS_DST_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_LATITUDE_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_LOCATION_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_LONGITUDE_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_REQUEST_ID_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_STATUS_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_TIME_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_TZ_NAME_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_URL_KEY;
import static com.lukekorth.httpebble.Constants.HTTP_UTC_OFFSET_KEY;
import static com.lukekorth.httpebble.Constants.PEBBLE_ADDRESS;

import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Base64;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public class PebbleProxyIntentService extends IntentService implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {

	private WakeLock wakelock;

	private boolean waitForLocation;
	private LocationClient mLocationClient;

	private PebbleDictionary responseDictionary;
	private UUID appUUID;

	public PebbleProxyIntentService() {
		super("PebbleProxyIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String data = intent.getStringExtra(MSG_DATA);
		appUUID = (UUID) intent.getSerializableExtra(APP_UUID);

		Log.d("httpebble", "Request from Pebble: " + data);

		PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PebbleProxyIntentService");
		wakelock.acquire();

		waitForLocation = false;

		try {
			PebbleDictionary pebbleDictionary = PebbleDictionary.fromJson(data);
			responseDictionary = new PebbleDictionary();

			// http request
			if (pebbleDictionary.getString(HTTP_URL_KEY) != null) {
				String url = pebbleDictionary.getString(HTTP_URL_KEY);
				pebbleDictionary.remove(HTTP_URL_KEY);

				long requestIdKey = pebbleDictionary.getInteger(HTTP_REQUEST_ID_KEY);
				pebbleDictionary.remove(HTTP_REQUEST_ID_KEY);

				long appIdKey;
				if(pebbleDictionary.getInteger(HTTP_APP_ID_KEY) != null)
					appIdKey = pebbleDictionary.getInteger(HTTP_APP_ID_KEY);
				else
					appIdKey = 0;

				pebbleDictionary.remove(HTTP_APP_ID_KEY);

				JSONObject request = new JSONObject();
				for (PebbleTuple tup : pebbleDictionary) {
					request.put(Integer.toString(tup.key), tup.value);
				}

				HttpRequest response = HttpRequest.post(url).contentType("application/json")
						.header("X-PEBBLE-ID", getSharedPreferences(HTTPEBBLE, 0).getString(PEBBLE_ADDRESS, ""))
						.send(request.toString());

				Log.d("httpebble", "Server request: " + request.toString());

				String responseString = response.body();

				Log.d("httpebble", "Server response: " + responseString);

				JSONObject json = new JSONObject(responseString);
				Iterator<String> keys = json.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					Object value = json.get(key);

					if (value instanceof String)
						responseDictionary.addString(Integer.parseInt(key), (String) value);
					else if (value instanceof Integer)
						responseDictionary.addInt32(Integer.parseInt(key), (Integer) value);
					else if (value instanceof JSONArray) {
						JSONArray arr = (JSONArray) value;
						String width = (String) arr.get(0);
						Object val = arr.get(1);

						if (width.equals("b"))
							responseDictionary.addInt8(Integer.parseInt(key), (byte) (int) (Integer) val);
						else if (width.equals("B"))
							responseDictionary.addUint8(Integer.parseInt(key), (byte) (int) (Integer) val);
						else if (width.equals("s"))
							responseDictionary.addInt16(Integer.parseInt(key), (short) (int) (Integer) val);
						else if (width.equals("S"))
							responseDictionary.addUint16(Integer.parseInt(key), (short) (int) (Integer) val);
						else if (width.equals("i"))
							responseDictionary.addInt32(Integer.parseInt(key), (Integer) val);
						else if (width.equals("I"))
							responseDictionary.addUint32(Integer.parseInt(key), (Integer) val);
						else if (width.equals("d"))
							responseDictionary.addBytes(Integer.parseInt(key),
									Base64.decode((String) val, Base64.DEFAULT));
					}
				}

				responseDictionary.addInt16(HTTP_STATUS_KEY, (short) response.code());
				responseDictionary.addInt8(HTTP_URL_KEY, (byte) ((response.ok()) ? 1 : 0));
				responseDictionary.addInt32(HTTP_REQUEST_ID_KEY, (int) requestIdKey);
				responseDictionary.addInt32(HTTP_APP_ID_KEY, (int) appIdKey);
			}
			// timezone infomation
			else if (pebbleDictionary.getUnsignedInteger(HTTP_TIME_KEY) != null) {
				responseDictionary.addInt32(HTTP_TIME_KEY, (int) (System.currentTimeMillis() / 1000));
				responseDictionary.addInt32(HTTP_UTC_OFFSET_KEY,
						TimeZone.getDefault().getOffset(new Date().getTime()) / 1000);
				responseDictionary.addUint8(HTTP_IS_DST_KEY,
						(byte) ((TimeZone.getDefault().inDaylightTime(new Date())) ? 1 : 0));
				responseDictionary.addString(HTTP_TZ_NAME_KEY, TimeZone.getDefault().getID());
			}
			// location information
			else if (pebbleDictionary.getUnsignedInteger(HTTP_LOCATION_KEY) != null) {
				waitForLocation = true;

				mLocationClient = new LocationClient(this, this, this);
				mLocationClient.connect();
			}
			// setting entries in key-value store
			else if (pebbleDictionary.getInteger(HTTP_COOKIE_STORE_KEY) != null) {
				long httpCookieStoreKey = pebbleDictionary.getInteger(HTTP_COOKIE_STORE_KEY);
				responseDictionary.addUint32(HTTP_COOKIE_STORE_KEY, (int) httpCookieStoreKey);
				pebbleDictionary.remove(HTTP_COOKIE_STORE_KEY);

				long httpAppIdKey = pebbleDictionary.getInteger(HTTP_APP_ID_KEY);
				String appKey = Long.toString(httpAppIdKey);
				responseDictionary.addUint32(HTTP_APP_ID_KEY, (int) httpAppIdKey);
				pebbleDictionary.remove(HTTP_APP_ID_KEY);

				SharedPreferences sharedPrefs = getSharedPreferences(appKey, 0);
				Editor editor = sharedPrefs.edit();
				for(PebbleTuple tuple : pebbleDictionary) {
					editor.putString(Integer.toString(tuple.key), PebbleDictionary.serializeTuple(tuple).toString());
				}
				editor.commit();
			}
			// retrieving entries from key-value store
			else if (pebbleDictionary.getInteger(HTTP_COOKIE_LOAD_KEY) != null) {
				long httpCookieLoadKey = pebbleDictionary.getInteger(HTTP_COOKIE_LOAD_KEY);
				responseDictionary.addUint32(HTTP_COOKIE_LOAD_KEY, (int) httpCookieLoadKey);
				pebbleDictionary.remove(HTTP_COOKIE_LOAD_KEY);

				long httpAppIdKey = pebbleDictionary.getInteger(HTTP_APP_ID_KEY);
				String appKey = Long.toString(httpAppIdKey);
				responseDictionary.addUint32(HTTP_APP_ID_KEY, (int) httpAppIdKey);
				pebbleDictionary.remove(HTTP_APP_ID_KEY);

				SharedPreferences sharedPrefs = getSharedPreferences(appKey, 0);
				for (PebbleTuple tuple : pebbleDictionary) {
					String storedTuple = sharedPrefs.getString(Integer.toString(tuple.key), null);

					if (storedTuple != null) {
						JSONObject o = new JSONObject(storedTuple);
						int key = o.getInt(KEY);
						PebbleTuple.TupleType type = PebbleTuple.TYPE_NAMES.get(o.getString(TYPE));
						PebbleTuple.Width width = PebbleTuple.WIDTH_MAP.get(o.getInt(LENGTH));

						switch (type) {
						case BYTES:
							byte[] bytes = Base64.decode(o.getString(VALUE), Base64.NO_WRAP);
							responseDictionary.addBytes(key, bytes);
							break;
						case STRING:
							responseDictionary.addString(key, o.getString(VALUE));
							break;
						case INT:
							if (width == PebbleTuple.Width.BYTE) {
								responseDictionary.addInt8(key, (byte) o.getInt(VALUE));
							} else if (width == PebbleTuple.Width.SHORT) {
								responseDictionary.addInt16(key, (short) o.getInt(VALUE));
							} else if (width == PebbleTuple.Width.WORD) {
								responseDictionary.addInt32(key, o.getInt(VALUE));
							}
							break;
						case UINT:
							if (width == PebbleTuple.Width.BYTE) {
								responseDictionary.addUint8(key, (byte) o.getInt(VALUE));
							} else if (width == PebbleTuple.Width.SHORT) {
								responseDictionary.addUint16(key, (short) o.getInt(VALUE));
							} else if (width == PebbleTuple.Width.WORD) {
								responseDictionary.addUint32(key, o.getInt(VALUE));
							}
							break;
						}
					}
				}
			}
			// deleting entries from key-value store
			else if (pebbleDictionary.getInteger(HTTP_COOKIE_DELETE_KEY) != null) {
				long httpCookieDeleteKey = pebbleDictionary.getInteger(HTTP_COOKIE_DELETE_KEY);
				responseDictionary.addUint32(HTTP_COOKIE_DELETE_KEY, (int) httpCookieDeleteKey);
				pebbleDictionary.remove(HTTP_COOKIE_DELETE_KEY);

				long httpAppIdKey = pebbleDictionary.getInteger(HTTP_APP_ID_KEY);
				String appKey = Long.toString(httpAppIdKey);
				responseDictionary.addUint32(HTTP_APP_ID_KEY, (int) httpAppIdKey);
				pebbleDictionary.remove(HTTP_APP_ID_KEY);

				SharedPreferences sharedPrefs = getSharedPreferences(appKey, 0);
				Editor editor = sharedPrefs.edit();
				for (PebbleTuple tuple : pebbleDictionary) {
					editor.putString(Integer.toString(tuple.key), null);
				}
				editor.commit();
			}
			// fsync
			else if (pebbleDictionary.getUnsignedInteger(HTTP_COOKIE_LOAD_KEY) != null) {
				responseDictionary.addUint8(HTTP_COOKIE_LOAD_KEY, (byte) 1);

				long httpAppIdKey = pebbleDictionary.getInteger(HTTP_APP_ID_KEY);
				responseDictionary.addUint32(HTTP_APP_ID_KEY, (int) httpAppIdKey);
			}

			if (responseDictionary.size() > 0 && !waitForLocation) {
				Log.d("httpebble", "Data sent to Pebble: " + responseDictionary.toJsonString());

				PebbleKit.sendDataToPebble(this, appUUID, responseDictionary);
			}

		} catch (JSONException e) {
			Log.w("Pebble", "JSONException: " + e.getMessage());
		}

		if (!waitForLocation)
			wakelock.release();
	}

	@Override
	public void onConnected(Bundle dataBundle) {
		Location location = mLocationClient.getLastLocation();
		mLocationClient.disconnect();

		Log.d("httpebble", "Location accuracy: " + location.getAccuracy() + " latitude: " + location.getLatitude()
				+ " longitude: " + location.getLongitude() + " altitude: " + location.getAltitude());

		responseDictionary.addInt32(HTTP_LOCATION_KEY, Float.floatToIntBits(location.getAccuracy()));
		responseDictionary.addInt32(HTTP_LATITUDE_KEY, Float.floatToIntBits((float) location.getLatitude()));
		responseDictionary.addInt32(HTTP_LONGITUDE_KEY, Float.floatToIntBits((float) location.getLongitude()));
		responseDictionary.addInt32(HTTP_ALTITUDE_KEY, Float.floatToIntBits((float) location.getAltitude()));

		Log.d("httpebble", "Data sent to Pebble: " + responseDictionary.toJsonString());

		PebbleKit.sendDataToPebble(this, appUUID, responseDictionary);

		if (wakelock.isHeld())
			wakelock.release();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d("httpebble", "Data sent to Pebble: " + responseDictionary.toJsonString());

		PebbleKit.sendDataToPebble(this, appUUID, responseDictionary);

		if (wakelock.isHeld())
			wakelock.release();
	}

	@Override
	public void onDisconnected() {
		Log.d("httpebble", "Data sent to Pebble: " + responseDictionary.toJsonString());

		PebbleKit.sendDataToPebble(this, appUUID, responseDictionary);

		if (wakelock.isHeld())
			wakelock.release();
	}

}
