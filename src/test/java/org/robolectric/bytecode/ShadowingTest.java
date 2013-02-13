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
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.Instrument;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.robolectric.Robolectric.bindShadowClass;
import static org.robolectric.Robolectric.directlyOn;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowingTest {

    @Test
    public void testStaticMethodsAreDelegated() throws Exception {
        bindShadowClass(ShadowAccountManagerForTests.class);

        Context context = mock(Context.class);
        AccountManager.get(context);
        assertThat(ShadowAccountManagerForTests.wasCalled, is(true));
        assertThat(ShadowAccountManagerForTests.context, sameInstance(context));
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
    public void testProtectedMethodsAreDelegated() throws Exception {
        bindShadowClass(ShadowClassWithProtectedMethod.class);

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
    public void testNativeMethodsAreDelegated() throws Exception {
        bindShadowClass(ShadowPaintForTests.class);

        Paint paint = new Paint();
        paint.setColor(1234);

        assertThat(paint.getColor(), is(1234));
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
    public void forClassWithNoDefaultConstructor_generatedDefaultConstructorShouldNotCallShadow() throws Exception {
        bindShadowClass(ShadowForClassWithNoDefaultConstructor.class);

        Constructor<ClassWithNoDefaultConstructor> ctor = ClassWithNoDefaultConstructor.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ClassWithNoDefaultConstructor instance = ctor.newInstance();
        assertThat(Robolectric.shadowOf_(instance), not(nullValue()));
        assertThat(Robolectric.shadowOf_(instance), instanceOf(ShadowForClassWithNoDefaultConstructor.class));
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
    public void directlyOn_shouldCallThroughToOriginalMethodBody() throws Exception {
        bindShadowClass(Pony.ShadowPony.class);
        Pony pony = new Pony();

        assertEquals("Fake whinny! You're on my neck!", pony.ride("neck"));
        assertEquals("Whinny! You're on my neck!", directlyOn(pony).ride("neck"));

        assertEquals("Fake whinny! You're on my haunches!", pony.ride("haunches"));
    }

    @Test
    public void testDirectlyOn_Statics() throws Exception {
        bindShadowClass(Pony.ShadowPony.class);

        assertEquals("I'm shadily prancing to market!", Pony.prance("market"));

        directlyOn(Pony.class);
        assertEquals("I'm prancing to market!", Pony.prance("market"));

        assertEquals("I'm shadily prancing to market!", Pony.prance("market"));
    }

    @Test
    public void whenShadowHandlerIsInClassicMode_shouldNotCallRealForUnshadowedMethod() throws Exception {
        bindShadowClass(Pony.ShadowPony.class);

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
        assertThat(message, equalTo("expected to perform direct call on instance 0xXXXXXXXX of android.view.View but got instance 0xXXXXXXXX of android.view.View"));
    }

    @Test
    public void testDirectlyOn_Statics_InstanceChecking() throws Exception {
        bindShadowClass(TextViewWithDummyGetTextColorsMethod.class);
        assertNotNull(TextView.getTextColors(null, null)); // the real implementation would asplode

        Exception e = null;
        try {
            directlyOn(View.class);
            TextView.getTextColors(null, null);
        } catch (RuntimeException e1) {
            e = e1;
        }

        assertNotNull(e);
        assertThat(e.getMessage(), equalTo("expected to perform direct call on class android.view.View but got class android.widget.TextView"));
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
        assertThat(e.getMessage(), equalTo("already expecting a direct call on <class android.view.View> but here's a new request for <class android.view.View>"));
    }

    @Test
    public void shouldGenerateSeparatedConstructorBodies() throws Exception {
        ClassWithSomeConstructors o = new ClassWithSomeConstructors("my name");
        assertNull(o.name);

        Method realConstructor = o.getClass().getMethod(InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME, String.class);
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

    @Test public void withNonApiSubclassesWhichExtendApi_shouldStillBeInvoked() throws Exception {
        bindShadowClass(ShadowActivity.class);
        assertEquals("did foo", new MyActivity().doSomething("foo"));
    }

    public static class MyActivity extends Activity { public String doSomething(String value) { return "did " + value; } }
    @Instrument public static class Activity { }
    @Implements(Activity.class) public static class ShadowActivity {}


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
