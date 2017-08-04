package org.robolectric.shadows.gms;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.auth.AccountChangeEvent;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.UserRecoverableNotifiedException;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Calls to static method of {@link GoogleAuthUtil} will be redirected to the provided
 * {@link GoogleAuthUtilImpl} implementation. Use {@link #provideImpl(GoogleAuthUtilImpl)}
 * to set the implementation instance. By default, a {@link GoogleAuthUtilImpl} is used in call
 * redirection. Use mocks or subclassing {@link GoogleAuthUtilImpl} to achieve desired behaviors.
 */
@Implements(GoogleAuthUtil.class)
public class ShadowGoogleAuthUtil {

  private static GoogleAuthUtilImpl googleAuthUtilImpl = new GoogleAuthUtilImpl();

  public static synchronized GoogleAuthUtilImpl getImpl() {
    return googleAuthUtilImpl;
  }

  public static synchronized void provideImpl(GoogleAuthUtilImpl impl) {
    googleAuthUtilImpl = Preconditions.checkNotNull(impl);
  }

  @Resetter
  public static synchronized void reset() {
    googleAuthUtilImpl = new GoogleAuthUtilImpl();
  }

  @Implementation
  public static synchronized void clearToken(Context context, String token)
      throws GooglePlayServicesAvailabilityException, GoogleAuthException, IOException {
    googleAuthUtilImpl.clearToken(context, token);
  }

  @Implementation
  public static synchronized List<AccountChangeEvent> getAccountChangeEvents(Context context,
      int eventIndex, String accountName)
          throws GoogleAuthException, IOException {
    return googleAuthUtilImpl.getAccountChangeEvents(context, eventIndex, accountName);
  }

  @Implementation
  public static synchronized String getAccountId(Context ctx, String accountName)
      throws GoogleAuthException, IOException {
    return googleAuthUtilImpl.getAccountId(ctx, accountName);
  }

  @Implementation
  public static synchronized String getToken(Context context, Account account, String scope)
      throws IOException, UserRecoverableAuthException, GoogleAuthException {
    return googleAuthUtilImpl.getToken(context, account, scope);
  }

  @Implementation
  public static synchronized String getToken(Context context, Account account, String scope,
      Bundle extras) throws IOException, UserRecoverableAuthException, GoogleAuthException {
    return googleAuthUtilImpl.getToken(context, account, scope, extras);
  }

  @Implementation
  public static synchronized String getToken(Context context, String accountName, String scope)
      throws IOException, UserRecoverableAuthException, GoogleAuthException {
    return googleAuthUtilImpl.getToken(context, accountName, scope);
  }

  @Implementation
  public static synchronized String getToken(Context context, String accountName, String scope,
      Bundle extras) throws IOException, UserRecoverableAuthException, GoogleAuthException {
    return googleAuthUtilImpl.getToken(context, accountName, scope, extras);
  }

  @Implementation
  public static synchronized String getTokenWithNotification(Context context, Account account,
      String scope, Bundle extras)
          throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
    return googleAuthUtilImpl.getTokenWithNotification(context, account, scope, extras);
  }

  @Implementation
  public static synchronized String getTokenWithNotification(Context context, Account account,
      String scope, Bundle extras, Intent callback)
          throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
    return googleAuthUtilImpl
        .getTokenWithNotification(context, account, scope, extras, callback);
  }

  @Implementation
  public static synchronized String getTokenWithNotification(Context context, Account account,
      String scope, Bundle extras, String authority, Bundle syncBundle)
          throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
    return googleAuthUtilImpl
        .getTokenWithNotification(context, account, scope, extras, authority, syncBundle);
  }

  @Implementation
  public static synchronized String getTokenWithNotification(Context context, String accountName,
      String scope, Bundle extras, Intent callback)
          throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
    return googleAuthUtilImpl
        .getTokenWithNotification(context, accountName, scope, extras, callback);
  }

  @Implementation
  public static synchronized String getTokenWithNotification(Context context, String accountName,
      String scope, Bundle extras)
          throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
    return googleAuthUtilImpl.getTokenWithNotification(context, accountName, scope, extras);
  }

  @Implementation
  public static synchronized String getTokenWithNotification(Context context, String accountName,
      String scope, Bundle extras, String authority, Bundle syncBundle)
          throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
    return googleAuthUtilImpl.getTokenWithNotification(context, accountName, scope, extras,
        authority, syncBundle);
  }

  @Implementation
  public static synchronized void invalidateToken(Context context, String token) {
    googleAuthUtilImpl.invalidateToken(context, token);
  }

  /**
   * Class containing methods with same signatures of the static methods of {@link GoogleAuthUtil}
   */
  public static class GoogleAuthUtilImpl {
    public void clearToken(Context context, String token)
        throws GooglePlayServicesAvailabilityException, GoogleAuthException, IOException {}

    public List<AccountChangeEvent> getAccountChangeEvents(Context context, int eventIndex,
        String accountName) throws GoogleAuthException, IOException {
      return new ArrayList<>();
    }

    public String getAccountId(Context ctx, String accountName)
        throws GoogleAuthException, IOException {
      return "accountId";
    }

    public String getToken(Context context, Account account, String scope)
        throws IOException, UserRecoverableAuthException, GoogleAuthException {
      return "token";
    }

    public String getToken(Context context, Account account, String scope, Bundle extras)
        throws IOException, UserRecoverableAuthException, GoogleAuthException {
      return "token";
    }

    public String getToken(Context context, String accountName, String scope)
        throws IOException, UserRecoverableAuthException, GoogleAuthException {
      return getToken(context, new Account(accountName, "robo"), scope);
    }

    public String getToken(Context context, String accountName, String scope, Bundle extras)
        throws IOException, UserRecoverableAuthException, GoogleAuthException {
      return getToken(context, new Account(accountName, "robo"), scope, extras);
    }

    public String getTokenWithNotification(Context context, Account account, String scope,
        Bundle extras)
            throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
      return "token";
    }

    public String getTokenWithNotification(Context context, Account account, String scope,
        Bundle extras, Intent callback)
            throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
      if (callback == null) {
        throw new IllegalArgumentException("Callback cannot be null.");
      }
      return "token";
    }

    public String getTokenWithNotification(Context context, Account account, String scope,
        Bundle extras, String authority, Bundle syncBundle)
            throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
      if (authority == null || authority.length() == 0) {
        throw new IllegalArgumentException("Authority cannot be empty.");
      }
      return "token";
    }

    public String getTokenWithNotification(Context context, String accountName, String scope,
        Bundle extras)
            throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
      return getTokenWithNotification(context, new Account(accountName, "robo"), scope,
          extras);
    }

    public String getTokenWithNotification(Context context, String accountName, String scope,
        Bundle extras, Intent callback)
            throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
      return getTokenWithNotification(context, new Account(accountName, "robo"), scope,
          extras, callback);
    }

    public String getTokenWithNotification(Context context, String accountName, String scope,
        Bundle extras, String authority, Bundle syncBundle)
            throws IOException, UserRecoverableNotifiedException, GoogleAuthException {
      return getTokenWithNotification(context, new Account(accountName, "robo"), scope,
          extras, authority, syncBundle);
    }

    public void invalidateToken(Context context, String token) {}
  }
}
