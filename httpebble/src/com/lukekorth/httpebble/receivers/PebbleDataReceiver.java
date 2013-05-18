package com.lukekorth.httpebble.receivers;

import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;

public class PebbleDataReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
		PebbleKit.sendAckToPebble(context, transactionId);

		String data = intent.getStringExtra(MSG_DATA);
		if (data != null && data.length() != 0) {
			Log.d("Pebble", data);

			Intent serviceIntent = new Intent(context,
					PebbleProxyIntentService.class);
			serviceIntent.putExtra(MSG_DATA, data);

			context.startService(serviceIntent);
		}
	}

}
