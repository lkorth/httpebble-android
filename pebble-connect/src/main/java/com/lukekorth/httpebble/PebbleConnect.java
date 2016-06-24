package com.lukekorth.httpebble;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.lukekorth.httpebble.billing.IabHelper;
import com.lukekorth.httpebble.billing.IabResult;
import com.lukekorth.httpebble.billing.Inventory;
import com.lukekorth.httpebble.billing.Purchase;

import static android.Manifest.permission.GET_ACCOUNTS;

public class PebbleConnect extends AppCompatActivity {

    private IabHelper mIabHelper;
    private Button mSetup;
    private TableLayout mCredentials;
    private TextView mUsername;
    private TextView mToken;
    private Button mReset;
    private Button mDocumentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Settings.upgradeVersion(this);

        ((TextView) findViewById(R.id.notifications)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.source)).setMovementMethod(LinkMovementMethod.getInstance());
        mSetup = (Button) findViewById(R.id.setup);
        mCredentials = (TableLayout) findViewById(R.id.credentials);
        mUsername = (TextView) findViewById(R.id.username);
        mToken = (TextView) findViewById(R.id.token);
        mReset = (Button) findViewById(R.id.reset);
        mDocumentation = (Button) findViewById(R.id.documentation);

        if (!TextUtils.isEmpty(Settings.getEmail(this))) {
            showCredentials();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 1).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.device_not_supported)
                        .setMessage(R.string.device_not_supported_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
            startService(new Intent(this, RegistrationIntentService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mIabHelper != null) {
            mIabHelper.dispose();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
            setup(null);
        }
    }

    private void showCredentials() {
        mSetup.setVisibility(View.GONE);
        mCredentials.setVisibility(View.VISIBLE);
        mUsername.setText(Settings.getEmail(this));
        mToken.setText(Settings.getToken(this));
        mReset.setVisibility(View.VISIBLE);
        mDocumentation.setVisibility(View.VISIBLE);
    }

    public void setup(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { GET_ACCOUNTS }, 1);
        } else {
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
                                    startService(new Intent(PebbleConnect.this, RegistrationIntentService.class));
                                }
                            }
                        })
                        .show();
            }
        }
    }

    public void reset(View v) {
        Settings.setEmail(this, null);
        Settings.clearToken(this);
        mCredentials.setVisibility(View.GONE);
        mReset.setVisibility(View.GONE);
        mDocumentation.setVisibility(View.GONE);
        mSetup.setVisibility(View.VISIBLE);
    }

    public void documentation(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://lukekorth.com/blog/restful-pebble/")));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (Settings.hasPurchased(this)) {
            return true;
        }

        mIabHelper = new IabHelper(this, getString(R.string.billing_public_key));
        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                mIabHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                    @Override
                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                        if (result.isSuccess()) {
                            if (inv.hasPurchase("httpebble.unlock") || inv.hasPurchase("httpebble.donation.1") ||
                                    inv.hasPurchase("httpebble.donation.2") || inv.hasPurchase("httpebble.donation.3") ||
                                    inv.hasPurchase("httpebble.donation.5") || inv.hasPurchase("httpebble.donation.10")) {
                                Settings.setPurchased(PebbleConnect.this, true);
                                startService(new Intent(PebbleConnect.this, RegistrationIntentService.class));
                            } else {
                                final MenuItem upgrade = menu.add(R.string.upgrade);
                                upgrade.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        new AlertDialog.Builder(PebbleConnect.this)
                                                .setTitle(R.string.upgrade)
                                                .setMessage(R.string.upgrade_description)
                                                .setPositiveButton(R.string.purchase, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mIabHelper.launchPurchaseFlow(PebbleConnect.this,
                                                                "httpebble.unlock", 1, new IabHelper.OnIabPurchaseFinishedListener() {
                                                                    @Override
                                                                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
                                                                        upgrade.setVisible(false);
                                                                        PebbleConnect.this.invalidateOptionsMenu();
                                                                        Settings.setPurchased(PebbleConnect.this,
                                                                                result.isSuccess());
                                                                        startService(new Intent(PebbleConnect.this,
                                                                                RegistrationIntentService.class));
                                                                    }
                                                                });
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .show();

                                        return true;
                                    }
                                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                            }
                        }
                    }
                });
            }
        });

        return true;
    }
}