package com.lukekorth.httpebble.receivers;

import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

import org.json.JSONException;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class PebbleDataReceiver extends IntentService {

	public PebbleDataReceiver() {
		super("PebbleDataReceiver");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
		PebbleKit.sendAckToPebble(this, transactionId);
		
        String data = intent.getStringExtra(MSG_DATA);
        if (data != null && data.length() != 0) {
			PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
			WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"PebbleDataReceiver");
			wakeLock.acquire();

        	try {
                PebbleDictionary pebbleDictionary = PebbleDictionary.fromJson(data);

                //process data here            
            } catch (JSONException e) {
                // silently fail
            }

			wakeLock.release();
        }
	}

}
