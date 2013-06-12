package org.robolectric.shadows;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shadow implementation for the Android {@code AccountManager } class.
 */
@Implements(AccountManager.class)
public class ShadowAccountManager {
  private static final HashMap<Context, AccountManager> instances = new HashMap<Context, AccountManager>();

  private List<Account> accounts = new ArrayList<Account>();
  private Map<Account, Map<String, String>> authTokens = new HashMap<Account, Map<String,String>>();

  public static void reset() {
    synchronized (instances) {
      instances.clear();
    }
  }

  @Implementation
  public static AccountManager get(Context context) {
    synchronized (instances) {
      if (!instances.containsKey(context)) {
        instances.put(context, Robolectric.newInstanceOf(AccountManager.class));
      }
      return instances.get(context);
    }
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
  public synchronized void setAuthToken(Account account, String tokenType, String authToken) {
    if(accounts.contains(account)) {
      Map<String, String> tokenMap = authTokens.get(account);
      if(tokenMap == null) {
        tokenMap = new HashMap<String, String>();
        authTokens.put(account, tokenMap);
      }
      tokenMap.put(tokenType, authToken);
    }
  }

  @Implementation
  public String peekAuthToken(Account account, String tokenType) {
    Map<String, String> tokenMap = authTokens.get(account);
    if(tokenMap != null) {
      return tokenMap.get(tokenType);
    }
    return null;
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
