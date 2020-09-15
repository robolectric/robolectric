package org.robolectric.shadows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowRecyclerView}. */
@RunWith(AndroidJUnit4.class)
public class ShadowRecyclerViewTest {

  private RecyclerView recyclerView;

  @Before
  public void setUp() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    recyclerView = new RecyclerView(context);
  }

  @Test
  public void triggerOnScrollStateChanged_notifiesListeners() {
    OnScrollListener mockedOnScrollListener = mock(OnScrollListener.class);
    recyclerView.addOnScrollListener(mockedOnScrollListener);

    shadowOf(recyclerView).triggerOnScrollStateChanged(RecyclerView.SCROLL_STATE_DRAGGING);

    verify(mockedOnScrollListener)
        .onScrollStateChanged(recyclerView, RecyclerView.SCROLL_STATE_DRAGGING);
  }

  @Test
  public void triggerOnScrollStateChanged_removedListener_notNotified() {
    OnScrollListener mockedOnScrollListener = mock(OnScrollListener.class);
    recyclerView.addOnScrollListener(mockedOnScrollListener);
    recyclerView.removeOnScrollListener(mockedOnScrollListener);

    shadowOf(recyclerView).triggerOnScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE);

    verifyZeroInteractions(mockedOnScrollListener);
  }

  @Test
  public void triggerOnScrollStateChanged_multipleListeners_allNotified() {
    OnScrollListener mockedOnScrollListener1 = mock(OnScrollListener.class);
    OnScrollListener mockedOnScrollListener2 = mock(OnScrollListener.class);
    recyclerView.addOnScrollListener(mockedOnScrollListener1);
    recyclerView.addOnScrollListener(mockedOnScrollListener2);

    shadowOf(recyclerView).triggerOnScrollStateChanged(RecyclerView.SCROLL_STATE_SETTLING);

    verify(mockedOnScrollListener1)
        .onScrollStateChanged(recyclerView, RecyclerView.SCROLL_STATE_SETTLING);
    verify(mockedOnScrollListener2)
        .onScrollStateChanged(recyclerView, RecyclerView.SCROLL_STATE_SETTLING);
  }

  @Test
  public void triggerOnScrollStateChanged_neverAdded_doesntCrash() {
    shadowOf(recyclerView).triggerOnScrollStateChanged(RecyclerView.SCROLL_STATE_SETTLING);

    // This test case not crashing verifies that the shadow properly handles the field being
    // initialized to null.
  }

  @Test(expected = IllegalArgumentException.class)
  public void triggerOnScrollStateChanged_invalidState_throws() {
    shadowOf(recyclerView).triggerOnScrollStateChanged(-1);
  }
}
