package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Scheduler;
import org.robolectric.util.reflector.ForType;

/**
 * The shadow {@link Message} for {@link LooperMode.Mode.LEGACY}.
 *
 * <p>In {@link LooperMode.Mode.LEGACY}, each Message is associated with a Runnable posted to the
 * {@link Scheduler}.
 *
 * @see ShadowLooper, ShadowLegacyMessageQueue
 */
@Implements(value = Message.class, isInAndroidSdk = false)
public class ShadowLegacyMessage extends ShadowMessage {
  @RealObject
  private Message realMessage;
  private Runnable scheduledRunnable;

  private void unschedule() {
    Handler target = realMessage.getTarget();

    if (target != null && scheduledRunnable != null) {
      shadowOf(target.getLooper()).getScheduler().remove(scheduledRunnable);
      scheduledRunnable = null;
    }
  }

  /**
   * Hook to unscheduled the callback when the message is recycled.
   * Invokes {@link #unschedule()} and then calls through to the
   * package private method {@link Message#recycleUnchecked()}
   * on the real object.
   */
  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  public void recycleUnchecked() {
    if (getApiLevel() >= LOLLIPOP) {
      unschedule();
      reflector(MessageReflector.class, realMessage).recycleUnchecked();
    } else {
      // provide forward compatibility with SDK 21.
      recycle();
    }
  }

  /**
   * Hook to unscheduled the callback when the message is recycled. Invokes {@link #unschedule()}
   * and then calls through to {@link Message#recycle()} on the real object.
   */
  @Implementation(maxSdk = KITKAT_WATCH)
  protected void recycle() {
    unschedule();
    directlyOn(realMessage, Message.class, "recycle");
  }

  @Override
  public void setScheduledRunnable(Runnable r) {
    scheduledRunnable = r;
  }

  @Override
  public Message getNext() {
    return reflector(MessageReflector.class, realMessage).getNext();
  }

  @Override
  public void setNext(Message next) {
    reflector(MessageReflector.class, realMessage).setNext(next);
  }

  private static ShadowLooper shadowOf(Looper looper) {
    return Shadow.extract(looper);
  }

  /** Accessor interface for {@link Message}'s internals. */
  @ForType(Message.class)
  interface LegacyMessageReflector {

    void markInUse();

    void recycle();

    void recycleUnchecked();
  }
}
