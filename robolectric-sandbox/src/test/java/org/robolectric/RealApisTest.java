package org.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.InstrumentingTestRunner;
import org.robolectric.internal.bytecode.RoboConfig;
import org.robolectric.testing.Pony;

import static org.junit.Assert.assertEquals;

@RunWith(InstrumentingTestRunner.class)
public class RealApisTest {
  @Test
  @RoboConfig(shadows = {ShimmeryShadowPony.class})
  public void whenShadowHandlerIsInRealityBasedMode_shouldNotCallRealForUnshadowedMethod() throws Exception {
    assertEquals("Off I saunter to the salon!", new Pony().saunter("the salon"));
  }

  @Implements(Pony.class)
  public static class ShimmeryShadowPony extends Pony.ShadowPony {
  }

  @Test
  @RoboConfig(shadows = {ShadowOfClassWithSomeConstructors.class})
  public void shouldCallOriginalConstructorBodySomehow() throws Exception {
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
