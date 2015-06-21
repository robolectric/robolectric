package org.robolectric.shadows.support.v4;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.support.v4.ShadowDrawerLayout;
import org.robolectric.util.TestRunnerWithManifest;

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

  private ShadowDrawerLayout shadowOf(DrawerLayout drawerLayout) {
    return (ShadowDrawerLayout) ShadowExtractor.extract(drawerLayout);
  }
}
