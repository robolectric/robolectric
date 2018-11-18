package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.setupActivity;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowId;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.android.DeviceConfig;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.AccessibilityChecks;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.TestRunnable;

@RunWith(AndroidJUnit4.class)
public class ShadowViewTest {
  private View view;
  private List<String> transcript;
  private Application context;

  @Before
  public void setUp() throws Exception {
    transcript = new ArrayList<>();
    context = ApplicationProvider.getApplicationContext();
    view = new View(context);
  }

  @Test
  public void testHasNullLayoutParamsUntilAddedToParent() throws Exception {
    assertThat(view.getLayoutParams()).isNull();
    new LinearLayout(context).addView(view);
    assertThat(view.getLayoutParams()).isNotNull();
  }

  @Test
  public void layout_shouldAffectWidthAndHeight() throws Exception {
    assertThat(view.getWidth()).isEqualTo(0);
    assertThat(view.getHeight()).isEqualTo(0);

    view.layout(100, 200, 303, 404);
    assertThat(view.getWidth()).isEqualTo(303 - 100);
    assertThat(view.getHeight()).isEqualTo(404 - 200);
  }

  @Test
  public void measuredDimensions() throws Exception {
    View view1 =
        new View(context) {
          {
            setMeasuredDimension(123, 456);
          }
        };
    assertThat(view1.getMeasuredWidth()).isEqualTo(123);
    assertThat(view1.getMeasuredHeight()).isEqualTo(456);
  }

