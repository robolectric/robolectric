package com.xtremelabs.robolectric.shadows;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadows the {@code android.accounts.AccountManager} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(AccountManager.class)
public class ShadowAccountManager {

    @RealObject private static AccountManager realAccountManager;

    @Implementation
    public static AccountManager get(Context context) {
        return Robolectric.newInstanceOf(AccountManager.class);
    }

    @Implementation
    public AccountManagerFuture<Bundle> getAuthTokenByFeatures(String accountType, String authTokenType, String[] features, Activity activity, Bundle addAccountOptions, Bundle getAuthTokenOptions, AccountManagerCallback<Bundle> callback, Handler handler) {
        //TODO: Add complete activity to perform the account intent dance.
        final String finalAccountType = accountType;
        return new AccountManagerFuture<Bundle>() {

            private boolean isFutureCancelled;
            private boolean isFutureDone;

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (isFutureDone) {
                    return false;
                }
                isFutureCancelled = true;
                return isCancelled();
            }

            @Override
            public Bundle getResult(long timeout, TimeUnit unit) throws OperationCanceledException,
                    AuthenticatorException, IOException {
                Bundle result = new Bundle();
                if (!isCancelled()) {
                    addBundleResults(result, finalAccountType);
                    isFutureDone = true;
                }
                return result;
            }

            @Override
            public Bundle getResult() throws OperationCanceledException,
                    AuthenticatorException, IOException {
                Bundle result = new Bundle();
                if (!isCancelled()) {
                    addBundleResults(result, finalAccountType);
                    isFutureDone = true;
                }
                return result;
            }

            @Override
            public boolean isCancelled() {
                return isFutureCancelled;
            }

            @Override
            public boolean isDone() {
                return isFutureDone || isFutureCancelled;
            }

            private void addBundleResults(Bundle bundle, final String accountType) {
                bundle.putString(AccountManager.KEY_AUTHTOKEN, "authToken");
                bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                bundle.putString(AccountManager.KEY_ACCOUNT_NAME, "accountName");
            }
        };
    }

    @Implementation
    public void invalidateAuthToken(String accountType, String authToken) {}
}
