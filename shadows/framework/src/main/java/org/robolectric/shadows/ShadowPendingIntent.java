package org.robolectric.shadows;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;

import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnMarshaledListener;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.fakes.RoboIntentSender;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(PendingIntent.class)
@SuppressLint("NewApi")
public class ShadowPendingIntent {

  private enum Type {
    ACTIVITY,
    BROADCAST,
    SERVICE,
    FOREGROUND_SERVICE
  }

  private static final int NULL_PENDING_INTENT_VALUE = -1;

  @GuardedBy("lock")
  private static final List<PendingIntent> createdIntents = new ArrayList<>();

  private static final Object lock = new Object();

  private static final List<PendingIntent> parceledPendingIntents = new ArrayList<>();

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
  protected static void __staticInitializer__() {
    Shadow.directInitialize(PendingIntent.class);
    ReflectionHelpers.setStaticField(PendingIntent.class, "CREATOR", ShadowPendingIntent.CREATOR);
  }

  @Implementation
  protected static PendingIntent getActivity(
      Context context, int requestCode, @NonNull Intent intent, int flags) {
    return create(context, new Intent[] {intent}, Type.ACTIVITY, requestCode, flags);
  }

  @Implementation
  protected static PendingIntent getActivity(
      Context context, int requestCode, @NonNull Intent intent, int flags, Bundle options) {
    return create(context, new Intent[] {intent}, Type.ACTIVITY, requestCode, flags);
  }

  @Implementation
  protected static PendingIntent getActivities(
      Context context, int requestCode, @NonNull Intent[] intents, int flags) {
    return create(context, intents, Type.ACTIVITY, requestCode, flags);
  }

  @Implementation
  protected static PendingIntent getActivities(
      Context context, int requestCode, @NonNull Intent[] intents, int flags, Bundle options) {
    return create(context, intents, Type.ACTIVITY, requestCode, flags);
  }

  @Implementation
  protected static PendingIntent getBroadcast(
      Context context, int requestCode, @NonNull Intent intent, int flags) {
    return create(context, new Intent[] {intent}, Type.BROADCAST, requestCode, flags);
  }

  @Implementation
  protected static PendingIntent getService(
      Context context, int requestCode, @NonNull Intent intent, int flags) {
    return create(context, new Intent[] {intent}, Type.SERVICE, requestCode, flags);
  }

  @Implementation(minSdk = O)
  protected static PendingIntent getForegroundService(
      Context context, int requestCode, @NonNull Intent intent, int flags) {
    return create(context, new Intent[] {intent}, Type.FOREGROUND_SERVICE, requestCode, flags);
  }

  @Implementation
  @SuppressWarnings("ReferenceEquality")
  protected void cancel() {
    synchronized (lock) {
      for (Iterator<PendingIntent> i = createdIntents.iterator(); i.hasNext(); ) {
        PendingIntent pendingIntent = i.next();
        if (pendingIntent == realPendingIntent) {
          canceled = true;
          i.remove();
          break;
        }
      }
    }
  }

  @Implementation
  protected void send() throws CanceledException {
    send(savedContext, 0, null);
  }

  @Implementation
  protected void send(int code) throws CanceledException {
    send(savedContext, code, null);
  }

  @Implementation
  protected void send(int code, PendingIntent.OnFinished onFinished, Handler handler)
      throws CanceledException {
    send(savedContext, code, null, onFinished, handler);
  }

  @Implementation
  protected void send(Context context, int code, Intent intent) throws CanceledException {
    send(context, code, intent, null, null);
  }

  @Implementation
  protected void send(Context context, int code, Intent intent, PendingIntent.OnFinished onFinished,
      Handler handler) throws CanceledException {
    send(context, code, intent, onFinished, handler, null);
  }

  @Implementation
  protected void send(Context context, int code, Intent intent, PendingIntent.OnFinished onFinished,
      Handler handler, String requiredPermission) throws CanceledException {
    // Manually propagating to keep only one implementation regardless of SDK
    send(context, code, intent, onFinished, handler, requiredPermission, null);
  }

  @Implementation(minSdk = M)
  protected void send(Context context, int code, Intent intent, PendingIntent.OnFinished onFinished,
      Handler handler, String requiredPermission, Bundle options) throws CanceledException {
    if (canceled) {
      throw new CanceledException();
    }

    // Fill in the last Intent, if it is mutable, with information now available at send-time.
    Intent[] intentsToSend;
    if (intent != null && isMutable(flags)) {
      // Copy the last intent before filling it in to avoid modifying this PendingIntent.
      intentsToSend = Arrays.copyOf(savedIntents, savedIntents.length);
      Intent lastIntentCopy = new Intent(intentsToSend[intentsToSend.length - 1]);
      lastIntentCopy.fillIn(intent, 0);
      intentsToSend[intentsToSend.length - 1] = lastIntentCopy;
    } else {
      intentsToSend = savedIntents;
    }

    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation = Shadow.extract(activityThread.getInstrumentation());
    if (isActivityIntent()) {
      for (Intent intentToSend : intentsToSend) {
        shadowInstrumentation.execStartActivity(
            context,
            (IBinder) null,
            (IBinder) null,
            (Activity) null,
            intentToSend,
            0,
            (Bundle) null);
      }
    } else if (isBroadcastIntent()) {
      for (Intent intentToSend : intentsToSend) {
        shadowInstrumentation.sendBroadcastWithPermission(
            intentToSend, requiredPermission, context, options, code);
      }
    } else if (isServiceIntent()) {
      for (Intent intentToSend : intentsToSend) {
        context.startService(intentToSend);
      }
    } else if (isForegroundServiceIntent()) {
      for (Intent intentToSend : intentsToSend) {
        context.startForegroundService(intentToSend);
      }
    }

    if (isOneShot(flags)) {
      cancel();
    }
  }

