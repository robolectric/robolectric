package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowPopupMenuTest {

  private PopupMenu popupMenu;
  private ShadowPopupMenu shadowPopupMenu;

  @Before
  public void setUp() {
    View anchorView = new View(ApplicationProvider.getApplicationContext());
    popupMenu = new PopupMenu(ApplicationProvider.getApplicationContext(), anchorView);
    shadowPopupMenu = shadowOf(popupMenu);
  }

  @Test
  public void testIsShowing_returnsFalseUponCreation() throws Exception {
    assertThat(shadowPopupMenu.isShowing()).isFalse();
  }

  @Test
  public void testIsShowing_returnsTrueIfShown() throws Exception {
    popupMenu.show();
    assertThat(shadowPopupMenu.isShowing()).isTrue();
  }

  @Test
  public void testIsShowing_returnsFalseIfShownThenDismissed() throws Exception {
    popupMenu.show();
    popupMenu.dismiss();
    assertThat(shadowPopupMenu.isShowing()).isFalse();
  }

  @Test
  public void getLatestPopupMenu_returnsNullUponCreation() throws Exception {
    assertThat(ShadowPopupMenu.getLatestPopupMenu()).isNull();
  }

  @Test
  public void getLatestPopupMenu_returnsLastMenuShown() throws Exception {
    popupMenu.show();
    assertThat(ShadowPopupMenu.getLatestPopupMenu()).isEqualTo(popupMenu);
  }

  @Test
  public void getOnClickListener_returnsOnClickListener() throws Exception {
    assertThat(shadowOf(popupMenu).getOnMenuItemClickListener()).isNull();

    PopupMenu.OnMenuItemClickListener listener = new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
      }
    };
    popupMenu.setOnMenuItemClickListener(listener);

    assertThat(shadowOf(popupMenu).getOnMenuItemClickListener()).isEqualTo(listener);
  }
}
