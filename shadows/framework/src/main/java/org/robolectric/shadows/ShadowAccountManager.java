package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.O;

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
import androidx.annotation.Nullable;
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
  /**
   * Maps listeners to a set of account types. If null, the listener should be notified for changes
   * to accounts of any type. Otherwise, the listener is only notified of changes to accounts of the
   * given type.
   */
  private Map<OnAccountsUpdateListener, Set<String>> listeners = new LinkedHashMap<>();

  private Map<Account, Map<String, String>> userData = new HashMap<>();
  private Map<Account, String> passwords = new HashMap<>();
  private Map<Account, Set<String>> accountFeatures = new HashMap<>();
  private Map<Account, Set<String>> packageVisibleAccounts = new HashMap<>();

  private List<Bundle> addAccountOptionsList = new ArrayList<>();
  private Handler mainHandler;
  private RoboAccountManagerFuture pendingAddFuture;
  private boolean authenticationErrorOnNextResponse = false;
  private Intent removeAccountIntent;

  @Implementation
  protected void __constructor__(Context context, IAccountManager service) {
    mainHandler = new Handler(context.getMainLooper());
  }

  @Implementation
  protected static AccountManager get(Context context) {
    return (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
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
    if (accounts.contains(account)) {
      Map<String, String> tokenMap = authTokens.get(account);
      if (tokenMap == null) {
        tokenMap = new HashMap<>();
        authTokens.put(account, tokenMap);
      }
      tokenMap.put(tokenType, authToken);
    }
  }

  @Implementation
  protected String peekAuthToken(Account account, String tokenType) {
    Map<String, String> tokenMap = authTokens.get(account);
    if (tokenMap != null) {
      return tokenMap.get(tokenType);
    }
    return null;
  }

  @SuppressWarnings("InconsistentCapitalization")
  @Implementation
  protected boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }
    for (Account a : getAccountsByType(account.type)) {
      if (a.name.equals(account.name)) {
        return false;
      }
    }

    if (!accounts.add(account)) {
      return false;
    }

    setPassword(account, password);

    if (userdata != null) {
      for (String key : userdata.keySet()) {
        setUserData(account, key, userdata.get(key).toString());
      }
    }

    notifyListeners(account);

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
          public Boolean doWork() {
            return removeAccountExplicitly(account);
          }
        });
  }

  /**
   * Removes the account unless {@link #setRemoveAccountIntent} has been set. If set, the future
   * Bundle will include the Intent and {@link AccountManager#KEY_BOOLEAN_RESULT} will be false.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected AccountManagerFuture<Bundle> removeAccount(
      Account account,
      Activity activity,
      AccountManagerCallback<Bundle> callback,
      Handler handler) {
    if (account == null) {
      throw new IllegalArgumentException("account is null");
    }
    return start(
        new BaseRoboAccountManagerFuture<Bundle>(callback, handler) {
          @Override
          public Bundle doWork() {
            Bundle result = new Bundle();
            if (removeAccountIntent == null) {
              result.putBoolean(
                  AccountManager.KEY_BOOLEAN_RESULT, removeAccountExplicitly(account));
            } else {
              result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
              result.putParcelable(AccountManager.KEY_INTENT, removeAccountIntent);
            }
            return result;
          }
        });
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean removeAccountExplicitly(Account account) {
    passwords.remove(account);
    userData.remove(account);
    if (accounts.remove(account)) {
      notifyListeners(account);
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
    addOnAccountsUpdatedListener(listener, handler, updateImmediately, /* accountTypes= */ null);
  }

  /**
   * Based on {@link AccountManager#addOnAccountsUpdatedListener(OnAccountsUpdateListener, Handler,
   * boolean, String[])}. {@link Handler} is ignored.
   */
  @Implementation(minSdk = O)
  protected void addOnAccountsUpdatedListener(
      @Nullable final OnAccountsUpdateListener listener,
      @Nullable Handler handler,
      boolean updateImmediately,
      @Nullable String[] accountTypes) {
    // TODO: Match real method behavior by throwing IllegalStateException.
    if (listeners.containsKey(listener)) {
      return;
    }

    Set<String> types = null;
    if (accountTypes != null) {
      types = new HashSet<>(Arrays.asList(accountTypes));
    }
    listeners.put(listener, types);

    if (updateImmediately) {
      notifyListener(listener, types, getAccounts());
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

  /**
   * Returns a bundle that contains the account session bundle under {@link
   * AccountManager#KEY_ACCOUNT_SESSION_BUNDLE} to later be passed on to {@link
   * AccountManager#finishSession(Bundle,Activity,AccountManagerCallback<Bundle>,Handler)}. The
   * session bundle simply propagates the given {@code accountType} so as not to be empty and is not
   * encrypted as it would be in the real implementation. If an activity isn't provided, resulting
   * bundle will only have a dummy {@link Intent} under {@link AccountManager#KEY_INTENT}.
   *
   * @param accountType An authenticator must exist for the accountType, or else {@link
   *     AuthenticatorException} is thrown.
   * @param authTokenType is ignored.
   * @param requiredFeatures is ignored.
   * @param options is ignored.
   * @param activity if null, only {@link AccountManager#KEY_INTENT} will be present in result.
   * @param callback if not null, will be called with result bundle.
   * @param handler is ignored.
   * @return future for bundle containing {@link AccountManager#KEY_ACCOUNT_SESSION_BUNDLE} if
   *     activity is provided, or {@link AccountManager#KEY_INTENT} otherwise.
   */
  @Implementation(minSdk = O)
  protected AccountManagerFuture<Bundle> startAddAccountSession(
      String accountType,
      String authTokenType,
      String[] requiredFeatures,
      Bundle options,
      Activity activity,
      AccountManagerCallback<Bundle> callback,
      Handler handler) {

    return start(
        new BaseRoboAccountManagerFuture<Bundle>(callback, handler) {
          @Override
          public Bundle doWork() throws AuthenticatorException {
            if (!authenticators.containsKey(accountType)) {
              throw new AuthenticatorException("No authenticator specified for " + accountType);
            }

            Bundle resultBundle = new Bundle();

            if (activity == null) {
              Intent resultIntent = new Intent();
              resultBundle.putParcelable(AccountManager.KEY_INTENT, resultIntent);
            } else {
              // This would actually be an encrypted bundle. Account type is copied as is simply to
              // make it non-empty.
              Bundle accountSessionBundle = new Bundle();
              accountSessionBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
              resultBundle.putBundle(AccountManager.KEY_ACCOUNT_SESSION_BUNDLE, Bundle.EMPTY);
            }

            return resultBundle;
          }
        });
  }

  /**
   * Returns sessionBundle as the result of finishSession.
   *
   * @param sessionBundle is returned as the result bundle.
   * @param activity is ignored.
   * @param callback if not null, will be called with result bundle.
   * @param handler is ignored.
   */
  @Implementation(minSdk = O)
  protected AccountManagerFuture<Bundle> finishSession(
      Bundle sessionBundle,
      Activity activity,
      AccountManagerCallback<Bundle> callback,
      Handler handler) {

    return start(
        new BaseRoboAccountManagerFuture<Bundle>(callback, handler) {
          @Override
          public Bundle doWork() {
            // Just return sessionBundle as the result since it's not really used, allowing it to
            // be easily controlled in tests.
            return sessionBundle;
          }
        });
  }

  /**
   * Based off of private method postToHandler(Handler, OnAccountsUpdateListener, Account[]) in
   * {@link AccountManager}
   */
  private void notifyListener(
      OnAccountsUpdateListener listener,
      @Nullable Set<String> accountTypesToReportOn,
      Account[] allAccounts) {
    if (accountTypesToReportOn != null) {
      ArrayList<Account> filtered = new ArrayList<>();
      for (Account account : allAccounts) {
        if (accountTypesToReportOn.contains(account.type)) {
          filtered.add(account);
        }
      }
      listener.onAccountsUpdated(filtered.toArray(new Account[0]));
    } else {
      listener.onAccountsUpdated(allAccounts);
    }
  }

  private void notifyListeners(Account changedAccount) {
    Account[] accounts = getAccounts();
    for (Map.Entry<OnAccountsUpdateListener, Set<String>> entry : listeners.entrySet()) {
      OnAccountsUpdateListener listener = entry.getKey();
      Set<String> types = entry.getValue();
      if (types == null || types.contains(changedAccount.type)) {
        notifyListener(listener, types, accounts);
      }
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
    notifyListeners(account);
  }

  /**
   * Adds an account to the AccountManager but when {@link
   * AccountManager#getAccountsByTypeForPackage(String, String)} is called will be included if is in
   * one of the #visibleToPackages
   *
   * @param account User account.
   */
  public void addAccount(Account account, String... visibleToPackages) {
    addAccount(account);
    HashSet<String> value = new HashSet<>();
    Collections.addAll(value, visibleToPackages);
    packageVisibleAccounts.put(account, value);
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
    public Bundle doWork() throws AuthenticatorException {
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
    if (activity == null) {
      // Caller only wants to get the intent, so start the future immediately.
      RoboAccountManagerFuture future =
          new RoboAccountManagerFuture(callback, handler, accountType, null);
      start(future);
      return future;
    } else {
      // Caller wants to start the sign in flow and return the intent with the new account added.
      // Account can be added via ShadowAccountManager#addAccount.
      pendingAddFuture = new RoboAccountManagerFuture(callback, handler, accountType, activity);
      return pendingAddFuture;
    }
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
          public Bundle doWork() throws AuthenticatorException {
            return getAuthToken(account, authTokenType);
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

    return start(
        new BaseRoboAccountManagerFuture<Bundle>(callback, handler) {
          @Override
          public Bundle doWork() throws AuthenticatorException {
            return getAuthToken(account, authTokenType);
          }
        });
  }

  private Bundle getAuthToken(Account account, String authTokenType) throws AuthenticatorException {
    Bundle result = new Bundle();

    String authToken = blockingGetAuthToken(account, authTokenType, false);
    result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
    result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
    result.putString(AccountManager.KEY_AUTHTOKEN, authToken);

    if (authToken != null) {
      return result;
    }

    if (!authenticators.containsKey(account.type)) {
      throw new AuthenticatorException("No authenticator specified for " + account.type);
    }

    Intent resultIntent = new Intent();
    result.putParcelable(AccountManager.KEY_INTENT, resultIntent);

    return result;
  }

  @Implementation
  protected AccountManagerFuture<Boolean> hasFeatures(
      final Account account,
      final String[] features,
      AccountManagerCallback<Boolean> callback,
      Handler handler) {
    return start(
        new BaseRoboAccountManagerFuture<Boolean>(callback, handler) {
          @Override
          public Boolean doWork() {
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
          public Account[] doWork() throws AuthenticatorException {

            if (authenticationErrorOnNextResponse) {
              setAuthenticationErrorOnNextResponse(false);
              throw new AuthenticatorException();
            }

            List<Account> result = new ArrayList<>();

            Account[] accountsByType = getAccountsByType(type);
            for (Account account : accountsByType) {
              Set<String> featureSet = accountFeatures.get(account);
              if (features == null
                  || (featureSet != null && featureSet.containsAll(Arrays.asList(features)))) {
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
      if (packageVisibleAccounts.containsKey(account)
          && packageVisibleAccounts.get(account).contains(packageName)) {
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

  /**
   * Sets the intent to include in Bundle result from {@link #removeAccount} if Activity is given.
   *
   * @param removeAccountIntent the intent to surface as {@link AccountManager#KEY_INTENT}.
   */
  public void setRemoveAccountIntent(Intent removeAccountIntent) {
    this.removeAccountIntent = removeAccountIntent;
  }

  public Map<OnAccountsUpdateListener, Set<String>> getListeners() {
    return listeners;
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
