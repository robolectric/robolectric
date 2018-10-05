package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.IAccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Scheduler.IdleState;

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

  private List<Bundle> addAccountOptionsList = new ArrayList<>();
  private Handler mainHandler;
  private RoboAccountManagerFuture pendingAddFuture;
  private boolean authenticationErrorOnNextResponse = false;

  @Implementation
  protected void __constructor__(Context context, IAccountManager service) {
    mainHandler = new Handler(context.getMainLooper());
  }

  @Implementation
  protected Account[] getAccounts() {
    return accounts.toArray(new Account[accounts.size()]);
  }

  @Implementation
  protected Account[] getAccountsByType(String type) {
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
  protected synchronized void setAuthToken(Account account, String tokenType, String authToken) {
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
  protected String peekAuthToken(Account account, String tokenType) {
    Map<String, String> tokenMap = authTokens.get(account);
    if(tokenMap != null) {
      return tokenMap.get(tokenType);
    }
    return null;
  }

  @Implementation
  protected boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
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

    notifyListeners();

    return true;
  }

  @Implementation
  protected String blockingGetAuthToken(
      Account account, String authTokenType, boolean notifyAuthFailure) {
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

  /**
   * The remove operation is posted to the given {@code handler}, and will be executed according to
   * the {@link IdleState} of the corresponding {@link org.robolectric.util.Scheduler}.
   */
  @Implementation
  protected AccountManagerFuture<Boolean> removeAccount(
      final Account account, AccountManagerCallback<Boolean> callback, Handler handler) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }

    return start(
        new BaseRoboAccountManagerFuture<Boolean>(callback, handler) {
          @Override
          public Boolean doWork()
              throws OperationCanceledException, IOException, AuthenticatorException {
            return removeAccountExplicitly(account);
          }
        });
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean removeAccountExplicitly(Account account) {
    passwords.remove(account);
    userData.remove(account);
    if (accounts.remove(account)) {
      notifyListeners();
      return true;
    }
    return false;
  }

  /**
   * Removes all accounts that have been added.
   */
  public void removeAllAccounts() {
    passwords.clear();
    userData.clear();
    accounts.clear();
  }

  @Implementation
  protected AuthenticatorDescription[] getAuthenticatorTypes() {
    return authenticators.values().toArray(new AuthenticatorDescription[authenticators.size()]);
  }

  @Implementation
  protected void addOnAccountsUpdatedListener(
      final OnAccountsUpdateListener listener, Handler handler, boolean updateImmediately) {

    if (listeners.contains(listener)) {
      return;
    }

    listeners.add(listener);

    if (updateImmediately) {
      listener.onAccountsUpdated(getAccounts());
    }
  }

  @Implementation
  protected void removeOnAccountsUpdatedListener(OnAccountsUpdateListener listener) {
    listeners.remove(listener);
  }

  @Implementation
  protected String getUserData(Account account, String key) {
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
  protected void setUserData(Account account, String key, String value) {
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
  protected void setPassword(Account account, String password) {
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
  protected String getPassword(Account account) {
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
  protected void invalidateAuthToken(final String accountType, final String authToken) {
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
   * @param account User account.
   */
  public void addAccount(Account account) {
    accounts.add(account);
    if (pendingAddFuture != null) {
      pendingAddFuture.resultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
      start(pendingAddFuture);
      pendingAddFuture = null;
    }
    notifyListeners();
  }

  /**
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
   * Consumes and returns the next {@code addAccountOptions} passed to {@link #addAccount}.
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
   * Returns the next {@code addAccountOptions} passed to {@link #addAccount} without consuming it.
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

  private class RoboAccountManagerFuture extends BaseRoboAccountManagerFuture<Bundle> {
    private final String accountType;
    private final Activity activity;
    private final Bundle resultBundle;

    RoboAccountManagerFuture(AccountManagerCallback<Bundle> callback, Handler handler, String accountType, Activity activity) {
      super(callback, handler);

      this.accountType = accountType;
      this.activity = activity;
      this.resultBundle = new Bundle();
    }

    @Override
    public Bundle doWork() throws OperationCanceledException, IOException, AuthenticatorException {
      if (!authenticators.containsKey(accountType)) {
        throw new AuthenticatorException("No authenticator specified for " + accountType);
      }

      resultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);

      if (activity == null) {
        Intent resultIntent = new Intent();
        resultBundle.putParcelable(AccountManager.KEY_INTENT, resultIntent);
      } else if (callback == null) {
        resultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, "some_user@gmail.com");
      }

      return resultBundle;
    }
  }

  @Implementation
  protected AccountManagerFuture<Bundle> addAccount(
      final String accountType,
      String authTokenType,
      String[] requiredFeatures,
      Bundle addAccountOptions,
      Activity activity,
      AccountManagerCallback<Bundle> callback,
      Handler handler) {
    addAccountOptionsList.add(addAccountOptions);
    pendingAddFuture = new RoboAccountManagerFuture(callback, handler, accountType, activity);
    return pendingAddFuture;
  }

  public void setFeatures(Account account, String[] accountFeatures) {
    HashSet<String> featureSet = new HashSet<>();
    featureSet.addAll(Arrays.asList(accountFeatures));
    this.accountFeatures.put(account, featureSet);
  }

  /**
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
   * Sets the previous name for an account, which will be returned by {@link AccountManager#getPreviousName(Account)}.
   *
   * @param account User account.
   * @param previousName Previous account name.
   */
  public void setPreviousAccountName(Account account, String previousName) {
    previousNames.put(account, previousName);
  }

  /** @see #setPreviousAccountName(Account, String) */
  @Implementation(minSdk = LOLLIPOP)
  protected String getPreviousName(Account account) {
    return previousNames.get(account);
  }

  @Implementation
  protected AccountManagerFuture<Bundle> getAuthToken(
      final Account account,
      final String authTokenType,
      final Bundle options,
      final Activity activity,
      final AccountManagerCallback<Bundle> callback,
      Handler handler) {

    return start(
        new BaseRoboAccountManagerFuture<Bundle>(callback, handler) {
          @Override
          public Bundle doWork()
              throws OperationCanceledException, IOException, AuthenticatorException {
            Bundle result = new Bundle();

            String authToken = blockingGetAuthToken(account, authTokenType, false);
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
          }
        });
  }

  @Implementation
  protected AccountManagerFuture<Bundle> getAuthToken(
      final Account account,
      final String authTokenType,
      final Bundle options,
      final boolean notifyAuthFailure,
      final AccountManagerCallback<Bundle> callback,
      Handler handler) {

    return start(new BaseRoboAccountManagerFuture<Bundle>(callback, handler) {
      @Override
      public Bundle doWork() throws OperationCanceledException, IOException, AuthenticatorException {
        Bundle result = new Bundle();

        String authToken = blockingGetAuthToken(account, authTokenType, false);
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        return result;
      }
    });
  }

  @Implementation
  protected AccountManagerFuture<Boolean> hasFeatures(
      final Account account,
      final String[] features,
      AccountManagerCallback<Boolean> callback,
      Handler handler) {
    return start(new BaseRoboAccountManagerFuture<Boolean>(callback, handler) {
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
    });
  }

  @Implementation
  protected AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(
      final String type,
      final String[] features,
      AccountManagerCallback<Account[]> callback,
      Handler handler) {
    return start(
        new BaseRoboAccountManagerFuture<Account[]>(callback, handler) {
          @Override
          public Account[] doWork()
              throws OperationCanceledException, IOException, AuthenticatorException {

            if (authenticationErrorOnNextResponse) {
              setAuthenticationErrorOnNextResponse(false);
              throw new AuthenticatorException();
            }

            List<Account> result = new ArrayList<>();

            Account[] accountsByType = getAccountsByType(type);
            for (Account account : accountsByType) {
              Set<String> featureSet = accountFeatures.get(account);
              if (features == null || featureSet.containsAll(Arrays.asList(features))) {
                result.add(account);
              }
            }
            return result.toArray(new Account[result.size()]);
          }
        });
  }

  private <T extends BaseRoboAccountManagerFuture> T start(T future) {
    future.start();
    return future;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected Account[] getAccountsByTypeForPackage(String type, String packageName) {
    List<Account> result = new ArrayList<>();

    Account[] accountsByType = getAccountsByType(type);
    for (Account account : accountsByType) {
      if (packageVisibileAccounts.containsKey(account) && packageVisibileAccounts.get(account).contains(packageName)) {
        result.add(account);
      }
    }

    return result.toArray(new Account[result.size()]);
  }

  /**
   * Sets authenticator exception, which will be thrown by {@link #getAccountsByTypeAndFeatures}.
   *
   * @param authenticationErrorOnNextResponse to set flag that exception will be thrown on next
   *     response.
   */
  public void setAuthenticationErrorOnNextResponse(boolean authenticationErrorOnNextResponse) {
    this.authenticationErrorOnNextResponse = authenticationErrorOnNextResponse;
  }

  private abstract class BaseRoboAccountManagerFuture<T> implements AccountManagerFuture<T> {
    protected final AccountManagerCallback<T> callback;
    private final Handler handler;
    protected T result;
    private Exception exception;
    private boolean started = false;

    BaseRoboAccountManagerFuture(AccountManagerCallback<T> callback, Handler handler) {
      this.callback = callback;
      this.handler = handler == null ? mainHandler : handler;
    }

    void start() {
      if (started) return;
      started = true;

      try {
        result = doWork();
      } catch (OperationCanceledException | IOException | AuthenticatorException e) {
        exception = e;
      }

      if (callback != null) {
        handler.post(
            new Runnable() {
              @Override
              public void run() {
                callback.run(BaseRoboAccountManagerFuture.this);
              }
            });
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
      return result != null || exception != null || isCancelled();
    }

    @Override
    public T getResult() throws OperationCanceledException, IOException, AuthenticatorException {
      start();

      if (exception instanceof OperationCanceledException) {
        throw new OperationCanceledException(exception);
      } else if (exception instanceof IOException) {
        throw new IOException(exception);
      } else if (exception instanceof AuthenticatorException) {
        throw new AuthenticatorException(exception);
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
