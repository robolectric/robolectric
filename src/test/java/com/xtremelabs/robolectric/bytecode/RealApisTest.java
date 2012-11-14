package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.bindShadowClass;
import static org.junit.Assert.assertEquals;

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
