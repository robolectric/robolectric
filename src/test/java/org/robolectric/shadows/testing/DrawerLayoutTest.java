package org.robolectric.shadows.testing;

import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for DrawerLayout shadow class.
 */
@RunWith(TestRunners.WithDefaults.class)
public class DrawerLayoutTest {
  private DrawerLayout drawerLayout;
  private View drawerView;

  /**
   * Sets up our DrawerLayout instance and adds a content view frame layout as well as a
   * list view positioned at {@code Gravity.LEFT}
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    drawerLayout = new DrawerLayout(Robolectric.application);

    final FrameLayout contentView = new FrameLayout(Robolectric.application);
    contentView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
    drawerLayout.addView(contentView);

    drawerView = new ListView(Robolectric.application);
    final LinearLayout.LayoutParams listLP = new LinearLayout.LayoutParams(
        240,
        ViewGroup.LayoutParams.MATCH_PARENT);
    listLP.gravity = Gravity.START;
    drawerView.setLayoutParams(listLP);

    drawerLayout.addView(drawerView);

    //Start all the drawers in a closed state.
    drawerLayout.closeDrawers();
  }

  @Test
  public void getLayoutParams_shouldReturnTheSameDrawerLayoutParamsFromTheSetter() throws Exception {
    DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(1, 2);
    drawerLayout.setLayoutParams(params);
    assertThat(drawerLayout.getLayoutParams()).isSameAs(params);
  }

  @Test
  public void shouldHaveContentView() throws Exception {
    final int childCount = drawerLayout.getChildCount();
    assertThat(childCount).isGreaterThan(0);

    final View allegedContentView = drawerLayout.getChildAt(0);
    assertThat(allegedContentView).isNotNull();
    assertThat(allegedContentView).isInstanceOf(FrameLayout.class);
  }

  @Test
  public void shouldHaveDrawerView() throws Exception {
    final int childCount = drawerLayout.getChildCount();
    assertThat(childCount).isGreaterThan(1);

    final View allegedMenuView = drawerLayout.getChildAt(1);
    assertThat(allegedMenuView).isNotNull();
    assertThat(allegedMenuView).isInstanceOf(ListView.class);
  }

  @Test
  public void canOpenAndCloseDrawer() throws Exception {
    assertThat(drawerLayout.isDrawerOpen(drawerView)).isFalse();
    drawerLayout.openDrawer(drawerView);
    assertThat(drawerLayout.isDrawerOpen(drawerView)).isTrue();
    drawerLayout.closeDrawer(drawerView);
    assertThat(drawerLayout.isDrawerOpen(drawerView)).isFalse();
  }

}
