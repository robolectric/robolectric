package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.idleMainLooper;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowViewGroupTest {
  private String defaultLineSeparator;
  private ViewGroup root;
  private View child1;
  private View child2;
  private ViewGroup child3;
  private View child3a;
  private View child3b;
  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();

    root = new FrameLayout(context);

    child1 = new View(context);
    child2 = new View(context);
    child3 = new FrameLayout(context);
    child3a = new View(context);
    child3b = new View(context);

    root.addView(child1);
    root.addView(child2);
    root.addView(child3);

    child3.addView(child3a);
    child3.addView(child3b);

    defaultLineSeparator = System.getProperty("line.separator");
    System.setProperty("line.separator", "\n");
  }

  @After
  public void tearDown() {
    System.setProperty("line.separator", defaultLineSeparator);
  }

  @Test
  public void removeNullView_doesNothing() {
    root.removeView(null);
  }

  @Test
  public void testLayoutAnimationListener() {
    assertThat(root.getLayoutAnimationListener()).isNull();

    AnimationListener animationListener = new AnimationListener() {
      @Override
      public void onAnimationEnd(Animation a) {
      }

      @Override
      public void onAnimationRepeat(Animation a) {
      }

      @Override
      public void onAnimationStart(Animation a) {
      }
    };
    root.setLayoutAnimationListener(animationListener);

    assertThat(root.getLayoutAnimationListener()).isSameInstanceAs(animationListener);
  }

  @Test
  public void testLayoutAnimation() {
    assertThat(root.getLayoutAnimation()).isNull();
    LayoutAnimationController layoutAnim = new LayoutAnimationController(context, null);
    root.setLayoutAnimation(layoutAnim);
    assertThat(root.getLayoutAnimation()).isSameInstanceAs(layoutAnim);
  }

  @Test
  public void testRemoveChildAt() {
    root.removeViewAt(1);

    assertThat(root.getChildCount()).isEqualTo(2);
    assertThat(root.getChildAt(0)).isSameInstanceAs(child1);
    assertThat(root.getChildAt(1)).isSameInstanceAs(child3);

    assertThat(child2.getParent()).isNull();
  }

  @Test
  public void testAddViewAt() {
    root.removeAllViews();
    root.addView(child1);
    root.addView(child2);
    root.addView(child3, 1);
    assertThat(root.getChildAt(0)).isSameInstanceAs(child1);
    assertThat(root.getChildAt(1)).isSameInstanceAs(child3);
    assertThat(root.getChildAt(2)).isSameInstanceAs(child2);
  }

  @Test
  public void shouldFindViewWithTag() {
    root.removeAllViews();
    child1.setTag("tag1");
    child2.setTag("tag2");
    child3.setTag("tag3");
    root.addView(child1);
    root.addView(child2);
    root.addView(child3, 1);
    assertThat((View) root.findViewWithTag("tag1")).isSameInstanceAs(child1);
    assertThat((View) root.findViewWithTag("tag2")).isSameInstanceAs(child2);
    assertThat((ViewGroup) root.findViewWithTag("tag3")).isSameInstanceAs(child3);
  }

  @Test
  public void shouldNotFindViewWithTagReturnNull() {
    root.removeAllViews();
    child1.setTag("tag1");
    child2.setTag("tag2");
    child3.setTag("tag3");
    root.addView(child1);
    root.addView(child2);
    root.addView(child3, 1);
    assertThat((View) root.findViewWithTag("tag21")).isNull();
    assertThat((ViewGroup) root.findViewWithTag("tag23")).isNull();
  }

  @Test
  public void shouldfindViewWithTagFromCorrectViewGroup() {
    root.removeAllViews();
    child1.setTag("tag1");
    child2.setTag("tag2");
    child3.setTag("tag3");
    root.addView(child1);
    root.addView(child2);
    root.addView(child3);

    child3a.setTag("tag1");
    child3b.setTag("tag2");

    // can find views by tag from root
    assertThat((View) root.findViewWithTag("tag1")).isSameInstanceAs(child1);
    assertThat((View) root.findViewWithTag("tag2")).isSameInstanceAs(child2);
    assertThat((ViewGroup) root.findViewWithTag("tag3")).isSameInstanceAs(child3);

    // can find views by tag from child3
    assertThat((View) child3.findViewWithTag("tag1")).isSameInstanceAs(child3a);
    assertThat((View) child3.findViewWithTag("tag2")).isSameInstanceAs(child3b);
  }

  @Test
  public void hasFocus_shouldReturnTrueIfAnyChildHasFocus() {
    makeFocusable(root, child1, child2, child3, child3a, child3b);
    assertFalse(root.hasFocus());

    child1.requestFocus();
    assertTrue(root.hasFocus());

    child1.clearFocus();
    assertFalse(child1.hasFocus());
    assertTrue(root.hasFocus());

    child3b.requestFocus();
    assertTrue(root.hasFocus());

    child3b.clearFocus();
    assertFalse(child3b.hasFocus());
    assertFalse(child3.hasFocus());
    assertTrue(root.hasFocus());

    child2.requestFocus();
    assertFalse(child3.hasFocus());
    assertTrue(child2.hasFocus());
    assertTrue(root.hasFocus());

    root.requestFocus();
    assertTrue(root.hasFocus());
  }

  @Test
  public void clearFocus_shouldRecursivelyClearTheFocusOfAllChildren() {
    child3a.requestFocus();

    root.clearFocus();

    assertFalse(child3a.hasFocus());
    assertFalse(child3.hasFocus());
    assertFalse(root.hasFocus());

    root.requestFocus();
    root.clearFocus();
    assertFalse(root.hasFocus());
  }

  @Test
  public void dump_shouldDumpStructure() {
    child3.setId(R.id.snippet_text);
    child3b.setVisibility(View.GONE);
    TextView textView = new TextView(context);
    textView.setText("Here's some text!");
    textView.setVisibility(View.INVISIBLE);
    child3.addView(textView);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    shadowOf(root).dump(new PrintStream(out), 0);
    String expected = "<FrameLayout>\n" +
        "  <View/>\n" +
        "  <View/>\n" +
        "  <FrameLayout id=\"org.robolectric:id/snippet_text\">\n" +
        "    <View/>\n" +
        "    <View visibility=\"GONE\"/>\n" +
        "    <TextView visibility=\"INVISIBLE\" text=\"Here&#39;s some text!\"/>\n" +
        "  </FrameLayout>\n" +
        "</FrameLayout>\n";
    assertEquals(expected.replaceAll("\n", System.lineSeparator()), out.toString());
  }

  @Test
  public void addViewWithLayoutParams_shouldStoreLayoutParams() {
    FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    View child1 = new View(ApplicationProvider.getApplicationContext());
    View child2 = new View(ApplicationProvider.getApplicationContext());
    root.addView(child1, layoutParams1);
    root.addView(child2, 1, layoutParams2);
    assertSame(layoutParams1, child1.getLayoutParams());
    assertSame(layoutParams2, child2.getLayoutParams());
  }

