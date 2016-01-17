package com.lukekorth.httpebble;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class PebbleConnect extends BaseActivity {

    private Button mSetup;
    private TableLayout mCredentials;
    private TextView mUsername;
    private TextView mToken;
    private Button mReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ((TextView) findViewById(R.id.notifications)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.source)).setMovementMethod(LinkMovementMethod.getInstance());
        mSetup = (Button) findViewById(R.id.setup);
        mCredentials = (TableLayout) findViewById(R.id.credentials);
        mUsername = (TextView) findViewById(R.id.username);
        mToken = (TextView) findViewById(R.id.token);
        mReset = (Button) findViewById(R.id.reset);

        if (!TextUtils.isEmpty(Settings.getEmail(this))) {
            showCredentials();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1).show();
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

        if (Settings.needToRegister(this)) {
            startService(new Intent(this, RegisterIntentService.class));
        }
    }

    private void showCredentials() {
        mSetup.setVisibility(View.GONE);
        mCredentials.setVisibility(View.VISIBLE);
        mUsername.setText(Settings.getEmail(this));
        mToken.setText(Settings.getToken(this));
        mReset.setVisibility(View.VISIBLE);
    }

    public void setup(View v) {
        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
        if (accounts.length == 0) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_emails_found)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            final String[] emails = new String[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                emails[i] = accounts[i].name;
            }

            new AlertDialog.Builder(this)
                    .setTitle(R.string.choose_email)
                    .setSingleChoiceItems(emails, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.setEmail(PebbleConnect.this, emails[which]);
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();

                            if (!TextUtils.isEmpty(Settings.getEmail(PebbleConnect.this))) {
                                showCredentials();

                                Settings.setNeedToRegister(PebbleConnect.this, true);
                                startService(new Intent(PebbleConnect.this, RegisterIntentService.class));
                            }
                        }
                    })
                    .show();
        }
    }

    public void reset(View v) {
        Settings.setEmail(this, null);
        Settings.clearToken(this);
        mCredentials.setVisibility(View.GONE);
        mReset.setVisibility(View.GONE);
        mSetup.setVisibility(View.VISIBLE);
    }
}