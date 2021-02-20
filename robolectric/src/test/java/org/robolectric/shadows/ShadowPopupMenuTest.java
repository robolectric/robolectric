package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

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
  public void testIsShowing_returnsFalseUponCreation() {
    assertThat(shadowPopupMenu.isShowing()).isFalse();
  }

  @Test
  public void testIsShowing_returnsTrueIfShown() {
    popupMenu.show();
    assertThat(shadowPopupMenu.isShowing()).isTrue();
  }

  @Test
  public void testIsShowing_returnsFalseIfShownThenDismissed() {
    popupMenu.show();
    popupMenu.dismiss();
    assertThat(shadowPopupMenu.isShowing()).isFalse();
  }

  @Test
  public void getLatestPopupMenu_returnsNullUponCreation() {
    assertThat(ShadowPopupMenu.getLatestPopupMenu()).isNull();
  }

  @Test
  public void getLatestPopupMenu_returnsLastMenuShown() {
    popupMenu.show();
    assertThat(ShadowPopupMenu.getLatestPopupMenu()).isEqualTo(popupMenu);
  }

  @Test
  public void getOnClickListener_returnsOnClickListener() {
    assertThat(shadowOf(popupMenu).getOnMenuItemClickListener()).isNull();

    PopupMenu.OnMenuItemClickListener listener = menuItem -> false;
    popupMenu.setOnMenuItemClickListener(listener);

    assertThat(shadowOf(popupMenu).getOnMenuItemClickListener()).isEqualTo(listener);
  }
}
