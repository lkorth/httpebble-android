package com.lukekorth.httpebble;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.lukekorth.httpebble.util.IabHelper;
import com.lukekorth.httpebble.util.IabResult;

public class BaseActivity extends SherlockActivity implements IabHelper.OnIabSetupFinishedListener {

	private IabHelper mHelper;
    private boolean mMakePurchase = false;
    private String mItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.donate).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(BaseActivity.this)
                        .setTitle("Select a donation amount")
                        .setItems(Constants.DONATION_AMOUNTS, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mItem = Constants.DONATION_ITEMS[which];
                                makePurchase();
                            }
                        })
                        .create();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    private void initialize() {
        disposeHelper();
        mHelper = new IabHelper(this, getString(R.string.billing_public_key));
        mHelper.startSetup(this);
    }

    private void makePurchase() {
        if(!mMakePurchase) {
            mMakePurchase = true;
            initialize();
        } else {
            mMakePurchase = false;
            mHelper.launchPurchaseFlow(this, mItem, 1, null, "donate");
        }
    }

	@Override
	public void onIabSetupFinished(IabResult result) {
		if(result.isSuccess()) {
             if(mMakePurchase)
                makePurchase();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disposeHelper();
	}

    private void disposeHelper() {
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;
    }
}