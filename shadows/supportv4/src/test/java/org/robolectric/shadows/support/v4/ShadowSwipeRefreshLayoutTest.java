package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.shadows.support.v4.Shadows.shadowOf;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AndroidJUnit4.class)
public class ShadowSwipeRefreshLayoutTest {

  @Test
  public void getOnRefreshListener_shouldReturnTheListener() {
    final OnRefreshListener listener = mock(OnRefreshListener.class);

    final SwipeRefreshLayout layout = new SwipeRefreshLayout(RuntimeEnvironment.application);
    layout.setOnRefreshListener(listener);

    assertThat(shadowOf(layout).getOnRefreshListener()).isSameInstanceAs(listener);
  }
}
