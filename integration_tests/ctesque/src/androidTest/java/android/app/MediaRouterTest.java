package android.app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.media.MediaRouter;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

@RunWith(AndroidJUnit4.class)
public class MediaRouterTest {

  @Test
  public void mediaRouter_applicationInstance_isNotSameAsActivityInstance() {
    MediaRouter applicationMediaRouter =
        (MediaRouter) getApplicationContext().getSystemService(Context.MEDIA_ROUTER_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaRouter activityMediaRouter =
                (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);
            assertThat(applicationMediaRouter).isNotSameInstanceAs(activityMediaRouter);
          });
    }
  }

  @Test
  public void mediaRouter_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaRouter activityMediaRouter =
                (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);
            MediaRouter anotherActivityMediaRouter =
                (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);
            assertThat(anotherActivityMediaRouter).isSameInstanceAs(activityMediaRouter);
          });
    }
  }

  @Test
  public void mediaRouter_instance_hasConsistentDefaultRoute() {
    MediaRouter applicationMediaRouter =
        (MediaRouter) getApplicationContext().getSystemService(Context.MEDIA_ROUTER_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaRouter activityMediaRouter =
                (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);

            MediaRouter.RouteInfo applicationDefaultRoute =
                applicationMediaRouter.getDefaultRoute();
            MediaRouter.RouteInfo activityDefaultRoute = activityMediaRouter.getDefaultRoute();

            assertThat(activityDefaultRoute).isEqualTo(applicationDefaultRoute);
          });
    }
  }
}
