package com.lukekorth.httpebble;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.wakeful.WakefulIntentService;

public class CloudAccess extends SherlockActivity {

	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.cloud_access);
		((TextView) findViewById(R.id.explanation)).setMovementMethod(LinkMovementMethod.getInstance());

		mPrefs = getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);

		String userId = mPrefs.getString("userId", "");
		if (!userId.equals("")) {
			((EditText) findViewById(R.id.userId)).setText(userId);
			((EditText) findViewById(R.id.userId)).setFocusable(false);
		}

		((EditText) findViewById(R.id.userToken)).setText(mPrefs.getString("userToken", ""));
	}

	public void save(View v) {
		String userId = ((EditText) findViewById(R.id.userId)).getText().toString();
		String userToken = ((EditText) findViewById(R.id.userToken)).getText().toString();

		Editor editor = mPrefs.edit();
		editor.putString("userId", userId);
		editor.putString("userToken", userToken);
		editor.putBoolean("needToRegister", true);
		editor.commit();

		WakefulIntentService.sendWakefulWork(this, RegisterIntentService.class);

		Toast.makeText(this, "Your values have been saved", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