//  todo: re-enable this
//  @Test @Config(minSdk = FROYO)
//  public void getChildAt_shouldThrowIndexOutOfBoundsForInvalidIndices() { // 'cause that's what Android does
//    assertThat(root.getChildCount()).isEqualTo(3);
//    assertThrowsExceptionForBadIndex(13);
//    assertThrowsExceptionForBadIndex(3);
//    assertThrowsExceptionForBadIndex(-1);
//  }
//
//  private void assertThrowsExceptionForBadIndex(int index) {
//    try {
//      assertThat(root.getChildAt(index)).isNull();
//      fail("no exception");
//    } catch (IndexOutOfBoundsException ex) {
//      //noinspection UnnecessaryReturnStatement
//      return;
//    } catch (Exception ex) {
//      fail("wrong exception type");
//    }
//  }

  @Test
  public void layoutParams_shouldBeViewGroupLayoutParams() {
    assertThat(child1.getLayoutParams()).isInstanceOf(FrameLayout.LayoutParams.class);
    assertThat(child1.getLayoutParams()).isInstanceOf(ViewGroup.LayoutParams.class);
  }

  @Test
  public void removeView_removesView() {
    assertThat(root.getChildCount()).isEqualTo(3);
    root.removeView(child1);
    assertThat(root.getChildCount()).isEqualTo(2);
    assertThat(root.getChildAt(0)).isSameInstanceAs(child2);
    assertThat(root.getChildAt(1)).isSameInstanceAs(child3);
    assertThat(child1.getParent()).isNull();
  }

  @Test
  public void removeView_resetsParentOnlyIfViewIsInViewGroup() {
    assertThat(root.getChildCount()).isEqualTo(3);
    assertNotSame(child3a.getParent(), root);
    root.removeView(child3a);
    assertThat(root.getChildCount()).isEqualTo(3);
    assertThat(child3a.getParent()).isSameInstanceAs(child3);
  }

  @Test
  public void addView_whenChildAlreadyHasAParent_shouldThrow() {
    ViewGroup newRoot = new FrameLayout(context);
    try {
      newRoot.addView(child1);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // pass
    }
  }

  @Test
  public void shouldKnowWhenOnInterceptTouchEventWasCalled() {
    ViewGroup viewGroup = new FrameLayout(context);

    MotionEvent touchEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0);
    viewGroup.onInterceptTouchEvent(touchEvent);

    assertThat(shadowOf(viewGroup).getInterceptedTouchEvent()).isEqualTo(touchEvent);
  }

  @Test
  public void removeView_shouldRequestLayout() {
    View view = new View(context);
    ViewGroup viewGroup = new FrameLayout(context);
    viewGroup.addView(view);
    shadowOf(viewGroup).setDidRequestLayout(false);

    viewGroup.removeView(view);
    assertThat(shadowOf(viewGroup).didRequestLayout()).isTrue();
  }

  @Test
  public void removeViewAt_shouldRequestLayout() {
    View view = new View(context);
    ViewGroup viewGroup = new FrameLayout(context);
    viewGroup.addView(view);
    shadowOf(viewGroup).setDidRequestLayout(false);

    viewGroup.removeViewAt(0);
    assertThat(shadowOf(viewGroup).didRequestLayout()).isTrue();
  }

  @Test
  public void removeAllViews_shouldRequestLayout() {
    View view = new View(context);
    ViewGroup viewGroup = new FrameLayout(context);
    viewGroup.addView(view);
    shadowOf(viewGroup).setDidRequestLayout(false);

    viewGroup.removeAllViews();
    assertThat(shadowOf(viewGroup).didRequestLayout()).isTrue();
  }

  @Test
  public void addView_shouldRequestLayout() {
    View view = new View(context);
    ViewGroup viewGroup = new FrameLayout(context);
    viewGroup.addView(view);

    assertThat(shadowOf(viewGroup).didRequestLayout()).isTrue();
  }

  @Test
  public void addView_withIndex_shouldRequestLayout() {
    View view = new View(context);
    ViewGroup viewGroup = new FrameLayout(context);
    viewGroup.addView(view, 0);

    assertThat(shadowOf(viewGroup).didRequestLayout()).isTrue();
  }

  @Test
  public void removeAllViews_shouldCallOnChildViewRemovedWithEachChild() {
    View view = new View(context);
    ViewGroup viewGroup = new FrameLayout(context);
    viewGroup.addView(view);

    TestOnHierarchyChangeListener testListener = new TestOnHierarchyChangeListener();

    viewGroup.setOnHierarchyChangeListener(testListener);
    viewGroup.removeAllViews();
    assertTrue(testListener.wasCalled());
  }

  @Test
  public void requestDisallowInterceptTouchEvent_storedOnShadow() {
    child3.requestDisallowInterceptTouchEvent(true);

    assertTrue(shadowOf(child3).getDisallowInterceptTouchEvent());
  }

  @Test
  public void requestDisallowInterceptTouchEvent_bubblesUp() {
    child3.requestDisallowInterceptTouchEvent(true);

    assertTrue(shadowOf(child3).getDisallowInterceptTouchEvent());
    assertTrue(shadowOf(root).getDisallowInterceptTouchEvent());
  }

  @Test
  public void requestDisallowInterceptTouchEvent_isReflected() {
    // Set up an Activity to accurately dispatch touch events.
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.setContentView(root);
    idleMainLooper();
    // Set a no-op click listener so we collect all the touch events.
    child3a.setOnClickListener(view -> {});
    // Request our parent not intercept our touch events.
    // This must be _during the initial down MotionEvent_ and not before.
    // The down event will reset this state (and so we do not need to reset it).
    // The value in getDisallowInterceptTouchEvent() is not in-sync with the flag and
    // only records the last call to requestDisallowInterceptTouchEvent().
    child3a.setOnTouchListener(
        (view, event) -> {
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
          }
          return false;
        });

    MotionEvent downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0);
    MotionEvent moveEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 0, 0);
    MotionEvent upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0);

    root.dispatchTouchEvent(downEvent);
    // Down event is _always_ intercepted by the root.
    assertTrue(shadowOf(root).getDisallowInterceptTouchEvent());
    assertSame(shadowOf(root).getInterceptedTouchEvent(), downEvent);
    assertSame(shadowOf(child3a).getLastTouchEvent(), downEvent);

    root.dispatchTouchEvent(moveEvent);
    // Subsequent event types are _not_ intercepted:
    assertTrue(shadowOf(root).getDisallowInterceptTouchEvent());
    assertSame(shadowOf(root).getInterceptedTouchEvent(), downEvent);
    assertSame(shadowOf(child3a).getLastTouchEvent(), moveEvent);

    root.dispatchTouchEvent(upEvent);
    // Subsequent event types are _not_ intercepted:
    assertTrue(shadowOf(root).getDisallowInterceptTouchEvent());
    assertSame(shadowOf(root).getInterceptedTouchEvent(), downEvent);
    assertSame(shadowOf(child3a).getLastTouchEvent(), upEvent);
  }

  @Test
  public void draw_drawsChildren() {
    DrawRecordView view = new DrawRecordView(context);
    ViewGroup viewGroup = new FrameLayout(context);
    viewGroup.addView(view);
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    viewGroup.draw(canvas);
    assertThat(view.wasDrawn).isTrue();
  }

  private void makeFocusable(View... views) {
    for (View view : views) {
      view.setFocusable(true);
    }
  }

  static class TestOnHierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {
    boolean wasCalled = false;

    @Override
    public void onChildViewAdded(View parent, View child) {
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
      wasCalled = true;
    }

    public boolean wasCalled() {
      return wasCalled;
    }
  }

  static class DrawRecordView extends View {

    boolean wasDrawn;

    public DrawRecordView(Context context) {
      super(context);
    }

    @Override
    public void draw(Canvas canvas) {
      super.draw(canvas);
      wasDrawn = true;
    }
  }
}