  @Test
  public void layout_shouldCallOnLayoutOnlyIfChanged() throws Exception {
    View view1 =
        new View(context) {
          @Override
          protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            transcript.add(
                "onLayout " + changed + " " + left + " " + top + " " + right + " " + bottom);
          }
        };
    view1.layout(0, 0, 0, 0);
    assertThat(transcript).isEmpty();
    view1.layout(1, 2, 3, 4);
    assertThat(transcript).containsExactly("onLayout true 1 2 3 4");
    transcript.clear();
    view1.layout(1, 2, 3, 4);
    assertThat(transcript).isEmpty();
  }

  @Test
  public void shouldFocus() throws Exception {
    final List<String> transcript = new ArrayList<>();

    view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        transcript.add(hasFocus ? "Gained focus" : "Lost focus");
      }
    });

    assertFalse(view.isFocused());
    assertFalse(view.hasFocus());
    assertThat(transcript).isEmpty();

    view.requestFocus();
    assertFalse(view.isFocused());
    assertFalse(view.hasFocus());
    assertThat(transcript).isEmpty();

    view.setFocusable(true);
    view.requestFocus();
    assertTrue(view.isFocused());
    assertTrue(view.hasFocus());
    assertThat(transcript).containsExactly("Gained focus");
    transcript.clear();

    shadowOf(view)
        .setMyParent(new LinearLayout(context)); // we can never lose focus unless a parent can
    // take it

    view.clearFocus();
    assertFalse(view.isFocused());
    assertFalse(view.hasFocus());
    assertThat(transcript).containsExactly("Lost focus");
  }

  @Test
  public void shouldNotBeFocusableByDefault() throws Exception {
    assertFalse(view.isFocusable());

    view.setFocusable(true);
    assertTrue(view.isFocusable());
  }

  @Test
  public void shouldKnowIfThisOrAncestorsAreVisible() throws Exception {
    assertThat(view.isShown()).named("view isn't considered shown unless it has a view root").isFalse();
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class));
    assertThat(view.isShown()).isTrue();
    shadowOf(view).setMyParent(null);

    ViewGroup parent = new LinearLayout(context);
    parent.addView(view);

    ViewGroup grandParent = new LinearLayout(context);
    grandParent.addView(parent);

    grandParent.setVisibility(View.GONE);

    assertFalse(view.isShown());
  }

  @Test
  public void shouldInflateMergeRootedLayoutAndNotCreateReferentialLoops() throws Exception {
    LinearLayout root = new LinearLayout(context);
    LinearLayout.inflate(context, R.layout.inner_merge, root);
    for (int i = 0; i < root.getChildCount(); i++) {
      View child = root.getChildAt(i);
      assertNotSame(root, child);
    }
  }

  @Test
  public void performLongClick_shouldClickOnView() throws Exception {
    OnLongClickListener clickListener = mock(OnLongClickListener.class);
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class));
    view.setOnLongClickListener(clickListener);
    view.performLongClick();

    verify(clickListener).onLongClick(view);
  }

  @Test
  public void checkedClick_shouldClickOnView() throws Exception {
    OnClickListener clickListener = mock(OnClickListener.class);
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class));
    view.setOnClickListener(clickListener);
    shadowOf(view).checkedPerformClick();

    verify(clickListener).onClick(view);
  }

  @Test(expected = RuntimeException.class)
  public void checkedClick_shouldThrowIfViewIsNotVisible() throws Exception {
    ViewGroup grandParent = new LinearLayout(context);
    ViewGroup parent = new LinearLayout(context);
    grandParent.addView(parent);
    parent.addView(view);
    grandParent.setVisibility(View.GONE);

    shadowOf(view).checkedPerformClick();
  }

  @Test(expected = RuntimeException.class)
  public void checkedClick_shouldThrowIfViewIsDisabled() throws Exception {
    view.setEnabled(false);
    shadowOf(view).checkedPerformClick();
  }

  /*
   * This test will throw an exception because the accessibility checks depend on the  Android
   * Support Library. If the support library is included at some point, a single test from
   * AccessibilityUtilTest could be moved here to make sure the accessibility checking is run.
   */
  @Test(expected = RuntimeException.class)
  @AccessibilityChecks
  public void checkedClick_withA11yChecksAnnotation_shouldThrow() throws Exception {
    shadowOf(view).checkedPerformClick();
  }

  @Test
  public void getBackground_shouldReturnNullIfNoBackgroundHasBeenSet() throws Exception {
    assertThat(view.getBackground()).isNull();
  }

  @Test
  public void shouldSetBackgroundColor() {
    int red = 0xffff0000;
    view.setBackgroundColor(red);
    ColorDrawable background = (ColorDrawable) view.getBackground();
    assertThat(background.getColor()).isEqualTo(red);
  }

  @Test
  public void shouldSetBackgroundResource() throws Exception {
    view.setBackgroundResource(R.drawable.an_image);
    assertThat(shadowOf((BitmapDrawable) view.getBackground()).getCreatedFromResId())
        .isEqualTo(R.drawable.an_image);
  }

  @Test
  public void shouldClearBackgroundResource() throws Exception {
    view.setBackgroundResource(R.drawable.an_image);
    view.setBackgroundResource(0);
    assertThat(view.getBackground()).isEqualTo(null);
  }

  @Test
  public void shouldRecordBackgroundColor() {
    int[] colors = {R.color.black, R.color.clear, R.color.white};

    for (int color : colors) {
      view.setBackgroundColor(color);
      ColorDrawable drawable = (ColorDrawable) view.getBackground();
      assertThat(drawable.getColor()).isEqualTo(color);
    }
  }

  @Test
  public void shouldRecordBackgroundDrawable() {
    Drawable drawable = new BitmapDrawable(BitmapFactory.decodeFile("some/fake/file"));
    view.setBackgroundDrawable(drawable);
    assertThat(view.getBackground()).isSameAs(drawable);
    assertThat(ShadowView.visualize(view)).isEqualTo("background:\nBitmap for file:some/fake/file");
  }

  @Test
  public void shouldPostActionsToTheMessageQueue() throws Exception {
    ShadowLooper.pauseMainLooper();

    TestRunnable runnable = new TestRunnable();
    view.post(runnable);
    assertFalse(runnable.wasRun);

    ShadowLooper.unPauseMainLooper();
    assertTrue(runnable.wasRun);
  }

  @Test
  public void shouldPostInvalidateDelayed() throws Exception {
    ShadowLooper.pauseMainLooper();

    view.postInvalidateDelayed(100);
    ShadowView shadowView = shadowOf(view);
    assertFalse(shadowView.wasInvalidated());

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    assertTrue(shadowView.wasInvalidated());
  }

  @Test
  public void shouldPostActionsToTheMessageQueueWithDelay() throws Exception {
    ShadowLooper.pauseMainLooper();

    TestRunnable runnable = new TestRunnable();
    view.postDelayed(runnable, 1);
    assertFalse(runnable.wasRun);

    Robolectric.getForegroundThreadScheduler().advanceBy(1);
    assertTrue(runnable.wasRun);
  }

  @Test
  public void shouldRemovePostedCallbacksFromMessageQueue() throws Exception {
    TestRunnable runnable = new TestRunnable();
    view.postDelayed(runnable, 1);

    view.removeCallbacks(runnable);

    Robolectric.getForegroundThreadScheduler().advanceBy(1);
    assertThat(runnable.wasRun).isFalse();
  }

  @Test
  public void shouldSupportAllConstructors() throws Exception {
    new View(context);
    new View(context, null);
    new View(context, null, 0);
  }

  @Test
  public void shouldRememberIsPressed() {
    view.setPressed(true);
    assertTrue(view.isPressed());
    view.setPressed(false);
    assertFalse(view.isPressed());
  }

  @Test
  public void shouldAddOnClickListenerFromAttribute() throws Exception {
    AttributeSet attrs = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.onClick, "clickMe")
        .build()
        ;

    view = new View(context, attrs);
    assertNotNull(shadowOf(view).getOnClickListener());
  }

  @Test
  public void shouldCallOnClickWithAttribute() throws Exception {
    MyActivity myActivity = buildActivity(MyActivity.class).create().get();

    AttributeSet attrs = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.onClick, "clickMe")
        .build();

    view = new View(myActivity, attrs);
    view.performClick();
    assertTrue("Should have been called", myActivity.called);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionWithBadMethodName() throws Exception {
    MyActivity myActivity = buildActivity(MyActivity.class).create().get();

    AttributeSet attrs = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.onClick, "clickYou")
        .build();

    view = new View(myActivity, attrs);
    view.performClick();
  }

  @Test
  public void shouldSetAnimation() throws Exception {
    Animation anim = new TestAnimation();
    view.setAnimation(anim);
    assertThat(view.getAnimation()).isSameAs(anim);
  }

  @Test
  public void shouldFindViewWithTag() {
    view.setTag("tagged");
    assertThat((View) view.findViewWithTag("tagged")).isSameAs(view);
  }

  @Test
  public void scrollTo_shouldStoreTheScrolledCoordinates() throws Exception {
    view.scrollTo(1, 2);
    assertThat(shadowOf(view).scrollToCoordinates).isEqualTo(new Point(1, 2));
  }

  @Test
  public void shouldScrollTo() throws Exception {
    view.scrollTo(7, 6);

    assertEquals(7, view.getScrollX());
    assertEquals(6, view.getScrollY());
  }

  @Test
  public void scrollBy_shouldStoreTheScrolledCoordinates() throws Exception {
    view.scrollTo(4, 5);
    view.scrollBy(10, 20);
    assertThat(shadowOf(view).scrollToCoordinates).isEqualTo(new Point(14, 25));

    assertThat(view.getScrollX()).isEqualTo(14);
    assertThat(view.getScrollY()).isEqualTo(25);
  }

  @Test
  public void shouldGetScrollXAndY() {
    assertEquals(0, view.getScrollX());
    assertEquals(0, view.getScrollY());
  }

  @Test
  public void getViewTreeObserver_shouldReturnTheSameObserverFromMultipleCalls() throws Exception {
    ViewTreeObserver observer = view.getViewTreeObserver();
    assertThat(observer).isInstanceOf(ViewTreeObserver.class);
    assertThat(view.getViewTreeObserver()).isSameAs(observer);
  }

  @Test
  public void dispatchTouchEvent_sendsMotionEventToOnTouchEvent() throws Exception {
    TouchableView touchableView = new TouchableView(context);
    MotionEvent event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0);
    touchableView.dispatchTouchEvent(event);
    assertThat(touchableView.event).isSameAs(event);
    view.dispatchTouchEvent(event);
    assertThat(shadowOf(view).getLastTouchEvent()).isSameAs(event);
  }

  @Test
  public void dispatchTouchEvent_listensToFalseFromListener() throws Exception {
    final AtomicBoolean called = new AtomicBoolean(false);
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        called.set(true); return false;
      }
    });
    MotionEvent event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0);
    view.dispatchTouchEvent(event);
    assertThat(shadowOf(view).getLastTouchEvent()).isSameAs(event);
    assertThat(called.get()).isTrue();
  }

  @Test
  public void test_nextFocusDownId() throws Exception {
    assertEquals(View.NO_ID, view.getNextFocusDownId());

    view.setNextFocusDownId(R.id.icon);
    assertEquals(R.id.icon, view.getNextFocusDownId());
  }

  @Test
  public void startAnimation() {
    TestView view = new TestView(buildActivity(Activity.class).create().get());
    AlphaAnimation animation = new AlphaAnimation(0, 1);

    Animation.AnimationListener listener = mock(Animation.AnimationListener.class);
    animation.setAnimationListener(listener);
    view.startAnimation(animation);

    verify(listener).onAnimationStart(animation);
    verify(listener).onAnimationEnd(animation);
  }

  @Test
  public void setAnimation() {
    TestView view = new TestView(buildActivity(Activity.class).create().get());
    AlphaAnimation animation = new AlphaAnimation(0, 1);

    Animation.AnimationListener listener = mock(Animation.AnimationListener.class);
    animation.setAnimationListener(listener);
    animation.setStartTime(1000);
    view.setAnimation(animation);

    verifyZeroInteractions(listener);

    Robolectric.getForegroundThreadScheduler().advanceToNextPostedRunnable();

    verify(listener).onAnimationStart(animation);
    verify(listener).onAnimationEnd(animation);
  }

  @Test
  public void setNullAnimation() {
    TestView view = new TestView(buildActivity(Activity.class).create().get());
    view.setAnimation(null);
    assertThat(view.getAnimation()).isNull();
  }

  @Test
  public void test_measuredDimension() {
    // View does not provide its own onMeasure implementation
    TestView view1 = new TestView(buildActivity(Activity.class).create().get());

    assertThat(view1.getHeight()).isEqualTo(0);
    assertThat(view1.getWidth()).isEqualTo(0);
    assertThat(view1.getMeasuredHeight()).isEqualTo(0);
    assertThat(view1.getMeasuredWidth()).isEqualTo(0);

    view1.measure(MeasureSpec.makeMeasureSpec(150, MeasureSpec.AT_MOST),
        MeasureSpec.makeMeasureSpec(300, MeasureSpec.AT_MOST));

    assertThat(view1.getHeight()).isEqualTo(0);
    assertThat(view1.getWidth()).isEqualTo(0);
    assertThat(view1.getMeasuredHeight()).isEqualTo(300);
    assertThat(view1.getMeasuredWidth()).isEqualTo(150);
  }

  @Test
  public void test_measuredDimensionCustomView() {
    // View provides its own onMeasure implementation
    TestView2 view2 = new TestView2(buildActivity(Activity.class).create().get(), 300, 100);

    assertThat(view2.getWidth()).isEqualTo(0);
    assertThat(view2.getHeight()).isEqualTo(0);
    assertThat(view2.getMeasuredWidth()).isEqualTo(0);
    assertThat(view2.getMeasuredHeight()).isEqualTo(0);

    view2.measure(MeasureSpec.makeMeasureSpec(200, MeasureSpec.AT_MOST),
    MeasureSpec.makeMeasureSpec(50, MeasureSpec.AT_MOST));

    assertThat(view2.getWidth()).isEqualTo(0);
    assertThat(view2.getHeight()).isEqualTo(0);
    assertThat(view2.getMeasuredWidth()).isEqualTo(300);
    assertThat(view2.getMeasuredHeight()).isEqualTo(100);
  }

  @Test
  public void shouldGetAndSetTranslations() throws Exception {
    view = new TestView(buildActivity(Activity.class).create().get());
    view.setTranslationX(8.9f);
    view.setTranslationY(4.6f);

    assertThat(view.getTranslationX()).isEqualTo(8.9f);
    assertThat(view.getTranslationY()).isEqualTo(4.6f);
  }

  @Test
  public void shouldGetAndSetAlpha() throws Exception {
    view = new TestView(buildActivity(Activity.class).create().get());
    view.setAlpha(9.1f);

    assertThat(view.getAlpha()).isEqualTo(9.1f);
  }

  @Test
  public void itKnowsIfTheViewIsShown() {
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class)); // a view is only considered visible if it is added to a view root
    view.setVisibility(View.VISIBLE);
    assertThat(view.isShown()).isTrue();
  }

  @Test
  public void itKnowsIfTheViewIsNotShown() {
    view.setVisibility(View.GONE);
    assertThat(view.isShown()).isFalse();

    view.setVisibility(View.INVISIBLE);
    assertThat(view.isShown()).isFalse();
  }

  @Test
  public void shouldTrackRequestLayoutCalls() throws Exception {
    assertThat(shadowOf(view).didRequestLayout()).isFalse();
    view.requestLayout();
    assertThat(shadowOf(view).didRequestLayout()).isTrue();
    shadowOf(view).setDidRequestLayout(false);
    assertThat(shadowOf(view).didRequestLayout()).isFalse();
  }

  @Test
  public void shouldClickAndNotClick() throws Exception {
    assertThat(view.isClickable()).isFalse();
    view.setClickable(true);
    assertThat(view.isClickable()).isTrue();
    view.setClickable(false);
    assertThat(view.isClickable()).isFalse();
    view.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        ;
      }
    });
    assertThat(view.isClickable()).isTrue();
  }

  @Test
  public void shouldLongClickAndNotLongClick() throws Exception {
    assertThat(view.isLongClickable()).isFalse();
    view.setLongClickable(true);
    assertThat(view.isLongClickable()).isTrue();
    view.setLongClickable(false);
    assertThat(view.isLongClickable()).isFalse();
    view.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        return false;
      }
    });
    assertThat(view.isLongClickable()).isTrue();
  }

  @Test
  public void rotationX() {
    view.setRotationX(10f);
    assertThat(view.getRotationX()).isEqualTo(10f);
  }

  @Test
  public void rotationY() {
    view.setRotationY(20f);
    assertThat(view.getRotationY()).isEqualTo(20f);
  }

  @Test
  public void rotation() {
    view.setRotation(30f);
    assertThat(view.getRotation()).isEqualTo(30f);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void cameraDistance() {
    view.setCameraDistance(100f);
    assertThat(view.getCameraDistance()).isEqualTo(100f);
  }

  @Test
  public void scaleX() {
    assertThat(view.getScaleX()).isEqualTo(1f);
    view.setScaleX(0.5f);
    assertThat(view.getScaleX()).isEqualTo(0.5f);
  }

  @Test
  public void scaleY() {
    assertThat(view.getScaleY()).isEqualTo(1f);
    view.setScaleY(0.5f);
    assertThat(view.getScaleY()).isEqualTo(0.5f);
  }

  @Test
  public void pivotX() {
    view.setPivotX(10f);
    assertThat(view.getPivotX()).isEqualTo(10f);
  }

  @Test
  public void pivotY() {
    view.setPivotY(10f);
    assertThat(view.getPivotY()).isEqualTo(10f);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void elevation() {
    view.setElevation(10f);
    assertThat(view.getElevation()).isEqualTo(10f);
  }

  @Test
  public void translationX() {
    view.setTranslationX(10f);
    assertThat(view.getTranslationX()).isEqualTo(10f);
  }

  @Test
  public void translationY() {
    view.setTranslationY(10f);
    assertThat(view.getTranslationY()).isEqualTo(10f);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void translationZ() {
    view.setTranslationZ(10f);
    assertThat(view.getTranslationZ()).isEqualTo(10f);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void clipToOutline() {
    view.setClipToOutline(true);
    assertThat(view.getClipToOutline()).isTrue();
  }

  @Test
  public void performHapticFeedback_shouldSetLastPerformedHapticFeedback() throws Exception {
    assertThat(shadowOf(view).lastHapticFeedbackPerformed()).isEqualTo(-1);
    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    assertThat(shadowOf(view).lastHapticFeedbackPerformed()).isEqualTo(HapticFeedbackConstants.LONG_PRESS);
  }

  @Test
  public void canAssertThatSuperDotOnLayoutWasCalledFromViewSubclasses() throws Exception {
    TestView2 view = new TestView2(setupActivity(Activity.class), 1111, 1112);
    assertThat(shadowOf(view).onLayoutWasCalled()).isFalse();
    view.onLayout(true, 1, 2, 3, 4);
    assertThat(shadowOf(view).onLayoutWasCalled()).isTrue();
  }

  @Test
  public void setScrolls_canBeAskedFor() throws Exception {
    view.setScrollX(234);
    view.setScrollY(544);
    assertThat(view.getScrollX()).isEqualTo(234);
    assertThat(view.getScrollY()).isEqualTo(544);
  }

  @Test
  public void setScrolls_firesOnScrollChanged() throws Exception {
    TestView testView = new TestView(buildActivity(Activity.class).create().get());
    testView.setScrollX(122);
    testView.setScrollY(150);
    testView.setScrollX(453);
    assertThat(testView.oldl).isEqualTo(122);
    testView.setScrollY(54);
    assertThat(testView.l).isEqualTo(453);
    assertThat(testView.t).isEqualTo(54);
    assertThat(testView.oldt).isEqualTo(150);
  }

  @Test
  public void layerType() throws Exception {
    assertThat(view.getLayerType()).isEqualTo(View.LAYER_TYPE_NONE);
    view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    assertThat(view.getLayerType()).isEqualTo(View.LAYER_TYPE_SOFTWARE);
  }

  private static class TestAnimation extends Animation {
  }

  private static class TouchableView extends View {
    MotionEvent event;

    public TouchableView(Context context) {
      super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      this.event = event;
      return false;
    }
  }

  public static class TestView extends View {
    boolean onAnimationEndWasCalled;
    private int l;
    private int t;
    private int oldl;
    private int oldt;

    public TestView(Context context) {
      super(context);
    }

    @Override
    protected void onAnimationEnd() {
      super.onAnimationEnd();
      onAnimationEndWasCalled = true;
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
      this.l = l;
      this.t = t;
      this.oldl = oldl;
      this.oldt = oldt;
    }
  }

  private static class TestView2 extends View {

    private int minWidth;
    private int minHeight;

    public TestView2(Context context, int minWidth, int minHeight) {
      super(context);
      this.minWidth = minWidth;
      this.minHeight = minHeight;
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
      super.onLayout(changed, l, t, r, b);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      setMeasuredDimension(minWidth, minHeight);
    }
  }

  @Test
  public void shouldCallOnAttachedToAndDetachedFromWindow() throws Exception {
    MyView parent = new MyView("parent", transcript);
    parent.addView(new MyView("child", transcript));
    assertThat(transcript).isEmpty();

    Activity activity = Robolectric.buildActivity(ContentViewActivity.class).create().get();
    activity.getWindowManager().addView(parent, new WindowManager.LayoutParams(100, 100));
    assertThat(transcript).containsExactly("parent attached", "child attached");
    transcript.clear();

    parent.addView(new MyView("another child", transcript));
    assertThat(transcript).containsExactly("another child attached");
    transcript.clear();

    MyView temporaryChild = new MyView("temporary child", transcript);
    parent.addView(temporaryChild);
    assertThat(transcript).containsExactly("temporary child attached");
    transcript.clear();
    assertTrue(shadowOf(temporaryChild).isAttachedToWindow());

    parent.removeView(temporaryChild);
    assertThat(transcript).containsExactly("temporary child detached");
    assertFalse(shadowOf(temporaryChild).isAttachedToWindow());
  }

  @Test @Config(minSdk = JELLY_BEAN_MR2)
  public void getWindowId_shouldReturnValidObjectWhenAttached() throws Exception {
    MyView parent = new MyView("parent", transcript);
    MyView child = new MyView("child", transcript);
    parent.addView(child);

    assertThat(parent.getWindowId()).isNull();
    assertThat(child.getWindowId()).isNull();

    Activity activity = Robolectric.buildActivity(ContentViewActivity.class).create().get();
    activity.getWindowManager().addView(parent, new WindowManager.LayoutParams(100, 100));

    WindowId windowId = parent.getWindowId();
    assertThat(windowId).isNotNull();
    assertThat(child.getWindowId()).isSameAs(windowId);
    assertThat(child.getWindowId()).isEqualTo(windowId); // equals must work!

    MyView anotherChild = new MyView("another child", transcript);
    parent.addView(anotherChild);
    assertThat(anotherChild.getWindowId()).isEqualTo(windowId);

    parent.removeView(anotherChild);
    assertThat(anotherChild.getWindowId()).isNull();
  }

  // todo looks like this is flaky...
  @Test
  public void removeAllViews_shouldCallOnAttachedToAndDetachedFromWindow() throws Exception {
    MyView parent = new MyView("parent", transcript);
    Activity activity = Robolectric.buildActivity(ContentViewActivity.class).create().get();
    activity.getWindowManager().addView(parent, new WindowManager.LayoutParams(100, 100));

    parent.addView(new MyView("child", transcript));
    parent.addView(new MyView("another child", transcript));
    ShadowLooper.runUiThreadTasks();
    transcript.clear();
    parent.removeAllViews();
    ShadowLooper.runUiThreadTasks();
    assertThat(transcript).containsExactly("another child detached", "child detached");
  }

  @Test
  public void capturesOnSystemUiVisibilityChangeListener() throws Exception {
    TestView testView = new TestView(buildActivity(Activity.class).create().get());
    View.OnSystemUiVisibilityChangeListener changeListener = new View.OnSystemUiVisibilityChangeListener() {
      @Override
      public void onSystemUiVisibilityChange(int i) { }
    };
    testView.setOnSystemUiVisibilityChangeListener(changeListener);

    assertThat(changeListener).isEqualTo(shadowOf(testView).getOnSystemUiVisibilityChangeListener());
  }

  @Test
  public void capturesOnCreateContextMenuListener() throws Exception {
    TestView testView = new TestView(buildActivity(Activity.class).create().get());
    assertThat(shadowOf(testView).getOnCreateContextMenuListener()).isNull();

    View.OnCreateContextMenuListener createListener = new View.OnCreateContextMenuListener() {
      @Override
      public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {}
    };

    testView.setOnCreateContextMenuListener(createListener);
    assertThat(shadowOf(testView).getOnCreateContextMenuListener()).isEqualTo(createListener);

    testView.setOnCreateContextMenuListener(null);
    assertThat(shadowOf(testView).getOnCreateContextMenuListener()).isNull();
  }

  @Test
  public void setsGlobalVisibleRect() {
    Rect globalVisibleRect = new Rect();
    shadowOf(view).setGlobalVisibleRect(new Rect());
    assertThat(view.getGlobalVisibleRect(globalVisibleRect))
        .isFalse();
    assertThat(globalVisibleRect.isEmpty())
        .isTrue();
    assertThat(view.getGlobalVisibleRect(globalVisibleRect, new Point(1, 1)))
        .isFalse();
    assertThat(globalVisibleRect.isEmpty())
        .isTrue();

    shadowOf(view).setGlobalVisibleRect(new Rect(1, 2, 3, 4));
    assertThat(view.getGlobalVisibleRect(globalVisibleRect))
        .isTrue();
    assertThat(globalVisibleRect)
        .isEqualTo(new Rect(1, 2, 3, 4));
    assertThat(view.getGlobalVisibleRect(globalVisibleRect, new Point(1, 1)))
        .isTrue();
    assertThat(globalVisibleRect)
        .isEqualTo(new Rect(0, 1, 2, 3));
  }

  @Test
  public void usesDefaultGlobalVisibleRect() {
    final ActivityController<Activity> activityController = Robolectric.buildActivity(Activity.class);
    final Activity activity = activityController.get();
    activity.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
    activityController.setup();

    Rect globalVisibleRect = new Rect();
    assertThat(view.getGlobalVisibleRect(globalVisibleRect))
        .isTrue();
    assertThat(globalVisibleRect)
        .isEqualTo(new Rect(0, 25,
            DeviceConfig.DEFAULT_SCREEN_SIZE.width, DeviceConfig.DEFAULT_SCREEN_SIZE.height));
  }

  public static class MyActivity extends Activity {
    public boolean called;

    @SuppressWarnings("UnusedDeclaration")
    public void clickMe(View view) {
      called = true;
    }
  }

  public static class MyView extends LinearLayout {
    private String name;
    private List<String> transcript;

    public MyView(String name, List<String> transcript) {
      super(ApplicationProvider.getApplicationContext());
      this.name = name;
      this.transcript = transcript;
    }

    @Override protected void onAttachedToWindow() {
      transcript.add(name + " attached");
      super.onAttachedToWindow();
    }

    @Override protected void onDetachedFromWindow() {
      transcript.add(name + " detached");
      super.onDetachedFromWindow();
    }
  }

  private static class ContentViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(new FrameLayout(this));
    }
  }
}
