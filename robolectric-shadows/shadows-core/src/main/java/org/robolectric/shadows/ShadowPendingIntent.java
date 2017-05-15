package org.robolectric.shadows;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.fakes.RoboIntentSender;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.robolectric.Shadows.shadowOf;

@Implements(PendingIntent.class)
public class ShadowPendingIntent {

  private static final int TYPE_ACTIVITY =  1;
  private static final int TYPE_SERVICE = 2;
  private static final int TYPE_BROADCAST = 3;

  private static final List<PendingIntent> createdIntents = new ArrayList<>();

  @RealObject
  PendingIntent realPendingIntent;

  private int intentType;
  private Intent[] savedIntents;
  private Context savedContext;
  private int requestCode;
  private int flags;
  private String creatorPackage;
  private volatile boolean canceled;
  private Bundle savedOptions;

  @Implementation
  public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
    return create(TYPE_ACTIVITY, context, requestCode, new Intent[] {intent}, flags, null);
  }

  @Implementation
  public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags, Bundle options) {
    return create(TYPE_ACTIVITY, context, requestCode, new Intent[] {intent}, flags, options);
  }

  @Implementation
  public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags) {
    return create(TYPE_ACTIVITY, context, requestCode, intents, flags, null);
  }

  @Implementation
  public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags, Bundle options) {
    return create(TYPE_ACTIVITY, context, requestCode, intents, flags, options);
  }

  @Implementation
  public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
    return create(TYPE_BROADCAST, context, requestCode, new Intent[] {intent}, flags, null);
  }

  @Implementation
  public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
    return create(TYPE_SERVICE, context, requestCode, new Intent[] {intent}, flags, null);
  }

  // this method is useless, but cannot be removed without breaking subclasses
  @Implementation
  public void send() throws CanceledException {
    send(null, 0, null, null, null, null, null);
  }

  // this method is useless, but cannot be removed without breaking subclasses
  @Implementation
  public void send(Context context, int code, Intent intent) throws CanceledException {
    send(context, code, intent, null, null, null, null);
  }

  @Implementation
  public void send(Context context, int resultCode, Intent intent, PendingIntent.OnFinished onFinished, Handler handler, String requiredPermission) throws CanceledException {
    // forward directly to the full implementation of send rather than relying on PendingIntent to
    // forward this; older versions of PendingIntent do not have the full overloads
    send(
        context,
        resultCode,
        intent,
        onFinished,
        handler,
        requiredPermission,
        null);
  }

  @Implementation(minSdk = 23)
  public synchronized void send(Context context, int resultCode, Intent intent, final PendingIntent.OnFinished onFinished, final Handler handler, String requiredPermission, Bundle options)
      throws CanceledException {
    if (canceled) {
      throw new CanceledException();
    }

    Intent[] sendIntents = copyIntents(savedIntents);
    if ((flags & PendingIntent.FLAG_IMMUTABLE) == 0 && intent != null) {
      for (int i = 0; i < savedIntents.length; i++) {
        sendIntents[i].fillIn(intent, 0);
      }
    }

    if (isActivityIntent()) {
      Bundle sendOptions = (savedOptions != null ? new Bundle(savedOptions) : null);
      if (options != null) {
        if (sendOptions == null) {
          sendOptions = new Bundle();
        }
        sendOptions.putAll(options);
      }

      for (Intent sendIntent : sendIntents) {
        savedContext.startActivity(sendIntent, sendOptions);
      }
    } else if (isBroadcastIntent()) {
      BroadcastReceiver finalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (onFinished == null) {
            return;
          }

          OnFinishedRunnable onFinishedRunnable = new OnFinishedRunnable(onFinished, intent, getResultCode(), getResultData(), getResultExtras(false));
          if (handler != null) {
            handler.post(onFinishedRunnable);
          } else {
            onFinishedRunnable.run();
          }
        }
      };

      // sendBroadcast doesn't allow passing through as many arguments as we'd like
      savedContext.sendOrderedBroadcast(sendIntents[0], requiredPermission, finalBroadcastReceiver, null, resultCode, null, null);
    } else if (isServiceIntent()) {
      savedContext.startService(sendIntents[0]);
    } else {
      throw new IllegalStateException();
    }

    if ((flags & PendingIntent.FLAG_ONE_SHOT) != 0) {
      cancel();
    }
  }

  @Implementation
  public IntentSender getIntentSender() {
    return new RoboIntentSender(realPendingIntent);
  }

  @Implementation
  public synchronized void cancel() {
    synchronized (createdIntents) {
      canceled = true;
      createdIntents.remove(realPendingIntent);
    }
  }

  public boolean isActivityIntent() {
    return intentType == TYPE_ACTIVITY;
  }

  public boolean isBroadcastIntent() {
    return intentType == TYPE_BROADCAST;
  }

  public boolean isServiceIntent() {
    return intentType == TYPE_SERVICE;
  }

  public Context getSavedContext() {
    return savedContext;
  }

  public synchronized Intent getSavedIntent() {
    return savedIntents[0];
  }

  public synchronized Intent[] getSavedIntents() {
    return savedIntents;
  }

  public Bundle getSavedOptions() {
    return savedOptions;
  }

  public int getRequestCode() {
    return requestCode;
  }

  public int getFlags() {
    return flags;
  }

  public synchronized boolean isCanceled() {
    return canceled;
  }

  @Implementation
  public String getTargetPackage() {
    return getCreatorPackage();
  }

  @Implementation(minSdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
  public String getCreatorPackage() {
    return (creatorPackage == null)
        ? savedContext.getPackageName()
        : creatorPackage;
  }

  public void setCreatorPackage(String creatorPackage) {
    this.creatorPackage = creatorPackage;
  }

  @Override
  @Implementation
  public boolean equals(Object o) {
    return realPendingIntent == o;
  }

  @Override
  @Implementation
  public int hashCode() {
    int result = savedIntents != null ? Arrays.hashCode(savedIntents) : 0;
    if (savedContext != null) {
      String packageName = savedContext.getPackageName();
      result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    }
    result = 31 * result + requestCode;
    return result;
  }

  @Override
  @Implementation
  public String toString() {
    return "PendingIntent{"
        + Integer.toHexString(System.identityHashCode(realPendingIntent))
        + ": "
        + Integer.toHexString(System.identityHashCode(this))
        + "}";
  }

  private static PendingIntent create(int intentType, Context context, int requestCode, Intent[] intents, int flags, Bundle options) {
    synchronized (createdIntents) {
      PendingIntent previousIntent = getCreatedIntentForLocked(intentType, requestCode, intents,  context.getPackageName());

      if ((flags & PendingIntent.FLAG_NO_CREATE) != 0) {
        return previousIntent;
      }

      if (previousIntent != null) {
        if ((flags & PendingIntent.FLAG_UPDATE_CURRENT) != 0) {
          ShadowPendingIntent previousIntentShadow = shadowOf(previousIntent);
          synchronized (previousIntentShadow) {
            for (int i = 0; i < previousIntentShadow.savedIntents.length; i++) {
              previousIntentShadow.savedIntents[i].replaceExtras(intents[i]);
            }
          }
        }

        if ((flags & PendingIntent.FLAG_CANCEL_CURRENT) != 0) {
          previousIntent.cancel();
        } else {
          return previousIntent;
        }
      }

      PendingIntent pendingIntent = ReflectionHelpers.callConstructor(PendingIntent.class);
      ShadowPendingIntent shadowPendingIntent = Shadows.shadowOf(pendingIntent);
      shadowPendingIntent.intentType = intentType;
      shadowPendingIntent.savedContext = context;
      shadowPendingIntent.requestCode = requestCode;
      shadowPendingIntent.savedIntents = copyIntents(intents);
      shadowPendingIntent.savedOptions = (options != null ? new Bundle(options) : null);
      shadowPendingIntent.flags = flags;
      shadowPendingIntent.canceled = false;

      createdIntents.add(pendingIntent);

      return pendingIntent;
    }
  }

  private static Intent[] copyIntents(Intent[] intents) {
    Intent[] intentsCopy = new Intent[intents.length];
    for (int i = 0; i < intents.length; i++) {
      intentsCopy[i] = intents[i] != null ? new Intent(intents[i]) : null;
    }
    return intentsCopy;
  }

  private static boolean compareIntents(Intent[] intentsThis, Intent[] intentsThat) {
    if (intentsThis == intentsThat) {
      return true;
    }
    if (intentsThis == null || intentsThat == null) {
      return false;
    }
    if (intentsThis.length != intentsThat.length) {
      return false;
    }
    // Order matters in the framework. If I call getActivities(Activity1, Activity2), that will
    // give me a different PendingIntent than if I call getActivities(Activity2, Activity1).
    for (int i = 0; i < intentsThis.length; i++) {
      if (intentsThis[i] == intentsThat[i]) {
        continue;
      }
      if (intentsThis[i] == null || !intentsThis[i].filterEquals(intentsThat[i])) {
        return false;
      }
    }

    return true;
  }

  private static PendingIntent getCreatedIntentForLocked(int intentType, int requestCode, Intent[] intents, String packageName) {
    for (PendingIntent createdIntent : createdIntents) {
      ShadowPendingIntent shadowPendingIntent = Shadows.shadowOf(createdIntent);
      synchronized (shadowPendingIntent) {
        if (shadowPendingIntent.intentType != intentType) {
          continue;
        }
        if (shadowPendingIntent.requestCode != requestCode) {
          continue;
        }
        if (!compareIntents(shadowPendingIntent.savedIntents, intents)) {
          continue;
        }
        if (!Objects.equals(shadowPendingIntent.getCreatorPackage(), packageName)) {
          continue;
        }
      }

      return createdIntent;
    }

    return null;
  }

  @Resetter
  public static void reset() {
    createdIntents.clear();
  }

  // not an anonymous class so that the broadcast reciever can be GCed before this runs
  private class OnFinishedRunnable implements Runnable {

    private final PendingIntent.OnFinished onFinished;
    private final Intent intent;
    private final int resultCode;
    private final String resultData;
    private final Bundle resultExtras;

    public OnFinishedRunnable(PendingIntent.OnFinished onFinished, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
      this.onFinished = onFinished;
      this.intent = intent;
      this.resultCode = resultCode;
      this.resultData = resultData;
      this.resultExtras = resultExtras;
    }

    @Override
    public void run() {
      onFinished.onSendFinished(realPendingIntent, intent, resultCode, resultData, resultExtras);
    }
  }
}
