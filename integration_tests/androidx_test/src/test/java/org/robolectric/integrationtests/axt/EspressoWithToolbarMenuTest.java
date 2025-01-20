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
import org.robolectric.shadows.ShadowViewConfiguration;

/** Test Espresso on Robolectric interoperability for toolbar menus. */
@RunWith(AndroidJUnit4.class)
public class EspressoWithToolbarMenuTest {
  @Test
  public void appCompatToolbarMenuClick() {
    ShadowViewConfiguration.setHasPermanentMenuKey(false);
    try (ActivityScenario<AppCompatActivityWithToolbarMenu> scenario =
        ActivityScenario.launch(AppCompatActivityWithToolbarMenu.class)) {
      openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
      onView(withText("menu_title")).perform(click());

      scenario.onActivity(activity -> assertThat(activity.menuClicked).isTrue());
    }
  }
}
