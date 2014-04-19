package com.lukekorth.httpebble;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.lukekorth.httpebble.Constants;
import com.lukekorth.httpebble.RegisterIntentService;

public class NetworkBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        NetworkInfo network = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (network != null && network.isConnected()) {
            if (context.getSharedPreferences(Constants.HTTPEBBLE, Context.MODE_PRIVATE).getBoolean(Constants.NEED_TO_REGISTER, true))
                startWakefulService(context, new Intent(context, RegisterIntentService.class));
        }
	}
}
