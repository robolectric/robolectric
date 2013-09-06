package org.robolectric.shadows;

import android.app.Application;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.tester.android.view.TestMenuItem;
import org.robolectric.util.I18nException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class MenuInflaterTest {
  private Application context;

  @Before
  public void setUp() throws Exception {
    context = Robolectric.application;
  }

  @Test
  public void canRetrieveMenuListAndFindMenuItemById() {
    TestMenu menu = new TestMenu(context);
    new MenuInflater(context).inflate(R.menu.test, menu);

    TestMenuItem testMenuItem = (TestMenuItem) menu.getItem(0);
    assertEquals("Test menu item 1", testMenuItem.getTitle().toString());
    testMenuItem.click();

    testMenuItem = (TestMenuItem) menu.getItem(1);
    assertEquals("Test menu item 2", testMenuItem.getTitle().toString());
    testMenuItem.click();

    assertNotNull(menu.findItem(R.id.test_menu_1));
  }

  @Test
  public void shouldInflateComplexMenu() throws Exception {
    TestMenu testMenu = new TestMenu();
    new MenuInflater(context).inflate(R.menu.test_withchilds, testMenu);
    assertThat(testMenu.size()).isEqualTo(4);
  }

  @Test
  public void shouldParseSubItemCorrectly() throws Exception {
    TestMenu testMenu = new TestMenu();
    new MenuInflater(context).inflate(R.menu.test_withchilds, testMenu);
    MenuItem mi = testMenu.findItem(R.id.test_submenu_1);
    assertTrue(mi.hasSubMenu());
    assertThat(mi.getSubMenu().size()).isEqualTo(2);
    assertThat(mi.getSubMenu().getItem(1).getTitle() + "").isEqualTo("Test menu item 3");
  }

  @Test(expected=I18nException.class)
  public void shouldThrowExceptionOnI18nStrictModeInflateMenu() throws Exception {
    shadowOf(context).setStrictI18n(true);
    new MenuInflater(context).inflate(R.menu.test, new TestMenu());
  }

  @Test
  public void shouldCreateActionViews() throws Exception {
    TestMenu testMenu = new TestMenu();
    new MenuInflater(context).inflate(R.menu.action_menu, testMenu);

    MenuItem item = testMenu.getItem(0);
    assertEquals(item.getActionView().getClass(), SearchView.class);
  }
}