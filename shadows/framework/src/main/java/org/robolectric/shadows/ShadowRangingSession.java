package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import android.uwb.RangingSession;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

/** Adds Robolectric support for UWB ranging. */
@Implements(value = RangingSession.class, minSdk = VERSION_CODES.S, isInAndroidSdk = false)
public class ShadowRangingSession {
  /**
   * Adapter interface for state change events, provided by the tester to dictate ranging results.
   */
  public interface Adapter {
    void onOpen(RangingSession session, RangingSession.Callback callback, PersistableBundle params);

    void onStart(
        RangingSession session, RangingSession.Callback callback, PersistableBundle params);

    void onReconfigure(
        RangingSession session, RangingSession.Callback callback, PersistableBundle params);

    void onStop(RangingSession session, RangingSession.Callback callback);

    void onClose(RangingSession session, RangingSession.Callback callback);
  }

  static RangingSession newInstance(
      Executor executor, RangingSession.Callback callback, Adapter adapter) {
    RangingSession rangingSession =
        RangingSessionBuilder.newBuilder().setExecutor(executor).setCallback(callback).build();

    ShadowRangingSession shadow = Shadow.extract(rangingSession);
    shadow.setCallback(callback, executor);
    shadow.setAdapter(adapter);

    return rangingSession;
  }

  @RealObject private RangingSession realRangingSession;

  private RangingSession.Callback callback;
  private Executor executor;
  private Adapter adapter;

  /**
   * Forwards parameters and the session's callback to the Shadow's adapter, allowing the tester to
   * dictate the results of the call.
   */
  @Implementation
  protected void start(PersistableBundle params) {
    executor.execute(() -> adapter.onStart(realRangingSession, callback, params));
  }

  /**
   * Forwards parameters and the session's callback to the Shadow's adapter, allowing the tester to
   * dictate the results of the call.
   */
  @Implementation
  protected void reconfigure(PersistableBundle params) {
    executor.execute(() -> adapter.onReconfigure(realRangingSession, callback, params));
  }

  /**
   * Forwards parameters and the session's callback to the Shadow's adapter, allowing the tester to
   * dictate the results of the call.
   */
  @Implementation
  protected void stop() {
    executor.execute(() -> adapter.onStop(realRangingSession, callback));
  }

  /**
   * Forwards parameters and the session's callback to the Shadow's adapter, allowing the tester to
   * dictate the results of the call.
   */
  @Implementation
  protected void close() {
    executor.execute(() -> adapter.onClose(realRangingSession, callback));
  }

  /**
   * Forwards parameters and the session's callback to the Shadow's adapter, allowing the tester to
   * dictate the results of the call.
   */
  void open(PersistableBundle params) {
    executor.execute(() -> adapter.onOpen(realRangingSession, callback, params));
  }

  private void setCallback(RangingSession.Callback callback, Executor executor) {
    this.callback = callback;
    this.executor = executor;
  }

  private void setAdapter(Adapter adapter) {
    this.adapter = adapter;
  }
}
