package org.robolectric.integration_tests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.annotation.TextLayoutMode.Mode;

/**
 * Test Espresso on Robolectric interoperability for menus.
 */
@RunWith(AndroidJUnit4.class)
@TextLayoutMode(Mode.REALISTIC)
@LooperMode(PAUSED)
public class EspressoWithMenuTest {

  @Rule
  public ActivityScenarioRule<ActivityWithMenu> rule =
      new ActivityScenarioRule<>(ActivityWithMenu.class);

  @Test
  public void menuClick() throws InterruptedException {
    openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
    onView(withText("menu_title")).perform(click());

    rule.getScenario().onActivity(activity -> assertThat(activity.menuClicked).isTrue());
  }
}
