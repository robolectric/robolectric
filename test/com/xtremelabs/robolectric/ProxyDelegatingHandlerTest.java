package com.xtremelabs.robolectric;

import android.content.Context;
import android.test.mock.MockContext;
import android.view.View;
import android.widget.TextView;
import com.xtremelabs.robolectric.util.RealObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

@RunWith(DogfoodRobolectricTestRunner.class)
public class ProxyDelegatingHandlerTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new MockContext();
    }

    @Test
    public void testConstructorInvocation_WithDefaultConstructorAndNoConstructorDelegateOnProxyClass() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView_WithDefaultConstructorAndNoConstructorDelegate.class);

        View view = new View(context);
        assertEquals(TestShadowView_WithDefaultConstructorAndNoConstructorDelegate.class, DogfoodRobolectricTestRunner.shadowOf(view).getClass());
    }

    @Test
    public void testConstructorInvocation() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);

        View view = new View(context);
        assertSame(context, shadowOf(view).context);
        assertSame(view, shadowOf(view).realViewCtor);
    }

    @Test
    public void testRealObjectAnnotatedFieldsAreSetBeforeConstructorIsCalled() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);

        View view = new View(context);
        assertSame(context, shadowOf(view).context);
        assertSame(view, shadowOf(view).realViewField);

        assertSame(view, shadowOf(view).realViewInConstructor);
        assertSame(view, shadowOf(view).realViewInParentConstructor);
    }

    @Test
    public void testMethodDelegation() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);

        View view = new View(context);
        assertSame(context, view.getContext());
    }

    @Test
    public void testProxySelectionSearchesSuperclasses() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);

        TextView textView = new TextView(context);
        assertEquals(TestShadowView.class, DogfoodRobolectricTestRunner.shadowOf(textView).getClass());
    }

    @Test
    public void testWeirdness() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);
        DogfoodRobolectricTestRunner.addProxy(TextView.class, TestShadowTextView.class);

        TextView textView = new TextView(context);
        assertThat(shadowOf(textView), instanceOf(TestShadowTextView.class));
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


    private TestShadowView shadowOf(View view) {
        return (TestShadowView) DogfoodRobolectricTestRunner.shadowOf(view);
    }

    private TestShadowTextView shadowOf(TextView view) {
        return (TestShadowTextView) DogfoodRobolectricTestRunner.shadowOf(view);
    }

    public static class TestShadowView extends TestShadowViewParent {
        @RealObject
        private View realViewField;
        private View realViewInConstructor;

        private View realViewCtor;

        private Context context;

        public TestShadowView(View view) {
            this.realViewCtor = view;
        }

        @Override
        @SuppressWarnings({"UnusedDeclaration"})
        public void __constructor__(Context context) {
            super.__constructor__(context);
            this.context = context;
            realViewInConstructor = realViewField;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public Context getContext() {
            return context;
        }
    }

    public static class TestShadowViewParent {
        @RealObject
        private View realView;
        View realViewInParentConstructor;

        public void __constructor__(Context context) {
            realViewInParentConstructor = realView;
        }
    }
    
    public static class TestShadowView_WithDefaultConstructorAndNoConstructorDelegate {
    }

    public static class TestShadowTextView {
    }
}
