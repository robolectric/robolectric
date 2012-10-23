package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.xtremelabs.robolectric.Robolectric.DEFAULT_SDK_VERSION;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ViewGroupTest {
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
        context = new Application();
        ShadowApplication.bind(context, new ResourceLoader(DEFAULT_SDK_VERSION, R.class, null, null));

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
    public void tearDown() throws Exception {
        System.setProperty("line.separator", defaultLineSeparator);
    }

    @Test
    public void testLayoutAnimationListener() {
        assertThat(root.getLayoutAnimationListener(), nullValue());

        AnimationListener animationListener = new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation a) { }

            @Override
            public void onAnimationRepeat(Animation a) { }

            @Override
            public void onAnimationStart(Animation a) { }
        };
        root.setLayoutAnimationListener(animationListener);

        assertThat(root.getLayoutAnimationListener(), sameInstance(animationListener));
    }

    @Test
    public void testRemoveChildAt() throws Exception {
        root.removeViewAt(1);

        assertThat(root.getChildCount(), equalTo(2));
        assertThat(root.getChildAt(0), sameInstance(child1));
        assertThat(root.getChildAt(1), sameInstance((View) child3));

        assertThat(child2.getParent(), nullValue());
    }

    @Test
    public void testAddViewAt() throws Exception {
        root.removeAllViews();
        root.addView(child1);
        root.addView(child2);
        root.addView(child3, 1);
        assertThat(root.getChildAt(0), sameInstance(child1));
        assertThat(root.getChildAt(1), sameInstance((View) child3));
        assertThat(root.getChildAt(2), sameInstance(child2));
    }

    @Test
    public void shouldfindViewWithTag() {
        root.removeAllViews();
        child1.setTag("tag1");
        child2.setTag("tag2");
        child3.setTag("tag3");
        root.addView(child1);
        root.addView(child2);
        root.addView(child3, 1);
        assertThat(root.findViewWithTag("tag1"), sameInstance(child1));
        assertThat(root.findViewWithTag("tag2"), sameInstance((View) child2));
        assertThat((ViewGroup) root.findViewWithTag("tag3"), sameInstance(child3));
    }

    @Test
    public void shouldNotfindViewWithTagReturnNull() {
        root.removeAllViews();
        child1.setTag("tag1");
        child2.setTag("tag2");
        child3.setTag("tag3");
        root.addView(child1);
        root.addView(child2);
        root.addView(child3, 1);
        assertThat(root.findViewWithTag("tag21"), equalTo(null));
        assertThat((ViewGroup) root.findViewWithTag("tag23"), equalTo(null));
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

        //can find views by tag from root
        assertThat(root.findViewWithTag("tag1"), sameInstance(child1));
        assertThat(root.findViewWithTag("tag2"), sameInstance((View) child2));
        assertThat((ViewGroup) root.findViewWithTag("tag3"), sameInstance(child3));

        //can find views by tag from child3
        assertThat(child3.findViewWithTag("tag1"), sameInstance(child3a));
        assertThat(child3.findViewWithTag("tag2"), sameInstance(child3b));
    }

    @Test
    public void shouldFindViewWithTag_whenViewGroupOverridesGetTag() throws Exception {
        ViewGroup viewGroup = new LinearLayout(Robolectric.application) {
            @Override
            public Object getTag() {
                return "blarg";
            }
        };
        assertThat((ViewGroup) viewGroup.findViewWithTag("blarg"), sameInstance(viewGroup));
    }

    @Test
    public void hasFocus_shouldReturnTrueIfAnyChildHasFocus() throws Exception {
        assertFalse(root.hasFocus());

        child1.requestFocus();
        assertTrue(root.hasFocus());

        child1.clearFocus();
        assertFalse(root.hasFocus());

        child3b.requestFocus();
        assertTrue(root.hasFocus());

        child3b.clearFocus();
        assertFalse(root.hasFocus());

        root.requestFocus();
        assertTrue(root.hasFocus());
    }

    @Test
    public void clearFocus_shouldRecursivelyClearTheFocusOfAllChildren() throws Exception {
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
    public void dump_shouldDumpStructure() throws Exception {
        child3.setId(R.id.snippet_text);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        shadowOf(root).dump(new PrintStream(out), 0);
        assertEquals("<FrameLayout>\n" +
                "  <View/>\n" +
                "  <View/>\n" +
                "  <FrameLayout id=\"id/snippet_text\">\n" +
                "    <View/>\n" +
                "    <View/>\n" +
                "  </FrameLayout>\n" +
                "</FrameLayout>\n", out.toString());
    }

    @Test
    public void addViewWithLayoutParams_shouldStoreLayoutParams() throws Exception {
        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView child1 = new ImageView(Robolectric.application);
        ImageView child2 = new ImageView(Robolectric.application);
        root.addView(child1, layoutParams1);
        root.addView(child2, 1, layoutParams2);
        assertSame(layoutParams1, child1.getLayoutParams());
        assertSame(layoutParams2, child2.getLayoutParams());
    }

    @Test
    public void shouldReturnIsVisibleWhenScrolled() throws Exception {
        View childView = new View(new Activity());
        ViewGroup rootView = new FrameLayout(new Activity());

        Rect r = new Rect(0, 0, 100, 100);
        childView.layout(0, 0, 100, 100);
        rootView.layout(0, 0, 100, 100);
        rootView.addView(childView);

        assertThat(rootView.getChildVisibleRect(childView, r, null), equalTo(true));

        rootView.scrollTo(50, 0);
        assertThat(rootView.getChildVisibleRect(childView, r, null), equalTo(true));
        assertThat(r, equalTo(new Rect(0, 0, 100, 100)));

        rootView.scrollTo(100, 0);
        assertThat(rootView.getChildVisibleRect(childView, r, null), equalTo(false));
        assertThat(r, equalTo(new Rect(0, 0, 100, 100)));
    }

    @Test
    public void shouldModifyOffsetWhenScrolled() throws Exception {
        View childView = new View(new Activity());
        ViewGroup rootView = new FrameLayout(new Activity());

        Rect r = new Rect(0, 0, 100, 100);
        childView.layout(0, 0, 100, 100);
        rootView.layout(0, 0, 100, 100);
        rootView.addView(childView);

        Point offset = new Point(); // 0,0

        rootView.scrollTo(50, 0);
        assertThat(rootView.getChildVisibleRect(childView, r, offset), equalTo(true));
        assertThat(offset, equalTo(new Point(-50, 0)));

        rootView.scrollTo(100, 0);
        offset = new Point(); // 0,0
        assertThat(rootView.getChildVisibleRect(childView, r, offset), equalTo(false));
        assertThat(offset, equalTo(new Point(-100, 0)));
    }

    @Test
    public void shouldModifyRectWhenScrolled() throws Exception {
        View childView = new View(new Activity());
        ViewGroup rootView = new FrameLayout(new Activity());
        ViewGroup parentView = new FrameLayout(new Activity());

        Rect r = new Rect(0, 0, 100, 100);
        childView.layout(0, 0, 100, 100);
        rootView.layout(0, 0, 100, 100);
        parentView.layout(0, 0, 100, 100);
        parentView.addView(childView);
        rootView.addView(parentView);

        assertThat(rootView.getChildVisibleRect(childView, r, null), equalTo(true));

        parentView.scrollTo(50, 0);
        assertThat(parentView.getChildVisibleRect(childView, r, null), equalTo(true));
        assertThat(r, equalTo(new Rect(-50, 0, 50, 100)));

        r = new Rect(0, 0, 100, 100);
        parentView.scrollTo(100, 0);
        assertThat(parentView.getChildVisibleRect(childView, r, null), equalTo(false));
        assertThat(r, equalTo(new Rect(0, 0, 100, 100)));
    }

    @Test
    public void shouldModifyRectWhenBothAncestorsAreScrolled() throws Exception {
        View childView = new View(new Activity());
        ViewGroup rootView = new FrameLayout(new Activity());
        ViewGroup parentXView = new FrameLayout(new Activity());
        ViewGroup parentYView = new FrameLayout(new Activity());

        Rect r = new Rect(0, 0, 100, 100);
        childView.layout(0, 0, 100, 100);
        rootView.layout(0, 0, 100, 100);
        parentXView.layout(0, 0, 100, 100);
        parentYView.layout(0, 0, 100, 100);

        parentXView.addView(childView);
        parentYView.addView(parentXView);
        rootView.addView(parentYView);

        parentXView.scrollTo(50, 0);
        parentYView.scrollTo(0, 50);
        rootView.scrollTo(0, 100);
        assertThat(parentXView.getChildVisibleRect(childView, r, null), equalTo(false));
        assertThat(r, equalTo(new Rect(-50, -50, 50, 50)));
    }
}
