package com.xtremelabs.robolectric;

import android.content.Context;
import android.test.mock.MockContext;
import android.view.View;
import android.widget.TextView;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

@RunWith(WithoutTestDefaultsRunner.class)
public class ShadowWranglerTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new MockContext();
    }

    @Test
    public void testConstructorInvocation_WithDefaultConstructorAndNoConstructorDelegateOnShadowClass() throws Exception {
        Robolectric.bindShadowClass(TestShadowView_WithDefaultConstructorAndNoConstructorDelegate.class);

        View view = new View(context);
        assertEquals(TestShadowView_WithDefaultConstructorAndNoConstructorDelegate.class, Robolectric.shadowOf_(view).getClass());
    }

    @Test
    public void testConstructorInvocation() throws Exception {
        Robolectric.bindShadowClass(TestShadowView.class);

        View view = new View(context);
        assertSame(context, shadowOf(view).context);
        assertSame(view, shadowOf(view).realViewCtor);
    }

    @Test
    public void testRealObjectAnnotatedFieldsAreSetBeforeConstructorIsCalled() throws Exception {
        Robolectric.bindShadowClass(TestShadowView.class);

        View view = new View(context);
        assertSame(context, shadowOf(view).context);
        assertSame(view, shadowOf(view).realViewField);

        assertSame(view, shadowOf(view).realViewInConstructor);
        assertSame(view, shadowOf(view).realViewInParentConstructor);
    }

    @Test
    public void testMethodDelegation() throws Exception {
        Robolectric.bindShadowClass(TestShadowView.class);

        View view = new View(context);
        assertSame(context, view.getContext());
    }

    @Test
    public void testEqualsMethodDelegation() throws Exception {
        Robolectric.bindShadowClass(WithEquals.class);

        View view1 = new View(context);
        View view2 = new View(context);
        assertEquals(view1, view2);
    }

    @Test
    public void testHashCodeMethodDelegation() throws Exception {
        Robolectric.bindShadowClass(WithEquals.class);

        View view = new View(context);
        assertEquals(42, view.hashCode());
    }

    @Test
    public void testToStringMethodDelegation() throws Exception {
        Robolectric.bindShadowClass(WithToString.class);

        View view = new View(context);
        assertEquals("the expected string", view.toString());
    }

    @Test
    public void testShadowSelectionSearchesSuperclasses() throws Exception {
        Robolectric.bindShadowClass(TestShadowView.class);

        TextView textView = new TextView(context);
        assertEquals(TestShadowView.class, Robolectric.shadowOf_(textView).getClass());
    }

    @Test
    public void shouldUseMostSpecificShadow() throws Exception {
        Robolectric.bindShadowClass(TestShadowView.class);
        Robolectric.bindShadowClass(TestShadowTextView.class);

        TextView textView = new TextView(context);
        assertThat(shadowOf(textView), instanceOf(TestShadowTextView.class));
    }

    @Test
    public void testPrimitiveArrays() throws Exception {
        Class<?> objArrayClass = ShadowWrangler.loadClass("java.lang.Object[]", getClass().getClassLoader());
        assertTrue(objArrayClass.isArray());
        assertEquals(Object.class, objArrayClass.getComponentType());

        Class<?> intArrayClass = ShadowWrangler.loadClass("int[]", getClass().getClassLoader());
        assertTrue(intArrayClass.isArray());
        assertEquals(Integer.TYPE, intArrayClass.getComponentType());
    }

    private TestShadowView shadowOf(View view) {
        return (TestShadowView) Robolectric.shadowOf_(view);
    }

    private TestShadowTextView shadowOf(TextView view) {
        return (TestShadowTextView) Robolectric.shadowOf_(view);
    }

    @Implements(View.class)
    public static class WithEquals {
        @Override
        public boolean equals(Object o) {
            return true;
        }

        @Override
        public int hashCode() {
            return 42;
        }

    }

    @Implements(View.class)
    public static class WithToString {
        @Override
        public String toString() {
            return "the expected string";
        }
    }

    @Implements(View.class)
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

    @Implements(View.class)
    public static class TestShadowViewParent {
        @RealObject
        private View realView;
        View realViewInParentConstructor;

        public void __constructor__(Context context) {
            realViewInParentConstructor = realView;
        }
    }

    @Implements(View.class)
    public static class TestShadowView_WithDefaultConstructorAndNoConstructorDelegate {
    }

    @Implements(TextView.class)
    public static class TestShadowTextView {
    }
}
