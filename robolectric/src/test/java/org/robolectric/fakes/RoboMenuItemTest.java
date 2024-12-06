package org.robolectric.fakes;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class RoboMenuItemTest {
  private Context context;
  @Nonnull private MenuItem item;
  private TestOnActionExpandListener listener;

  public RoboMenuItemTest(@Nonnull RoboMenuItem roboMenuItem) {
    item = roboMenuItem;
  }

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    listener = new TestOnActionExpandListener();
    item.setOnActionExpandListener(listener);
  }

  @Test
  public void shouldCheckTheMenuItem() {
    assertThat(item.isChecked()).isFalse();
    item.setChecked(true);
    assertThat(item.isChecked()).isTrue();
  }

  @Test
  public void shouldAllowSettingCheckable() {
    assertThat(item.isCheckable()).isFalse();
    item.setCheckable(true);
    assertThat(item.isCheckable()).isTrue();
  }

  @Test
  public void shouldAllowSettingVisible() {
    assertThat(item.isVisible()).isTrue();
    item.setVisible(false);
    assertThat(item.isVisible()).isFalse();
  }

  @Test
  public void expandActionView_shouldReturnFalseIfActionViewIsNull() {
    item.setActionView(null);
    assertThat(item.expandActionView()).isFalse();
  }

  @Test
  public void expandActionView_shouldSetExpandedTrue() {
    View actionView = new View(context);
    item.setActionView(actionView);
    assertThat(item.getActionView()).isEqualTo(actionView);
    assertThat(item.expandActionView()).isTrue();
    assertThat(item.isActionViewExpanded()).isTrue();
  }

  @Test
  public void expandActionView_shouldInvokeListener() {
    View actionView = new View(context);
    item.setActionView(actionView);
    assertThat(item.getActionView()).isEqualTo(actionView);
    assertThat(item.expandActionView()).isTrue();
    assertThat(listener.expanded).isTrue();
  }

  @Test
  public void collapseActionView_shouldReturnFalseIfActionViewIsNull() {
    item.setActionView(null);
    assertThat(item.collapseActionView()).isFalse();
  }

  @Test
  public void collapseActionView_shouldSetExpandedFalse() {
    View actionView = new View(context);
    item.setActionView(actionView);
    assertThat(item.getActionView()).isEqualTo(actionView);
    item.expandActionView();
    assertThat(item.collapseActionView()).isTrue();
    assertThat(item.isActionViewExpanded()).isFalse();
  }

  @Test
  public void collapseActionView_shouldInvokeListener() {
    View actionView = new View(context);
    item.setActionView(actionView);
    assertThat(item.getActionView()).isEqualTo(actionView);
    listener.expanded = true;
    assertThat(item.collapseActionView()).isTrue();
    assertThat(listener.expanded).isFalse();
  }

  @Test
  public void methodsShouldReturnThis() {
    item = item.setEnabled(true);
    assertThat(item).isNotNull();
    item = item.setOnMenuItemClickListener(null);
    assertThat(item).isNotNull();
    item = item.setActionProvider(null);
    assertThat(item).isNotNull();
    item = item.setActionView(R.layout.custom_layout);
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
  public void setIcon_shouldNullifyOnZero() {
    assertThat(item.getIcon()).isNull();
    item.setIcon(R.drawable.an_image);
    assertThat(shadowOf(item.getIcon()).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
    item.setIcon(0);
    assertThat(item.getIcon()).isNull();
  }

  @Test
  public void getIcon_shouldReturnDrawableFromSetIconDrawable() {
    Drawable testDrawable = context.getDrawable(R.drawable.an_image);
    assertThat(testDrawable).isNotNull();
    assertThat(item.getIcon()).isNull();
    item.setIcon(testDrawable);
    assertThat(item.getIcon()).isSameInstanceAs(testDrawable);
  }

  @Test
  public void getIcon_shouldReturnDrawableFromSetIconResourceId() {
    assertThat(item.getIcon()).isNull();
    item.setIcon(R.drawable.an_other_image);
    assertThat(shadowOf(item.getIcon()).getCreatedFromResId()).isEqualTo(R.drawable.an_other_image);
  }

  @Test
  public void setOnActionExpandListener_shouldReturnMenuItem() {
    assertThat(item.setOnActionExpandListener(listener)).isSameInstanceAs(item);
  }

  @Test
  public void setTitle_shouldNullifyOnZero() {
    item.setTitle("title");
    assertThat(item.getTitle()).isNotNull();
    item.setTitle(0);
    assertThat(item.getTitle()).isNull();
  }

  @Test
  public void setTitle_shouldSetTitleFromResourceId() {
    assertThat(item.getTitle()).isNull();
    item.setTitle(R.string.app_name);
    assertThat(item.getTitle()).isEqualTo(context.getString(R.string.app_name));
  }

  static class TestOnActionExpandListener implements MenuItem.OnActionExpandListener {
    private boolean expanded = false;

    @Override
    public boolean onMenuItemActionExpand(@Nonnull MenuItem menuItem) {
      expanded = true;
      return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(@Nonnull MenuItem menuItem) {
      expanded = false;
      return true;
    }
  }

  @ParameterizedRobolectricTestRunner.Parameters
  public static Iterable<?> data() {
    return Arrays.asList(
        new RoboMenuItem(),
        new RoboMenuItem(R.id.text1),
        new RoboMenuItem(RuntimeEnvironment.getApplication()));
  }
}
