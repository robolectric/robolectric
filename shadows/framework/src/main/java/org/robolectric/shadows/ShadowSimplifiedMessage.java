package org.robolectric.shadows;

import static org.robolectric.util.ReflectionHelpers.getStaticField;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Message;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(
    value = Message.class,
    shadowPicker = ShadowBaseMessage.Picker.class,
    isInAndroidSdk = false)
public class ShadowSimplifiedMessage extends ShadowBaseMessage {

  private static final Object lock = getStaticField(Message.class, "sPoolSync");
  
  /**
   * Resets the static state of the {@link Message} class by
   * emptying the message pool.
   */
  @Resetter
  public static void reset() {
    synchronized (lock) {
      reflector(_Message_.class).setPoolSize(0);
      reflector(_Message_.class).setPool(null);
    }
  }

  /** Accessor interface for {@link Message}'s internals. */
  @ForType(Message.class)
  interface _Message_ {

    @Accessor("when")
    long getWhen();

    @Static @Accessor("sPool")
    void setPool(Message o);

    @Static @Accessor("sPoolSize")
    void setPoolSize(int size);
  }
}
