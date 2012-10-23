package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.tester.android.view.TestWindow;
import com.xtremelabs.robolectric.util.TestAnimationListener;
import com.xtremelabs.robolectric.util.TestOnClickListener;
import com.xtremelabs.robolectric.util.TestOnLongClickListener;
import com.xtremelabs.robolectric.util.TestRunnable;
import com.xtremelabs.robolectric.util.Transcript;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.application;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.Robolectric.visualize;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ViewTest {
    private View view;
    private Transcript transcript;

    @Before
    public void setUp() throws Exception {
        transcript = new Transcript();
        view = new View(new Activity());
    }

    @Test
    public void testHasEmptyLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        assertThat(layoutParams, notNullValue());
    }

    @Test
    public void layout_shouldAffectWidthAndHeight() throws Exception {
        assertThat(view.getWidth(), equalTo(0));
        assertThat(view.getHeight(), equalTo(0));

        view.layout(100, 200, 303, 404);
        assertThat(view.getWidth(), equalTo(303 - 100));
        assertThat(view.getHeight(), equalTo(404 - 200));
    }

    @Test
    public void measuredDimensions() throws Exception {
        View view1 = new View(null) {
            {
                setMeasuredDimension(123, 456);
            }
        };
        assertThat(view1.getMeasuredWidth(), equalTo(123));
        assertThat(view1.getMeasuredHeight(), equalTo(456));
    }

    @Test
    public void layout_shouldCallOnLayoutOnlyIfChanged() throws Exception {
        View view1 = new View(null) {
            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                transcript.add("onLayout " + changed + " " + left + " " + top + " " + right + " " + bottom);
            }

            @Override
            public void invalidate() {
                transcript.add("invalidate");
            }
        };
        view1.layout(0, 0, 0, 0);
        transcript.assertNoEventsSoFar();
        view1.layout(1, 2, 3, 4);
        transcript.assertEventsSoFar("invalidate", "onLayout true 1 2 3 4");
        view1.layout(1, 2, 3, 4);
        transcript.assertNoEventsSoFar();
    }

    @Test
    public void shouldFocus() throws Exception {
        final Transcript transcript = new Transcript();

        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                transcript.add(hasFocus ? "Gained focus" : "Lost focus");
            }
        });

        assertFalse(view.isFocused());
        assertFalse(view.hasFocus());
        transcript.assertNoEventsSoFar();

        view.requestFocus();
        assertTrue(view.isFocused());
        assertTrue(view.hasFocus());
        transcript.assertEventsSoFar("Gained focus");

        view.clearFocus();
        assertFalse(view.isFocused());
        assertFalse(view.hasFocus());
        transcript.assertEventsSoFar("Lost focus");
    }

    @Test
    public void shouldNotBeFocusableByDefault() throws Exception {
        assertFalse(view.isFocusable());

        view.setFocusable(true);
        assertTrue(view.isFocusable());
    }

    @Test
    public void shouldKnowIfThisOrAncestorsAreVisible() throws Exception {
        assertTrue(shadowOf(view).derivedIsVisible());

        ViewGroup grandParent = new LinearLayout(null);
        ViewGroup parent = new LinearLayout(null);
        grandParent.addView(parent);
        parent.addView(view);

        grandParent.setVisibility(View.GONE);

        assertFalse(shadowOf(view).derivedIsVisible());
    }

    @Test
    public void shouldInflateMergeRootedLayoutAndNotCreateReferentialLoops() throws Exception {
        LinearLayout root = new LinearLayout(null);
        root.inflate(new Activity(), R.layout.inner_merge, root);
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            assertNotSame(root, child);
        }
    }

    @Test
    public void performLongClick_shouldClickOnView() throws Exception {
        TestOnLongClickListener clickListener = new TestOnLongClickListener();
        view.setOnLongClickListener(clickListener);
        shadowOf(view).performLongClick();

        assertTrue(clickListener.clicked);
    }


    @Test
    public void checkedClick_shouldClickOnView() throws Exception {
        TestOnClickListener clickListener = new TestOnClickListener();
        view.setOnClickListener(clickListener);
        shadowOf(view).checkedPerformClick();

        assertTrue(clickListener.clicked);
    }

    @Test(expected = RuntimeException.class)
    public void checkedClick_shouldThrowIfViewIsNotVisible() throws Exception {
        ViewGroup grandParent = new LinearLayout(null);
        ViewGroup parent = new LinearLayout(null);
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

    @Test
    public void getBackground_shouldReturnNullIfNoBackgroundHasBeenSet() throws Exception {
        assertThat(view.getBackground(), nullValue());
    }

    @Test
    public void shouldSetBackgroundColor() {
        view.setBackgroundColor(R.color.android_red);
        int intColor = view.getResources().getColor(R.color.android_red);

        assertThat((ColorDrawable) view.getBackground(), equalTo(new ColorDrawable(intColor)));
    }

    @Test
    public void shouldSetBackgroundResource() throws Exception {
        view.setBackgroundResource(R.drawable.an_image);
        assertThat(view.getBackground(), equalTo(view.getResources().getDrawable(R.drawable.an_image)));
    }

    @Test
    public void shouldRecordBackgroundColor() {
        int[] colors = {0, 1, 727};

        for (int color : colors) {
            view.setBackgroundColor(color);
            assertThat(shadowOf(view).getBackgroundColor(), equalTo(color));
        }
    }

    @Test
    public void shouldRecordBackgroundDrawable() {
        Drawable drawable = new BitmapDrawable(BitmapFactory.decodeFile("some/fake/file"));
        view.setBackgroundDrawable(drawable);
        assertThat(view.getBackground(), sameInstance(drawable));
        assertThat(visualize(view), equalTo("background:\nBitmap for file:some/fake/file"));
    }

    @Test
    public void shouldPostActionsToTheMessageQueue() throws Exception {
        Robolectric.pauseMainLooper();

        TestRunnable runnable = new TestRunnable();
        view.post(runnable);
        assertFalse(runnable.wasRun);

        Robolectric.unPauseMainLooper();
        assertTrue(runnable.wasRun);
    }

    @Test
    public void shouldPostInvalidateDelayed() throws Exception {
        Robolectric.pauseMainLooper();

        view.postInvalidateDelayed(100);
        ShadowView shadowView = shadowOf(view);
        assertFalse(shadowView.wasInvalidated());

        Robolectric.unPauseMainLooper();
        assertTrue(shadowView.wasInvalidated());
    }

    @Test
    public void shouldPostActionsToTheMessageQueueWithDelay() throws Exception {
        Robolectric.pauseMainLooper();

        TestRunnable runnable = new TestRunnable();
        view.postDelayed(runnable, 1);
        assertFalse(runnable.wasRun);

        Robolectric.getUiThreadScheduler().advanceBy(1);
        assertTrue(runnable.wasRun);
    }

    @Test
    public void shouldSupportAllConstructors() throws Exception {
        new View(null);
        new View(null, null);
        new View(null, null, 0);
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
      TestAttributeSet attrs = new TestAttributeSet();
      attrs.put("android:onClick", "clickMe");

      view = new View(null, attrs);
      assertNotNull(shadowOf(view).getOnClickListener());
    }

    @Test
    public void shouldCallOnClickWithAttribute() throws Exception {
      final AtomicBoolean called = new AtomicBoolean(false);
      Activity context = new Activity() {
        public void clickMe(View view) {
          called.set(true);
        }
      };
      TestAttributeSet attrs = new TestAttributeSet();
      attrs.put("android:onClick", "clickMe");

      view = new View(context, attrs);
      view.performClick();
      assertTrue("Should have been called", called.get());
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWithBadMethodName() throws Exception {
      final AtomicBoolean called = new AtomicBoolean(false);
      Activity context = new Activity() {
        public void clickMe(View view) {
          called.set(true);
        }
      };
      TestAttributeSet attrs = new TestAttributeSet();
      attrs.put("android:onClick", "clickYou");

      view = new View(context, attrs);
      view.performClick();
    }

    @Test
    public void shouldSetAnimation() throws Exception {
        Animation anim = new TestAnimation();
        view.setAnimation(anim);
        assertThat(view.getAnimation(), sameInstance(anim));
    }

    @Test
    public void shouldStartAndClearAnimation() throws Exception {
        Animation anim = new TestAnimation();
        TestAnimationListener listener = new TestAnimationListener();
        anim.setAnimationListener(listener);
        assertThat(listener.wasStartCalled, equalTo(false));
        assertThat(listener.wasRepeatCalled, equalTo(false));
        assertThat(listener.wasEndCalled, equalTo(false));
        view.startAnimation(anim);
        assertThat(listener.wasStartCalled, equalTo(true));
        assertThat(listener.wasRepeatCalled, equalTo(false));
        assertThat(listener.wasEndCalled, equalTo(false));
        view.clearAnimation();
        assertThat(listener.wasStartCalled, equalTo(true));
        assertThat(listener.wasRepeatCalled, equalTo(false));
        assertThat(listener.wasEndCalled, equalTo(true));
    }

    private static class TestAnimation extends Animation {
    }

    @Test
    public void shouldfindViewWithTag() {
    	String tagged = "tagged";
    	String tagged2 = "tagged";
    	view.setTag(tagged);
    	assertThat(view.findViewWithTag(tagged2),sameInstance(view));
    }

    @Test
    public void scrollTo_shouldStoreTheScrolledCoordinates() throws Exception {
        view.scrollTo(1, 2);
        assertThat(shadowOf(view).scrollToCoordinates, equalTo(new Point(1, 2)));
    }

    @Test public void shouldCallOnAttachedToAndDetachedFromWindow() throws Exception {
        MyView parent = new MyView("parent");
        parent.addView(new MyView("child"));
        transcript.assertNoEventsSoFar();

        TestWindow window = new TestWindow(application);
        window.setContentView(parent);
        transcript.assertEventsSoFar("parent attached", "child attached");

        parent.addView(new MyView("another child"));
        transcript.assertEventsSoFar("another child attached");

        MyView temporaryChild = new MyView("temporary child");
        parent.addView(temporaryChild);
        transcript.assertEventsSoFar("temporary child attached");
        assertTrue(shadowOf(temporaryChild).isAttachedToWindow());

        parent.removeView(temporaryChild);
        transcript.assertEventsSoFar("temporary child detached");
        assertFalse(shadowOf(temporaryChild).isAttachedToWindow());

        window.setContentView(null);
        transcript.assertEventsSoFar("parent detached", "child detached", "another child detached");
    }

    @Test public void removeAllViews_shouldCallOnAttachedToAndDetachedFromWindow() throws Exception {
        MyView parent = new MyView("parent");
        parent.addView(new MyView("child"));
        parent.addView(new MyView("another child"));
        new TestWindow(application).setContentView(parent);
        transcript.clear();
        parent.removeAllViews();
        transcript.assertEventsSoFar("child detached", "another child detached");
    }

    private class MyView extends LinearLayout {
        private String name;

        public MyView(String name) {
            super(Robolectric.application);
            this.name = name;
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
}
