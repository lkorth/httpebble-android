package com.lukekorth.httpebble.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lukekorth.httpebble.Constants;

public class PebbleRequestReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.getpebble.action.app.RECEIVE_ACK"))
			Log.d(Constants.HTTPEBBLE, "Got ACK from Pebble");
		else if (intent.getAction().equals("com.getpebble.action.app.RECEIVE_NACK"))
			Log.d(Constants.HTTPEBBLE, "Got NACK from Pebble");
	}

}
