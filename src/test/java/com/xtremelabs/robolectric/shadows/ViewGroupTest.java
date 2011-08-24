package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Application;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.ResourceLoader;

@RunWith(WithTestDefaultsRunner.class)
public class ViewGroupTest {
    private String defaultLineSeparator;
    private ViewGroup root;
    private View child1;
    private View child2;
    private ViewGroup child3;
    private View child3a;
    private View child3b;

    @Before public void setUp() throws Exception {
        Application context = new Application();
        ShadowApplication.bind(context, new ResourceLoader(10, R.class, null, null));

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
    	assertThat( root.getLayoutAnimationListener(), nullValue() );
    	root.setLayoutAnimationListener( new AnimationListener() {
    		 @Override
    		 public void onAnimationEnd( Animation a ) { }
    		 @Override
    		 public void onAnimationRepeat(Animation a ) { }
    		 @Override
    		 public void onAnimationStart(Animation a ) { }
    	 });
    	 
    	assertThat( root.getLayoutAnimationListener(), notNullValue() ); 
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
    	 String tag1 = "tag1";
    	 String tag2 = "tag2";
    	 String tag3 = "tag3";
    	 child1.setTag(tag1);
    	 child2.setTag(tag2);
    	 child3.setTag(tag3);
         root.addView(child1);
         root.addView(child2);
         root.addView(child3, 1);
         assertThat(root.findViewWithTag("tag1"), sameInstance(child1));
         assertThat(root.findViewWithTag("tag2"), sameInstance((View) child2));
         assertThat((ViewGroup)root.findViewWithTag("tag3"), sameInstance(child3));
    }
    
    @Test
    public void shouldNotfindViewWithTagReturnNull() {
    	 root.removeAllViews();
    	 String tag1 = "tag1";
    	 String tag2 = "tag2";
    	 String tag3 = "tag3";
    	 child1.setTag(tag1);
    	 child2.setTag(tag2);
    	 child3.setTag(tag3);
         root.addView(child1);
         root.addView(child2);
         root.addView(child3, 1);
         assertThat(root.findViewWithTag("tag21"), equalTo(null));
         assertThat((ViewGroup)root.findViewWithTag("tag23"), equalTo(null));
    }
    
    @Test
    public void shouldfindViewWithTagFromCorrectViewGroup() {
    	 root.removeAllViews();
    	 String tag1 = "tag1";
    	 String tag2 = "tag2";
    	 String tag3 = "tag3";
    	 child1.setTag(tag1);
    	 child2.setTag(tag2);
    	 child3.setTag(tag3);
         root.addView(child1);
         root.addView(child2);
         root.addView(child3);
         
         child3a.setTag(tag1);
         child3b.setTag(tag2);
         
         //can find views by tag from root
         assertThat(root.findViewWithTag("tag1"), sameInstance(child1));
         assertThat(root.findViewWithTag("tag2"), sameInstance((View) child2));
         assertThat((ViewGroup)root.findViewWithTag("tag3"), sameInstance(child3));
         
         //can find views by tag from child3
         assertThat(child3.findViewWithTag("tag1"), sameInstance(child3a));
         assertThat(child3.findViewWithTag("tag2"), sameInstance(child3b));
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
}
