package com.lukekorth.httpebble.receivers;

import static com.getpebble.android.kit.Constants.APP_UUID;
import static com.getpebble.android.kit.Constants.MSG_DATA;

import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONException;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.lukekorth.httpebble.Constants;

public class PebbleProxyIntentService extends IntentService {

	public PebbleProxyIntentService() {
		super("PebbleDataReceiver");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String data = intent.getStringExtra(MSG_DATA);

		PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"PebbleDataReceiver");
		wakeLock.acquire();

		try {
			PebbleDictionary pebbleDictionary = PebbleDictionary.fromJson(data);
			PebbleDictionary responseDictionary = new PebbleDictionary();

			// http request
			if (pebbleDictionary.getString(Constants.HTTP_URL_KEY) != null) {
				// HTTP_URL_KEY: (char*)"http://example.com/foo" // Any URL
				// of the user's choosing. Both HTTP and HTTPS should be
				// supported.
				// HTTP_COOKIE_KEY: (int32_t)42 // An arbitrary 32-bit
				// signed integer provided by the user, to be sent with the
				// response.
				// HTTP_APP_ID_KEY: (int32_t)84 // An arbitrary 32-bit
				// signed integer that uniquely identifies an individual
				// application
			}
			// timezone infomation
			else if (pebbleDictionary
					.getUnsignedInteger(Constants.HTTP_TIME_KEY) != null) {
				responseDictionary.addInt32(Constants.HTTP_TIME_KEY,
						(int) System.currentTimeMillis() / 1000);

				responseDictionary.addInt32(
						Constants.HTTP_UTC_OFFSET_KEY,
						TimeZone.getDefault().getOffset(
								new Date().getTime()) / 1000);

				responseDictionary.addUint8(Constants.HTTP_IS_DST_KEY,
						(byte) ((TimeZone.getDefault()
								.inDaylightTime(new Date())) ? 1 : 0));

				responseDictionary.addString(Constants.HTTP_TZ_NAME_KEY,
						TimeZone.getDefault().getID());
			}
			// location information
			else if (pebbleDictionary
					.getUnsignedInteger(Constants.HTTP_LOCATION_KEY) != null) {

			}
			// setting entries in key-value store
			else if (pebbleDictionary
					.getInteger(Constants.HTTP_COOKIE_STORE_KEY) != null) {
				// Iterator<PebbleTuple> itr = pebbleDictionary.iterator();

			}
			// removing entries from key-value store
			else if (pebbleDictionary
					.getInteger(Constants.HTTP_COOKIE_LOAD_KEY) != null) {

			}
			// deleting entries from key-value store
			else if (pebbleDictionary
					.getInteger(Constants.HTTP_COOKIE_DELETE_KEY) != null) {

			}
			// fsync
			else if (pebbleDictionary
					.getUnsignedInteger(Constants.HTTP_COOKIE_LOAD_KEY) != null) {

			}

			if (responseDictionary.size() > 0)
				PebbleKit.sendDataToPebble(this,
						(UUID) intent.getSerializableExtra(APP_UUID),
						responseDictionary);
		} catch (JSONException e) {
			// silently fail
		}

		wakeLock.release();
	}

}
