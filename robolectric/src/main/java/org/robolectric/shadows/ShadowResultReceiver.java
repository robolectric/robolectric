package org.robolectric.shadows;

import android.os.Bundle;
import android.os.ResultReceiver;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(ResultReceiver.class)
public class ShadowResultReceiver {
  // TODO: Use handler to make asynchronous

  @RealObject private ResultReceiver realResultReceiver;

  @Implementation
  public void send(int resultCode, android.os.Bundle resultData) {
    ReflectionHelpers.callInstanceMethodReflectively(realResultReceiver, "onReceiveResult", new ReflectionHelpers.ClassParameter(Integer.TYPE, resultCode),
        new ReflectionHelpers.ClassParameter(Bundle.class, resultData));
  }
}
