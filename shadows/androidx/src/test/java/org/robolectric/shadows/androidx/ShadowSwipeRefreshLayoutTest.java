package org.robolectric.shadows.androidx;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.shadows.androidx.Shadows.shadowOf;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class ShadowSwipeRefreshLayoutTest {

  @Test
  public void getOnRefreshListener_shouldReturnTheListener() {
    final OnRefreshListener listener = mock(OnRefreshListener.class);

    final SwipeRefreshLayout layout = new SwipeRefreshLayout(RuntimeEnvironment.application);
    layout.setOnRefreshListener(listener);

    assertThat(shadowOf(layout).getOnRefreshListener()).isSameAs(listener);
  }
}
