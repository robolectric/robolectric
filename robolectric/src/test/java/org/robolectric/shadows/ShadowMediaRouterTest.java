package org.robolectric.shadows;

import android.content.Context;
import android.media.MediaRouter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowMediaRouter;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ShadowMediaRouterTest {

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void canAddCallback() throws Exception {
    Context context = RuntimeEnvironment.application;
    MediaRouter router = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
    ShadowMediaRouter shadow = shadowOf(router);

    assertThat(shadow.getRegisteredCallbacks().size()).isEqualTo(0);
    final MediaRouter.SimpleCallback callback = new MediaRouter.SimpleCallback();

    router.addCallback(0, callback, 0);
    assertThat(shadow.getRegisteredCallbacks().size()).isEqualTo(1);
    assertThat(shadow.getRegisteredCallbacks().get(0)).isSameAs(callback);

    //removing something else
    router.removeCallback(new MediaRouter.SimpleCallback());
    assertThat(shadow.getRegisteredCallbacks().size()).isEqualTo(1);
    assertThat(shadow.getRegisteredCallbacks().get(0)).isSameAs(callback);

    router.removeCallback(callback);
    assertThat(shadow.getRegisteredCallbacks().size()).isEqualTo(0);
  }
}
