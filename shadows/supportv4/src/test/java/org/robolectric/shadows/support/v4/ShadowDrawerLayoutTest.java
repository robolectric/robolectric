package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.TestRunnerWithManifest;

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
    return (ShadowDrawerLayout) Shadow.extract(drawerLayout);
  }
}
