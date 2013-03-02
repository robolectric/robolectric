package org.robolectric.bytecode;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Config;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.Instrument;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.robolectric.Robolectric.directlyOn;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowingTest {

    @Test
    @Config(shadows = {ShadowAccountManagerForTests.class})
    public void testStaticMethodsAreDelegated() throws Exception {
        Context context = mock(Context.class);
        AccountManager.get(context);
        assertThat(ShadowAccountManagerForTests.wasCalled).isTrue();
        assertThat(ShadowAccountManagerForTests.context).isSameAs(context);
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

    @Test
    @Config(shadows = {ShadowClassWithProtectedMethod.class})
    public void testProtectedMethodsAreDelegated() throws Exception {
        ClassWithProtectedMethod overlay = new ClassWithProtectedMethod();
        assertEquals("shadow name", overlay.getName());
    }

    @Implements(ClassWithProtectedMethod.class)
    public static class ShadowClassWithProtectedMethod {
        @Implementation
        protected String getName() {
            return "shadow name";
        }
    }

    @Instrument
    public static class ClassWithProtectedMethod {
        protected String getName() {
            return "protected name";
        }
    }

    @Test
    @Config(shadows = {ShadowPaintForTests.class})
    public void testNativeMethodsAreDelegated() throws Exception {
        Paint paint = new Paint();
        paint.setColor(1234);

        assertThat(paint.getColor()).isEqualTo(1234);
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

    @Test
    public void testPrintlnWorks() throws Exception {
        Log.println(1, "tag", "msg");
    }

    @Ignore // todo we need to figure out a better way to deal with this...
    @Test // the shadow will still have its default constructor called; it would be duplicative to call __constructor__() too.
    @Config(shadows = {ShadowForClassWithNoDefaultConstructor.class})
    public void forClassWithNoDefaultConstructor_generatedDefaultConstructorShouldNotCallShadow() throws Exception {
        Constructor<ClassWithNoDefaultConstructor> ctor = ClassWithNoDefaultConstructor.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ClassWithNoDefaultConstructor instance = ctor.newInstance();
        assertThat(Robolectric.shadowOf_(instance)).isNotNull();
        assertThat(Robolectric.shadowOf_(instance)).isInstanceOf(ShadowForClassWithNoDefaultConstructor.class);
        assertTrue(ShadowForClassWithNoDefaultConstructor.shadowDefaultConstructorCalled);
        assertFalse(ShadowForClassWithNoDefaultConstructor.shadowDefaultConstructorImplementorCalled);
    }

    @Implements(ClassWithNoDefaultConstructor.class)
    public static class ShadowForClassWithNoDefaultConstructor {
        public static boolean shadowDefaultConstructorCalled = false;
        public static boolean shadowDefaultConstructorImplementorCalled = false;

        public ShadowForClassWithNoDefaultConstructor() {
            this.shadowDefaultConstructorCalled = true;
        }

        public void __constructor__() {
            shadowDefaultConstructorImplementorCalled = true;
        }
    }

    @Instrument @SuppressWarnings({"UnusedDeclaration"})
    public static class ClassWithNoDefaultConstructor {
        ClassWithNoDefaultConstructor(String string) {
        }
    }

    @Test
    @Config(shadows = {Pony.ShadowPony.class})
    public void directlyOn_shouldCallThroughToOriginalMethodBody() throws Exception {
        Pony pony = new Pony();

        assertEquals("Fake whinny! You're on my neck!", pony.ride("neck"));
        assertEquals("Whinny! You're on my neck!", directlyOn(pony).ride("neck"));

        assertEquals("Fake whinny! You're on my haunches!", pony.ride("haunches"));
    }

    @Test
    @Config(shadows = {Pony.ShadowPony.class})
    public void testDirectlyOn_Statics() throws Exception {
        assertEquals("I'm shadily prancing to market!", Pony.prance("market"));

        directlyOn(Pony.class);
        assertEquals("I'm prancing to market!", Pony.prance("market"));

        assertEquals("I'm shadily prancing to market!", Pony.prance("market"));
    }

    @Test
    @Config(shadows = {Pony.ShadowPony.class})
    public void whenShadowHandlerIsInClassicMode_shouldNotCallRealForUnshadowedMethod() throws Exception {
        assertEquals(null, new Pony().saunter("the salon"));
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
        String message = e.getMessage().replaceAll("0x[0-9a-z]+", "0xXXXXXXXX");
        assertThat(message).isEqualTo("expected to perform direct call on instance 0xXXXXXXXX of android.view.View but got instance 0xXXXXXXXX of android.view.View");
    }

    @Test
    @Config(shadows = {TextViewWithDummyGetTextColorsMethod.class})
    public void testDirectlyOn_Statics_InstanceChecking() throws Exception {
        assertNotNull(TextView.getTextColors(null, null)); // the real implementation would asplode

        Exception e = null;
        try {
            directlyOn(View.class);
            TextView.getTextColors(null, null);
        } catch (RuntimeException e1) {
            e = e1;
        }

        assertNotNull(e);
        assertThat(e.getMessage()).isEqualTo("expected to perform direct call on class android.view.View but got class android.widget.TextView");
    }

    @Implements(TextView.class)
    public static class TextViewWithDummyGetTextColorsMethod {
        public static ColorStateList getTextColors(Context context, TypedArray attrs) {
            return new ColorStateList(new int[0][0], new int[0]);
        }
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
        assertThat(e.getMessage()).isEqualTo("already expecting a direct call on <class android.view.View> but here's a new request for <class android.view.View>");
    }

    @Test
    public void shouldGenerateSeparatedConstructorBodies() throws Exception {
        ClassWithSomeConstructors o = new ClassWithSomeConstructors("my name");
        assertNull(o.name);

        Method realConstructor = o.getClass().getDeclaredMethod(InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME, String.class);
        realConstructor.setAccessible(true);
        realConstructor.invoke(o, "my name");
        assertEquals("my name", o.name);
    }

    @Instrument
    public static class ClassWithSomeConstructors {
        public String name;

        public ClassWithSomeConstructors(String name) {
            this.name = name;
        }
    }

    @Implements(ClassWithSomeConstructors.class)
    public static class ShadowOfClassWithSomeConstructors {
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

    @Test
    @Config(shadows = {ShadowApiImplementedClass.class})
    public void withNonApiSubclassesWhichExtendApi_shouldStillBeInvoked() throws Exception {
        assertEquals("did foo", new NonApiSubclass().doSomething("foo"));
    }

    public static class NonApiSubclass extends ApiImplementedClass { public String doSomething(String value) { return "did " + value; } }
    @Instrument public static class ApiImplementedClass { }
    @Implements(ApiImplementedClass.class) public static class ShadowApiImplementedClass {}


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

}
