package com.xtremelabs.robolectric;

import android.content.Context;
import android.test.mock.MockContext;
import android.view.View;
import android.widget.TextView;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.util.SheepWrangler;
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
        assertEquals(TestShadowView_WithDefaultConstructorAndNoConstructorDelegate.class, DogfoodRobolectricTestRunner.proxyFor(view).getClass());
    }

    @Test
    public void testConstructorInvocation() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);

        View view = new View(context);
        assertSame(context, proxyFor(view).context);
        assertSame(view, proxyFor(view).realViewCtor);
    }

    @Test
    public void testRealObjectAnnotatedFieldsAreSetBeforeConstructorIsCalled() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);

        View view = new View(context);
        assertSame(context, proxyFor(view).context);
        assertSame(view, proxyFor(view).realViewField);

        assertSame(view, proxyFor(view).realViewInConstructor);
        assertSame(view, proxyFor(view).realViewInParentConstructor);
    }

    @Test
    public void testSheepWranglerAnnotatedFieldsAreSetBeforeConstructorIsCalled() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);

        View view = new View(context);
        ProxyDelegatingHandler proxyDelegatingHandler = ProxyDelegatingHandler.getInstance();

        assertSame(proxyDelegatingHandler, proxyFor(view).sheepWranglerInConstructor);
        assertSame(proxyDelegatingHandler, proxyFor(view).sheepWranglerInParentConstructor);
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
        assertEquals(TestShadowView.class, DogfoodRobolectricTestRunner.proxyFor(textView).getClass());
    }

    @Test
    public void testWeirdness() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(View.class, TestShadowView.class);
        DogfoodRobolectricTestRunner.addProxy(TextView.class, TestShadowTextView.class);

        TextView textView = new TextView(context);
        assertThat(proxyFor(textView), instanceOf(TestShadowTextView.class));
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


    private TestShadowView proxyFor(View view) {
        return (TestShadowView) DogfoodRobolectricTestRunner.proxyFor(view);
    }

    private TestShadowTextView proxyFor(TextView view) {
        return (TestShadowTextView) DogfoodRobolectricTestRunner.proxyFor(view);
    }

    public static class TestShadowView extends TestShadowViewParent {
        @RealObject
        private View realViewField;
        private View realViewInConstructor;

        @SheepWrangler
        ProxyDelegatingHandler sheepWranglerField;
        ProxyDelegatingHandler sheepWranglerInConstructor;

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
            sheepWranglerInConstructor = sheepWranglerField;
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

        @SheepWrangler
        ProxyDelegatingHandler sheepWranglerParentField;
        ProxyDelegatingHandler sheepWranglerInParentConstructor;

        public void __constructor__(Context context) {
            realViewInParentConstructor = realView;
            sheepWranglerInParentConstructor = sheepWranglerParentField;
        }
    }
    
    public static class TestShadowView_WithDefaultConstructorAndNoConstructorDelegate {
    }

    public static class TestShadowTextView {
    }
}
