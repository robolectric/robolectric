package org.robolectric.shadows;

import android.uwb.IUwbAdapter;
import android.uwb.RangingSession;
import android.uwb.SessionHandle;
import java.util.concurrent.Executor;

/** Class to build {@link RangingSession} */
public class RangingSessionBuilder {

  private Executor executor;
  private RangingSession.Callback callback;
  private IUwbAdapter adapter;
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
    this.adapter = adapter;
    return this;
  }

  public RangingSessionBuilder setSessionHandle(SessionHandle handle) {
    this.handle = handle;
    return this;
  }

  public RangingSession build() {
    return new RangingSession(executor, callback, adapter, handle);
  }
}
