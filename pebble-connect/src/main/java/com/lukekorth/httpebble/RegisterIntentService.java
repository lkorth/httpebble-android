package com.lukekorth.httpebble;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterIntentService extends IntentService {

    public RegisterIntentService() {
        super("RegisterIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);

        if (!TextUtils.isEmpty(Settings.getEmail(this)) && !TextUtils.isEmpty(Settings.getToken(this))) {
            if (TextUtils.isEmpty(Settings.getGCMRegistrationId(this))) {
                try {
                    Settings.setGCMRegistrationId(this, GoogleCloudMessaging.getInstance(this).register(Constants.GCM_ID));
                } catch (IOException ignored) {}

                Settings.setNeedToRegister(this, true);
            }

            register(prefs);
        }

        NetworkBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void register(SharedPreferences prefs) {
        JSONObject data = new JSONObject();
        try {
            data.put("userId", Settings.getEmail(this));
            data.put("userToken", Settings.getToken(this));
            data.put("gcmId", Settings.getGCMRegistrationId(this));
        } catch (JSONException e1) {
            data = new JSONObject();
        }

        int code;
        try {
            HttpRequest response = HttpRequest.post(Constants.REGISTER_URL).send("data=" + data.toString());
            code = response.code();
        } catch (HttpRequestException e) {
            code = 400;
        }

        if (code == 200) {
            Settings.setNeedToRegister(this, false);
        } else {
            Settings.setNeedToRegister(this, true);
        }
    }
}
