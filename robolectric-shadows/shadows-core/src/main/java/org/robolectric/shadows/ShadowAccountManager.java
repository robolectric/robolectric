package org.robolectric.shadows;

import android.accounts.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

/**
 * Shadow for {@link android.accounts.AccountManager}.
 */
@Implements(AccountManager.class)
public class ShadowAccountManager {

  private List<Account> accounts = new ArrayList<>();
  private Map<Account, Map<String, String>> authTokens = new HashMap<>();
  private Map<String, AuthenticatorDescription> authenticators = new LinkedHashMap<>();
  private List<OnAccountsUpdateListener> listeners = new ArrayList<>();
  private Map<Account, Map<String, String>> userData = new HashMap<>();
  private Map<Account, String> passwords = new HashMap<>();
  private Map<Account, Set<String>> accountFeatures = new HashMap<>();
  private Map<Account, Set<String>> packageVisibileAccounts = new HashMap<>();

  private BaseRoboAccountManagerFuture<Bundle> pendingAddFuture;
  private List<Bundle> addAccountOptionsList = new ArrayList<>();
  private Handler mainHandler;

  public void __constructor__(Context context, IAccountManager service) {
    mainHandler = new Handler(context.getMainLooper());
  }

  @Implementation
  public Account[] getAccounts() {
    return accounts.toArray(new Account[accounts.size()]);
  }

  @Implementation
  public Account[] getAccountsByType(String type) {
    if (type == null) {
      return getAccounts();
    }
    List<Account> accountsByType = new ArrayList<>();

    for (Account a : accounts) {
      if (type.equals(a.type)) {
        accountsByType.add(a);
      }
    }

    return accountsByType.toArray(new Account[accountsByType.size()]);
  }

  @Implementation
  public synchronized void setAuthToken(Account account, String tokenType, String authToken) {
    if(accounts.contains(account)) {
      Map<String, String> tokenMap = authTokens.get(account);
      if(tokenMap == null) {
        tokenMap = new HashMap<>();
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

  @Implementation
  public boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }
    for (Account a: getAccountsByType(account.type)) {
      if (a.name.equals(account.name)) {
        return false;
      }
    }
    
    if (!accounts.add(account)) {
    	return false;
    }
    
    setPassword(account, password);
    
    if(userdata != null) {
      for (String key : userdata.keySet()) {
        setUserData(account, key, userdata.get(key).toString());
      }
    }
    
    return true;    
  }

  @Implementation
  public String blockingGetAuthToken(Account account, String authTokenType,
                                     boolean notifyAuthFailure) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }
    if (authTokenType == null) {
      throw new IllegalArgumentException("authTokenType is null");
    }

