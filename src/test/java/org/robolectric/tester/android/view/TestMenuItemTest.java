package org.robolectric.tester.android.view;

import org.junit.Test;
import android.view.MenuItem;
import static org.fest.assertions.api.Assertions.assertThat;

public class TestMenuItemTest {
  private MenuItem item = new TestMenuItem();

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
}