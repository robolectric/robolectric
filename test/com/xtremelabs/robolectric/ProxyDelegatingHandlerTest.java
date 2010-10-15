package com.xtremelabs.robolectric;

import android.content.Context;
import android.test.mock.MockContext;
import android.view.View;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

@RunWith(RobolectricAndroidTestRunner.class)
public class ProxyDelegatingHandlerTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new MockContext();
    }

    @Test
    public void testConstructorInvocation_WithDefaultConstructorAndNoConstructorDelegateOnProxyClass() throws Exception {
        RobolectricAndroidTestRunner.addProxy(View.class, TestFakeView_WithDefaultConstructorAndNoConstructorDelegate.class);

        View view = new View(context);
        assertEquals(TestFakeView_WithDefaultConstructorAndNoConstructorDelegate.class, RobolectricAndroidTestRunner.proxyFor(view).getClass());
    }

    @Test
    public void testConstructorInvocation() throws Exception {
        RobolectricAndroidTestRunner.addProxy(View.class, TestFakeView.class);

        View view = new View(context);
        assertSame(context, proxyFor(view).context);
        assertSame(view, proxyFor(view).realView);
    }

    @Test
    public void testMethodDelegation() throws Exception {
        RobolectricAndroidTestRunner.addProxy(View.class, TestFakeView.class);

        View view = new View(context);
        assertSame(context, view.getContext());
    }

    @Test
    public void testProxySelectionSearchesSuperclasses() throws Exception {
        RobolectricAndroidTestRunner.addProxy(View.class, TestFakeView.class);

        TextView textView = new TextView(context);
        assertEquals(TestFakeView.class, RobolectricAndroidTestRunner.proxyFor(textView).getClass());
    }

    @Test
    public void testWeirdness() throws Exception {
        RobolectricAndroidTestRunner.addProxy(View.class, TestFakeView.class);
        RobolectricAndroidTestRunner.addProxy(TextView.class, TestFakeTextView.class);

        TextView textView = new TextView(context);
        assertThat(proxyFor(textView), instanceOf(TestFakeTextView.class));
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
        return (TestFakeView) RobolectricAndroidTestRunner.proxyFor(view);
    }

    private TestFakeTextView proxyFor(TextView view) {
        return (TestFakeTextView) RobolectricAndroidTestRunner.proxyFor(view);
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
