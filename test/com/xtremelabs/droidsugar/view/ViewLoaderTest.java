package com.xtremelabs.droidsugar.view;

import android.test.mock.*;
import android.view.*;
import android.widget.*;
import com.xtremelabs.droidsugar.*;
import com.xtremelabs.droidsugar.util.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;

import static android.test.MoreAsserts.*;
import static org.junit.Assert.*;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ViewLoaderTest {
    private ViewLoader viewLoader;
    private MockContext context;

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(View.class, FakeView.class);
        DroidSugarAndroidTestRunner.addProxy(ViewGroup.class, FakeViewGroup.class);

        viewLoader = new ViewLoader(R.class, new File("test/res/layout"));

        context = new MockContext();
    }

    @Test
    public void testCreatesCorrectClasses() throws Exception {
        ViewGroup view = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        TestUtil.assertInstanceOf(LinearLayout.class, view);

        assertSame(context, view.getContext());
    }

    @Test
    public void testAddsChildren() throws Exception {
        ViewGroup view = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        assertNotEqual(0, view.getChildCount());

        assertSame(context, view.getChildAt(0).getContext());
    }

    @Test
    public void testFindsChildrenById() throws Exception {
        ViewGroup view = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        TestUtil.assertInstanceOf(TextView.class, view.findViewById(R.id.media_title));
    }

}
