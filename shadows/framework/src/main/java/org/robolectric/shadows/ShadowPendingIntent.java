package org.robolectric.shadows;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static org.robolectric.Shadows.shadowOf;

import android.annotation.NonNull;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.fakes.RoboIntentSender;
import org.robolectric.util.ReflectionHelpers;

@Implements(PendingIntent.class)
public class ShadowPendingIntent {

  private enum Type {ACTIVITY, BROADCAST, SERVICE}

  private static final List<PendingIntent> createdIntents = new ArrayList<>();

  @RealObject
  private PendingIntent realPendingIntent;

  @NonNull private Intent[] savedIntents;
  private Context savedContext;
  private Type type;
  private int requestCode;
  private int flags;
  private String creatorPackage;
  private boolean canceled;

  @Implementation
  public static PendingIntent getActivity(
      Context context, int requestCode, @NonNull Intent intent, int flags) {
    return create(context, new Intent[] {intent}, Type.ACTIVITY, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getActivity(
      Context context, int requestCode, @NonNull Intent intent, int flags, Bundle options) {
    return create(context, new Intent[] {intent}, Type.ACTIVITY, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getActivities(
      Context context, int requestCode, @NonNull Intent[] intents, int flags) {
    return create(context, intents, Type.ACTIVITY, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getActivities(
      Context context, int requestCode, @NonNull Intent[] intents, int flags, Bundle options) {
    return create(context, intents, Type.ACTIVITY, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getBroadcast(
      Context context, int requestCode, @NonNull Intent intent, int flags) {
    return create(context, new Intent[] {intent}, Type.BROADCAST, requestCode, flags);
  }

  @Implementation
  public static PendingIntent getService(
      Context context, int requestCode, @NonNull Intent intent, int flags) {
    return create(context, new Intent[] {intent}, Type.SERVICE, requestCode, flags);
  }

  @Implementation
  @SuppressWarnings("ReferenceEquality")
  public void cancel() {
    for (Iterator<PendingIntent> i = createdIntents.iterator(); i.hasNext(); ) {
      PendingIntent pendingIntent = i.next();
      if (pendingIntent == realPendingIntent) {
        canceled = true;
        i.remove();
        break;
      }
    }
  }

  @Implementation
  public void send() throws CanceledException {
    send(savedContext, 0, null);
  }

  @Implementation
  public void send(Context context, int code, Intent intent) throws CanceledException {
    if (canceled) {
      throw new CanceledException();
    }

    // Fill in the last Intent, if it is mutable, with information now available at send-time.
    if (intent != null && isMutable(flags)) {
      getSavedIntent().fillIn(intent, 0);
    }

    if (isActivityIntent()) {
      for (Intent savedIntent : savedIntents) {
        context.startActivity(savedIntent);
      }
    } else if (isBroadcastIntent()) {
      for (Intent savedIntent : savedIntents) {
        context.sendBroadcast(savedIntent);
      }
    } else if (isServiceIntent()) {
      for (Intent savedIntent : savedIntents) {
        context.startService(savedIntent);
      }
    }

    if (isOneShot(flags)) {
      cancel();
    }
  }

  @Implementation
  public IntentSender getIntentSender() {
    return new RoboIntentSender(realPendingIntent);
  }

  /**
   * @return {@code true} iff sending this PendingIntent will start an activity
   */
  public boolean isActivityIntent() {
    return type == Type.ACTIVITY;
  }

  /**
   * @return {@code true} iff sending this PendingIntent will broadcast an Intent
   */
  public boolean isBroadcastIntent() {
    return type == Type.BROADCAST;
  }

  /**
   * @return {@code true} iff sending this PendingIntent will start a service
   */
  public boolean isServiceIntent() {
    return type == Type.SERVICE;
  }

  /**
   * @return the context in which this PendingIntent was created
   */
  public Context getSavedContext() {
    return savedContext;
  }

  /**
   * This returns the last Intent in the Intent[] to be delivered when the PendingIntent is sent.
   * This method is particularly useful for PendingIntents created with a single Intent:
   * <ul>
   *   <li>{@link #getActivity(Context, int, Intent, int)}</li>
   *   <li>{@link #getActivity(Context, int, Intent, int, Bundle)}</li>
   *   <li>{@link #getBroadcast(Context, int, Intent, int)}</li>
   *   <li>{@link #getService(Context, int, Intent, int)}</li>
   * </ul>
   *
   * @return the final Intent to be delivered when the PendingIntent is sent
   */
  public Intent getSavedIntent() {
    return savedIntents[savedIntents.length - 1];
  }

  /**
   * This method is particularly useful for PendingIntents created with multiple Intents:
   * <ul>
   *   <li>{@link #getActivities(Context, int, Intent[], int)}</li>
   *   <li>{@link #getActivities(Context, int, Intent[], int, Bundle)}</li>
   * </ul>
   *
   * @return all Intents to be delivered when the PendingIntent is sent
   */
  public Intent[] getSavedIntents() {
    return savedIntents;
  }

  /**
   * @return {@true} iff this PendingIntent has been canceled
   */
  public boolean isCanceled() {
    return canceled;
  }

  /**
   * @return the request code with which this PendingIntent was created
   */
  public int getRequestCode() {
    return requestCode;
  }

  /**
   * @return the flags with which this PendingIntent was created
   */
  public int getFlags() {
    return flags;
  }

  @Implementation
  public String getTargetPackage() {
    return getCreatorPackage();
  }

  @Implementation
  public String getCreatorPackage() {
    return (creatorPackage == null)
        ? RuntimeEnvironment.application.getPackageName()
        : creatorPackage;
  }

  public void setCreatorPackage(String creatorPackage) {
    this.creatorPackage = creatorPackage;
  }

  @Override
  @Implementation
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || realPendingIntent.getClass() != o.getClass()) return false;
    ShadowPendingIntent that = shadowOf((PendingIntent) o);

    String packageName = savedContext == null ? null : savedContext.getPackageName();
    String thatPackageName = that.savedContext == null ? null : that.savedContext.getPackageName();
    if (!Objects.equals(packageName, thatPackageName)) {
      return false;
    }

    if (this.savedIntents.length != that.savedIntents.length) {
      return false;
    }

    for (int i = 0; i < this.savedIntents.length; i++) {
      if (!this.savedIntents[i].filterEquals(that.savedIntents[i])) {
        return false;
      }
    }

    if (this.requestCode != that.requestCode) {
      return false;
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
    result = 31 * result + requestCode;
    return result;
  }

  private static PendingIntent create(Context context, Intent[] intents, Type type, int requestCode,
      int flags) {
    Objects.requireNonNull(intents, "intents may not be null");

    // Search for a matching PendingIntent.
    PendingIntent pendingIntent = getCreatedIntentFor(type, intents, requestCode, flags);
    if ((flags & FLAG_NO_CREATE) != 0) {
      return pendingIntent;
    }

    // If requested, update the existing PendingIntent if one exists.
    if (pendingIntent != null && (flags & FLAG_UPDATE_CURRENT) != 0) {
      Intent intent = shadowOf(pendingIntent).getSavedIntent();
      Bundle extras = intent.getExtras();
      if (extras != null) {
        extras.clear();
      }
      intent.putExtras(intents[intents.length - 1]);
      return pendingIntent;
    }

    // If requested, cancel the existing PendingIntent if one exists.
    if (pendingIntent != null && (flags & FLAG_CANCEL_CURRENT) != 0) {
      ShadowPendingIntent toCancel = shadowOf(pendingIntent);
      toCancel.cancel();
      pendingIntent = null;
    }

    // Build the PendingIntent if it does not exist.
    if (pendingIntent == null) {
      pendingIntent = ReflectionHelpers.callConstructor(PendingIntent.class);
      ShadowPendingIntent shadowPendingIntent = shadowOf(pendingIntent);
      shadowPendingIntent.savedIntents = intents;
      shadowPendingIntent.type = type;
      shadowPendingIntent.savedContext = context;
      shadowPendingIntent.requestCode = requestCode;
      shadowPendingIntent.flags = flags;

      createdIntents.add(pendingIntent);
    }

    return pendingIntent;
  }

  private static PendingIntent getCreatedIntentFor(Type type, Intent[] intents, int requestCode,
      int flags) {
    for (PendingIntent createdIntent : createdIntents) {
      ShadowPendingIntent shadowPendingIntent = shadowOf(createdIntent);

      if (isOneShot(shadowPendingIntent.flags) != isOneShot(flags)) {
        continue;
      }

      if (isMutable(shadowPendingIntent.flags) != isMutable(flags)) {
        continue;
      }

      if (shadowPendingIntent.type != type) {
        continue;
      }

      if (shadowPendingIntent.requestCode != requestCode) {
        continue;
      }

      // The last Intent in the array acts as the "significant element" for matching as per
      // {@link #getActivities(Context, int, Intent[], int)}.
      Intent savedIntent = shadowPendingIntent.getSavedIntent();
      Intent targetIntent = intents[intents.length - 1];

      if (savedIntent == null ? targetIntent == null : savedIntent.filterEquals(targetIntent)) {
        return createdIntent;
      }
    }
    return null;
  }

  private static boolean isOneShot(int flags) {
    return (flags & FLAG_ONE_SHOT) != 0;
  }

  private static boolean isMutable(int flags) {
    return (flags & FLAG_IMMUTABLE) == 0;
  }

  @Resetter
  public static void reset() {
    createdIntents.clear();
  }
}
