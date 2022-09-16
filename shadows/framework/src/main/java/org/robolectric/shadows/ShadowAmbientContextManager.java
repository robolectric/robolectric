package org.robolectric.shadows;

import android.app.PendingIntent;
import android.app.ambientcontext.AmbientContextEventRequest;
import android.app.ambientcontext.AmbientContextManager;
import android.os.Build.VERSION_CODES;
import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link AmbientContextManager} */
@Implements(
    value = AmbientContextManager.class,
    minSdk = VERSION_CODES.TIRAMISU,
    isInAndroidSdk = false)
public class ShadowAmbientContextManager {

  private final Object lock = new Object();

  /**
   * Caches the last {@link AmbientContextEventRequest} passed into {@link
   * #registerObserver(AmbientContextEventRequest, PendingIntent, Executor, Consumer)}
   *
   * <p>If {@link #unregisterObserver()} was called after {@link
   * #registerObserver(AmbientContextEventRequest, PendingIntent, Executor, Consumer)}, this is set
   * to null.
   */
  @GuardedBy("lock")
  @Nullable
  private AmbientContextEventRequest lastRegisterObserverRequest;

  /**
   * The ambient context service status code that will be consumed by the {@code consumer} which is
   * passed in {@link #queryAmbientContextServiceStatus(Set, Executor, Consumer)} or {@link
   * #registerObserver(AmbientContextEventRequest, PendingIntent, Executor, Consumer)}.
   */
  @GuardedBy("lock")
  private Integer ambientContextServiceStatus = AmbientContextManager.STATUS_NOT_SUPPORTED;

  /** Caches the last requested event codes passed into {@link #startConsentActivity(Set)}. */
  @GuardedBy("lock")
  @Nullable
  private Set<Integer> lastRequestedEventCodesForConsentActivity;

  @Implementation
  protected void registerObserver(
      AmbientContextEventRequest request,
      PendingIntent resultPendingIntent,
      Executor executor,
      Consumer<Integer> statusConsumer) {
    synchronized (lock) {
      lastRegisterObserverRequest = request;
      statusConsumer.accept(ambientContextServiceStatus);
    }
  }

  @Implementation
  protected void unregisterObserver() {
    synchronized (lock) {
      lastRegisterObserverRequest = null;
    }
  }

  /**
   * Returns the last {@link AmbientContextEventRequest} passed into {@link
   * AmbientContextManager#registerObserver(AmbientContextEventRequest, PendingIntent, Executor,
   * Consumer)}.
   *
   * <p>Returns null if {@link AmbientContextManager#unregisterObserver()} is invoked or there is no
   * invocation of {@link AmbientContextManager#registerObserver(AmbientContextEventRequest,
   * PendingIntent, Executor, Consumer)}.
   */
  @Nullable
  public AmbientContextEventRequest getLastRegisterObserverRequest() {
    synchronized (lock) {
      return lastRegisterObserverRequest;
    }
  }

  @Implementation
  protected void queryAmbientContextServiceStatus(
      Set<Integer> eventTypes, Executor executor, Consumer<Integer> consumer) {
    synchronized (lock) {
      consumer.accept(ambientContextServiceStatus);
    }
  }

  /**
   * Sets a {@code status} that will be consumed by the {@code consumer} which is passed in {@link
   * #queryAmbientContextServiceStatus(Set, Executor, Consumer)} or {@link
   * #registerObserver(AmbientContextEventRequest, PendingIntent, Executor, Consumer)}.
   */
  public void setAmbientContextServiceStatus(Integer status) {
    synchronized (lock) {
      ambientContextServiceStatus = status;
    }
  }

  @Implementation
  protected void startConsentActivity(Set<Integer> eventTypes) {
    synchronized (lock) {
      lastRequestedEventCodesForConsentActivity = eventTypes;
    }
  }

  /**
   * Returns the last requested event codes that were passed into {@link
   * #startConsentActivity(Set)}.
   *
   * <p>If {@link #startConsentActivity(Set)} is never invoked, returns {@code null}.
   */
  @Nullable
  public Set<Integer> getLastRequestedEventCodesForConsentActivity() {
    synchronized (lock) {
      return lastRequestedEventCodesForConsentActivity;
    }
  }
}
