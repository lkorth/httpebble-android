package com.lukekorth.httpebble.receivers;

import static com.lukekorth.httpebble.Constants.HTTPEBBLE;
import static com.lukekorth.httpebble.Constants.PEBBLE_ADDRESS;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.lukekorth.httpebble.Constants;

public class PebbleConnectionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.getpebble.action.PEBBLE_CONNECTED")) {
			SharedPreferences sharedPrefs = context.getSharedPreferences(HTTPEBBLE, 0);
			sharedPrefs.edit().putString(PEBBLE_ADDRESS, intent.getStringExtra("address")).commit();

			Log.d(Constants.HTTPEBBLE, "Pebble " + intent.getStringExtra("address") + " connected");
		} else if (intent.getAction().equals("com.getpebble.action.PEBBLE_DISCONNECTED")) {
			Log.d(Constants.HTTPEBBLE, "Pebble " + intent.getStringExtra("address") + " disconnected");
		}
	}

}
