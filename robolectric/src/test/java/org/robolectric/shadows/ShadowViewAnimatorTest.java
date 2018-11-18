package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.app.Application;
import android.view.View;
import android.widget.ViewAnimator;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowViewAnimatorTest {

  ViewAnimator viewAnimator;
  final Application application = ApplicationProvider.getApplicationContext();

  @Before
  public void setUp() {
    viewAnimator = new ViewAnimator(application);
  }

  @Test
  public void getDisplayedChildWhenEmpty_shouldDefaultToZero() {
    assertEquals(0, viewAnimator.getDisplayedChild());
  }

  @Test
  public void getDisplayedChild_shouldDefaultToZero() {
    viewAnimator.addView(new View(application));
    assertEquals(0, viewAnimator.getDisplayedChild());
  }

  @Test
  public void setDisplayedChild_shouldUpdateDisplayedChildIndex() {
    viewAnimator.addView(new View(application));
    viewAnimator.addView(new View(application));
    viewAnimator.setDisplayedChild(2);
    assertEquals(2, viewAnimator.getDisplayedChild());
  }

  @Test
  public void getCurrentView_shouldWork() {
    View view0 = new View(application);
    View view1 = new View(application);
    viewAnimator.addView(view0);
    viewAnimator.addView(view1);
    assertSame(view0, viewAnimator.getCurrentView());
    viewAnimator.setDisplayedChild(1);
    assertSame(view1, viewAnimator.getCurrentView());
  }

  @Test
  public void showNext_shouldDisplayNextChild() {
    viewAnimator.addView(new View(application));
    viewAnimator.addView(new View(application));
    assertEquals(0, viewAnimator.getDisplayedChild());
    viewAnimator.showNext();
    assertEquals(1, viewAnimator.getDisplayedChild());
    viewAnimator.showNext();
    assertEquals(0, viewAnimator.getDisplayedChild());
  }

  @Test
  public void showPrevious_shouldDisplayPreviousChild() {
    viewAnimator.addView(new View(application));
    viewAnimator.addView(new View(application));
    assertEquals(0, viewAnimator.getDisplayedChild());
    viewAnimator.showPrevious();
    assertEquals(1, viewAnimator.getDisplayedChild());
    viewAnimator.showPrevious();
    assertEquals(0, viewAnimator.getDisplayedChild());
  }
}
