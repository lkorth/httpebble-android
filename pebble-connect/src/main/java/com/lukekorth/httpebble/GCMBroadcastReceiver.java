package com.lukekorth.httpebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GCMBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("type").equals("notification")) {
            final Map<String, Object> data = new HashMap<String, Object>();
            data.put("title", intent.getStringExtra("title"));
            data.put("body", intent.getStringExtra("body"));
            final JSONObject jsonData = new JSONObject(data);
            final String notificationData = new JSONArray().put(jsonData).toString();

            final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
            i.putExtra("messageType", "PEBBLE_ALERT");
            i.putExtra("sender", Constants.HTTPEBBLE);
            i.putExtra("notificationData", notificationData);

            context.sendBroadcast(i);
        }
    }
}