package com.lukekorth.httpebble.receivers;

import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

import org.json.JSONException;

import android.app.IntentService;
import android.content.Intent;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class PebbleDataReceiver extends IntentService {

	public PebbleDataReceiver() {
		super("PebbleDataReciver");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
		PebbleKit.sendAckToPebble(this, transactionId);
		
        String data = intent.getStringExtra(MSG_DATA);
        if (data != null && data.length() != 0) {
        	try {
                PebbleDictionary pebbleDictionary = PebbleDictionary.fromJson(data);

                //process data here            
            } catch (JSONException e) {
                // silently fail
            }
        }
	}

}
