package com.lukekorth.httpebble;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

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

        if (!prefs.getString("userId", "").isEmpty() && !prefs.getString("userToken", "").isEmpty()) {
            if (prefs.getString(Constants.REGISTRATION_ID, "").isEmpty()) {
                try {
                    String registrationId = GoogleCloudMessaging.getInstance(this).register(Constants.GCM_ID);
                    prefs.edit()
                            .putString(Constants.REGISTRATION_ID, registrationId)
                            .putBoolean(Constants.NEED_TO_REGISTER, true)
                            .commit();
                } catch (IOException e) {
                    prefs.edit().putBoolean(Constants.NEED_TO_REGISTER, true).commit();
                }
            }

            register(prefs);
        }

        NetworkBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void register(SharedPreferences prefs) {
		JSONObject data = new JSONObject();
		try {
			data.put("userId", prefs.getString("userId", ""));
			data.put("userToken", prefs.getString("userToken", ""));
			data.put("gcmId", prefs.getString(Constants.REGISTRATION_ID, ""));
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
            prefs.edit()
                    .putBoolean(Constants.NEED_TO_REGISTER, false)
                    .putInt(Constants.STORED_VERSION, BuildConfig.VERSION_CODE)
                    .commit();
        } else {
            prefs.edit().putBoolean(Constants.NEED_TO_REGISTER, true).commit();
        }
	}
}
