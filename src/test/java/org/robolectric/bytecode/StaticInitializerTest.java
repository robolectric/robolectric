package org.robolectric.bytecode;

import org.robolectric.TestRunners;
import org.robolectric.internal.Implements;
import org.robolectric.internal.Instrument;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.robolectric.Robolectric.bindShadowClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithoutDefaults.class)
public class StaticInitializerTest {
    @Test
    public void whenClassIsUnshadowed_shouldPerformStaticInitialization() throws Exception {
        assertEquals("Floyd", ClassWithStaticInitializerA.name);
    }

    @Instrument
    public static class ClassWithStaticInitializerA { static String name = "Floyd"; }


    @Test
    public void whenClassHasShadowWithoutOverrideMethod_shouldPerformStaticInitialization() throws Exception {
        bindShadowClass(ShadowClassWithoutStaticInitializerOverride.class);
        assertEquals("Floyd", ClassWithStaticInitializerB.name);

        RobolectricInternals.performStaticInitialization(ClassWithStaticInitializerB.class);
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

        RobolectricInternals.performStaticInitialization(ClassWithStaticInitializerC.class);
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
}
