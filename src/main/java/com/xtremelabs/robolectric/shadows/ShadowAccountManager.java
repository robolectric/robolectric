package com.xtremelabs.robolectric.shadows;

import android.accounts.*;
import android.content.Context;
import android.os.Handler;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shadow implementation for the Android {@code AccountManager } class.
 */
@Implements(AccountManager.class)
public class ShadowAccountManager {
	
	private static HashMap<Context,AccountManager> instances = new HashMap<Context,AccountManager>();

	private List<Account> accounts = new ArrayList<Account>();
    private Map<String, String> authTokenMap = new HashMap<String, String>();
	
	@Implementation
	public static AccountManager get(Context context) {
		if (!instances.containsKey(context)) {
			instances.put(context, Robolectric.newInstanceOf(AccountManager.class));
		}
		return instances.get(context);
	}
	
	@Implementation
	public Account[] getAccounts() {
		return accounts.toArray(new Account[0]);
	}
	
	@Implementation
	public Account[] getAccountsByType(String type) {
		List<Account> accountsByType = new ArrayList<Account>();
		
		for (Account a : accounts) {
			if (type.equals(a.type)) {
				accountsByType.add(a);
			}
		}
		
		return accountsByType.toArray(new Account[0]);
	}

    @Implementation
    public AccountManagerFuture<Boolean> removeAccount(final Account account,
                                                       AccountManagerCallback<Boolean> callback, Handler handler) {
        accounts.remove(account);
        return null;
    }

    @Implementation
    public String blockingGetAuthToken(Account account, String authTokenType,
                                       boolean notifyAuthFailure)
            throws OperationCanceledException, IOException, AuthenticatorException {
        if (account == null) throw new IllegalArgumentException("account is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");

        return authTokenMap.get(authTokenType);
    }

    @Implementation
    public void invalidateAuthToken(final String accountType, final String authToken) {
        if (authTokenMap.get(accountType) == authToken) {
            authTokenMap.remove(accountType);
        }
    }

    public void setAuthTokenForAccountType(String accountType, String authToken) {
        authTokenMap.put(accountType, authToken);
    }

	/**
	 * Non-android accessor.  Allows the test case to populate the
	 * list of active accounts.
	 * 
	 * @param account
	 */
	public void addAccount(Account account) {
		accounts.add(account);
	}
}
