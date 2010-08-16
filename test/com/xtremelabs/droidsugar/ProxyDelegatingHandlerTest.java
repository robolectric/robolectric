package com.xtremelabs.droidsugar;

import android.content.Context;
import android.test.mock.MockContext;
import android.view.View;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ProxyDelegatingHandlerTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new MockContext();
    }

    @Test
    public void testConstructorInvocation_WithDefaultConstructorAndNoConstructorDelegateOnProxyClass() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(View.class, TestFakeView_WithDefaultConstructorAndNoConstructorDelegate.class);

        View view = new View(context);
        assertEquals(TestFakeView_WithDefaultConstructorAndNoConstructorDelegate.class, DroidSugarAndroidTestRunner.proxyFor(view).getClass());
    }

    @Test
    public void testConstructorInvocation() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(View.class, TestFakeView.class);

        View view = new View(context);
        assertSame(context, proxyFor(view).context);
        assertSame(view, proxyFor(view).realView);
    }

    @Test
    public void testMethodDelegation() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(View.class, TestFakeView.class);

        View view = new View(context);
        assertSame(context, view.getContext());
    }

    @Test
    public void testProxySelectionSearchesSuperclasses() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(View.class, TestFakeView.class);

        TextView textView = new TextView(context);
        assertEquals(TestFakeView.class, DroidSugarAndroidTestRunner.proxyFor(textView).getClass());
    }

    @Test
    public void testWeirdness() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(View.class, TestFakeView.class);
        DroidSugarAndroidTestRunner.addProxy(TextView.class, TestFakeTextView.class);

        TextView textView = new TextView(context);
        TestFakeTextView textViewProxy = proxyFor(textView);
        System.out.println("textViewProxy = " + textViewProxy);
    }

    @Test
    public void testPrimitiveArrays() throws Exception {
        Class<?> objArrayClass = ProxyDelegatingHandler.loadClass("java.lang.Object[]", getClass().getClassLoader());
        assertTrue(objArrayClass.isArray());
        assertEquals(Object.class, objArrayClass.getComponentType());

        Class<?> intArrayClass = ProxyDelegatingHandler.loadClass("int[]", getClass().getClassLoader());
        assertTrue(intArrayClass.isArray());
        assertEquals(Integer.TYPE, intArrayClass.getComponentType());
    }


    private TestFakeView proxyFor(View view) {
        return (TestFakeView) DroidSugarAndroidTestRunner.proxyFor(view);
    }

    private TestFakeTextView proxyFor(TextView view) {
        return (TestFakeTextView) DroidSugarAndroidTestRunner.proxyFor(view);
    }

    public static class TestFakeView {
        private View realView;
        private Context context;

        public TestFakeView(View view) {
            this.realView = view;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public void __constructor__(Context context) {
            this.context = context;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public Context getContext() {
            return context;
        }
    }
    
    public static class TestFakeView_WithDefaultConstructorAndNoConstructorDelegate {
    }

    public static class TestFakeTextView {
    }
}
