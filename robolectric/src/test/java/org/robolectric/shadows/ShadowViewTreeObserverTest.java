package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowViewTreeObserver} */
@RunWith(AndroidJUnit4.class)
public final class ShadowViewTreeObserverTest {
  private ViewTreeObserver viewTreeObserver;
  private ShadowViewTreeObserver shadowViewTreeObserver;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    viewTreeObserver = new View(context).getViewTreeObserver();
    shadowViewTreeObserver = Shadow.extract(viewTreeObserver);
  }

  @Test
  public void getOnGlobalLayoutListeners_addListeners_returnsExpectedList() {
    ViewTreeObserver.OnGlobalLayoutListener listener =
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {}
        };
    viewTreeObserver.addOnGlobalLayoutListener(listener);

    ImmutableList<ViewTreeObserver.OnGlobalLayoutListener> listeners =
        shadowViewTreeObserver.getOnGlobalLayoutListeners();

    assertThat(listeners).containsExactly(listener);
  }

  @Test
  public void getOnGlobalLayoutListeners_removeListeners_returnsExpectedList() {
    ViewTreeObserver.OnGlobalLayoutListener listener1 =
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {}
        };
    ViewTreeObserver.OnGlobalLayoutListener listener2 =
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {}
        };
    viewTreeObserver.addOnGlobalLayoutListener(listener1);
    viewTreeObserver.addOnGlobalLayoutListener(listener2);
    viewTreeObserver.removeOnGlobalLayoutListener(listener1);

    ImmutableList<ViewTreeObserver.OnGlobalLayoutListener> listeners =
        shadowViewTreeObserver.getOnGlobalLayoutListeners();

    assertThat(listeners).containsExactly(listener2);
  }

  @Test
  public void getOnGlobalLayoutListeners_noListeners_returnsEmptyList() {
    ImmutableList<ViewTreeObserver.OnGlobalLayoutListener> listeners =
        shadowViewTreeObserver.getOnGlobalLayoutListeners();

    assertThat(listeners).isEmpty();
  }
}
