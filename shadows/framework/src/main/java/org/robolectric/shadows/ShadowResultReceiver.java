package org.robolectric.shadows;

import android.os.Bundle;
import android.os.ResultReceiver;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(ResultReceiver.class)
public class ShadowResultReceiver {
  @RealObject private ResultReceiver realResultReceiver;

  @Implementation
  protected void send(int resultCode, android.os.Bundle resultData) {
    ReflectionHelpers.callInstanceMethod(realResultReceiver, "onReceiveResult",
        ClassParameter.from(Integer.TYPE, resultCode),
        ClassParameter.from(Bundle.class, resultData));
  }
}
