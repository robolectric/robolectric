package org.robolectric.shadows;

import android.view.ViewTreeObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ViewTreeObserverTest {

  private ViewTreeObserver viewTreeObserver;
  private TestOnGlobalLayoutListener listener1;
  private TestOnGlobalLayoutListener listener2;

  @Before
  public void setUp() throws Exception {
    viewTreeObserver = Robolectric.Reflection.newInstanceOf(ViewTreeObserver.class);
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
    viewTreeObserver.removeGlobalOnLayoutListener(listener1);
    shadowOf(viewTreeObserver).fireOnGlobalLayoutListeners();

    assertFalse(listener1.onGlobalLayoutWasCalled);
    assertTrue(listener2.onGlobalLayoutWasCalled);

    listener1.reset();
    listener2.reset();
    viewTreeObserver.removeGlobalOnLayoutListener(listener2);
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
