package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.robolectric.util.ReflectionHelpers.createDeepProxy;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ComponentName;
import android.content.Context;
import android.media.session.ISessionManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener;
import android.os.Handler;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link MediaSessionManager}. */
@Implements(MediaSessionManager.class)
public class ShadowMediaSessionManager {
  private static final List<MediaController> controllers = new CopyOnWriteArrayList<>();
  private static final Set<OnActiveSessionsChangedListener> listeners = new CopyOnWriteArraySet<>();
  private static final List<PackageListener> packageListeners = new CopyOnWriteArrayList<>();

  @RealObject MediaSessionManager realMediaSessionManager;

  @Implementation(minSdk = S)
  protected void __constructor__(Context context) {
    // the real constructor throws NPE when trying to load the service
    reflector(MediaSessionManagerReflector.class, realMediaSessionManager).setContext(context);
    reflector(MediaSessionManagerReflector.class, realMediaSessionManager)
        .setService(createDeepProxy(ISessionManager.class));
  }

  @Implementation
  protected List<MediaController> getActiveSessions(ComponentName ignoredNotificationListener) {
    return ImmutableList.copyOf(controllers);
  }

  @Implementation(minSdk = 37)
  protected List<MediaController> getActiveSessionsForPackage(
      String packageName, ComponentName notificationListener) {
    return controllers.stream()
        .filter(c -> c.getPackageName().equals(packageName))
        .collect(toImmutableList());
  }

  @Implementation
  protected void addOnActiveSessionsChangedListener(
      OnActiveSessionsChangedListener listener, ComponentName ignoredNotificationListener) {
    listeners.add(listener);
  }

  @Implementation
  protected void addOnActiveSessionsChangedListener(
      OnActiveSessionsChangedListener listener,
      ComponentName ignoredNotificationListener,
      Handler ignoreHandler) {
    listeners.add(listener);
  }

  @Implementation
  protected void removeOnActiveSessionsChangedListener(OnActiveSessionsChangedListener listener) {
    listeners.remove(listener);
  }

  @Implementation(minSdk = 37)
  protected void addOnActiveSessionsForPackageChangedListener(
      String packageName, Executor executor, OnActiveSessionsChangedListener listener) {
    packageListeners.add(new PackageListener(packageName, executor, listener));
  }

  @Implementation(minSdk = 37)
  protected void removeOnActiveSessionsForPackageChangedListener(
      OnActiveSessionsChangedListener listener) {
    packageListeners.removeIf(p -> p.listener.equals(listener));
  }

  /**
   * Adds a {@link MediaController} that will be returned when calling {@link
   * #getActiveSessions(ComponentName)}. This will trigger a callback on each {@link
   * OnActiveSessionsChangedListener} callback registered with this class.
   *
   * @param controller The controller to add.
   */
  public void addController(MediaController controller) {
    controllers.add(controller);
    for (OnActiveSessionsChangedListener listener : listeners) {
      listener.onActiveSessionsChanged(controllers);
    }
    for (PackageListener p : packageListeners) {
      ImmutableList<MediaController> packageControllers =
          controllers.stream()
              .filter(c -> c.getPackageName().equals(p.packageName))
              .collect(toImmutableList());
      p.executor.execute(() -> p.listener.onActiveSessionsChanged(packageControllers));
    }
  }

  /**
   * Clears all controllers such that {@link #getActiveSessions(ComponentName)} will return the
   * empty list. This will trigger a callback on each {@link OnActiveSessionsChangedListener}
   * callback registered with this class.
   */
  public void clearControllers() {
    controllers.clear();
    for (OnActiveSessionsChangedListener listener : listeners) {
      listener.onActiveSessionsChanged(controllers);
    }
    for (PackageListener p : packageListeners) {
      p.executor.execute(() -> p.listener.onActiveSessionsChanged(ImmutableList.of()));
    }
  }

  @Resetter
  public static void reset() {
    controllers.clear();
    listeners.clear();
    packageListeners.clear();
  }

  @ForType(MediaSessionManager.class)
  interface MediaSessionManagerReflector {

    @Accessor("mContext")
    void setContext(Context context);

    @Accessor("mService")
    void setService(ISessionManager service);
  }

  private static class PackageListener {
    final String packageName;
    final Executor executor;
    final OnActiveSessionsChangedListener listener;

    PackageListener(
        String packageName, Executor executor, OnActiveSessionsChangedListener listener) {
      this.packageName = packageName;
      this.executor = executor;
      this.listener = listener;
    }
  }
}