    Map<String, String> tokensForAccount = authTokens.get(account);
    if (tokensForAccount == null) {
      return null;
    }
    return tokensForAccount.get(authTokenType);
  }

  @Implementation
  public AccountManagerFuture<Boolean> removeAccount(final Account account,
                                                      AccountManagerCallback<Boolean> callback,
                                                      Handler handler) {

    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }

	  return new BaseRoboAccountManagerFuture<Boolean>(callback, handler) {
      @Override
      public Boolean doWork() throws OperationCanceledException, IOException, AuthenticatorException {
        return removeAccountExplicitly(account);
      }
    };
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  public boolean removeAccountExplicitly(Account account) {
    passwords.remove(account);
    userData.remove(account);
    return accounts.remove(account);
  }

  @Implementation
  public AuthenticatorDescription[] getAuthenticatorTypes() {
    return authenticators.values().toArray(new AuthenticatorDescription[authenticators.size()]);
  }

  @Implementation
  public void addOnAccountsUpdatedListener(final OnAccountsUpdateListener listener,
      Handler handler, boolean updateImmediately) {

    if (listeners.contains(listener)) {
      return;
    }

    listeners.add(listener);

    if (updateImmediately) {
      listener.onAccountsUpdated(getAccounts());
    }
  }

  @Implementation
  public void removeOnAccountsUpdatedListener(OnAccountsUpdateListener listener) {
    listeners.remove(listener);
  }

  @Implementation
  public String getUserData(Account account, String key) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }

    if (!userData.containsKey(account)) {
      return null;
    }
    
    Map<String, String> userDataMap = userData.get(account);
    if (userDataMap.containsKey(key)) {
      return userDataMap.get(key);
    }

	return null;
  }
  
  @Implementation
  public void setUserData(Account account, String key, String value) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }
    
    if (!userData.containsKey(account)) {
      userData.put(account, new HashMap<String, String>());
    }
    
    Map<String, String> userDataMap = userData.get(account);
    
    if (value == null) {
      userDataMap.remove(key);
    } else {
      userDataMap.put(key, value);
    }
  }
  
  @Implementation
  public void setPassword (Account account, String password) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }
    
    if (password == null) {
      passwords.remove(account);
    } else {
      passwords.put(account, password);
    }
  }
  
  @Implementation
  public String getPassword (Account account) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }
	
    if (passwords.containsKey(account)) {
      return passwords.get(account);
    } else {
      return null;
    }
  }

  @Implementation
  public void invalidateAuthToken(final String accountType, final String authToken) {
    Account[] accountsByType = getAccountsByType(accountType);
    for (Account account : accountsByType) {
      Map<String, String> tokenMap = authTokens.get(account);
      if (tokenMap != null) {
        Iterator<Entry<String, String>> it = tokenMap.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<String, String> map = it.next();
          if (map.getValue().equals(authToken)) {
            it.remove();
          }
        }
        authTokens.put(account, tokenMap);
      }
    }
  }

  private void notifyListeners() {
    Account[] accounts = getAccounts();
    Iterator<OnAccountsUpdateListener> iter = listeners.iterator();
    OnAccountsUpdateListener listener;
    while (iter.hasNext()) {
      listener = iter.next();
      listener.onAccountsUpdated(accounts);
    }
  }

  /**
   * Non-android accessor.
   *
   * @param account User account.
   */
  public void addAccount(Account account) {
    accounts.add(account);
    if (pendingAddFuture != null) {
      pendingAddFuture.result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
    }
    notifyListeners();
  }

  /**
   * Non-android accessor.
   *
   * Adds an account to the AccountManager but when {@link AccountManager#getAccountsByTypeForPackage(String, String)}
   * is called will be included if is in one of the #visibileToPackages
   *
   * @param account User account.
   */
  public void addAccount(Account account, String... visibileToPackages) {
    addAccount(account);
    HashSet<String> value = new HashSet<>();
    Collections.addAll(value, visibileToPackages);
    packageVisibileAccounts.put(account, value);
  }

  /**
   * Non-Android accessor consumes and returns the next {@code addAccountOptions} passed to addAccount.
   *
   * @return the next {@code addAccountOptions}
   */
  public Bundle getNextAddAccountOptions() {
    if (addAccountOptionsList.isEmpty()) {
      return null;
    } else {
      return addAccountOptionsList.remove(0);
    }
  }

  /**
   * Non-Android accessor returns the next {@code addAccountOptions} passed to addAccount without consuming it.
   *
   * @return the next {@code addAccountOptions}
   */
  public Bundle peekNextAddAccountOptions() {
    if (addAccountOptionsList.isEmpty()) {
      return null;
    } else {
      return addAccountOptionsList.get(0);
    }
  }

  public void setFeatures(Account account, String[] accountFeatures) {
    HashSet<String> featureSet = new HashSet<>();
    featureSet.addAll(Arrays.asList(accountFeatures));
    this.accountFeatures.put(account, featureSet);
  }

  @Implementation
  public AccountManagerFuture<Bundle> addAccount(final String accountType, String authTokenType, String[] requiredFeatures, final Bundle addAccountOptions, final Activity activity, final AccountManagerCallback<Bundle> callback, Handler handler) {
    pendingAddFuture = new BaseRoboAccountManagerFuture<Bundle>(callback, handler) {

      {
        super.result = new Bundle();
      }

      public boolean isDone() {
        if (activity == null) {
          return super.isDone();
        } else {
          return super.isDone() && result.containsKey(AccountManager.KEY_ACCOUNT_NAME);
        }
      }

      @Override
      public Bundle doWork() throws OperationCanceledException, IOException, AuthenticatorException {
        if (activity == null) {
          Intent resultIntent = new Intent();
          result.putParcelable(AccountManager.KEY_INTENT, resultIntent);
        } else {
          if (!result.containsKey(AccountManager.KEY_ACCOUNT_NAME)) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, "some_user@gmail.com");
          }
        }
        if (!authenticators.containsKey(accountType)) {
          throw new AuthenticatorException("No authenticator specified for " + accountType);
        }
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        addAccountOptionsList.add(addAccountOptions);
        return result;
      }
    };
    return pendingAddFuture;
  }

  /**
   * Non-android accessor.
   *
   * @param authenticator System authenticator.
   */
  public void addAuthenticator(AuthenticatorDescription authenticator) {
    authenticators.put(authenticator.type, authenticator);
  }

  public void addAuthenticator(String type) {
    addAuthenticator(AuthenticatorDescription.newKey(type));
  }

  private Map<Account, String> previousNames = new HashMap<Account, String>();

  /**
   * Non-android accessor.
   *
   * @param account User account.
   * @param previousName Previous account name.
   */
  public void setPreviousAccountName(Account account, String previousName) {
    previousNames.put(account, previousName);
  }

  @Implementation(minSdk = LOLLIPOP)
  public String getPreviousName(Account account) {
    return previousNames.get(account);
  }

  @Implementation
  public AccountManagerFuture<Bundle> getAuthToken(
      final Account account, final String authTokenType, final Bundle options,
      final Activity activity, final AccountManagerCallback<Bundle> callback, Handler handler) {

    return new BaseRoboAccountManagerFuture<Bundle>(callback, handler) {

      @Override
      public Bundle doWork() throws OperationCanceledException, IOException, AuthenticatorException {
        Bundle result = new Bundle();

        String authToken = blockingGetAuthToken(account, authTokenType, false);
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        return result;
      }
    };
  }

  @Implementation
  public AccountManagerFuture<Boolean> hasFeatures(final Account account,
                                                   final String[] features,
                                                   AccountManagerCallback<Boolean> callback, Handler handler) {
    return new BaseRoboAccountManagerFuture<Boolean>(callback, handler) {

      @Override
      public Boolean doWork() throws OperationCanceledException, IOException, AuthenticatorException {
        Set<String> availableFeatures = accountFeatures.get(account);
        for (String feature : features) {
          if (!availableFeatures.contains(feature)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  @Implementation
  public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(
      final String type, final String[] features,
      AccountManagerCallback<Account[]> callback, Handler handler) {
    return new BaseRoboAccountManagerFuture<Account[]>(callback, handler) {
      @Override
      public Account[] doWork() throws OperationCanceledException, IOException, AuthenticatorException {
        List<Account> result = new LinkedList<>();

        Account[] accountsByType = getAccountsByType(type);
        for (Account account : accountsByType) {
          Set<String> featureSet = accountFeatures.get(account);
          if (featureSet.containsAll(Arrays.asList(features))) {
            result.add(account);
          }
        }
        return result.toArray(new Account[result.size()]);
      }
    };
  }

  @Implementation
  public Account[] getAccountsByTypeForPackage (String type, String packageName) {
    List<Account> result = new LinkedList<>();

    Account[] accountsByType = getAccountsByType(type);
    for (Account account : accountsByType) {
      if (packageVisibileAccounts.containsKey(account) && packageVisibileAccounts.get(account).contains(packageName)) {
        result.add(account);
      }
    }

    return result.toArray(new Account[result.size()]);
  }

  private abstract class BaseRoboAccountManagerFuture<T> implements AccountManagerFuture<T> {

    private final AccountManagerCallback<T> callback;
    private final Handler handler;
    protected T result;

    BaseRoboAccountManagerFuture(AccountManagerCallback<T> callback, Handler handler) {
      this.callback = callback;
      if (handler == null) {
        this.handler = mainHandler;
      } else {
        this.handler = handler;
      }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return result != null;
    }

    @Override
    public T getResult() throws OperationCanceledException, IOException, AuthenticatorException {
      result = doWork();
      if (callback != null) {
        handler.post(new Runnable() {
          @Override
          public void run() {
            callback.run(BaseRoboAccountManagerFuture.this);
          }
        });
      }
      return result;
    }

    @Override
    public T getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
      return getResult();
    }

    public abstract T doWork() throws OperationCanceledException, IOException, AuthenticatorException;
  }
}
