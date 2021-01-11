package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * The shadow {@link Message} for {@link LooperMode.Mode.PAUSED}.
 *
 * <p>This class should not be referenced directly. Use {@link ShadowMessage} instead.
 */
@Implements(value = Message.class, isInAndroidSdk = false)
public class ShadowPausedMessage extends ShadowMessage {

  private @RealObject Message realObject;

  long getWhen() {
    return reflector(ReflectorMessage.class, realObject).getWhen();
  }

  Message internalGetNext() {
    return reflector(ReflectorMessage.class, realObject).getNext();
  }

  // TODO: reconsider this being exposed as a public method
  @Override
  @Implementation(minSdk = LOLLIPOP)
  public void recycleUnchecked() {
    if (Build.VERSION.SDK_INT >= LOLLIPOP) {
      directlyOn(realObject, Message.class, "recycleUnchecked");
    } else {
      directlyOn(realObject, Message.class).recycle();
    }
  }

  @Override
  public void setScheduledRunnable(Runnable r) {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode");
  }

  // we could support these methods, but intentionally do not for now as its unclear what the
  // use case is.

  @Override
  public Message getNext() {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode");
  }

  @Override
  public void setNext(Message next) {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode");
  }

  Handler getTarget() {
    return reflector(ReflectorMessage.class, realObject).getTarget();
  }

  /** Accessor interface for {@link Message}'s internals. */
  @ForType(Message.class)
  private interface ReflectorMessage {

    @Accessor("when")
    long getWhen();

    @Accessor("next")
    Message getNext();

    @Accessor("target")
    Handler getTarget();
  }
}
