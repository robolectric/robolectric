package org.robolectric.android.compat;

import static android.os.Build.VERSION_CODES.O;

import android.app.Instrumentation;
import android.os.Build;
import android.os.Looper;

// TODO: move into androidx ?
public class InstrumentationCompat {

  public static TestLooperManagerCompat acquireLooperManager(
      Instrumentation instrumentation, Looper looper) {
    if (Build.VERSION.SDK_INT >= O) {
      return new TestLooperManagerCompatO(instrumentation.acquireLooperManager(looper));
    } else {
      // TODO: add a checkInstrumenting call
      // return new TestLooperManagerCompatPreO(looper);
      return new TestLooperManagerCompatPreO(looper);
    }
  }
}
