package com.lukekorth.httpebble.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.lukekorth.httpebble.Constants;
import com.lukekorth.httpebble.RegisterIntentService;

public class NetworkBroadcastReceiver extends BroadcastReceiver {

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (info.isAvailable()) {
				if (context.getSharedPreferences(Constants.HTTPEBBLE, Context.MODE_PRIVATE).getBoolean(
						"needToRegister", true))
					WakefulIntentService.sendWakefulWork(context, RegisterIntentService.class);
			}
		}
	}

}
