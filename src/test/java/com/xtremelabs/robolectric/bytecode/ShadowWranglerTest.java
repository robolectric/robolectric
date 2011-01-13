package com.xtremelabs.robolectric.bytecode;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithoutTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

@RunWith(WithoutTestDefaultsRunner.class)
public class ShadowWranglerTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
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

    @Test
    public void shouldRemoveNoiseFromStackTraces() throws Exception {
        Robolectric.bindShadowClass(ExceptionThrowingShadowView.class);
        View view = new View(null);

        Exception e = null;
        try {
            view.getContext();
        } catch (Exception e1) {
            e = e1;
        }

        assertNotNull(e);
        assertEquals(IOException.class, e.getClass());
        assertEquals("fake exception", e.getMessage());
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.getBuffer().toString();

        assertThat(stackTrace, containsString("fake exception"));
        assertThat(stackTrace, containsString(ExceptionThrowingShadowView.class.getName() + ".getContext("));
        assertThat(stackTrace, containsString(View.class.getName() + ".getContext("));
        assertThat(stackTrace, containsString(ShadowWranglerTest.class.getName() + ".shouldRemoveNoiseFromStackTraces"));

        assertThat(stackTrace, not(containsString("sun.reflect")));
        assertThat(stackTrace, not(containsString("java.lang.reflect")));
        assertThat(stackTrace, not(containsString(ShadowWrangler.class.getName() + ".")));
        assertThat(stackTrace, not(containsString(RobolectricInternals.class.getName() + ".")));
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

    @Implements(View.class)
    public static class ExceptionThrowingShadowView {
        @SuppressWarnings({"UnusedDeclaration"})
        public Context getContext() throws IOException {
            throw new IOException("fake exception");
        }
    }
}
