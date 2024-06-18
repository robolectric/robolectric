package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Handler;
import android.os.Message;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;

/**
 * The shadow {@link Message} for {@link LooperMode.Mode.PAUSED}.
 *
 * <p>This class should not be referenced directly. Use {@link ShadowMessage} instead.
 */
@Implements(value = Message.class, isInAndroidSdk = false)
public class ShadowPausedMessage extends ShadowMessage {

  @RealObject private Message realMessage;

  @Implementation
  protected long getWhen() {
    return reflector(MessageReflector.class, realMessage).getWhen();
  }

  Message internalGetNext() {
    return reflector(MessageReflector.class, realMessage).getNext();
  }

  // TODO: Reconsider this being exposed as a public method
  @Override
  @Implementation
  public void recycleUnchecked() {
    reflector(MessageReflector.class, realMessage).recycleUnchecked();
  }

  @Override
  public void setScheduledRunnable(Runnable r) {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode");
  }

  // We could support these methods, but intentionally do not for now as its unclear what the
  // use case is.

  @Override
  public Message getNext() {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode");
  }

  @Override
  public void setNext(Message next) {
    throw new UnsupportedOperationException("Not supported in PAUSED LooperMode");
  }

  @Implementation
  protected Handler getTarget() {
    return reflector(MessageReflector.class, realMessage).getTarget();
  }
}
