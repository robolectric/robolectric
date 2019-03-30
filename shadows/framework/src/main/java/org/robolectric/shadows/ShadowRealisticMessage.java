package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.getStaticField;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.Message;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(
    value = Message.class,
    shadowPicker = ShadowBaseMessage.Picker.class,
    isInAndroidSdk = false)
public class ShadowRealisticMessage extends ShadowBaseMessage {

  private static final Object lock = getStaticField(Message.class, "sPoolSync");

  @RealObject Message realObject;

  /** Resets the static state of the {@link Message} class by emptying the message pool. */
  @Resetter
  public static void reset() {
    synchronized (lock) {
      reflector(ReflectorMessage.class).setPoolSize(0);
      reflector(ReflectorMessage.class).setPool(null);
    }
  }

  void recycleQuietly() {
    if (Build.VERSION.SDK_INT <= KITKAT) {
      directlyOn(realObject, Message.class).recycle();
    } else {
      reflector(ReflectorMessage.class, realObject).recycleUnchecked();
    }
  }

  long getWhen() {
    return reflector(ReflectorMessage.class, realObject).getWhen();
  }

  /** Accessor interface for {@link Message}'s internals. */
  @ForType(Message.class)
  private interface ReflectorMessage {

    @Accessor("when")
    long getWhen();

    @Static
    @Accessor("sPool")
    void setPool(Message o);

    @Static
    @Accessor("sPoolSize")
    void setPoolSize(int size);

    void recycleUnchecked();
  }
}
