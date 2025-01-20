package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test Espresso on Robolectric interoperability for menus. */
@RunWith(AndroidJUnit4.class)
public class EspressoWithMenuTest {

  @Test
  public void platformMenuClick() {
    try (ActivityScenario<ActivityWithPlatformMenu> scenario =
        ActivityScenario.launch(ActivityWithPlatformMenu.class)) {
      openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
      onView(withText("menu_title")).perform(click());

      scenario.onActivity(activity -> assertThat(activity.menuClicked).isTrue());
    }
  }

  @Test
  public void appCompatMenuClick() {
    try (ActivityScenario<ActivityWithAppCompatMenu> scenario =
        ActivityScenario.launch(ActivityWithAppCompatMenu.class)) {
      openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
      onView(withText("menu_title")).perform(click());

      scenario.onActivity(activity -> assertThat(activity.menuClicked).isTrue());
    }
  }
}
