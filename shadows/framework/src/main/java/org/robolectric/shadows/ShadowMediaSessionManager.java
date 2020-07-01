package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.ComponentName;
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

/** Shadow for {@link MediaSessionManager}. */
@Implements(value = MediaSessionManager.class, minSdk = LOLLIPOP)
public class ShadowMediaSessionManager {
  private final List<MediaController> controllers = new CopyOnWriteArrayList<>();
  private final Set<OnActiveSessionsChangedListener> listeners = new CopyOnWriteArraySet<>();

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
}
