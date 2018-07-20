package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;

import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/** Tests for {@link org.robolectric.shadows.ShadowMediaBrowserCompat}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowMediaBrowserCompatTest {

  private static final String ROOT_ID = "root_id";

  private Context context = RuntimeEnvironment.application;
  private ComponentName componentName;
  private MediaBrowserCompat mediaBrowser;

  @Before
  public void setUp() {
    componentName = new ComponentName("a", "b");
    mediaBrowser =
        new MediaBrowserCompat(
            context, componentName, new MediaBrowserCompat.ConnectionCallback(), null);
  }

  @Test
  public void mediaBrowserConnection_isConnected() {
    mediaBrowser.connect();

    assertThat(mediaBrowser.isConnected()).isTrue();
  }

  @Test
  public void mediaBrowserConnection_isDisconnected() {
    mediaBrowser.disconnect();

    assertThat(mediaBrowser.isConnected()).isFalse();
  }

  @Test
  public void mediaBrowser_getRootId() {
    String rootId = mediaBrowser.getRoot();

    assertThat(rootId).isEqualTo(ROOT_ID);
  }
}
