package org.robolectric.shadows.support.v4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.TestRunnerWithManifest;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.shadows.support.v4.Shadows.shadowOf;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

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
