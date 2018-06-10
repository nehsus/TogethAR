package io.agora.agoravideodemo.syncad;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.util.Log;

import io.agora.agoravideodemo.R;


public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private AccountManager mAccountManager;
    private static final String TAG = "AuthenticatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);

        Log.i(TAG, "AuthenticatorActivity");
        Intent res = new Intent();
        res.putExtra(AccountManager.KEY_ACCOUNT_NAME, SyncStateContract.Constants.ACCOUNT_NAME);
        res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, SyncStateContract.Constants.ACCOUNT_TYPE);
//        res.putExtra(AccountManager.KEY_AUTHTOKEN, SyncStateContract.Constants.ACCOUNT_TOKEN);
        Account account = new Account(SyncStateContract.Constants.ACCOUNT_NAME, SyncStateContract.Constants.ACCOUNT_TYPE);
        mAccountManager = AccountManager.get(this);
        mAccountManager.addAccountExplicitly(account, null, null);
//      mAccountManager.setAuthToken(account, Constants.AUTHTOKEN_TYPE_FULL_ACCESS, Constants.ACCOUNT_TOKEN);
        ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
        setAccountAuthenticatorResult(res.getExtras());
        setResult(RESULT_OK, res);
        finish();
    }

}