  @Implementation
  protected IntentSender getIntentSender() {
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

  /** @return {@code true} iff sending this PendingIntent will start a foreground service */
  public boolean isForegroundServiceIntent() {
    return type == Type.FOREGROUND_SERVICE;
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
   *
   * <ul>
   *   <li>{@link #getActivity(Context, int, Intent, int)}
   *   <li>{@link #getActivity(Context, int, Intent, int, Bundle)}
   *   <li>{@link #getBroadcast(Context, int, Intent, int)}
   *   <li>{@link #getService(Context, int, Intent, int)}
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
  protected String getTargetPackage() {
    return getCreatorPackage();
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected String getCreatorPackage() {
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
    ShadowPendingIntent that = Shadow.extract((PendingIntent) o);

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

  @Implementation
  @Nullable
  public static PendingIntent readPendingIntentOrNullFromParcel(@NonNull Parcel in) {
    int intentIndex = in.readInt();
    if (intentIndex == NULL_PENDING_INTENT_VALUE) {
      return null;
    }
    return parceledPendingIntents.get(intentIndex);
  }

  @Implementation
  public static void writePendingIntentOrNullToParcel(
      @Nullable PendingIntent sender, @NonNull Parcel out) {
    if (sender == null) {
      out.writeInt(NULL_PENDING_INTENT_VALUE);
      return;
    }

    int index = parceledPendingIntents.size();
    parceledPendingIntents.add(sender);
    out.writeInt(index);

    if (RuntimeEnvironment.getApiLevel() >= N) {
      ThreadLocal<OnMarshaledListener> sOnMarshaledListener =
          ReflectionHelpers.getStaticField(PendingIntent.class, "sOnMarshaledListener");
      OnMarshaledListener listener = sOnMarshaledListener.get();
      if (listener != null) {
        listener.onMarshaled(sender, out, 0);
      }
    }
  }

  static final Creator<PendingIntent> CREATOR =
      new Creator<PendingIntent>() {
        @Override
        public PendingIntent createFromParcel(Parcel in) {
          return readPendingIntentOrNullFromParcel(in);
        }

        @Override
        public PendingIntent[] newArray(int size) {
          return new PendingIntent[size];
        }
      };

  @Implementation
  protected void writeToParcel(Parcel out, int flags) {
    writePendingIntentOrNullToParcel(realPendingIntent, out);
  }

  private static PendingIntent create(
      Context context, Intent[] intents, Type type, int requestCode, int flags) {
    synchronized (lock) {
      Objects.requireNonNull(intents, "intents may not be null");

      // Search for a matching PendingIntent.
      PendingIntent pendingIntent = getCreatedIntentFor(type, intents, requestCode, flags);
      if ((flags & FLAG_NO_CREATE) != 0) {
        return pendingIntent;
      }

      // If requested, update the existing PendingIntent if one exists.
      if (pendingIntent != null && (flags & FLAG_UPDATE_CURRENT) != 0) {
        ShadowPendingIntent shadowPendingIntent = Shadow.extract(pendingIntent);
        Intent intent = shadowPendingIntent.getSavedIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
          extras.clear();
        }
        intent.putExtras(intents[intents.length - 1]);
        return pendingIntent;
      }

      // If requested, cancel the existing PendingIntent if one exists.
      if (pendingIntent != null && (flags & FLAG_CANCEL_CURRENT) != 0) {
        ShadowPendingIntent shadowPendingIntent = Shadow.extract(pendingIntent);
        shadowPendingIntent.cancel();
        pendingIntent = null;
      }

      // Build the PendingIntent if it does not exist.
      if (pendingIntent == null) {
        pendingIntent = ReflectionHelpers.callConstructor(PendingIntent.class);
        ShadowPendingIntent shadowPendingIntent = Shadow.extract(pendingIntent);
        shadowPendingIntent.savedIntents = intents;
        shadowPendingIntent.type = type;
        shadowPendingIntent.savedContext = context;
        shadowPendingIntent.requestCode = requestCode;
        shadowPendingIntent.flags = flags;

        createdIntents.add(pendingIntent);
      }

      return pendingIntent;
    }
  }

  private static PendingIntent getCreatedIntentFor(Type type, Intent[] intents, int requestCode,
      int flags) {
    synchronized (lock) {
      for (PendingIntent createdIntent : createdIntents) {
        ShadowPendingIntent shadowPendingIntent = Shadow.extract(createdIntent);

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
  }

  private static boolean isOneShot(int flags) {
    return (flags & FLAG_ONE_SHOT) != 0;
  }

  private static boolean isMutable(int flags) {
    return (flags & FLAG_IMMUTABLE) == 0;
  }

  @Resetter
  public static void reset() {
    synchronized (lock) {
      createdIntents.clear();
    }
  }
}
