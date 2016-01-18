package com.lukekorth.httpebble;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.lukekorth.httpebble.billing.IabHelper;
import com.lukekorth.httpebble.billing.IabResult;

public class BaseActivity extends AppCompatActivity {

    private static final String ONE_DOLLAR = "$1";
    private static final String TWO_DOLLARS = "$2";
    private static final String THREE_DOLLARS = "$3";
    private static final String FIVE_DOLLARS = "$5";
    private static final String TEN_DOLLARS = "$10";
    private static final String[] DONATION_AMOUNTS = { ONE_DOLLAR, TWO_DOLLARS, THREE_DOLLARS, FIVE_DOLLARS, TEN_DOLLARS };
    private static final String ONE_DOLLAR_ITEM = "httpebble.donation.1";
    private static final String TWO_DOLLARS_ITEM = "httpebble.donation.2";
    private static final String THREE_DOLLARS_ITEM = "httpebble.donation.3";
    private static final String FIVE_DOLLARS_ITEM = "httpebble.donation.5";
    private static final String TEN_DOLLARS_ITEM = "httpebble.donation.10";
    private static final String[] DONATION_ITEMS = { ONE_DOLLAR_ITEM, TWO_DOLLARS_ITEM, THREE_DOLLARS_ITEM,
            FIVE_DOLLARS_ITEM, TEN_DOLLARS_ITEM };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.donate).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(BaseActivity.this)
                        .setTitle(R.string.select_donation_amount)
                        .setItems(DONATION_AMOUNTS, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                makePurchase(DONATION_ITEMS[which]);
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