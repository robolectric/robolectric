package org.robolectric.shadows.support.v7;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.*;
import static org.robolectric.shadows.support.v7.Shadows.*;

import android.support.v7.widget.PopupMenu;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class ShadowPopupMenuTest {

  @Test
  public void getOnDismissListener_shouldReturnListener() {
    final PopupMenu.OnDismissListener listener = mock(PopupMenu.OnDismissListener.class);

    final PopupMenu popup = new PopupMenu(RuntimeEnvironment.application, null);
    popup.setOnDismissListener(listener);

    assertThat(shadowOf(popup)).isSameAs(listener);
  }
}