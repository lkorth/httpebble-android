package com.lukekorth.httpebble.receivers;

import static com.getpebble.android.kit.Constants.APP_UUID;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

import java.util.UUID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.lukekorth.httpebble.Constants;

public class PebbleDataReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String uuid = ((UUID) intent.getSerializableExtra(APP_UUID)).toString().toUpperCase();

		Log.d(Constants.HTTPEBBLE, "Pebble Request UUID: " + uuid);

		if (uuid.startsWith(Constants.HTTPEBBLE_UUID_PREFIX)) {
			int transactionId = intent.getIntExtra(TRANSACTION_ID, Integer.MIN_VALUE);
			String data = intent.getStringExtra(MSG_DATA);

			Log.d(Constants.HTTPEBBLE, "Pebble transaction id: " + transactionId);
			Log.d(Constants.HTTPEBBLE, "Request data: " + data);

			if (transactionId == Integer.MIN_VALUE) {
				Log.d(Constants.HTTPEBBLE, "No transaction id");
				return;
			}

			transactionId &= 0xff;

			if (transactionId >= 0 && transactionId <= 255 && data != null && data.length() != 0) {
				PebbleKit.sendAckToPebble(context, transactionId);

				Intent serviceIntent = new Intent(context, PebbleProxyIntentService.class);
				serviceIntent.putExtras(intent.getExtras());

				context.startService(serviceIntent);
			} else {
				Log.d(Constants.HTTPEBBLE, "Transaction Id out of range, or no data");
			}
		}
	}

}
