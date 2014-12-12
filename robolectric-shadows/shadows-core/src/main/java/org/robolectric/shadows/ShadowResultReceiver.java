package org.robolectric.shadows;

import android.os.Bundle;
import android.os.ResultReceiver;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.*;

@Implements(ResultReceiver.class)
public class ShadowResultReceiver {
  // TODO: Use handler to make asynchronous

  @RealObject private ResultReceiver realResultReceiver;

  @Implementation
  public void send(int resultCode, android.os.Bundle resultData) {
    ReflectionHelpers.callInstanceMethodReflectively(realResultReceiver, "onReceiveResult", from(resultCode),
        from(Bundle.class, resultData));
  }
}
