package com.lukekorth.httpebble;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CloudAccess extends BaseActivity {

	private SharedPreferences mPrefs;
    private EditText mUserId;
    private EditText mUserToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_access);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mPrefs = getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);

        ((TextView) findViewById(R.id.explanation)).setMovementMethod(LinkMovementMethod.getInstance());
		mUserId = ((EditText) findViewById(R.id.userId));
        mUserId.setText(mPrefs.getString("userId", ""));
		mUserToken = ((EditText) findViewById(R.id.userToken));
        mUserToken.setText(mPrefs.getString("userToken", ""));
	}

	public void save(View v) {
		mPrefs.edit()
		    .putString("userId", mUserId.getText().toString())
		    .putString("userToken", mUserToken.getText().toString())
		    .putBoolean(Constants.NEED_TO_REGISTER, true)
		    .apply();

        startService(new Intent(this, RegisterIntentService.class));

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
