package com.lukekorth.httpebble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;

import com.lukekorth.httpebble.util.IabHelper;
import com.lukekorth.httpebble.util.IabResult;

public class BaseActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.donate).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(BaseActivity.this)
                        .setTitle("Select a donation amount")
                        .setItems(Constants.DONATION_AMOUNTS, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                makePurchase(Constants.DONATION_ITEMS[which]);
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    private void makePurchase(final String purchaseItem) {
        final IabHelper iabHelper = new IabHelper(this, getString(R.string.billing_public_key));
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                iabHelper.launchPurchaseFlow(BaseActivity.this, purchaseItem, 1, null, "donate");
            }
        });
    }
}