package com.lukekorth.httpebble;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    public RegistrationIntentService() {
        super("RegisterIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!TextUtils.isEmpty(Settings.getEmail(this)) && !TextUtils.isEmpty(Settings.getToken(this))) {
            try {
                JSONObject data = new JSONObject();
                data.put("userId", Settings.getEmail(this));
                data.put("userToken", Settings.getToken(this));
                data.put("gcmId", InstanceID.getInstance(this).getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null));
                data.put("purchased", Settings.hasPurchased(this));

                int code = HttpRequest.post("https://ofkorth.net/pebble/register")
                        .send("data=" + data.toString())
                        .code();
                if (code == 200) {
                    Settings.setNeedToRegister(this, false);
                } else {
                    Settings.setNeedToRegister(this, true);
                }
            } catch (IOException | JSONException e) {
                Settings.setNeedToRegister(this, true);
            }
        }
    }
}
