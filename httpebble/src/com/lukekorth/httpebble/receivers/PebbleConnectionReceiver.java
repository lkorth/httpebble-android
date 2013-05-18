package com.lukekorth.httpebble.receivers;

import static com.lukekorth.httpebble.Constants.HTTPEBBLE;
import static com.lukekorth.httpebble.Constants.PEBBLE_ADDRESS;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class PebbleConnectionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPrefs = context.getSharedPreferences(HTTPEBBLE, 0);
		sharedPrefs.edit().putString(PEBBLE_ADDRESS, intent.getStringExtra("address")).commit();
	}

}
