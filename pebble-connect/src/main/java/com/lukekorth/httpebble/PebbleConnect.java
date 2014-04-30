package com.lukekorth.httpebble;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class PebbleConnect extends BaseActivity implements View.OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((TextView) findViewById(R.id.notifications)).setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) findViewById(R.id.source)).setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.cloud_access).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

        checkForPlayServices();
        if (needToRegister()) {
            startService(new Intent(this, RegisterIntentService.class));
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, CloudAccess.class));
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private void checkForPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Device not supported")
                        .setMessage("This device does not support the required Google services. The app will now close")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                PebbleConnect.this.finish();
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private boolean needToRegister() {
        final SharedPreferences prefs = getSharedPreferences(Constants.HTTPEBBLE, MODE_PRIVATE);

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.STORED_VERSION, Integer.MIN_VALUE);
        int currentVersion = BuildConfig.VERSION_CODE;
        if (registeredVersion != currentVersion) {
            return true;
        }

        return prefs.getString(Constants.REGISTRATION_ID, "").isEmpty();
    }
}