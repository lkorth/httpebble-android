package com.lukekorth.httpebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo network = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (network != null && network.isConnected()) {
            if (Settings.needToRegister(context)) {
                context.startService(new Intent(context, RegistrationIntentService.class));
            }
        }
    }
}
