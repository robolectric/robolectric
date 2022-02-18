package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.uwb.IUwbAdapter;
import android.uwb.RangingSession;
import android.uwb.SessionHandle;
import java.util.concurrent.Executor;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

/** Class to build {@link RangingSession} */
public class RangingSessionBuilder {

  private Executor executor;
  private RangingSession.Callback callback;
  private IUwbAdapter adapter1; // apiLevel <= 32
  private /*IUwbAdapter2*/ Object adapter2; // apiLevel >= 33
  private SessionHandle handle;

  private RangingSessionBuilder() {}

  public static RangingSessionBuilder newBuilder() {
    return new RangingSessionBuilder();
  }

  public RangingSessionBuilder setExecutor(Executor executor) {
    this.executor = executor;
    return this;
  }

  public RangingSessionBuilder setCallback(RangingSession.Callback callback) {
    this.callback = callback;
    return this;
  }

  public RangingSessionBuilder setIUwbAdapter(IUwbAdapter adapter) {
    this.adapter1 = adapter;
    return this;
  }

  public RangingSessionBuilder setIUwbAdapter2(/*IUwbAdapter2*/ Object adapter) {
    this.adapter2 = adapter;
    return this;
  }

  public RangingSessionBuilder setSessionHandle(SessionHandle handle) {
    this.handle = handle;
    return this;
  }

  public RangingSession build() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel >= S && apiLevel <= 32) {
      return new RangingSession(executor, callback, adapter1, handle);
    } else if (apiLevel >= 33) {
      try {
        return ReflectionHelpers.callConstructor(
            RangingSession.class,
            from(Executor.class, executor),
            from(RangingSession.Callback.class, callback),
            from(Class.forName("android.uwb.IUwbAdapter2"), adapter2),
            from(SessionHandle.class, handle));
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Unable to create RangingSession due to", e);
      }
    }
    throw new IllegalStateException("RangingSession hidden constructor not found");
  }
}
