package com.xtremelabs.robolectric.bytecode;

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
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.xtremelabs.robolectric.Robolectric.bindShadowClass;
import static com.xtremelabs.robolectric.Robolectric.directlyOn;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(WithTestDefaultsRunner.class)
public class AndroidTranslatorTest {

    private boolean originalDelegateBackToInstrumented;

    @Before
    public void setUp() throws Exception {
        originalDelegateBackToInstrumented = Robolectric.getShadowWranger().delegateBackToInstrumented;
    }

    @After
    public void tearDown() throws Exception {
        Robolectric.getShadowWranger().delegateBackToInstrumented = originalDelegateBackToInstrumented;
    }

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
        bindShadowClass(ShadowPony.class);
        Pony pony = new Pony();

        assertEquals("Fake whinny! You're on my neck!", pony.ride("neck"));
        assertEquals("Whinny! You're on my neck!", directlyOn(pony).ride("neck"));

        assertEquals("Fake whinny! You're on my haunches!", pony.ride("haunches"));
    }

    @Test
    public void testDirectlyOn_Statics() throws Exception {
        bindShadowClass(ShadowPony.class);

        assertEquals("I'm shadily prancing to market!", Pony.prance("market"));
        directlyOn(Pony.class);
        assertEquals("I'm prancing to market!", Pony.prance("market"));

        assertEquals("I'm shadily prancing to market!", Pony.prance("market"));
    }

    @Test
    public void whenShadowHandlerIsInClassicMode_shouldNotCallRealForUnshadowedMethod() throws Exception {
        bindShadowClass(ShadowPony.class);

        assertEquals(null, new Pony().saunter("the salon"));
    }

    @Test
    public void whenShadowHandlerIsInRealityBasedMode_shouldNotCallRealForUnshadowedMethod() throws Exception {
        Robolectric.getShadowWranger().delegateBackToInstrumented = true;
        bindShadowClass(ShadowPony.class);

        assertEquals("Off I saunter to the salon!", new Pony("abc").saunter("the salon"));
    }

    @Instrument
    public static class Pony {
        public Pony() {
        }

        public Pony(String abc) {
            System.out.println("abc = " + abc);
        }

        public String ride(String where) {
            return "Whinny! You're on my " + where + "!";
        }

        public static String prance(String where) {
            return "I'm prancing to " + where + "!";
        }

        public String saunter(String where) {
            return "Off I saunter to " + where + "!";
        }
    }

    @Implements(Pony.class)
    public static class ShadowPony {
        @Implementation
        public String ride(String where) {
            return "Fake whinny! You're on my " + where + "!";
        }

        @Implementation
        public static String prance(String where) {
            return "I'm shadily prancing to " + where + "!";
        }
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
        assertThat(e.getMessage(), equalTo("expected to perform direct call on <class android.view.View> but got <class android.widget.TextView>"));
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
//        Robolectric.getShadowWranger().delegateBackToInstrumented = true;
//        Robolectric.bindShadowClass(ShadowOfClassWithSomeConstructors.class);
        ClassWithSomeConstructors o = new ClassWithSomeConstructors("my name");
        assertNull(o.name);

        Robolectric.directlyOn(o);
        Method realConstructor = o.getClass().getMethod("__constructor__", String.class);
        realConstructor.invoke(o, "my name");
        assertEquals("my name", o.name);
    }

    @Test
    public void shouldCallOriginalConstructorBodySomehow() throws Exception {
        Robolectric.getShadowWranger().delegateBackToInstrumented = true;

        bindShadowClass(ShadowOfClassWithSomeConstructors.class);
        ClassWithSomeConstructors o = new ClassWithSomeConstructors("my name");
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
    public void whenClassIsUnshadowed_shouldPerformStaticInitialization() throws Exception {
        assertEquals("Floyd", ClassWithStaticInitializerA.name);
    }

    @Instrument public static class ClassWithStaticInitializerA { static String name = "Floyd"; }

    @Test
    public void whenClassHasShadowWithoutOverrideMethod_shouldDeferStaticInitialization() throws Exception {
        bindShadowClass(ShadowClassWithoutStaticInitializerOverride.class);
        assertEquals("Floyd", ClassWithStaticInitializerB.name);

        AndroidTranslator.performStaticInitialization(ClassWithStaticInitializerB.class);
        assertEquals("Floyd", ClassWithStaticInitializerB.name);
    }

    @Instrument public static class ClassWithStaticInitializerB { public static String name = "Floyd"; }
    @Implements(ClassWithStaticInitializerB.class) public static class ShadowClassWithoutStaticInitializerOverride { }

    @Test
    public void whenClassHasShadowWithOverrideMethod_shouldDeferStaticInitialization() throws Exception {
        assertFalse(ShadowClassWithStaticInitializerOverride.initialized);
        bindShadowClass(ShadowClassWithStaticInitializerOverride.class);
        assertEquals(null, ClassWithStaticInitializerC.name);
        assertTrue(ShadowClassWithStaticInitializerOverride.initialized);

        AndroidTranslator.performStaticInitialization(ClassWithStaticInitializerC.class);
        assertEquals("Floyd", ClassWithStaticInitializerC.name);
    }

    @Instrument public static class ClassWithStaticInitializerC { public static String name = "Floyd"; }

    @Implements(ClassWithStaticInitializerC.class)
    public static class ShadowClassWithStaticInitializerOverride {
        public static boolean initialized = false;

        public static void __staticInitializer__() {
            initialized = true;
        }
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

}
