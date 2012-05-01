package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@RunWith(WithTestDefaultsRunner.class)
public class AccountManagerTest {

    private AccountManager accountManager;
    private Activity activity;
    private String accountType;
    private String authTokenType;
    private String[] features;

    @Before
    public void setUp() {
        activity = new Activity();
        accountManager = AccountManager.get(activity);
        accountManager.invalidateAuthToken(null, null);
        accountType = "accountType";
        authTokenType = "authTokenType";
        accountType = "accountType";
        features = new String[]{};
    }

    @Test
    public void testGetAuthTokenByFeatures_isCancelled() throws Exception {
        AccountManagerFuture<Bundle> future =
                accountManager.getAuthTokenByFeatures(accountType, authTokenType, features, activity, null, null, null, null);

        assertThat(future.isCancelled(), equalTo(false));
        future.cancel(true);
        assertThat(future.isCancelled(), equalTo(true));
    }

    @Test
    public void testGetAuthTokenByFeatures_isDoneWithCancel() throws Exception {
        AccountManagerFuture<Bundle> future =
                accountManager.getAuthTokenByFeatures(accountType, authTokenType, features, activity, null, null, null, null);

        assertThat(future.isDone(), equalTo(false));
        future.cancel(true);
        assertThat(future.isDone(), equalTo(true));
    }

    @Test
    public void testGetAuthTokenByFeatures_isDoneWithGetResult() throws Exception {
        AccountManagerFuture<Bundle> future =
                accountManager.getAuthTokenByFeatures(accountType, authTokenType, features, activity, null, null, null, null);

        assertThat(future.isDone(), equalTo(false));
        future.getResult();
        assertThat(future.isDone(), equalTo(true));
    }

    @Test
    public void testInvalidateAuthToken() throws Exception {
        // Check that it doesn't crash
        accountManager.invalidateAuthToken(accountType, null);
    }

    @Test
    public void testGetAuthTokenByFeatures_getResult() throws Exception {
        AccountManagerFuture<Bundle> future =
                accountManager.getAuthTokenByFeatures(accountType, authTokenType, features, activity, null, null, null, null);

        Bundle result = future.getResult();
        assertThat(result.containsKey(AccountManager.KEY_AUTHTOKEN), equalTo(true));
        assertThat(result.containsKey(AccountManager.KEY_ACCOUNT_NAME), equalTo(true));
        assertThat(result.containsKey(AccountManager.KEY_ACCOUNT_TYPE), equalTo(true));
    }
}
