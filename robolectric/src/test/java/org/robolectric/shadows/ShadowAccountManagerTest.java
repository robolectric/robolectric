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
import android.app.Application;
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

import java.io.IOException;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowAccountManagerTest {
  Application app;
  AccountManager am;

  @Before
  public void setUp() throws Exception {
    app = RuntimeEnvironment.application;
    am = AccountManager.get(app);
  }

  @Test
  public void testGet() {
    assertThat(am).isNotNull();
    assertThat(am).isSameAs(AccountManager.get(app));

    AccountManager activityAM = AccountManager.get(RuntimeEnvironment.application);
    assertThat(activityAM).isNotNull();
    assertThat(activityAM).isSameAs(am);
  }

  @Test
  public void testGetAccounts() {
    assertThat(am.getAccounts()).isNotNull();
    assertThat(am.getAccounts().length).isEqualTo(0);

    Account a1 = new Account("name_a", "type_a");
    shadowOf(am).addAccount(a1);
    assertThat(am.getAccounts()).isNotNull();
    assertThat(am.getAccounts().length).isEqualTo(1);
    assertThat(am.getAccounts()[0]).isSameAs(a1);

    Account a2 = new Account("name_b", "type_b");
    shadowOf(am).addAccount(a2);
    assertThat(am.getAccounts()).isNotNull();
    assertThat(am.getAccounts().length).isEqualTo(2);
    assertThat(am.getAccounts()[1]).isSameAs(a2);
  }

  @Test
  public void testGetAccountsByType() {
    assertThat(am.getAccountsByType("name_a")).isNotNull();
    assertThat(am.getAccounts().length).isEqualTo(0);

    Account a1 = new Account("name_a", "type_a");
    shadowOf(am).addAccount(a1);
    Account[] accounts = am.getAccountsByType("type_a");
    assertThat(accounts).isNotNull();
    assertThat(accounts.length).isEqualTo(1);
    assertThat(accounts[0]).isSameAs(a1);

    Account a2 = new Account("name_b", "type_b");
    shadowOf(am).addAccount(a2);
    accounts = am.getAccountsByType("type_a");
    assertThat(accounts).isNotNull();
    assertThat(accounts.length).isEqualTo(1);
    assertThat(accounts[0]).isSameAs(a1);

    Account a3 = new Account("name_c", "type_a");
    shadowOf(am).addAccount(a3);
    accounts = am.getAccountsByType("type_a");
    assertThat(accounts).isNotNull();
    assertThat(accounts.length).isEqualTo(2);
    assertThat(accounts[0]).isSameAs(a1);
    assertThat(accounts[1]).isSameAs(a3);
  }

  @Test
  public void addAuthToken() {
    Account account = new Account("name", "type");
    shadowOf(am).addAccount(account);

    am.setAuthToken(account, "token_type_1", "token1");
    am.setAuthToken(account, "token_type_2", "token2");

    assertThat(am.peekAuthToken(account, "token_type_1")).isEqualTo("token1");
    assertThat(am.peekAuthToken(account, "token_type_2")).isEqualTo("token2");
  }

  @Test
  public void setAuthToken_shouldNotAddTokenIfAccountNotPresent() {
    Account account = new Account("name", "type");
    am.setAuthToken(account, "token_type_1", "token1");
    assertThat(am.peekAuthToken(account, "token_type_1")).isNull();
  }

  @Test
  public void testAddAccountExplicitly_noPasswordNoExtras() {
    Account account = new Account("name", "type");
    boolean accountAdded = am.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();
    assertThat(am.getAccountsByType("type").length).isEqualTo(1);
    assertThat(am.getAccountsByType("type")[0].name).isEqualTo("name");

    boolean accountAddedTwice = am.addAccountExplicitly(account, null, null);
    assertThat(accountAddedTwice).isFalse();

    account = new Account("another_name", "type");
    accountAdded = am.addAccountExplicitly(account, null, null);
    assertThat(accountAdded).isTrue();
    assertThat(am.getAccountsByType("type").length).isEqualTo(2);
    assertThat(am.getAccountsByType("type")[0].name).isEqualTo("name");
    assertThat(am.getAccountsByType("type")[1].name).isEqualTo("another_name");
    assertThat(am.getPassword(account)).isNull();

    try {
      am.addAccountExplicitly(null, null, null);
      fail("An illegal argument exception should have been thrown when trying to add a null account");
    } catch (IllegalArgumentException iae) {
      // NOP
    }
  }

  @Test
  public void testAddAccountExplicitly_withPassword() {
    Account account = new Account("name", "type");
    boolean accountAdded = am.addAccountExplicitly(account, "passwd", null);

    assertThat(accountAdded).isTrue();
    assertThat(am.getPassword(account)).isEqualTo("passwd");
  }

  @Test
  public void testAddAccountExplicitly_withExtras() {
    Account account = new Account("name", "type");
    Bundle extras = new Bundle();
    extras.putString("key123", "value123");
    boolean accountAdded = am.addAccountExplicitly(account, null, extras);

    assertThat(accountAdded).isTrue();
    assertThat(am.getUserData(account, "key123")).isEqualTo("value123");
    assertThat(am.getUserData(account, "key456")).isNull();
  }

  @Test
  public void testGetSetUserData_addToInitiallyEmptyExtras() {
    Account account = new Account("name", "type");
    boolean accountAdded = am.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();

    am.setUserData(account, "key123", "value123");
    assertThat(am.getUserData(account, "key123")).isEqualTo("value123");
  }

  @Test
  public void testGetSetUserData_overwrite() {
    Account account = new Account("name", "type");
    boolean accountAdded = am.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();

    am.setUserData(account, "key123", "value123");
    assertThat(am.getUserData(account, "key123")).isEqualTo("value123");

    am.setUserData(account, "key123", "value456");
    assertThat(am.getUserData(account, "key123")).isEqualTo("value456");
  }

  @Test
  public void testGetSetUserData_remove() {
    Account account = new Account("name", "type");
    boolean accountAdded = am.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();

    am.setUserData(account, "key123", "value123");
    assertThat(am.getUserData(account, "key123")).isEqualTo("value123");

    am.setUserData(account, "key123", null);
    assertThat(am.getUserData(account, "key123")).isNull();
  }

  @Test
  public void testGetSetPassword_setInAccountInitiallyWithNoPassword() {
    Account account = new Account("name", "type");
    boolean accountAdded = am.addAccountExplicitly(account, null, null);

    assertThat(accountAdded).isTrue();
    assertThat(am.getPassword(account)).isNull();

    am.setPassword(account, "passwd");
    assertThat(am.getPassword(account)).isEqualTo("passwd");
  }

  @Test
  public void testGetSetPassword_overwrite() {
    Account account = new Account("name", "type");
    boolean accountAdded = am.addAccountExplicitly(account, "passwd1", null);

    assertThat(accountAdded).isTrue();
    assertThat(am.getPassword(account)).isEqualTo("passwd1");

    am.setPassword(account, "passwd2");
    assertThat(am.getPassword(account)).isEqualTo("passwd2");
  }

  @Test
  public void testGetSetPassword_remove() {
    Account account = new Account("name", "type");
    boolean accountAdded = am.addAccountExplicitly(account, "passwd1", null);

    assertThat(accountAdded).isTrue();
    assertThat(am.getPassword(account)).isEqualTo("passwd1");

    am.setPassword(account, null);
    assertThat(am.getPassword(account)).isNull();
  }

  @Test
  public void testBlockingGetAuthToken() throws AuthenticatorException, OperationCanceledException, IOException {
    Account account = new Account("name", "type");
    shadowOf(am).addAccount(account);

    am.setAuthToken(account, "token_type_1", "token1");
    am.setAuthToken(account, "token_type_2", "token2");

    assertThat(am.blockingGetAuthToken(account, "token_type_1", false)).isEqualTo("token1");
    assertThat(am.blockingGetAuthToken(account, "token_type_2", false)).isEqualTo("token2");

    try {
      am.blockingGetAuthToken(null, "token_type_1", false);
      fail("blockingGetAuthToken() should throw an illegal argument exception if the account is null");
    } catch (IllegalArgumentException iae) {
      // Expected
    }
    try {
      am.blockingGetAuthToken(account, null, false);
      fail("blockingGetAuthToken() should throw an illegal argument exception if the auth token type is null");
    } catch (IllegalArgumentException iae) {
      // Expected
    }

    Account account1 = new Account("unknown", "type");
    assertThat(am.blockingGetAuthToken(account1, "token_type_1", false)).isNull();
  }

  @Test
  public void removeAccount_throwsIllegalArgumentException_whenPassedNullAccount() {
    Account account = new Account("name", "type");
    shadowOf(am).addAccount(account);

    try {
      am.removeAccount(null, null, null);
      fail("removeAccount() should throw an illegal argument exception if the account is null");
    } catch (IllegalArgumentException iae) {
      // Expected
    }
  }

  @Test
  public void removeAccount_doesNotRemoveAccountOfDifferentName() throws Exception {
    Account account = new Account("name", "type");
    shadowOf(am).addAccount(account);

    Account wrongAccount = new Account("wrong_name", "type");
    AccountManagerFuture<Boolean> future = am.removeAccount(wrongAccount, null, null);
    assertThat(future.getResult()).isFalse();
    assertThat(am.getAccountsByType("type")).isNotEmpty();
  }

  @Test
  public void removeAccount_does() throws Exception {
    Account account = new Account("name", "type");
    shadowOf(am).addAccount(account);

    AccountManagerFuture<Boolean> future = am.removeAccount(account, null, null);
    assertThat(future.getResult()).isTrue();
    assertThat(am.getAccountsByType("type")).isEmpty();
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
    am.addOnAccountsUpdatedListener(listener, null, false);
    assertThat(listener.getInvocationCount()).isEqualTo(0);

    Account account = new Account("name", "type");
    shadowOf(am).addAccount(account);
    assertThat(listener.getInvocationCount()).isEqualTo(1);
  }

  @Test
  public void testAccountsUpdateListener_duplicate() {
    TestOnAccountsUpdateListener listener = new TestOnAccountsUpdateListener();
    am.addOnAccountsUpdatedListener(listener, null, false);
    am.addOnAccountsUpdatedListener(listener, null, false);
    assertThat(listener.getInvocationCount()).isEqualTo(0);

    Account account = new Account("name", "type");
    shadowOf(am).addAccount(account);
    assertThat(listener.getInvocationCount()).isEqualTo(1);
  }

  @Test
  public void testAccountsUpdateListener_updateImmediately() {
    TestOnAccountsUpdateListener listener = new TestOnAccountsUpdateListener();
    am.addOnAccountsUpdatedListener(listener, null, true);
    assertThat(listener.getInvocationCount()).isEqualTo(1);
  }

  @Test
  public void testAddAuthenticator() {
    shadowOf(am).addAuthenticator("type");
    AuthenticatorDescription[] result = am.getAuthenticatorTypes();
    assertThat(result.length).isEqualTo(1);
    assertThat(result[0].type).isEqualTo("type");
  }

  @Test
  public void invalidateAuthToken_noAccount() {
    am.invalidateAuthToken("type1", "token1");
  }

  @Test
  public void invalidateAuthToken_noToken() {
    Account account1 = new Account("name", "type1");
    shadowOf(am).addAccount(account1);
    am.invalidateAuthToken("type1", "token1");
  }

  @Test
  public void invalidateAuthToken_multipleAccounts() {
    Account account1 = new Account("name", "type1");
    shadowOf(am).addAccount(account1);

    Account account2 = new Account("name", "type2");
    shadowOf(am).addAccount(account2);

    am.setAuthToken(account1, "token_type_1", "token1");
    am.setAuthToken(account2, "token_type_1", "token1");

    assertThat(am.peekAuthToken(account1, "token_type_1")).isEqualTo("token1");
    assertThat(am.peekAuthToken(account2, "token_type_1")).isEqualTo("token1");

    // invalidate token for type1 account 
    am.invalidateAuthToken("type1", "token1");
    assertThat(am.peekAuthToken(account1, "token_type_1")).isNull();
    assertThat(am.peekAuthToken(account2, "token_type_1")).isEqualTo("token1");

    // invalidate token for type2 account 
    am.invalidateAuthToken("type2", "token1");
    assertThat(am.peekAuthToken(account1, "token_type_1")).isNull();
    assertThat(am.peekAuthToken(account2, "token_type_1")).isNull();
  }

  @Test
  public void invalidateAuthToken_multipleTokens() {
    Account account = new Account("name", "type1");
    shadowOf(am).addAccount(account);

    am.setAuthToken(account, "token_type_1", "token1");
    am.setAuthToken(account, "token_type_2", "token2");

    assertThat(am.peekAuthToken(account, "token_type_1")).isEqualTo("token1");
    assertThat(am.peekAuthToken(account, "token_type_2")).isEqualTo("token2");

    // invalidate token1
    am.invalidateAuthToken("type1", "token1");
    assertThat(am.peekAuthToken(account, "token_type_1")).isNull();
    assertThat(am.peekAuthToken(account, "token_type_2")).isEqualTo("token2");

    // invalidate token2
    am.invalidateAuthToken("type1", "token2");
    assertThat(am.peekAuthToken(account, "token_type_1")).isNull();
    assertThat(am.peekAuthToken(account, "token_type_2")).isNull();
  }

  @Test
  public void invalidateAuthToken_multipleTokenTypesSameToken() {
    Account account = new Account("name", "type1");
    shadowOf(am).addAccount(account);

    am.setAuthToken(account, "token_type_1", "token1");
    am.setAuthToken(account, "token_type_2", "token1");

    assertThat(am.peekAuthToken(account, "token_type_1")).isEqualTo("token1");
    assertThat(am.peekAuthToken(account, "token_type_2")).isEqualTo("token1");

    // invalidate token1
    am.invalidateAuthToken("type1", "token1");
    assertThat(am.peekAuthToken(account, "token_type_1")).isNull();
    assertThat(am.peekAuthToken(account, "token_type_2")).isNull();
  }

  @Test
  public void addAccount_noActivitySpecified() throws Exception {
    shadowOf(am).addAuthenticator("google.com");

    AccountManagerFuture<Bundle> result = am.addAccount("google.com", "auth_token_type", null, null, null, null, null);

    Bundle resultBundle = result.getResult();

    assertThat(resultBundle.getParcelable(AccountManager.KEY_INTENT)).isNotNull();
  }

  @Test
  public void addAccount_activitySpecified() throws Exception {
    shadowOf(am).addAuthenticator("google.com");

    AccountManagerFuture<Bundle> result = am.addAccount("google.com", "auth_token_type", null, null, new Activity(), null, null);

    Bundle resultBundle = result.getResult();

    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_TYPE)).isEqualTo("google.com");
    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_NAME)).isNotNull();
  }

  @Test
  public void addAccount_shouldCallCallback() throws Exception {
    shadowOf(am).addAuthenticator("google.com");

    TestAccountManagerCallback<Bundle> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Bundle> result = am.addAccount("google.com", "auth_token_type", null, null, new Activity(), callback, null);
    assertThat(callback.accountManagerFuture).isNull();
    assertThat(result.isDone()).isFalse();

    shadowOf(am).addAccount(new Account("thebomb@google.com", "google.com"));
    assertThat(result.isDone()).isTrue();
    assertThat(callback.accountManagerFuture).isNotNull();

    Bundle resultBundle = callback.accountManagerFuture.getResult();
    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_TYPE)).isEqualTo("google.com");
    assertThat(resultBundle.getString(AccountManager.KEY_ACCOUNT_NAME)).isEqualTo("thebomb@google.com");
  }

  @Test
  public void addAccount_noAuthenticatorDefined() throws Exception {
    try {
      am.addAccount("unknown_account_type", "auth_token_type", null, null, new Activity(), null, null).getResult();
      fail("addAccount() should throw an authenticator exception if no authenticator was registered for this account type");
    } catch(AuthenticatorException e) {
      // Expected
    }
  }

  @Test
  public void addAccount_withOptionsShouldSupportGetNextAddAccountOptions() throws Exception {
    assertThat(shadowOf(am).getNextAddAccountOptions()).isNull();

    shadowOf(am).addAuthenticator("google.com");

    Bundle expectedAddAccountOptions = new Bundle();
    expectedAddAccountOptions.putString("option", "value");
    am.addAccount("google.com", "auth_token_type", null, expectedAddAccountOptions, new Activity(), null, null);

    Bundle actualAddAccountOptions = shadowOf(am).getNextAddAccountOptions();
    assertThat(shadowOf(am).getNextAddAccountOptions()).isNull();
    assertThat(actualAddAccountOptions).isEqualTo(expectedAddAccountOptions);
  }

  @Test
  public void addAccount_withOptionsShouldSupportPeekNextAddAccountOptions() throws Exception {
    assertThat(shadowOf(am).peekNextAddAccountOptions()).isNull();

    shadowOf(am).addAuthenticator("google.com");

    Bundle expectedAddAccountOptions = new Bundle();
    expectedAddAccountOptions.putString("option", "value");
    am.addAccount("google.com", "auth_token_type", null, expectedAddAccountOptions, new Activity(), null, null);

    Bundle actualAddAccountOptions = shadowOf(am).peekNextAddAccountOptions();
    assertThat(shadowOf(am).peekNextAddAccountOptions()).isNotNull();
    assertThat(actualAddAccountOptions).isEqualTo(expectedAddAccountOptions);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void addPreviousAccount() {
    Account account = new Account("name_a", "type_a");
    shadowOf(am).setPreviousAccountName(account, "old_name");
    assertThat(am.getPreviousName(account)).isEqualTo("old_name");
  }

  @Test
  public void testGetAsSystemService() throws Exception {
    AccountManager systemService = (AccountManager) app.getSystemService(Context.ACCOUNT_SERVICE);
    assertThat(systemService).isNotNull();
    assertThat(am).isEqualTo(systemService);
  }

  @Test
  public void getAuthToken() throws Exception {
    Account account = new Account("name", "google.com");
    shadowOf(am).addAccount(account);
    shadowOf(am).addAuthenticator("google.com");

    am.setAuthToken(account, "auth_token_type", "token1");

    TestAccountManagerCallback<Bundle> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Bundle> future = am.getAuthToken(account,
        "auth_token_type",
        new Bundle(),
        Robolectric.setupActivity(Activity.class),
        callback,
        new Handler());

    assertThat(future.isDone()).isFalse();
    assertThat(future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME)).isEqualTo(account.name);
    assertThat(future.getResult().getString(AccountManager.KEY_ACCOUNT_TYPE)).isEqualTo(account.type);
    assertThat(future.getResult().getString(AccountManager.KEY_AUTHTOKEN)).isEqualTo("token1");
    assertThat(future.isDone()).isTrue();
  }

  @Test
  public void getHasFeatures_returnsTrueWhenAllFeaturesSatisfied() throws Exception {
    Account account = new Account("name", "google.com");
    shadowOf(am).addAccount(account);
    shadowOf(am).setFeatures(account, new String[] { "FEATURE_1", "FEATURE_2" });

    TestAccountManagerCallback<Boolean> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Boolean> future = am.hasFeatures(account, new String[] { "FEATURE_1", "FEATURE_2" }, callback, new Handler());

    assertThat(future.isDone()).isFalse();
    assertThat(future.getResult().booleanValue()).isEqualTo(true);
    assertThat(future.isDone()).isTrue();
  }

  @Test
  public void getHasFeatures_returnsFalseWhenAllFeaturesNotSatisfied() throws Exception {
    Account account = new Account("name", "google.com");
    shadowOf(am).addAccount(account);
    shadowOf(am).setFeatures(account, new String[] { "FEATURE_1" });

    TestAccountManagerCallback<Boolean> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Boolean> future = am.hasFeatures(account, new String[] { "FEATURE_1", "FEATURE_2" }, callback, new Handler());

    assertThat(future.isDone()).isFalse();
    assertThat(future.getResult().booleanValue()).isEqualTo(false);
    assertThat(future.isDone()).isTrue();
  }

  @Test
  public void getAccountsByTypeAndFeatures() throws Exception {

    Account accountWithCorrectTypeAndFeatures = new Account("account_1", "google.com");
    shadowOf(am).addAccount(accountWithCorrectTypeAndFeatures);
    shadowOf(am).setFeatures(accountWithCorrectTypeAndFeatures, new String[] { "FEATURE_1", "FEATURE_2" });

    Account accountWithCorrectTypeButNotFeatures = new Account("account_2", "google.com");
    shadowOf(am).addAccount(accountWithCorrectTypeButNotFeatures);
    shadowOf(am).setFeatures(accountWithCorrectTypeButNotFeatures, new String[] { "FEATURE_1" });

    Account accountWithCorrectFeaturesButNotType = new Account("account_3", "facebook.com");
    shadowOf(am).addAccount(accountWithCorrectFeaturesButNotType);
    shadowOf(am).setFeatures(accountWithCorrectFeaturesButNotType, new String[] { "FEATURE_1", "FEATURE_2" });


    TestAccountManagerCallback<Account[]> callback = new TestAccountManagerCallback<>();
    AccountManagerFuture<Account[]> future = am.getAccountsByTypeAndFeatures("google.com", new String[] { "FEATURE_1", "FEATURE_2" }, callback, new Handler());

    assertThat(future.isDone()).isFalse();
    assertThat(future.getResult()).containsOnly(accountWithCorrectTypeAndFeatures);
    assertThat(future.isDone()).isTrue();
  }

  private static class TestAccountManagerCallback<T> implements AccountManagerCallback<T> {
    AccountManagerFuture<T> accountManagerFuture;

    @Override
    public void run(AccountManagerFuture<T> accountManagerFuture) {
      this.accountManagerFuture = accountManagerFuture;
    }
  }
}
