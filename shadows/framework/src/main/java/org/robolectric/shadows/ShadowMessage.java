package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Handler;
import android.os.Message;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * The shadow API for {@link android.os.Message}.
 *
 * <p>Different shadow implementations will be used depending on the current {@link LooperMode}. See
 * {@link ShadowLegacyMessage} and {@link ShadowPausedMessage} for details.
 */
@Implements(value = Message.class, shadowPicker = ShadowMessage.Picker.class)
public abstract class ShadowMessage {

  /** The shadow Picker for this class */
  public static class Picker extends LooperShadowPicker<ShadowMessage> {

    public Picker() {
      super(ShadowLegacyMessage.class, ShadowPausedMessage.class);
    }
  }

  /** Exposes the package-private {@link Message#recycleUnchecked()} */
  public abstract void recycleUnchecked();

  /**
   * Stores the {@link Runnable} instance that has been scheduled to invoke this message. This is
   * called when the message is enqueued by {@link ShadowLegacyMessageQueue#enqueueMessage} and is
   * used when the message is recycled to ensure that the correct {@link Runnable} instance is
   * removed from the associated scheduler.
   *
   * @param r the {@link Runnable} instance that is scheduled to trigger this message.
   *     <p>#if ($api >= 21) * @see #recycleUnchecked() #else * @see #recycle() #end
   *     <p>Only supported in {@link LooperMode.Mode.LEGACY}.
   */
  public abstract void setScheduledRunnable(Runnable r);

  /**
   * Convenience method to provide getter access to the private field {@code Message.next}.
   *
   * <p>Only supported in {@link LooperMode.Mode.LEGACY}
   *
   * @return The next message in the current message chain.
   * @see #setNext(Message)
   */
  public abstract Message getNext();

  /**
   * Convenience method to provide setter access to the private field {@code Message.next}.
   *
   * <p>Only supported in {@link LooperMode.Mode.LEGACY}
   *
   * @param next the new next message for the current message.
   * @see #getNext()
   */
  public abstract void setNext(Message next);

  /**
   * Resets the static state of the {@link Message} class by
   * emptying the message pool.
   */
  @Resetter
  public static void reset() {
    Object lock = reflector(MessageReflector.class).getPoolSync();
    synchronized (lock) {
      reflector(MessageReflector.class).setPoolSize(0);
      reflector(MessageReflector.class).setPool(null);
    }
  }

  /** Accessor interface for {@link Message}'s internals. */
  @ForType(Message.class)
  interface MessageReflector {

    @Direct
    void recycle();

    @Direct
    void recycleUnchecked();

    @Static
    @Accessor("sPool")
    void setPool(Message o);

    @Static
    @Accessor("sPoolSize")
    void setPoolSize(int size);

    @Static
    @Accessor("sPoolSync")
    Object getPoolSync();

    @Accessor("when")
    long getWhen();

    @Accessor("next")
    Message getNext();

    @Accessor("next")
    void setNext(Message next);

    @Accessor("target")
    Handler getTarget();
  }
}
