package org.robolectric.shadows.support.v4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.shadows.support.v4.Shadows.shadowOf;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
public class ShadowSwipeRefreshLayoutTest {

  @Test
  public void getOnRefreshListener_shouldReturnTheListener() {
    final OnRefreshListener listener = mock(OnRefreshListener.class);

    final SwipeRefreshLayout layout = new SwipeRefreshLayout(RuntimeEnvironment.application);
    layout.setOnRefreshListener(listener);

    assertThat(shadowOf(layout).getOnRefreshListener()).isSameAs(listener);
  }
}
