package com.lukekorth.httpebble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context context, String errorId) {
		context.getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE).edit().putBoolean("needToRegister", true)
		.commit();
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		SharedPreferences mPrefs = context.getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);
		Editor editor = mPrefs.edit();
		editor.putString("gcmId", registrationId);
		editor.putBoolean("needToRegister", true);
		editor.commit();

		RegisterIntentService.register(context);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		SharedPreferences mPrefs = context.getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);
		Editor editor = mPrefs.edit();
		editor.putString("gcmId", registrationId);
		editor.putBoolean("needToRegister", true);
		editor.commit();

		RegisterIntentService.register(context);
	}

}
