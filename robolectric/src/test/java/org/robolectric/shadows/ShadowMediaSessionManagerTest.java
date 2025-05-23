package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;

/** Tests for {@link ShadowMediaSessionManager} */
@RunWith(AndroidJUnit4.class)
public class ShadowMediaSessionManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private MediaSessionManager mediaSessionManager;
  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void setUp() throws Exception {
    mediaSessionManager =
        (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
  }

  @Test
  public void getActiveSessions_returnsAddedControllers() {
    MediaSession mediaSession = new MediaSession(context, "tag");
    MediaController mediaController = new MediaController(context, mediaSession.getSessionToken());
    Shadows.shadowOf(mediaSessionManager).addController(mediaController);
    List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(null);
    assertThat(mediaControllers).containsExactly(mediaController);
  }

  @Test
  public void getActiveSessions_callsActiveSessionListeners() {
    MediaSession mediaSession = new MediaSession(context, "tag");
    MediaController mediaController = new MediaController(context, mediaSession.getSessionToken());
    final List<MediaController> changedMediaControllers = new ArrayList<>();
    Shadows.shadowOf(mediaSessionManager)
        .addOnActiveSessionsChangedListener(changedMediaControllers::addAll, null);
    Shadows.shadowOf(mediaSessionManager).addController(mediaController);
    assertThat(changedMediaControllers).containsExactly(mediaController);
  }

  @Test
  public void getActiveSessions_callsActiveSessionListenersWithProvidedHandler() {
    MediaSession mediaSession = new MediaSession(context, "tag");
    MediaController mediaController = new MediaController(context, mediaSession.getSessionToken());
    final List<MediaController> changedMediaControllers = new ArrayList<>();
    Shadows.shadowOf(mediaSessionManager)
        .addOnActiveSessionsChangedListener(changedMediaControllers::addAll, null, null);
    Shadows.shadowOf(mediaSessionManager).addController(mediaController);
    assertThat(changedMediaControllers).containsExactly(mediaController);
  }

  @Test
  @Config(minSdk = O)
  public void mediaSessionManager_activityContextEnabled_differentInstancesRetrieveSessions() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      MediaSessionManager applicationMediaSessionManager =
          RuntimeEnvironment.getApplication().getSystemService(MediaSessionManager.class);
      Activity activity = controller.get();
      MediaSessionManager activityMediaSessionManager =
          activity.getSystemService(MediaSessionManager.class);

      assertThat(applicationMediaSessionManager).isNotSameInstanceAs(activityMediaSessionManager);

      List<MediaController> applicationControllers =
          applicationMediaSessionManager.getActiveSessions(null);
      List<MediaController> activityControllers =
          activityMediaSessionManager.getActiveSessions(null);

      assertThat(activityControllers).isEqualTo(applicationControllers);
    }
  }
}
