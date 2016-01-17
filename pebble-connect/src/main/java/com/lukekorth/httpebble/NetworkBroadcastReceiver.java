package com.lukekorth.httpebble;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.WakefulBroadcastReceiver;

public class NetworkBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo network = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (network != null && network.isConnected()) {
            if (Settings.needToRegister(context)) {
                startWakefulService(context, new Intent(context, RegisterIntentService.class));
            }
        }
    }
}
