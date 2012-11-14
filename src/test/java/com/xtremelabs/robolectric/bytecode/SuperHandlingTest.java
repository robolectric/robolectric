package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.internal.RealObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.xtremelabs.robolectric.Robolectric.bindShadowClasses;
import static com.xtremelabs.robolectric.Robolectric.directlyOn;
import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.RealApisWithoutDefaults.class)
public class SuperHandlingTest {
    @Test public void subclassesNotExplicitlyMarkedInstrumentedShouldBeAbleToCallSuperWithoutLooping() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ChildShadow.class, ParentShadow.class, GrandparentShadow.class));
        assertEquals("[3s-(4-(3-(2-(1-{3s-boof-3s}-1)-2)-3)-4)-3s]", new Child() {
            @Override
            public String method(String value) {
                return "(4-" + super.method(value) + "-4)";
            }
        }.method("boof"));
    }

    @Test public void shadowInvocationWhenAllAreShadowed() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ChildShadow.class, ParentShadow.class, GrandparentShadow.class));

        assertEquals("[3s-(3-(2-(1-{3s-boof-3s}-1)-2)-3)-3s]", new Child().method("boof"));
        assertEquals("[2s-(2-(1-{2s-boof-2s}-1)-2)-2s]", new Parent().method("boof"));
        assertEquals("[1s-(1-{1s-boof-1s}-1)-1s]", new Grandparent().method("boof"));
    }

    @Test public void shadowInvocationWhenChildIsUnshadowed() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ParentShadow.class, GrandparentShadow.class));

        assertEquals("[2s-(3-(2-(1-{2s-boof-2s}-1)-2)-3)-2s]", new Child().method("boof"));
        assertEquals("[2s-(2-(1-{2s-boof-2s}-1)-2)-2s]", new Parent().method("boof"));
        assertEquals("[1s-(1-{1s-boof-1s}-1)-1s]", new Grandparent().method("boof"));
    }

    @Test public void whenIntermediateIsShadowed() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ParentShadow.class));

        assertEquals("[2s-(3-(2-(1-{2s-boof-2s}-1)-2)-3)-2s]", new Child().method("boof"));
        assertEquals("[2s-(2-(1-{2s-boof-2s}-1)-2)-2s]", new Parent().method("boof"));
        assertEquals("(1-boof-1)", new Grandparent().method("boof"));
    }

    @Test public void whenNoneAreShadowed() throws Exception {
        assertEquals("(3-(2-(1-boof-1)-2)-3)", new Child().method("boof"));
        assertEquals("(2-(1-boof-1)-2)", new Parent().method("boof"));
        assertEquals("(1-boof-1)", new Grandparent().method("boof"));
    }

    @Implements(Child.class)
    public static class ChildShadow extends ParentShadow {
        private @RealObject Child realObject;
        @Override public String method(String value) {
            return "[3s-" + directlyOn(realObject).method("{3s-" + value + "-3s}") + "-3s]";
        }
    }

    @Implements(Parent.class)
    public static class ParentShadow extends GrandparentShadow {
        private @RealObject Parent realObject;
        @Override public String method(String value) {
            return "[2s-" + directlyOn(realObject).method("{2s-" + value + "-2s}") + "-2s]";
        }
    }

    @Implements(Grandparent.class)
    public static class GrandparentShadow {
        private @RealObject Grandparent realObject;
        public String method(String value) {
            return "[1s-" + directlyOn(realObject).method("{1s-" + value + "-1s}") + "-1s]";
        }
    }

    @Instrument
    public static class Child extends Parent {
        @Override public String method(String value) {
            return "(3-" + super.method(value) + "-3)";
        }
    }

    @Instrument
    public static class Parent extends Grandparent {
        @Override public String method(String value) {
            return "(2-" + super.method(value) + "-2)";
        }
    }

    @Instrument
    private static class Grandparent {
        public String method(String value) {
            return "(1-" + value + "-1)";
        }
    }
}
