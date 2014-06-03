package org.robolectric.shadows;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.TestIntentSender;
import android.os.Bundle;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Arrays;

/**
 * Shadow of {@code PendingIntent} that creates and sends {@code Intent}s appropriately.
 */
@Implements(PendingIntent.class)
public class ShadowPendingIntent {
  private Intent[] savedIntents;
  private Context savedContext;
  private boolean isActivityIntent;
  private boolean isBroadcastIntent;
  private boolean isServiceIntent;
  private int requestCode;
  private int flags;

  @Implementation
  public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
    return create(context, new Intent[] {intent}, true, false, false, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags) {
    return create(context, intents, true, false, false, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags, Bundle options) {
    return create(context, intents, true, false, false, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
    return create(context, new Intent[] {intent}, false, true, false, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
    return create(context, new Intent[] {intent}, false, false, true, requestCode, flags);
  }

  @Implementation
  public void send() throws CanceledException {
    send(savedContext, 0, null);
  }

  @Implementation
  public void send(Context context, int code, Intent intent) throws CanceledException {
    if (intent != null) {
      for (Intent savedIntent : savedIntents) {
        savedIntent.fillIn(intent, 0);
      }
    }

    if (isActivityIntent) {
      for (Intent savedIntent : savedIntents) {
        context.startActivity(savedIntent);
      }
    } else if (isBroadcastIntent) {
      for (Intent savedIntent : savedIntents) {
        context.sendBroadcast(savedIntent);
      }
    } else if (isServiceIntent) {
      for (Intent savedIntent : savedIntents) {
        context.startService(savedIntent);
      }
    }
  }

  @Implementation
  public IntentSender getIntentSender() {
    TestIntentSender testIntentSender = new TestIntentSender();
    testIntentSender.intent = savedIntents[0];
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
    return savedIntents[0];
  }

  public Intent[] getSavedIntents() {
    return savedIntents;
  }

  public int getRequestCode() {
    return requestCode;
  }

  public int getFlags() {
    return flags;
  }

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
    if (savedIntents != null) {
      if (!Arrays.equals(this.savedIntents, that.savedIntents)) return false;
    }
    return true;
  }

  @Override
  @Implementation
  public int hashCode() {
    int result = savedIntents != null ? Arrays.hashCode(savedIntents) : 0;
    if (savedContext != null) {
      String packageName = savedContext.getPackageName();
      result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    }
    return result;
  }

  private static PendingIntent create(Context context, Intent[] intents, boolean isActivity, boolean isBroadcast, boolean isService, int requestCode, int flags) {
    PendingIntent pendingIntent = Robolectric.newInstanceOf(PendingIntent.class);
    ShadowPendingIntent shadowPendingIntent = Robolectric.shadowOf(pendingIntent);
    shadowPendingIntent.savedIntents = intents;
    shadowPendingIntent.isActivityIntent = isActivity;
    shadowPendingIntent.isBroadcastIntent = isBroadcast;
    shadowPendingIntent.isServiceIntent = isService;
    shadowPendingIntent.savedContext = context;
    shadowPendingIntent.requestCode = requestCode;
    shadowPendingIntent.flags = flags;
    return pendingIntent;
  }
}
