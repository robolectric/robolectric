package org.robolectric.shadows.gms;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.common.base.Preconditions;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Calls to static method of {@link GooglePlayServicesUtil} will be redirected to the provided
 * {@link GooglePlayServicesUtilImpl} implementation. Use
 * {@link #provideImpl(GooglePlayServicesUtilImpl)} to
 * set the implementation instance. By default, a {@link GooglePlayServicesUtilImpl} is used in call
 * redirection. Use mocks or subclassing {@link GooglePlayServicesUtilImpl} to achieve desired
 * behaviors.
 */
@Implements(GooglePlayServicesUtil.class)
public class ShadowGooglePlayServicesUtil {
  private static GooglePlayServicesUtilImpl googlePlayServicesUtilImpl = 
      new GooglePlayServicesUtilImpl();

  public static synchronized GooglePlayServicesUtilImpl getImpl() {
    return googlePlayServicesUtilImpl;
  }

  public static synchronized void provideImpl(GooglePlayServicesUtilImpl impl) {
    googlePlayServicesUtilImpl = Preconditions.checkNotNull(impl);
  }

  @Resetter
  public static synchronized void reset() {
    googlePlayServicesUtilImpl = new GooglePlayServicesUtilImpl();
  }

  @Implementation
  public static synchronized Context getRemoteContext(Context context) {
    return googlePlayServicesUtilImpl.getRemoteContext(context);
  }

  @Implementation
  public static synchronized Resources getRemoteResource(Context context) {
    return googlePlayServicesUtilImpl.getRemoteResource(context);
  }

  @Implementation
  public static synchronized boolean showErrorDialogFragment(int errorCode, Activity activity,
      Fragment fragment, int requestCode, OnCancelListener cancelListener) {
    return googlePlayServicesUtilImpl.showErrorDialogFragment(
        errorCode, activity, fragment, requestCode, cancelListener);
  }

  @Implementation
  public static synchronized boolean showErrorDialogFragment(int errorCode, Activity activity,
      int requestCode) {
    return googlePlayServicesUtilImpl.showErrorDialogFragment(
        errorCode, activity, requestCode);
  }

  @Implementation
  public static synchronized boolean showErrorDialogFragment(
      int errorCode, Activity activity, int requestCode, OnCancelListener cancelListener) {
    return googlePlayServicesUtilImpl.showErrorDialogFragment(
        errorCode, activity, requestCode, cancelListener);
  }

  @Implementation
  public static synchronized Dialog getErrorDialog(int errorCode, Activity activity,
      int requestCode) {
    return googlePlayServicesUtilImpl.getErrorDialog(errorCode, activity, requestCode);
  }

  @Implementation
  public static synchronized Dialog getErrorDialog(int errorCode, Activity activity,
      int requestCode, OnCancelListener cancelListener) {
    return googlePlayServicesUtilImpl.getErrorDialog(
        errorCode, activity, requestCode, cancelListener);
  }

  @Implementation
  public static synchronized PendingIntent getErrorPendingIntent(int errorCode, Context context,
      int requestCode) {
    return googlePlayServicesUtilImpl.getErrorPendingIntent(errorCode, context, requestCode);
  }

  @Implementation
  public static synchronized String getOpenSourceSoftwareLicenseInfo(Context context) {
    return googlePlayServicesUtilImpl.getOpenSourceSoftwareLicenseInfo(context);
  }

  @Implementation
  public static synchronized int isGooglePlayServicesAvailable(Context context) {
    return googlePlayServicesUtilImpl.isGooglePlayServicesAvailable(context);
  }

  @Implementation
  public static synchronized void showErrorNotification(int errorCode, Context context) {
    googlePlayServicesUtilImpl.showErrorNotification(errorCode, context);
  }

  /**
   * Class containing methods with same signatures of the static methods of
   * {@link GooglePlayServicesUtil}.
   */
  public static class GooglePlayServicesUtilImpl {
    public Dialog getErrorDialog(int errorCode, Activity activity, int requestCode) {
      return getErrorDialog(errorCode, activity, requestCode, null);
    }

    public Dialog getErrorDialog(int errorCode, Activity activity, int requestCode,
        OnCancelListener cancelListener) {
      if (errorCode == ConnectionResult.SUCCESS) {
        return null;
      }
      return new Dialog(RuntimeEnvironment.application);
    }

    public PendingIntent getErrorPendingIntent(int errorCode, Context context,
        int requestCode) {
      if (errorCode == ConnectionResult.SUCCESS) {
        return null;
      }
      return PendingIntent.getActivity(
          context, requestCode, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public String getOpenSourceSoftwareLicenseInfo(Context context) {
      return "license";
    }

    public Context getRemoteContext(Context context) {
      return RuntimeEnvironment.application;
    }

    public Resources getRemoteResource(Context context) {
      return RuntimeEnvironment.application.getResources();
    }

    public int isGooglePlayServicesAvailable(Context context) {
      return ConnectionResult.SERVICE_MISSING;
    }

    public boolean showErrorDialogFragment(int errorCode, Activity activity,
        Fragment fragment, int requestCode, OnCancelListener cancelListener) {
      return false;
    }

    public boolean showErrorDialogFragment(int errorCode, Activity activity, int requestCode) {
      return false;
    }

    public boolean showErrorDialogFragment(int errorCode, Activity activity, int requestCode,
        OnCancelListener cancelListener) {
      return false;
    }

    public void showErrorNotification(int errorCode, Context context) {}
  }
}