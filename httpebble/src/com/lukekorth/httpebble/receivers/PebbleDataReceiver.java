package com.lukekorth.httpebble.receivers;

import android.app.IntentService;
import android.content.Intent;

public class PebbleDataReceiver extends IntentService {

	public PebbleDataReceiver() {
		super("PebbleDataReciver");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
	}

}
