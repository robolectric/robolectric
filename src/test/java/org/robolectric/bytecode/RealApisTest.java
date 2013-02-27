package org.robolectric.bytecode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.Implements;
import org.robolectric.internal.Instrument;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Robolectric.bindShadowClass;

@RunWith(TestRunners.RealApisWithoutDefaults.class)
public class RealApisTest {
    @Test
    public void whenShadowHandlerIsInRealityBasedMode_shouldNotCallRealForUnshadowedMethod() throws Exception {
        bindShadowClass(Pony.ShadowPony.class);

        assertEquals("Off I saunter to the salon!", new Pony("abc").saunter("the salon"));
    }

    @Test
    public void shouldCallOriginalConstructorBodySomehow() throws Exception {
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
}
