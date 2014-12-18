package org.robolectric.internal.bytecode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.testing.Pony;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.Instrument;

import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.RealApisWithoutDefaults.class)
public class RealApisTest {
  @Test
  @Config(shadows = {ShimmeryShadowPony.class})
  public void whenShadowHandlerIsInRealityBasedMode_shouldNotCallRealForUnshadowedMethod() throws Exception {
    assertEquals("Off I saunter to the salon!", new Pony("abc").saunter("the salon"));
  }

  @Implements(Pony.class)
  public static class ShimmeryShadowPony extends Pony.ShadowPony {
  }

  @Test
  @Config(shadows = {ShadowOfClassWithSomeConstructors.class})
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
