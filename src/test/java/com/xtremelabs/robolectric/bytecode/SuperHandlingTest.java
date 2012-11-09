package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.WithoutTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.internal.RealObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.xtremelabs.robolectric.Robolectric.bindShadowClasses;
import static com.xtremelabs.robolectric.Robolectric.directlyOn;
import static org.junit.Assert.assertEquals;

@RunWith(WithoutTestDefaultsRunner.class)
public class SuperHandlingTest {
    @Before
    public void setUp() throws Exception {
        ShadowWrangler.getInstance().delegateBackToInstrumented = true;
    }

    @Test public void subclassesNotExplicitlyMarkedInstrumentedShouldBeAbleToCallSuperWithoutLooping() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ChildShadow.class, ParentShadow.class, GrandparentShadow.class));
        assertEquals("[3-(4-(3-(2-(1-{3-boof-3}-1)-2)-3)-4)-3]", new Child() {
            @Override
            public String method(String value) {
                return "(4-" + super.method(value) + "-4)";
            }
        }.method("boof"));
    }

    @Test public void shadowInvocationWhenAllAreShadowed() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ChildShadow.class, ParentShadow.class, GrandparentShadow.class));

        assertEquals("[3-(3-(2-(1-{3-boof-3}-1)-2)-3)-3]", new Child().method("boof"));
        assertEquals("[2-(2-(1-{2-boof-2}-1)-2)-2]", new Parent().method("boof"));
        assertEquals("[1-(1-{1-boof-1}-1)-1]", new Grandparent().method("boof"));
    }

    @Test public void shadowInvocationWhenChildIsUnshadowed() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ParentShadow.class, GrandparentShadow.class));

        assertEquals("[2-(3-(2-(1-{2-boof-2}-1)-2)-3)-2]", new Child().method("boof"));
        assertEquals("[2-(2-(1-{2-boof-2}-1)-2)-2]", new Parent().method("boof"));
        assertEquals("[1-(1-{1-boof-1}-1)-1]", new Grandparent().method("boof"));
    }

    @Test public void whenIntermediateIsShadowed() throws Exception {
        bindShadowClasses(Arrays.<Class<?>>asList(ParentShadow.class));

        assertEquals("[2-(3-(2-(1-{2-boof-2}-1)-2)-3)-2]", new Child().method("boof"));
        assertEquals("[2-(2-(1-{2-boof-2}-1)-2)-2]", new Parent().method("boof"));
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
            return "[3-" + directlyOn(realObject).method("{3-" + value + "-3}") + "-3]";
        }
    }

    @Implements(Parent.class)
    public static class ParentShadow extends GrandparentShadow {
        private @RealObject Parent realObject;
        @Override public String method(String value) {
            return "[2-" + directlyOn(realObject).method("{2-" + value + "-2}") + "-2]";
        }
    }

    @Implements(Grandparent.class)
    public static class GrandparentShadow {
        private @RealObject Grandparent realObject;
        public String method(String value) {
            return "[1-" + directlyOn(realObject).method("{1-" + value + "-1}") + "-1]";
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
