package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit test for {@link ShadowWindowManagerImpl}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowWindowManagerImplTest {

  private View view;
  private LayoutParams layoutParams;
  private WindowManager windowManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    view = new View(context);
    windowManager = context.getSystemService(WindowManager.class);
    layoutParams =
        new LayoutParams(
            /*w=*/ ViewGroup.LayoutParams.MATCH_PARENT,
            /*h=*/ ViewGroup.LayoutParams.MATCH_PARENT,
            LayoutParams.TYPE_APPLICATION_OVERLAY,
            LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
  }

  @Test
  public void getViews_isInitiallyEmpty() {
    List<View> views = ((ShadowWindowManagerImpl) shadowOf(windowManager)).getViews();

    assertThat(views).isEmpty();
  }

  @Test
  public void getViews_returnsAnAddedView() {
    windowManager.addView(view, layoutParams);

    List<View> views = ((ShadowWindowManagerImpl) shadowOf(windowManager)).getViews();

    assertThat(views).hasSize(1);
    assertThat(views.get(0)).isSameAs(view);
  }

  @Test
  public void getViews_doesNotReturnARemovedView() {
    windowManager.addView(view, layoutParams);
    windowManager.removeView(view);

    List<View> views = ((ShadowWindowManagerImpl) shadowOf(windowManager)).getViews();

    assertThat(views).isEmpty();
  }
}
