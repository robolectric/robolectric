package org.robolectric.shadows;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.Scheduler;

import java.io.IOException;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowAccountManagerTest {
  private AccountManager accountManager;
  private Scheduler scheduler;
  private Activity activity;
  public static final String GOOGLE_ACCOUNT_TYPE = "google.com";
  private OnAccountsUpdateListener mockListener;
  private ShadowAccountManager shadowAccountManager;

  @Before
  public void setUp() throws Exception {
    accountManager = AccountManager.get(RuntimeEnvironment.application);
    scheduler = Robolectric.getForegroundThreadScheduler();
    activity = new Activity();
    mockListener = mock(OnAccountsUpdateListener.class);
    shadowAccountManager = shadowOf(accountManager);
  }

  @Test
  public void testGet() {
    assertThat(accountManager).isNotNull();
    assertThat(accountManager).isSameAs(AccountManager.get(RuntimeEnvironment.application));

    AccountManager activityAM = AccountManager.get(RuntimeEnvironment.application);
    assertThat(activityAM).isNotNull();
    assertThat(activityAM).isSameAs(accountManager);
  }

  @Test
  public void testGetAccounts() {
    assertThat(accountManager.getAccounts()).isNotNull();
    assertThat(accountManager.getAccounts().length).isEqualTo(0);

    Account a1 = new Account("name_a", "type_a");
    shadowAccountManager.addAccount(a1);
    assertThat(accountManager.getAccounts()).isNotNull();
    assertThat(accountManager.getAccounts().length).isEqualTo(1);
    assertThat(accountManager.getAccounts()[0]).isSameAs(a1);

    Account a2 = new Account("name_b", "type_b");
    shadowAccountManager.addAccount(a2);
    assertThat(accountManager.getAccounts()).isNotNull();
    assertThat(accountManager.getAccounts().length).isEqualTo(2);
    assertThat(accountManager.getAccounts()[1]).isSameAs(a2);
  }

  @Test
  public void getAccountsByType_nullTypeReturnsAllAccounts() {
    shadowAccountManager.addAccount(new Account("name_1", "type_1"));
    shadowAccountManager.addAccount(new Account("name_2", "type_2"));
    shadowAccountManager.addAccount(new Account("name_3", "type_3"));

    assertThat(accountManager.getAccountsByType(null)).containsExactly(accountManager.getAccounts());
  }

  @Test
  public void testGetAccountsByType() {
    assertThat(accountManager.getAccountsByType("name_a")).isNotNull();
    assertThat(accountManager.getAccounts().length).isEqualTo(0);

    Account a1 = new Account("name_a", "type_a");
    shadowAccountManager.addAccount(a1);
    Account[] accounts = accountManager.getAccountsByType("type_a");
    assertThat(accounts).isNotNull();
    assertThat(accounts.length).isEqualTo(1);
    assertThat(accounts[0]).isSameAs(a1);

    Account a2 = new Account("name_b", "type_b");
    shadowAccountManager.addAccount(a2);
    accounts = accountManager.getAccountsByType("type_a");
    assertThat(accounts).isNotNull();
    assertThat(accounts.length).isEqualTo(1);
    assertThat(accounts[0]).isSameAs(a1);

    Account a3 = new Account("name_c", "type_a");
    shadowAccountManager.addAccount(a3);
    accounts = accountManager.getAccountsByType("type_a");
    assertThat(accounts).isNotNull();
    assertThat(accounts.length).isEqualTo(2);
    assertThat(accounts[0]).isSameAs(a1);
    assertThat(accounts[1]).isSameAs(a3);
  }

  @Test
  public void addAuthToken() {
    Account account = new Account("name", "type");
    shadowAccountManager.addAccount(account);

    accountManager.setAuthToken(account, "token_type_1", "token1");
    accountManager.setAuthToken(account, "token_type_2", "token2");

    assertThat(accountManager.peekAuthToken(account, "token_type_1")).isEqualTo("token1");
    assertThat(accountManager.peekAuthToken(account, "token_type_2")).isEqualTo("token2");
  }

  @Test
  public void setAuthToken_shouldNotAddTokenIfAccountNotPresent() {
    Account account = new Account("name", "type");
    accountManager.setAuthToken(account, "token_type_1", "token1");
    assertThat(accountManager.peekAuthToken(account, "token_type_1")).isNull();
  }

  @Test
  public void testAddAccountExplicitly_noPasswordNoExtras() {
    Account account = new Account("name", "type");
    boolean accountAdded = accountManager.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();
    assertThat(accountManager.getAccountsByType("type").length).isEqualTo(1);
    assertThat(accountManager.getAccountsByType("type")[0].name).isEqualTo("name");

    boolean accountAddedTwice = accountManager.addAccountExplicitly(account, null, null);
    assertThat(accountAddedTwice).isFalse();

    account = new Account("another_name", "type");
    accountAdded = accountManager.addAccountExplicitly(account, null, null);
    assertThat(accountAdded).isTrue();
    assertThat(accountManager.getAccountsByType("type").length).isEqualTo(2);
    assertThat(accountManager.getAccountsByType("type")[0].name).isEqualTo("name");
    assertThat(accountManager.getAccountsByType("type")[1].name).isEqualTo("another_name");
    assertThat(accountManager.getPassword(account)).isNull();

    try {
      accountManager.addAccountExplicitly(null, null, null);
      fail("An illegal argument exception should have been thrown when trying to add a null account");
    } catch (IllegalArgumentException iae) {
      // NOP
    }
  }

  @Test
  public void testAddAccountExplicitly_withPassword() {
    Account account = new Account("name", "type");
    boolean accountAdded = accountManager.addAccountExplicitly(account, "passwd", null);

    assertThat(accountAdded).isTrue();
    assertThat(accountManager.getPassword(account)).isEqualTo("passwd");
  }

  @Test
  public void testAddAccountExplicitly_withExtras() {
    Account account = new Account("name", "type");
    Bundle extras = new Bundle();
    extras.putString("key123", "value123");
    boolean accountAdded = accountManager.addAccountExplicitly(account, null, extras);

    assertThat(accountAdded).isTrue();
    assertThat(accountManager.getUserData(account, "key123")).isEqualTo("value123");
    assertThat(accountManager.getUserData(account, "key456")).isNull();
  }

  @Test
  public void testGetSetUserData_addToInitiallyEmptyExtras() {
    Account account = new Account("name", "type");
    boolean accountAdded = accountManager.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();

    accountManager.setUserData(account, "key123", "value123");
    assertThat(accountManager.getUserData(account, "key123")).isEqualTo("value123");
  }

  @Test
  public void testGetSetUserData_overwrite() {
    Account account = new Account("name", "type");
    boolean accountAdded = accountManager.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();

    accountManager.setUserData(account, "key123", "value123");
    assertThat(accountManager.getUserData(account, "key123")).isEqualTo("value123");

    accountManager.setUserData(account, "key123", "value456");
    assertThat(accountManager.getUserData(account, "key123")).isEqualTo("value456");
  }

  @Test
  public void testGetSetUserData_remove() {
    Account account = new Account("name", "type");
    boolean accountAdded = accountManager.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();

    accountManager.setUserData(account, "key123", "value123");
    assertThat(accountManager.getUserData(account, "key123")).isEqualTo("value123");

    accountManager.setUserData(account, "key123", null);
    assertThat(accountManager.getUserData(account, "key123")).isNull();
  }

  @Test
  public void testGetSetPassword_setInAccountInitiallyWithNoPassword() {
    Account account = new Account("name", "type");
    boolean accountAdded = accountManager.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();
    assertThat(accountManager.getPassword(account)).isNull();

    accountManager.setPassword(account, "passwd");
    assertThat(accountManager.getPassword(account)).isEqualTo("passwd");
  }

  @Test
  public void testGetSetPassword_overwrite() {
    Account account = new Account("name", "type");
    boolean accountAdded = accountManager.addAccountExplicitly(account, "passwd1", null);

    assertThat(accountAdded).isTrue();
    assertThat(accountManager.getPassword(account)).isEqualTo("passwd1");

    accountManager.setPassword(account, "passwd2");
    assertThat(accountManager.getPassword(account)).isEqualTo("passwd2");
  }

  @Test
  public void testGetSetPassword_remove() {
    Account account = new Account("name", "type");
    boolean accountAdded = accountManager.addAccountExplicitly(account, "passwd1", null);

    assertThat(accountAdded).isTrue();
    assertThat(accountManager.getPassword(account)).isEqualTo("passwd1");

    accountManager.setPassword(account, null);
    assertThat(accountManager.getPassword(account)).isNull();
  }

  @Test
  public void testBlockingGetAuthToken() throws AuthenticatorException, OperationCanceledException, IOException {
    Account account = new Account("name", "type");
    shadowAccountManager.addAccount(account);

    accountManager.setAuthToken(account, "token_type_1", "token1");
    accountManager.setAuthToken(account, "token_type_2", "token2");

    assertThat(accountManager.blockingGetAuthToken(account, "token_type_1", false)).isEqualTo("token1");
    assertThat(accountManager.blockingGetAuthToken(account, "token_type_2", false)).isEqualTo("token2");

    try {
      accountManager.blockingGetAuthToken(null, "token_type_1", false);
      fail("blockingGetAuthToken() should throw an illegal argument exception if the account is null");
    } catch (IllegalArgumentException iae) {
      // Expected
    }
    try {
      accountManager.blockingGetAuthToken(account, null, false);
      fail("blockingGetAuthToken() should throw an illegal argument exception if the auth token type is null");
    } catch (IllegalArgumentException iae) {
      // Expected
    }

    Account account1 = new Account("unknown", "type");
    assertThat(accountManager.blockingGetAuthToken(account1, "token_type_1", false)).isNull();
  }

  @Test
  public void removeAccount_throwsIllegalArgumentException_whenPassedNullAccount() {
    Account account = new Account("name", "type");
    shadowAccountManager.addAccount(account);

    try {
      accountManager.removeAccount(null, null, null);
      fail("removeAccount() should throw an illegal argument exception if the account is null");
    } catch (IllegalArgumentException iae) {
      // Expected
    }
  }

  @Test
  public void removeAccount_doesNotRemoveAccountOfDifferentName() throws Exception {
    Account account = new Account("name", "type");
    shadowAccountManager.addAccount(account);

    Account wrongAccount = new Account("wrong_name", "type");
    AccountManagerFuture<Boolean> future = accountManager.removeAccount(wrongAccount, null, null);
    assertThat(future.getResult()).isFalse();
    assertThat(accountManager.getAccountsByType("type")).isNotEmpty();
  }

  @Test
  public void removeAccount_does() throws Exception {
    Account account = new Account("name", "type");
    shadowAccountManager.addAccount(account);

    TestAccountManagerCallback<Boolean> testAccountManagerCallback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Boolean> future = accountManager.removeAccount(account, testAccountManagerCallback, null);
    assertThat(future.getResult()).isTrue();
    assertThat(accountManager.getAccountsByType("type")).isEmpty();

    assertThat(testAccountManagerCallback.accountManagerFuture).isNotNull();
  }

  private static class TestOnAccountsUpdateListener implements OnAccountsUpdateListener {
    private int invocationCount = 0;

    @Override
    public void onAccountsUpdated(Account[] accounts) {
      invocationCount++;
    }

    public int getInvocationCount() {
      return invocationCount;
    }
  }

  @Test
  public void testAccountsUpdateListener() {
    TestOnAccountsUpdateListener listener = new TestOnAccountsUpdateListener();
    accountManager.addOnAccountsUpdatedListener(listener, null, false);
    assertThat(listener.getInvocationCount()).isEqualTo(0);

    Account account = new Account("name", "type");
    shadowAccountManager.addAccount(account);
    assertThat(listener.getInvocationCount()).isEqualTo(1);
  }

  @Test
  public void testAccountsUpdateListener_duplicate() {
    TestOnAccountsUpdateListener listener = new TestOnAccountsUpdateListener();
    accountManager.addOnAccountsUpdatedListener(listener, null, false);
    accountManager.addOnAccountsUpdatedListener(listener, null, false);
    assertThat(listener.getInvocationCount()).isEqualTo(0);

    Account account = new Account("name", "type");
    shadowAccountManager.addAccount(account);
    assertThat(listener.getInvocationCount()).isEqualTo(1);
  }

  @Test
  public void testAccountsUpdateListener_updateImmediately() {
    TestOnAccountsUpdateListener listener = new TestOnAccountsUpdateListener();
    accountManager.addOnAccountsUpdatedListener(listener, null, true);
    assertThat(listener.getInvocationCount()).isEqualTo(1);
  }

  @Test
  public void testAccountsUpdateListener_listenerNotInvokedAfterRemoval() {
    TestOnAccountsUpdateListener listener = new TestOnAccountsUpdateListener();
    accountManager.addOnAccountsUpdatedListener(listener, null, false);
    assertThat(listener.getInvocationCount()).isEqualTo(0);

    Account account = new Account("name", "type");
    shadowAccountManager.addAccount(account);

    assertThat(listener.getInvocationCount()).isEqualTo(1);

    accountManager.removeOnAccountsUpdatedListener(listener);

    shadowAccountManager.addAccount(account);

    assertThat(listener.getInvocationCount()).isEqualTo(1);
  }

  @Test
  public void testAddAuthenticator() {
    shadowAccountManager.addAuthenticator("type");
    AuthenticatorDescription[] result = accountManager.getAuthenticatorTypes();
    assertThat(result.length).isEqualTo(1);
    assertThat(result[0].type).isEqualTo("type");
  }

  @Test
  public void invalidateAuthToken_noAccount() {
    accountManager.invalidateAuthToken("type1", "token1");
  }

  @Test
  public void invalidateAuthToken_noToken() {
    Account account1 = new Account("name", "type1");
    shadowAccountManager.addAccount(account1);
    accountManager.invalidateAuthToken("type1", "token1");
  }

  @Test
  public void invalidateAuthToken_multipleAccounts() {
    Account account1 = new Account("name", "type1");
    shadowAccountManager.addAccount(account1);

    Account account2 = new Account("name", "type2");
    shadowAccountManager.addAccount(account2);

    accountManager.setAuthToken(account1, "token_type_1", "token1");
    accountManager.setAuthToken(account2, "token_type_1", "token1");

    assertThat(accountManager.peekAuthToken(account1, "token_type_1")).isEqualTo("token1");
    assertThat(accountManager.peekAuthToken(account2, "token_type_1")).isEqualTo("token1");

    // invalidate token for type1 account 
    accountManager.invalidateAuthToken("type1", "token1");
    assertThat(accountManager.peekAuthToken(account1, "token_type_1")).isNull();
    assertThat(accountManager.peekAuthToken(account2, "token_type_1")).isEqualTo("token1");

    // invalidate token for type2 account 
    accountManager.invalidateAuthToken("type2", "token1");
    assertThat(accountManager.peekAuthToken(account1, "token_type_1")).isNull();
    assertThat(accountManager.peekAuthToken(account2, "token_type_1")).isNull();
  }

  @Test
  public void invalidateAuthToken_multipleTokens() {
    Account account = new Account("name", "type1");
    shadowAccountManager.addAccount(account);

    accountManager.setAuthToken(account, "token_type_1", "token1");
    accountManager.setAuthToken(account, "token_type_2", "token2");

    assertThat(accountManager.peekAuthToken(account, "token_type_1")).isEqualTo("token1");
    assertThat(accountManager.peekAuthToken(account, "token_type_2")).isEqualTo("token2");

    // invalidate token1
    accountManager.invalidateAuthToken("type1", "token1");
    assertThat(accountManager.peekAuthToken(account, "token_type_1")).isNull();
    assertThat(accountManager.peekAuthToken(account, "token_type_2")).isEqualTo("token2");

    // invalidate token2
    accountManager.invalidateAuthToken("type1", "token2");
    assertThat(accountManager.peekAuthToken(account, "token_type_1")).isNull();
    assertThat(accountManager.peekAuthToken(account, "token_type_2")).isNull();
  }

  @Test
  public void invalidateAuthToken_multipleTokenTypesSameToken() {
    Account account = new Account("name", "type1");
    shadowAccountManager.addAccount(account);

    accountManager.setAuthToken(account, "token_type_1", "token1");
    accountManager.setAuthToken(account, "token_type_2", "token1");

    assertThat(accountManager.peekAuthToken(account, "token_type_1")).isEqualTo("token1");
    assertThat(accountManager.peekAuthToken(account, "token_type_2")).isEqualTo("token1");

    // invalidate token1
    accountManager.invalidateAuthToken("type1", "token1");
    assertThat(accountManager.peekAuthToken(account, "token_type_1")).isNull();
    assertThat(accountManager.peekAuthToken(account, "token_type_2")).isNull();
  }

  @Test
  public void addAccount_noActivitySpecified() throws Exception {
    shadowAccountManager.addAuthenticator("google.com");

    AccountManagerFuture<Bundle> result = accountManager.addAccount("google.com", "auth_token_type", null, null, null, null, null);

    Bundle resultBundle = result.getResult();

    assertThat(resultBundle.getParcelable(AccountManager.KEY_INTENT)).isNotNull();
  }

  @Test
  public void addAccount_activitySpecified() throws Exception {
    shadowAccountManager.addAuthenticator("google.com");

    AccountManagerFuture<Bundle> result = accountManager.addAccount("google.com", "auth_token_type", null, null, activity, null, null);
    Bundle resultBundle = result.getResult();

    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_TYPE)).isEqualTo("google.com");
    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_NAME)).isEqualTo("some_user@gmail.com");
  }

  @Test
  public void addAccount_shouldCallCallback() throws Exception {
    shadowAccountManager.addAuthenticator("google.com");

    TestAccountManagerCallback<Bundle> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Bundle> result = accountManager.addAccount("google.com", "auth_token_type", null, null, activity, callback, new Handler());

    assertThat(callback.hasBeenCalled()).isFalse();
    assertThat(result.isDone()).isFalse();

    shadowAccountManager.addAccount(new Account("thebomb@google.com", "google.com"));
    assertThat(result.isDone()).isTrue();
    assertThat(callback.accountManagerFuture).isNotNull();

    Bundle resultBundle = callback.getResult();
    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_TYPE)).isEqualTo("google.com");
    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_NAME)).isEqualTo("thebomb@google.com");
  }

  @Test
  public void addAccount_whenSchedulerPaused_shouldCallCallbackAfterSchedulerUnpaused() throws Exception {
    scheduler.pause();
    shadowAccountManager.addAuthenticator("google.com");

    TestAccountManagerCallback<Bundle> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Bundle> result = accountManager.addAccount("google.com", "auth_token_type", null, null, activity, callback, new Handler());
    assertThat(callback.hasBeenCalled()).isFalse();
    assertThat(result.isDone()).isFalse();

    shadowAccountManager.addAccount(new Account("thebomb@google.com", "google.com"));

    scheduler.unPause();

    assertThat(result.isDone()).isTrue();
    assertThat(callback.hasBeenCalled()).isTrue();

    Bundle resultBundle = callback.getResult();
    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_TYPE)).isEqualTo("google.com");
    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_NAME)).isEqualTo("thebomb@google.com");
  }

  @Test
  public void addAccount_noAuthenticatorDefined() throws Exception {
    AccountManagerFuture<Bundle> future = accountManager.addAccount("unknown_account_type", "auth_token_type", null, null, activity, null, null);
    try {
      future.getResult();
      fail("addAccount() should throw an authenticator exception if no authenticator was registered for this account type");
    } catch(AuthenticatorException e) {
      // Expected
    }
  }

  @Test
  public void addAccount_withOptionsShouldSupportGetNextAddAccountOptions() throws Exception {
    assertThat(shadowAccountManager.getNextAddAccountOptions()).isNull();

    shadowAccountManager.addAuthenticator(GOOGLE_ACCOUNT_TYPE);

    Bundle expectedAddAccountOptions = new Bundle();
    expectedAddAccountOptions.putString("option", "value");

    accountManager.addAccount(GOOGLE_ACCOUNT_TYPE, "auth_token_type", null, expectedAddAccountOptions, activity, null, null);

    Bundle actualAddAccountOptions = shadowAccountManager.getNextAddAccountOptions();
    assertThat(shadowAccountManager.getNextAddAccountOptions()).isNull();
    assertThat(actualAddAccountOptions).isEqualTo(expectedAddAccountOptions);
  }

  @Test
  public void addAccount_withDeprecatedAddAccount_shouldAddAccount() throws Exception {
    shadowAccountManager.addAuthenticator(GOOGLE_ACCOUNT_TYPE);

    AccountManagerFuture<Bundle> future =
        accountManager.addAccount(GOOGLE_ACCOUNT_TYPE, "auth_token_type", null, null, activity, null, null);

    assertThat(accountManager.getAccounts()).isEmpty();

    future.getResult();
    assertThat(accountManager.getAccounts()).containsExactly(new Account("some_user@gmail.com", GOOGLE_ACCOUNT_TYPE));
  }

  @Test
  public void addAccount_withDialogOk_shouldCreateAccountAndNotifyListeners() throws Exception {
    shadowAccountManager.addAuthenticator(GOOGLE_ACCOUNT_TYPE);

    accountManager.addAccount(GOOGLE_ACCOUNT_TYPE, "auth_token_type", null, null, activity, null, null);
    accountManager.addOnAccountsUpdatedListener(mockListener, null, false);

    shadowAccountManager.getAddAccountPrompt().enterAccountName("me@google.com");

    Account expectedAccount = new Account("me@google.com", GOOGLE_ACCOUNT_TYPE);
    assertThat(accountManager.getAccounts()).containsExactly(expectedAccount);
    verify(mockListener).onAccountsUpdated(new Account[] { expectedAccount});

    assertThat(shadowAccountManager.getAddAccountPrompt()).isNull();
  }

  @Test
  public void addAccount_withDialogCancel_shouldThrowOperationCancelledException() throws Exception {
    shadowAccountManager.addAuthenticator(GOOGLE_ACCOUNT_TYPE);

    AccountManagerFuture<Bundle> future =
        accountManager.addAccount(GOOGLE_ACCOUNT_TYPE, "auth_token_type", null, null, activity, null, null);
    accountManager.addOnAccountsUpdatedListener(mockListener, null, false);

    shadowAccountManager.getAddAccountPrompt().cancel();
    assertThat(accountManager.getAccounts()).isEmpty();
    verifyZeroInteractions(mockListener);

    try {
      future.getResult();
      fail("should throw");
    } catch (OperationCanceledException expected) { /* cool*/ }

    assertThat(shadowAccountManager.getAddAccountPrompt()).isNull();
  }

  @Test
  public void addAccount_withOptionsShouldSupportPeekNextAddAccountOptions() throws Exception {
    assertThat(shadowAccountManager.peekNextAddAccountOptions()).isNull();

    shadowAccountManager.addAuthenticator("google.com");

    Bundle expectedAddAccountOptions = new Bundle();
    expectedAddAccountOptions.putString("option", "value");
    accountManager.addAccount("google.com", "auth_token_type", null, expectedAddAccountOptions, activity, null, null);

    Bundle actualAddAccountOptions = shadowAccountManager.peekNextAddAccountOptions();
    assertThat(shadowAccountManager.peekNextAddAccountOptions()).isNotNull();
    assertThat(actualAddAccountOptions).isEqualTo(expectedAddAccountOptions);
  }

  @Test
  public void addAccount_withNoAuthenticatorForType_throwsExceptionInGetResult() throws Exception {
    assertThat(shadowAccountManager.peekNextAddAccountOptions()).isNull();

    AccountManagerFuture<Bundle> futureResult = accountManager.addAccount("google.com", "auth_token_type", null, null, activity, null, null);
    try {
      futureResult.getResult();
      fail("should have thrown");
    } catch (AuthenticatorException expected) { }
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void addPreviousAccount() {
    Account account = new Account("name_a", "type_a");
    shadowAccountManager.setPreviousAccountName(account, "old_name");
    assertThat(accountManager.getPreviousName(account)).isEqualTo("old_name");
  }

  @Test
  public void testGetAsSystemService() throws Exception {
    AccountManager systemService = (AccountManager) RuntimeEnvironment.application.getSystemService(Context.ACCOUNT_SERVICE);
    assertThat(systemService).isNotNull();
    assertThat(accountManager).isEqualTo(systemService);
  }

  @Test
  public void getAuthToken() throws Exception {
    Account account = new Account("name", "google.com");
    shadowAccountManager.addAccount(account);
    shadowAccountManager.addAuthenticator("google.com");

    accountManager.setAuthToken(account, "auth_token_type", "token1");

    TestAccountManagerCallback<Bundle> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account,
        "auth_token_type",
        new Bundle(),
        activity,
        callback,
        new Handler());

    assertThat(future.isDone()).isTrue();
    assertThat(future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME)).isEqualTo(account.name);
    assertThat(future.getResult().getString(AccountManager.KEY_ACCOUNT_TYPE)).isEqualTo(account.type);
    assertThat(future.getResult().getString(AccountManager.KEY_AUTHTOKEN)).isEqualTo("token1");

    assertThat(callback.hasBeenCalled()).isTrue();
  }

  @Test
  public void whenPaused_getAuthToken() throws Exception {
    scheduler.pause();
    Account account = new Account("name", "google.com");
    shadowAccountManager.addAccount(account);
    shadowAccountManager.addAuthenticator("google.com");

    accountManager.setAuthToken(account, "auth_token_type", "token1");

    TestAccountManagerCallback<Bundle> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account, "auth_token_type", new Bundle(), activity, callback, new Handler());

    assertThat(future.isDone()).isFalse();
    assertThat(callback.hasBeenCalled()).isFalse();

    scheduler.unPause();

    assertThat(future.isDone()).isTrue();
    assertThat(callback.hasBeenCalled()).isTrue();
  }

  @Test
  public void getHasFeatures_returnsTrueWhenAllFeaturesSatisfied() throws Exception {
    Account account = new Account("name", "google.com");
    shadowAccountManager.addAccount(account);
    shadowAccountManager.setFeatures(account, new String[] { "FEATURE_1", "FEATURE_2" });

    TestAccountManagerCallback<Boolean> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Boolean> future = accountManager.hasFeatures(account, new String[] { "FEATURE_1", "FEATURE_2" }, callback, new Handler());

    assertThat(future.isDone()).isTrue();
    assertThat(future.getResult().booleanValue()).isEqualTo(true);

    assertThat(callback.hasBeenCalled()).isTrue();
  }

  @Test
  public void whenSchedulerPaused_getHasFeatures_returnsTrueWhenAllFeaturesSatisfied() throws Exception {
    scheduler.pause();

    Account account = new Account("name", "google.com");
    shadowAccountManager.addAccount(account);
    shadowAccountManager.setFeatures(account, new String[] { "FEATURE_1", "FEATURE_2" });

    TestAccountManagerCallback<Boolean> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Boolean> future = accountManager.hasFeatures(account, new String[] { "FEATURE_1", "FEATURE_2" }, callback, new Handler());

    assertThat(future.isDone()).isFalse();
    assertThat(callback.hasBeenCalled()).isFalse();
    assertThat(future.getResult()).isNull();

    scheduler.unPause();
    assertThat(future.getResult().booleanValue()).isEqualTo(true);
    assertThat(future.isDone()).isTrue();
    assertThat(callback.hasBeenCalled()).isTrue();
  }

  @Test
  public void getHasFeatures_returnsFalseWhenAllFeaturesNotSatisfied() throws Exception {
    Account account = new Account("name", "google.com");
    shadowAccountManager.addAccount(account);
    shadowAccountManager.setFeatures(account, new String[] { "FEATURE_1" });

    TestAccountManagerCallback<Boolean> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Boolean> future = accountManager.hasFeatures(account, new String[] { "FEATURE_1", "FEATURE_2" }, callback, new Handler());

    assertThat(future.isDone()).isTrue();
    assertThat(future.getResult().booleanValue()).isEqualTo(false);
    assertThat(callback.hasBeenCalled()).isTrue();
  }

  @Test
  public void getAccountsByTypeAndFeatures() throws Exception {

    Account accountWithCorrectTypeAndFeatures = new Account("account_1", "google.com");
    shadowAccountManager.addAccount(accountWithCorrectTypeAndFeatures);
    shadowAccountManager.setFeatures(accountWithCorrectTypeAndFeatures, new String[] { "FEATURE_1", "FEATURE_2" });

    Account accountWithCorrectTypeButNotFeatures = new Account("account_2", "google.com");
    shadowAccountManager.addAccount(accountWithCorrectTypeButNotFeatures);
    shadowAccountManager.setFeatures(accountWithCorrectTypeButNotFeatures, new String[] { "FEATURE_1" });

    Account accountWithCorrectFeaturesButNotType = new Account("account_3", "facebook.com");
    shadowAccountManager.addAccount(accountWithCorrectFeaturesButNotType);
    shadowAccountManager.setFeatures(accountWithCorrectFeaturesButNotType, new String[] { "FEATURE_1", "FEATURE_2" });


    TestAccountManagerCallback<Account[]> callback = new TestAccountManagerCallback<>();

    AccountManagerFuture<Account[]> future = accountManager.getAccountsByTypeAndFeatures("google.com", new String[] { "FEATURE_1", "FEATURE_2" }, callback, new Handler());

    assertThat(future.isDone()).isTrue();
    assertThat(future.getResult()).containsOnly(accountWithCorrectTypeAndFeatures);

    assertThat(callback.hasBeenCalled()).isTrue();
  }

  @Test
  public void whenSchedulerPaused_getAccountsByTypeAndFeatures() throws Exception {
    scheduler.pause();

    Account accountWithCorrectTypeAndFeatures = new Account("account_1", "google.com");
    shadowAccountManager.addAccount(accountWithCorrectTypeAndFeatures);
    shadowAccountManager.setFeatures(accountWithCorrectTypeAndFeatures, new String[] { "FEATURE_1", "FEATURE_2" });

    TestAccountManagerCallback<Account[]> callback = new TestAccountManagerCallback<>();

    AccountManagerFuture<Account[]> future = accountManager.getAccountsByTypeAndFeatures("google.com", new String[] { "FEATURE_1", "FEATURE_2" }, callback, new Handler());

    assertThat(future.isDone()).isFalse();
    assertThat(callback.hasBeenCalled()).isFalse();

    scheduler.unPause();
    assertThat(future.getResult()).containsOnly(accountWithCorrectTypeAndFeatures);

    assertThat(future.isDone()).isTrue();
    assertThat(callback.hasBeenCalled()).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getAccountsByTypeForPackage() {
    Account[] accountsByTypeForPackage = accountManager.getAccountsByTypeForPackage(null, "org.somepackage");

    assertThat(accountsByTypeForPackage).isEmpty();

    Account accountVisibleToPackage = new Account("user@gmail.com", "gmail.com");
    shadowAccountManager.addAccount(accountVisibleToPackage, "org.somepackage");

    accountsByTypeForPackage = accountManager.getAccountsByTypeForPackage("other_type", "org.somepackage");
    assertThat(accountsByTypeForPackage).isEmpty();

    accountsByTypeForPackage = accountManager.getAccountsByTypeForPackage("gmail.com", "org.somepackage");
    assertThat(accountsByTypeForPackage).containsOnly(accountVisibleToPackage);

    accountsByTypeForPackage = accountManager.getAccountsByTypeForPackage(null, "org.somepackage");
    assertThat(accountsByTypeForPackage).containsOnly(accountVisibleToPackage);
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void removeAccountExplicitly() {
    assertThat(accountManager.removeAccountExplicitly(new Account("non_existant_account@gmail.com", "gmail.com"))).isFalse();
    assertThat(accountManager.removeAccountExplicitly(null)).isFalse();

    Account account = new Account("name@gmail.com", "gmail.com");
    shadowAccountManager.addAccount(account);

    assertThat(accountManager.removeAccountExplicitly(account)).isTrue();
  }

  @Test
  public void removeAllAccounts() throws Exception {

    Account account = new Account("name@gmail.com", "gmail.com");
    shadowAccountManager.addAccount(account);

    assertThat(accountManager.getAccounts()).isNotEmpty();

    shadowAccountManager.removeAllAccounts();

    assertThat(accountManager.getAccounts()).isEmpty();
  }

  private static class TestAccountManagerCallback<T> implements AccountManagerCallback<T> {
    private AccountManagerFuture<T> accountManagerFuture;

    @Override
    public void run(AccountManagerFuture<T> accountManagerFuture) {
      this.accountManagerFuture = accountManagerFuture;
    }
    
    boolean hasBeenCalled() {
      return accountManagerFuture != null;
    }
    
    T getResult() throws Exception {
      return accountManagerFuture.getResult();
    }
  }
}
