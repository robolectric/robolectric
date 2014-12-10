package org.robolectric.shadows;

import android.app.Application;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class MenuInflaterTest {
  private Application context;

  @Before
  public void setUp() throws Exception {
    context = RuntimeEnvironment.application;
  }

  @Test
  public void canRetrieveMenuListAndFindMenuItemById() {
    RoboMenu menu = new RoboMenu(context);
    new MenuInflater(context).inflate(R.menu.test, menu);

    RoboMenuItem item = (RoboMenuItem) menu.getItem(0);
    assertEquals("Test menu item 1", item.getTitle().toString());
    item.click();

    item = (RoboMenuItem) menu.getItem(1);
    assertEquals("Test menu item 2", item.getTitle().toString());
    item.click();

    assertNotNull(menu.findItem(R.id.test_menu_1));
  }

  @Test
  public void shouldInflateComplexMenu() throws Exception {
    RoboMenu menu = new RoboMenu();
    new MenuInflater(context).inflate(R.menu.test_withchilds, menu);
    assertThat(menu.size()).isEqualTo(4);
  }

  @Test
  public void shouldParseSubItemCorrectly() throws Exception {
    RoboMenu menu = new RoboMenu();
    new MenuInflater(context).inflate(R.menu.test_withchilds, menu);
    MenuItem mi = menu.findItem(R.id.test_submenu_1);
    assertTrue(mi.hasSubMenu());
    assertThat(mi.getSubMenu().size()).isEqualTo(2);
    assertThat(mi.getSubMenu().getItem(1).getTitle() + "").isEqualTo("Test menu item 3");
  }

  @Test
  public void shouldCreateActionViews() throws Exception {
    RoboMenu menu = new RoboMenu();
    new MenuInflater(context).inflate(R.menu.action_menu, menu);

    MenuItem item = menu.getItem(0);
    assertEquals(item.getActionView().getClass(), SearchView.class);
  }

  @Test
  public void shouldOrderItemsInCategory() {
    RoboMenu menu = new RoboMenu();
    new MenuInflater(context).inflate(R.menu.test_withorder, menu);

    RoboMenuItem item = (RoboMenuItem) menu.getItem(0);
    assertEquals("Test menu item 1", item.getTitle().toString());

    item = (RoboMenuItem) menu.getItem(1);
    assertEquals("Test menu item 2", item.getTitle().toString());
  }
}