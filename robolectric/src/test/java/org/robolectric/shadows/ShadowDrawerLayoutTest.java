package org.robolectric.shadows;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.RobolectricBase.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowDrawerLayoutTest {
  @Test
  public void canGetAndSetDrawerListener() throws Exception {
    DrawerLayout drawerLayout = new DrawerLayout(buildActivity(Activity.class).create().get());
    DrawerLayout.DrawerListener mockDrawerListener = mock(DrawerLayout.DrawerListener.class);
    drawerLayout.setDrawerListener(mockDrawerListener);
    assertThat(shadowOf(drawerLayout).getDrawerListener()).isSameAs(mockDrawerListener);
  }
}
