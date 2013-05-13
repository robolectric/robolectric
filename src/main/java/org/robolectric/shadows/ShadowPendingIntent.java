package org.robolectric.shadows;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.TestIntentSender;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@code PendingIntent} that creates and sends {@code Intent}s appropriately.
 */
@Implements(PendingIntent.class)
public class ShadowPendingIntent {
  private Intent savedIntent;
  private Context savedContext;
  private boolean isActivityIntent;
  private boolean isBroadcastIntent;
  private boolean isServiceIntent;
  private int requestCode;

  @Implementation
  public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
    return create(context, intent, true, false, false, requestCode);
  }

  @Implementation
  public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
    return create(context, intent, false, true, false, requestCode);
  }

  @Implementation
  public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
    return create(context, intent, false, false, true, requestCode);
  }

  @Implementation
  public void send() throws CanceledException {
    send(savedContext, 0, savedIntent);
  }

  @Implementation
  public void send(Context context, int code, Intent intent) throws CanceledException {
    savedIntent.fillIn(intent, 0 );
    if (isActivityIntent) {
      context.startActivity(savedIntent);
    } else if (isBroadcastIntent) {
      context.sendBroadcast(savedIntent);
    } else if (isServiceIntent) {
      context.startService(savedIntent);
    }
  }

  @Implementation
  public IntentSender getIntentSender() {
    TestIntentSender testIntentSender = new TestIntentSender();
    testIntentSender.intent = savedIntent;
    return testIntentSender;
  }

  public boolean isActivityIntent() {
    return isActivityIntent;
  }

  public boolean isBroadcastIntent() {
    return isBroadcastIntent;
  }

  public boolean isServiceIntent() {
    return isServiceIntent;
  }

  public Context getSavedContext() {
    return savedContext;
  }

  public Intent getSavedIntent() {
    return savedIntent;
  }

  public int getRequestCode() {
    return requestCode;
  }

  // no idea if these are right....
  @Override
  @Implementation
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ShadowPendingIntent that = (ShadowPendingIntent) o;

    if (savedContext != null) {
      String packageName = savedContext.getPackageName();
      String thatPackageName = that.savedContext.getPackageName();
      if (packageName != null ? !packageName.equals(thatPackageName) : thatPackageName != null) return false;
    } else {
      if (that.savedContext != null) return false;
    }
    if (savedIntent != null ? !savedIntent.equals(that.savedIntent) : that.savedIntent != null) return false;

    return true;
  }

  @Override
  @Implementation
  public int hashCode() {
    int result = savedIntent != null ? savedIntent.hashCode() : 0;
    if (savedContext != null) {
      String packageName = savedContext.getPackageName();
      result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    }
    return result;
  }

  private static PendingIntent create(Context context, Intent intent, boolean isActivity, boolean isBroadcast, boolean isService, int requestCode) {
    PendingIntent pendingIntent = Robolectric.newInstanceOf(PendingIntent.class);
    ShadowPendingIntent shadowPendingIntent = (ShadowPendingIntent) Robolectric.shadowOf(pendingIntent);
    shadowPendingIntent.savedIntent = intent;
    shadowPendingIntent.isActivityIntent = isActivity;
    shadowPendingIntent.isBroadcastIntent = isBroadcast;
    shadowPendingIntent.isServiceIntent = isService;
    shadowPendingIntent.savedContext = context;
    shadowPendingIntent.requestCode = requestCode;
    return pendingIntent;
  }
}
