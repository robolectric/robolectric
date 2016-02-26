package org.robolectric.shadows.support.v4;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.support.v4.ShadowDrawerLayout;
import org.robolectric.util.TestRunnerWithManifest;

import static junit.framework.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(TestRunnerWithManifest.class)
public class ShadowDrawerLayoutTest {

  @Test
  public void canGetAndSetDrawerListener() throws Exception {
    DrawerLayout drawerLayout = new DrawerLayout(Robolectric.buildActivity(Activity.class).create().get());
    DrawerLayout.DrawerListener mockDrawerListener = mock(DrawerLayout.DrawerListener.class);
    drawerLayout.setDrawerListener(mockDrawerListener);
    assertThat(shadowOf(drawerLayout).getDrawerListener()).isSameAs(mockDrawerListener);
  }

  @Test
  public void canAddAndRemoveDrawerListener() throws Exception {
    DrawerLayout drawerLayout = new DrawerLayout(Robolectric.buildActivity(Activity.class).create().get());
    DrawerLayout.DrawerListener mockDrawerListener1 = mock(DrawerLayout.DrawerListener.class);
    DrawerLayout.DrawerListener mockDrawerListener2 = mock(DrawerLayout.DrawerListener.class);
    drawerLayout.addDrawerListener(mockDrawerListener1);
    drawerLayout.addDrawerListener(mockDrawerListener2);
    assertThat(shadowOf(drawerLayout).getDrawerListeners())
            .containsExactly(mockDrawerListener1, mockDrawerListener2);
    assertThat(shadowOf(drawerLayout).getDrawerListener()).isSameAs(mockDrawerListener1);

    drawerLayout.removeDrawerListener(mockDrawerListener1);
    assertThat(shadowOf(drawerLayout).getDrawerListeners())
            .containsExactly(mockDrawerListener2);
    assertThat(shadowOf(drawerLayout).getDrawerListener()).isSameAs(mockDrawerListener2);

    drawerLayout.removeDrawerListener(mockDrawerListener2);
    assertThat(shadowOf(drawerLayout).getDrawerListeners()).isEmpty();
    assertNull(shadowOf(drawerLayout).getDrawerListener());
  }

  private ShadowDrawerLayout shadowOf(DrawerLayout drawerLayout) {
    return (ShadowDrawerLayout) ShadowExtractor.extract(drawerLayout);
  }
}
