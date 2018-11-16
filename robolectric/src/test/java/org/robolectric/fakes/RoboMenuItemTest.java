package org.robolectric.fakes;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class RoboMenuItemTest {
  private MenuItem item;
  private TestOnActionExpandListener listener;

  @Before
  public void setUp() throws Exception {
    item = new RoboMenuItem(ApplicationProvider.getApplicationContext());
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
    item.setActionView(new View(ApplicationProvider.getApplicationContext()));
    assertThat(item.expandActionView()).isTrue();
    assertThat(item.isActionViewExpanded()).isTrue();
  }

  @Test
  public void expandActionView_shouldInvokeListener() throws Exception {
    item.setActionView(new View(ApplicationProvider.getApplicationContext()));
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
    item.setActionView(new View(ApplicationProvider.getApplicationContext()));
    item.expandActionView();
    assertThat(item.collapseActionView()).isTrue();
    assertThat(item.isActionViewExpanded()).isFalse();
  }

  @Test
  public void collapseActionView_shouldInvokeListener() throws Exception {
    item.setActionView(new View(ApplicationProvider.getApplicationContext()));
    listener.expanded = true;
    item.collapseActionView();
    assertThat(listener.expanded).isFalse();
  }

  @Test
  public void methodsShouldReturnThis() throws Exception {
    item = item.setEnabled(true);
    assertThat(item).isNotNull();
    item = item.setOnMenuItemClickListener(null);
    assertThat(item).isNotNull();
    item = item.setActionProvider(null);
    assertThat(item).isNotNull();
    item = item.setActionView(0);
    assertThat(item).isNotNull();
    item = item.setActionView(null);
    assertThat(item).isNotNull();
    item = item.setAlphabeticShortcut('a');
    assertThat(item).isNotNull();
    item = item.setCheckable(false);
    assertThat(item).isNotNull();
    item = item.setChecked(true);
    assertThat(item).isNotNull();
    item = item.setIcon(null);
    assertThat(item).isNotNull();
    item = item.setIcon(0);
    assertThat(item).isNotNull();
    item = item.setIntent(null);
    assertThat(item).isNotNull();
    item = item.setNumericShortcut('6');
    assertThat(item).isNotNull();
    item = item.setOnActionExpandListener(null);
    assertThat(item).isNotNull();
    item = item.setShortcut('6', 'z');
    assertThat(item).isNotNull();
    item = item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    assertThat(item).isNotNull();
    item = item.setTitleCondensed("condensed");
    assertThat(item).isNotNull();
    item = item.setVisible(true);
    assertThat(item).isNotNull();
    item = item.setTitle("title");
    assertThat(item).isNotNull();
    item = item.setTitle(0);
    assertThat(item).isNotNull();
  }

  @Test
  public void setIcon_shouldNullifyOnZero() throws Exception {
    assertThat(item.getIcon()).isNull();
    item.setIcon(R.drawable.an_image);
    assertThat(shadowOf(item.getIcon()).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
    item.setIcon(0);
    assertThat(item.getIcon()).isNull();
  }

  @Test
  public void getIcon_shouldReturnDrawableFromSetIconDrawable() throws Exception {
    Drawable testDrawable =
        ApplicationProvider.getApplicationContext().getResources().getDrawable(R.drawable.an_image);
    assertThat(testDrawable).isNotNull();
    assertThat(item.getIcon()).isNull();
    item.setIcon(testDrawable);
    assertThat(item.getIcon()).isSameAs(testDrawable);
  }

  @Test
  public void getIcon_shouldReturnDrawableFromSetIconResourceId() throws Exception {
    assertThat(item.getIcon()).isNull();
    item.setIcon(R.drawable.an_other_image);
    assertThat(shadowOf(item.getIcon()).getCreatedFromResId()).isEqualTo(R.drawable.an_other_image);
  }

  @Test
  public void setOnActionExpandListener_shouldReturnMenuItem() throws Exception {
    assertThat(item.setOnActionExpandListener(listener)).isSameAs(item);
  }

  static class TestOnActionExpandListener implements MenuItem.OnActionExpandListener {
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
