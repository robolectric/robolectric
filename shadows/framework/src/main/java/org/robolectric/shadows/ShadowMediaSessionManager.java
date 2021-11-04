package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.ReflectionHelpers.createDeepProxy;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ComponentName;
import android.content.Context;
import android.media.session.ISessionManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link MediaSessionManager}. */
@Implements(value = MediaSessionManager.class, minSdk = LOLLIPOP)
public class ShadowMediaSessionManager {
  private final List<MediaController> controllers = new CopyOnWriteArrayList<>();
  private final Set<OnActiveSessionsChangedListener> listeners = new CopyOnWriteArraySet<>();
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

  @Implementation
  protected void addOnActiveSessionsChangedListener(
      OnActiveSessionsChangedListener listener, ComponentName ignoredNotificationListener) {
    listeners.add(listener);
  }

  @Implementation
  protected void removeOnActiveSessionsChangedListener(OnActiveSessionsChangedListener listener) {
    listeners.remove(listener);
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
  }

  /**
   * Clears all controllers such that {@link #getActiveSessions(ComponentName)} will return the
   * empty list.
   */
  public void clearControllers() {
    controllers.clear();
  }

  @ForType(MediaSessionManager.class)
  interface MediaSessionManagerReflector {

    @Accessor("mContext")
    void setContext(Context context);

    @Accessor("mService")
    void setService(ISessionManager service);
  }
}
