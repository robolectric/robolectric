package com.xtremelabs.robolectric.bytecode;

import static com.xtremelabs.robolectric.Robolectric.directlyOn;
import static com.xtremelabs.robolectric.Robolectric.directlyOnFullStack;
import static com.xtremelabs.robolectric.RobolectricForMaps.shadowOf;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.bytecode.foo.Foo2;
import com.xtremelabs.robolectric.bytecode.foo.ShadowFoo2;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.shadows.ShadowItemizedOverlay;

@RunWith(WithTestDefaultsRunner.class)
public class AndroidTranslatorTest {

    @Test
    public void testStaticMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ShadowAccountManagerForTests.class);

        Context context = mock(Context.class);
        AccountManager.get(context);
        assertThat(ShadowAccountManagerForTests.wasCalled, is(true));
        assertThat(ShadowAccountManagerForTests.context, sameInstance(context));
    }

    @Test
    public void testProtectedMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ShadowItemizedOverlay.class);

        ItemizedOverlayForTests overlay = new ItemizedOverlayForTests(null);
        overlay.triggerProtectedCall();

        assertThat(shadowOf(overlay).isPopulated(), is(true));
    }

    @Test
    public void testNativeMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ShadowPaintForTests.class);

        Paint paint = new Paint();
        paint.setColor(1234);

        assertThat(paint.getColor(), is(1234));
    }

    @Test
    public void testPrintlnWorks() throws Exception {
        Log.println(1, "tag", "msg");
    }

    @Test
    public void testGeneratedDefaultConstructorIsWired() throws Exception {
        Robolectric.bindShadowClass(ShadowClassWithNoDefaultConstructor.class);

        Constructor<ClassWithNoDefaultConstructor> ctor = ClassWithNoDefaultConstructor.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ClassWithNoDefaultConstructor instance = ctor.newInstance();
        assertThat(Robolectric.shadowOf_(instance), not(nullValue()));
        assertThat(Robolectric.shadowOf_(instance), instanceOf(ShadowClassWithNoDefaultConstructor.class));
    }

    @Test
    public void testDirectlyOn() throws Exception {
        View view = new View(null);
        view.bringToFront();

        Exception e = null;
        try {
            directlyOn(view).bringToFront();
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertEquals("Stub!", e.getMessage());

        view.bringToFront();
    }

    @Test
    public void testDirectlyOn_Statics() throws Exception {
        View.resolveSize(0, 0);

        Exception e = null;
        try {
            directlyOn(View.class);
            View.resolveSize(0, 0);
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertEquals("Stub!", e.getMessage());

        View.resolveSize(0, 0);
    }

    @Test
    public void testDirectlyOn_InstanceChecking() throws Exception {
        View view1 = new View(null);
        View view2 = new View(null);

        Exception e = null;
        try {
            directlyOn(view1);
            view2.bringToFront();
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertThat(e.getMessage(), startsWith("expected to perform direct call on <android.view.View"));
        assertThat(e.getMessage(), containsString("> but got <android.view.View"));
    }

    @Test
    public void testDirectlyOn_Statics_InstanceChecking() throws Exception {
        TextView.getTextColors(null, null);

        Exception e = null;
        try {
            directlyOn(View.class);
            TextView.getTextColors(null, null);
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertThat(e.getMessage(), equalTo("expected to perform direct call on <class android.view.View> but got <class android.widget.TextView>"));
    }

    @Test
    public void testDirectlyOn_CallTwiceChecking() throws Exception {
        directlyOn(View.class);

        Exception e = null;
        try {
            directlyOn(View.class);
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertThat(e.getMessage(), equalTo("already expecting a direct call on <class android.view.View> but here's a new request for <class android.view.View>"));
    }

    @Test
    public void testDirectlyOnFullStack() throws Exception {
        Robolectric.bindShadowClass(ShadowFoo.class);
        Foo foo = new Foo("shadow");
        // instrumented call
        assertThat(foo.callGetName(), equalTo("shadow"));
        assertFalse(foo.callInvokedDirectly());
        // one shot direct call
        assertThat(directlyOn(foo).callGetName(), equalTo("shadow"));
        assertFalse(directlyOn(foo).callInvokedDirectly());

        // full stack direct call
        
        Exception e = null;
        try {
            directlyOnFullStack(foo).callGetName();
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertEquals("stub!", e.getMessage());

        assertTrue(directlyOnFullStack(foo).callInvokedDirectly());
        
        // everything still works
        assertThat(foo.callGetName(), equalTo("shadow"));
        assertThat(foo.callGetName(), equalTo("shadow"));
    }

    @Test
    public void testDirectlyOnFullStack_IncludeClasses() throws Exception {
        Robolectric.bindShadowClass(ShadowFoo.class);
        Robolectric.bindShadowClass(ShadowFoo2.class);

        Foo foo = new Foo("");
        Foo2 foo2 = new Foo2();
        // instrumented call
        assertFalse(foo2.crossCallFooDirectly(foo));
        // one shot direct call
        assertFalse(directlyOn(foo2).crossCallFooDirectly(foo));
        // simple full stack
        assertFalse(directlyOnFullStack(foo2).crossCallFooDirectly(foo));
        
        // include class
        assertTrue(
            directlyOnFullStack(
                DirectCallPolicy.FullStackDirectCallPolicy.build(foo2)
                    .include(Foo.class)
            ).crossCallFooDirectly(foo)
        );
        
        // include package
        assertTrue(
            directlyOnFullStack(
                DirectCallPolicy.FullStackDirectCallPolicy.build(foo2)
                    .include(Arrays.asList(Foo.class.getPackage().getName()))
            ).crossCallFooDirectly(foo)
        );
    }
    
    @Test
    public void testDirectlyOnFullStack_Statics() throws Exception {
        View.resolveSize(0, 0);

        Exception e = null;
        try {
            directlyOnFullStack(View.class);
            View.resolveSize(0, 0);
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertEquals("Stub!", e.getMessage());

        View.resolveSize(0, 0);
    }

    @Test
    public void testDirectlyOnFullStack_InstanceChecking() throws Exception {
        View view1 = new View(null);
        View view2 = new View(null);

        Exception e = null;
        try {
            directlyOnFullStack(view1);
            view2.bringToFront();
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertThat(e.getMessage(), startsWith("expected to perform direct call on <android.view.View"));
        assertThat(e.getMessage(), containsString("> but got <android.view.View"));
    }

    @Test
    public void testDirectlyOnFullStack_Statics_InstanceChecking() throws Exception {
        TextView.getTextColors(null, null);

        Exception e = null;
        try {
            directlyOnFullStack(View.class);
            TextView.getTextColors(null, null);
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertThat(e.getMessage(), equalTo("expected to perform direct call on <class android.view.View> but got <class android.widget.TextView>"));
    }

    @Test
    public void testDirectlyOnFullStack_CallTwiceChecking() throws Exception {
        directlyOnFullStack(View.class);

        Exception e = null;
        try {
            directlyOnFullStack(View.class);
        } catch (RuntimeException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertThat(e.getMessage(), startsWith("Direct call policy is already set"));
    }

    @Test
    public void shouldDelegateToObjectToStringIfShadowHasNone() throws Exception {
        assertTrue(new View(null).toString().startsWith("android.view.View@"));
    }

    @Test
    public void shouldDelegateToObjectHashCodeIfShadowHasNone() throws Exception {
        assertFalse(new View(null).hashCode() == 0);
    }

    @Test
    public void shouldDelegateToObjectEqualsIfShadowHasNone() throws Exception {
        View view = new View(null);
        assertEquals(view, view);
    }

    @Implements(ItemizedOverlay.class)
    public static class ItemizedOverlayForTests extends ItemizedOverlay {
        public ItemizedOverlayForTests(Drawable drawable) {
            super(drawable);
        }

        @Override
        protected OverlayItem createItem(int i) {
            return null;
        }

        public void triggerProtectedCall() {
            populate();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    @Implements(Paint.class)
    public static class ShadowPaintForTests {
        private int color;

        @Implementation
        public void setColor(int color) {
            this.color = color;
        }

        @Implementation
        public int getColor() {
            return color;
        }
    }

    @Implements(AccountManager.class)
    public static class ShadowAccountManagerForTests {
        public static boolean wasCalled = false;
        public static Context context;

        public static AccountManager get(Context context) {
            wasCalled = true;
            ShadowAccountManagerForTests.context = context;
            return mock(AccountManager.class);
        }
    }

    @Instrument
    @SuppressWarnings({"UnusedDeclaration"})
    public static class ClassWithNoDefaultConstructor {
        ClassWithNoDefaultConstructor(String string) {
        }
    }

    @Implements(ClassWithNoDefaultConstructor.class)
    public static class ShadowClassWithNoDefaultConstructor {
    }
}
