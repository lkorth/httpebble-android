package com.lukekorth.httpebble;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegisterIntentService extends WakefulIntentService {

	public RegisterIntentService() {
		super("RegisterIntentService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		SharedPreferences prefs = getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);

		if (prefs.getString("userId", "").equals("") || prefs.getString("userToken", "").equals(""))
			return;

		if (prefs.getString("gcmId", "").equals("")) {
			try {
				GoogleCloudMessaging.getInstance(this).register(Constants.GCM_ID);
			} catch (IOException e) {
			}
		} else {
			register(this, prefs);
		}
	}

	private void register(Context context, SharedPreferences prefs) {
		JSONObject data = new JSONObject();
		try {
			data.put("userId", prefs.getString("userId", ""));
			data.put("userToken", prefs.getString("userToken", ""));
			data.put("gcmId", prefs.getString("gcmId", ""));
		} catch (JSONException e1) {
			data = new JSONObject();
		}

		int code;
		try {
			HttpRequest response = HttpRequest.post(Constants.URL + "register").send("data=" + data.toString());
			code = response.code();
		} catch (HttpRequestException e) {
			code = 400;
		}

		if (code == 200)
			prefs.edit().putBoolean("needToRegister", false).commit();
		else
			prefs.edit().putBoolean("needToRegister", true).commit();
	}
}
