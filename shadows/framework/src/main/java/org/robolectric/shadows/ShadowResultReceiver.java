package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Bundle;
import android.os.ResultReceiver;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(ResultReceiver.class)
public class ShadowResultReceiver {
  @RealObject private ResultReceiver realResultReceiver;

  /**
   * Calls {@code onReceiveResult} directly on a receiver created in this process, ignoring its
   * handler. A receiver read from a {@link android.os.Parcel} forwards the result to the original
   * receiver, as on a device.
   */
  @Implementation
  protected void send(int resultCode, android.os.Bundle resultData) {
    ResultReceiverReflector receiver = reflector(ResultReceiverReflector.class, realResultReceiver);
    if (!receiver.getLocal()) {
      receiver.send(resultCode, resultData);
      return;
    }
    ReflectionHelpers.callInstanceMethod(
        realResultReceiver,
        "onReceiveResult",
        ClassParameter.from(Integer.TYPE, resultCode),
        ClassParameter.from(Bundle.class, resultData));
  }

  @ForType(ResultReceiver.class)
  interface ResultReceiverReflector {
    @Accessor("mLocal")
    boolean getLocal();

    @Direct
    void send(int resultCode, Bundle resultData);
  }
}
