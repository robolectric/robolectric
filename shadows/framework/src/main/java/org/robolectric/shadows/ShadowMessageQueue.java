package org.robolectric.shadows;

import android.os.Message;
import android.os.MessageQueue;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Scheduler;

/**
 * The shadow API for {@link MessageQueue}.
 *
 * <p>Different shadow implementations will be used depending on the current {@link LooperMode}. See
 * {@link ShadowLegacyMessageQueue} and {@link ShadowPausedMessageQueue} for details.
 */
@Implements(value = MessageQueue.class, shadowPicker = ShadowMessageQueue.Picker.class)
public abstract class ShadowMessageQueue {

  /** The shadow Picker for this class. */
  public static class Picker extends LooperShadowPicker<ShadowMessageQueue> {

    public Picker() {
      super(ShadowLegacyMessageQueue.class, ShadowPausedMessageQueue.class);
    }
  }

  /**
   * Return this queue's Scheduler.
   *
   * <p>Only supported in {@link LooperMode.Mode.LEGACY}.
   */
  public abstract Scheduler getScheduler();

  /**
   * Set this queue's Scheduler.
   *
   * <p>Only supported in {@link LooperMode.Mode.LEGACY}.
   */
  public abstract void setScheduler(Scheduler scheduler);

  /**
   * Retrieves the current Message at the top of the queue.
   *
   * <p>Only supported in {@link LooperMode.Mode.LEGACY}.
   */
  public abstract Message getHead();

  /**
   * Sets the current Message at the top of the queue.
   *
   * <p>Only supported in {@link LooperMode.Mode.LEGACY}.
   */
  public abstract void setHead(Message msg);

  /**
   * Reset the messageQueue state. Should not be called by tests - it intended for use by the
   * Robolectric framework.
   */
  public abstract void reset();
}
