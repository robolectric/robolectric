package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.internal.RealObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.xtremelabs.robolectric.Robolectric.bindShadowClasses;
import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithoutDefaults.class)
public class ClassicSuperHandlingTest {
    @Test
    public void uninstrumentedSubclassesShouldBeAbleToCallSuperWithoutLooping() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ChildShadow.class, ParentShadow.class, GrandparentShadow.class));
        assertEquals("4-3s-2s-1s-boof", new BabiesHavingBabies().method("boof"));
        /*
         * Something like:
         *   directlyOn(realObject, Parent.class).method("boof") to call Parent's boof()
         */
    }

    @Test public void shadowInvocationWhenAllAreShadowed() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ChildShadow.class, ParentShadow.class, GrandparentShadow.class));

        assertEquals("3s-2s-1s-boof", new Child().method("boof"));
        assertEquals("2s-1s-boof", new Parent().method("boof"));
        assertEquals("1s-boof", new Grandparent().method("boof"));
    }

    @Test public void shadowInvocationWhenChildIsInstrmentedButUnshadowed() throws Exception {
        System.out.println("ShadowWrangler is " + Robolectric.getShadowWrangler() + " from " + RobolectricInternals.class.getClassLoader());
        bindShadowClasses(Arrays.<Class<?>>asList(ParentShadow.class, GrandparentShadow.class));

        assertEquals("2s-1s-boof", new Child().method("boof"));
        assertEquals("2s-1s-boof", new Parent().method("boof"));
        assertEquals("1s-boof", new Grandparent().method("boof"));
    }

    @Test public void whenIntermediateIsShadowed() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ParentShadow.class));

        assertEquals("2s-1s-boof", new Child().method("boof"));
        assertEquals("2s-1s-boof", new Parent().method("boof"));
        assertEquals(null, new Grandparent().method("boof"));
    }

    @Test public void whenNoneAreShadowed() throws Exception {
        assertEquals(null, new Child().method("boof"));
        assertEquals(null, new Parent().method("boof"));
        assertEquals(null, new Grandparent().method("boof"));
    }

    @Implements(Child.class)
    public static class ChildShadow extends ParentShadow {
        private @RealObject Child realObject;
        @Override public String method(String value) {
            return "3s-" + super.method(value);
        }
    }

    @Implements(Parent.class)
    public static class ParentShadow extends GrandparentShadow {
        private @RealObject Parent realObject;
        @Override public String method(String value) {
            return "2s-" + super.method(value);
        }
    }

    @Implements(Grandparent.class)
    public static class GrandparentShadow {
        private @RealObject Grandparent realObject;
        public String method(String value) {
            return "1s-" + value;
        }
    }

    private static class BabiesHavingBabies extends Child {
        @Override
        public String method(String value) {
            return "4-" + super.method(value);
        }
    }

    @Instrument
    public static class Child extends Parent {
        @Override public String method(String value) {
            throw new RuntimeException("Stub!");
        }
    }

    @Instrument
    public static class Parent extends Grandparent {
        @Override public String method(String value) {
            throw new RuntimeException("Stub!");
        }
    }

    @Instrument
    private static class Grandparent {
        public String method(String value) {
            throw new RuntimeException("Stub!");
        }
    }
}
