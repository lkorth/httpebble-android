package com.lukekorth.httpebble;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	
	public GCMIntentService() {
		super(Constants.GCM_ID);
	}

	@Override
	protected void onError(Context context, String errorId) {
		context.getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE).edit().putBoolean("needToRegister", true)
				.commit();
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		if (intent.getStringExtra("type").equals("notification")) {
			final Map<String, Object> data = new HashMap<String, Object>();
			data.put("title", intent.getStringExtra("title"));
			data.put("body", intent.getStringExtra("body"));
			final JSONObject jsonData = new JSONObject(data);
			final String notificationData = new JSONArray().put(jsonData).toString();

			final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
			i.putExtra("messageType", "PEBBLE_ALERT");
			i.putExtra("sender", Constants.HTTPEBBLE);
			i.putExtra("notificationData", notificationData);

			Log.d(Constants.HTTPEBBLE, "Notification sent to Pebble: " + notificationData);

			context.sendBroadcast(i);
		}
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		SharedPreferences mPrefs = context.getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);
		Editor editor = mPrefs.edit();
		editor.putString("gcmId", registrationId);
		editor.putBoolean("needToRegister", true);
		editor.commit();

		RegisterIntentService.register(context);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		SharedPreferences mPrefs = context.getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);
		Editor editor = mPrefs.edit();
		editor.putString("gcmId", registrationId);
		editor.putBoolean("needToRegister", true);
		editor.commit();

		RegisterIntentService.register(context);
	}

}
