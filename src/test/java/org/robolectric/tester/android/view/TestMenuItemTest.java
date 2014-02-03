package org.robolectric.tester.android.view;

import android.view.View;
import org.junit.Before;
import org.junit.Test;
import android.view.MenuItem;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class TestMenuItemTest {
  private MenuItem item;
  private TestOnActionExpandListener listener;

  @Before
  public void setUp() throws Exception {
    item = new TestMenuItem();
    listener =  new TestOnActionExpandListener();
    item.setOnActionExpandListener(listener);
  }

  @Test
  public void shouldCheckTheMenuItem() throws Exception {
    assertThat(item.isChecked()).isFalse();
    item.setChecked(true);
    assertThat(item.isChecked()).isTrue();
  }

  @Test
  public void shouldAllowSettingCheckable() throws Exception {
    assertThat(item.isCheckable()).isFalse();
    item.setCheckable(true);
    assertThat(item.isCheckable()).isTrue();
  }

  @Test
  public void shouldAllowSettingVisible() throws Exception {
    assertThat(item.isVisible()).isTrue();
    item.setVisible(false);
    assertThat(item.isVisible()).isFalse();
  }

  @Test
  public void expandActionView_shouldReturnFalseIfActionViewIsNull() throws Exception {
    item.setActionView(null);
    assertThat(item.expandActionView()).isFalse();
  }

  @Test
  public void expandActionView_shouldSetExpandedTrue() throws Exception {
    item.setActionView(new View(Robolectric.application));
    assertThat(item.expandActionView()).isTrue();
    assertThat(item.isActionViewExpanded()).isTrue();
  }

  @Test
  public void expandActionView_shouldInvokeListener() throws Exception {
    item.setActionView(new View(Robolectric.application));
    item.expandActionView();
    assertThat(listener.expanded).isTrue();
  }

  @Test
  public void collapseActionView_shouldReturnFalseIfActionViewIsNull() throws Exception {
    item.setActionView(null);
    assertThat(item.collapseActionView()).isFalse();
  }

  @Test
  public void collapseActionView_shouldSetExpandedFalse() throws Exception {
    item.setActionView(new View(Robolectric.application));
    item.expandActionView();
    assertThat(item.collapseActionView()).isTrue();
    assertThat(item.isActionViewExpanded()).isFalse();
  }

  @Test
  public void collapseActionView_shouldInvokeListener() throws Exception {
    item.setActionView(new View(Robolectric.application));
    listener.expanded = true;
    item.collapseActionView();
    assertThat(listener.expanded).isFalse();
  }

  @Test
  public void setOnActionExpandListener_shouldReturnMenuItem() throws Exception {
    assertThat(item.setOnActionExpandListener(listener)).isSameAs(item);
  }

  class TestOnActionExpandListener implements MenuItem.OnActionExpandListener {
    private boolean expanded = false;

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
      expanded = true;
      return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
      expanded = false;
      return true;
    }
  }
}
