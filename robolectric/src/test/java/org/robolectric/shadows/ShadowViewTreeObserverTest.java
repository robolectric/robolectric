package org.robolectric.shadows;

import android.view.ViewTreeObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowViewTreeObserverTest {

  private ViewTreeObserver viewTreeObserver;
  private TestOnGlobalLayoutListener listener1;
  private TestOnGlobalLayoutListener listener2;

  @Before
  public void setUp() throws Exception {
    viewTreeObserver = Shadow.newInstanceOf(ViewTreeObserver.class);
    listener1 = new TestOnGlobalLayoutListener();
    listener2 = new TestOnGlobalLayoutListener();
  }

  @Test
  public void shouldRecordMultipleOnGlobalLayoutListenersAndFireThemWhenAsked() throws Exception {
    viewTreeObserver.addOnGlobalLayoutListener(listener1);
    viewTreeObserver.addOnGlobalLayoutListener(listener2);

    shadowOf(viewTreeObserver).fireOnGlobalLayoutListeners();
    assertTrue(listener1.onGlobalLayoutWasCalled);
    assertTrue(listener2.onGlobalLayoutWasCalled);

    listener1.reset();
    listener2.reset();
    viewTreeObserver.removeOnGlobalLayoutListener(listener1);
    shadowOf(viewTreeObserver).fireOnGlobalLayoutListeners();

    assertFalse(listener1.onGlobalLayoutWasCalled);
    assertTrue(listener2.onGlobalLayoutWasCalled);

    listener1.reset();
    listener2.reset();
    viewTreeObserver.removeOnGlobalLayoutListener(listener2);
    shadowOf(viewTreeObserver).fireOnGlobalLayoutListeners();

    assertFalse(listener1.onGlobalLayoutWasCalled);
    assertFalse(listener2.onGlobalLayoutWasCalled);
  }

  @Test
  public void getGlobalLayoutListeners_shouldReturnTheListeners() throws Exception {
    viewTreeObserver.addOnGlobalLayoutListener(listener1);
    viewTreeObserver.addOnGlobalLayoutListener(listener2);

    List<ViewTreeObserver.OnGlobalLayoutListener> listeners = shadowOf(viewTreeObserver).getOnGlobalLayoutListeners();
    assertTrue(listeners.size() == 2);
    assertTrue(listeners.contains(listener1));
    assertTrue(listeners.contains(listener2));
  }

  private static class TestOnGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
    boolean onGlobalLayoutWasCalled;

    @Override
    public void onGlobalLayout() {
      onGlobalLayoutWasCalled = true;
    }

    public void reset() {
      onGlobalLayoutWasCalled = false;
    }
  }
}
