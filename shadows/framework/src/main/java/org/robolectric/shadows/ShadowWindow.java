package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.FrameMetrics;
import android.view.Window;
import android.view.Window.OnFrameMetricsAvailableListener;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Window.class)
public class ShadowWindow {
  @RealObject private Window realWindow;

  protected CharSequence title = "";
  protected Drawable backgroundDrawable;
  private int flags;
  private int softInputMode;
  private final Set<OnFrameMetricsAvailableListener> onFrameMetricsAvailableListeners =
      new HashSet<>();

  public static Window create(Context context) throws ClassNotFoundException {
    String className = getApiLevel() >= M
        ? "com.android.internal.policy.PhoneWindow"
        : "com.android.internal.policy.impl.PhoneWindow";
    Class<? extends Window> phoneWindowClass =
        (Class<? extends Window>) Window.class.getClassLoader().loadClass(className);
    return ReflectionHelpers.callConstructor(phoneWindowClass, ClassParameter.from(Context.class, context));
  }

  @Implementation
  protected void setFlags(int flags, int mask) {
    this.flags = (this.flags & ~mask) | (flags & mask);
    reflector(WindowReflector.class, realWindow).setFlags(flags, mask);
  }

  @Implementation
  protected void setSoftInputMode(int softInputMode) {
    this.softInputMode = softInputMode;
    reflector(WindowReflector.class, realWindow).setSoftInputMode(softInputMode);
  }

  public boolean getFlag(int flag) {
    return (flags & flag) == flag;
  }

  public CharSequence getTitle() {
    return title;
  }

  public int getSoftInputMode() {
    return softInputMode;
  }

  public Drawable getBackgroundDrawable() {
    return backgroundDrawable;
  }

  @Implementation(minSdk = N)
  protected void addOnFrameMetricsAvailableListener(
      Window.OnFrameMetricsAvailableListener listener, Handler handler) {
    onFrameMetricsAvailableListeners.add(listener);
  }

  @Implementation(minSdk = N)
  protected void removeOnFrameMetricsAvailableListener(
      Window.OnFrameMetricsAvailableListener listener) {
    if (!onFrameMetricsAvailableListeners.remove(listener)) {
      // Matches current behavior of android.
      throw new IllegalArgumentException(
          "attempt to remove OnFrameMetricsAvailableListener that was never added");
    }
  }

  /**
   * Calls {@link Window.OnFrameMetrisAvailableListener#onFrameMetricsAvailable()} on each current
   * listener with 0 as the dropCountSinceLastInvocation.
   */
  public void reportOnFrameMetricsAvailable(FrameMetrics frameMetrics) {
    reportOnFrameMetricsAvailable(frameMetrics, /* dropCountSinceLastInvocation= */ 0);
  }

  /**
   * Calls {@link Window.OnFrameMetrisAvailableListener#onFrameMetricsAvailable()} on each current
   * listener.
   *
   * @param frameMetrics the {@link FrameMetrics} instance passed to the listeners.
   * @param dropCountSinceLastInvocation the dropCountSinceLastInvocation passed to the listeners.
   */
  public void reportOnFrameMetricsAvailable(
      FrameMetrics frameMetrics, int dropCountSinceLastInvocation) {
    for (OnFrameMetricsAvailableListener listener : onFrameMetricsAvailableListeners) {
      listener.onFrameMetricsAvailable(realWindow, frameMetrics, dropCountSinceLastInvocation);
    }
  }

  @ForType(Window.class)
  interface WindowReflector {

    @Direct
    void setFlags(int flags, int mask);

    @Direct
    void setSoftInputMode(int softInputMode);
  }
}
