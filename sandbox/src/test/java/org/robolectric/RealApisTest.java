package org.robolectric;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.testing.Pony;

@RunWith(SandboxTestRunner.class)
public class RealApisTest {
  @Test
  @SandboxConfig(shadows = {ShimmeryShadowPony.class})
  public void whenShadowHandlerIsInRealityBasedMode_shouldNotCallRealForUnshadowedMethod() throws Exception {
    assertEquals("Off I saunter to the salon!", new Pony().saunter("the salon"));
  }

  @Implements(Pony.class)
  public static class ShimmeryShadowPony extends Pony.ShadowPony {
  }

  @Test
  @SandboxConfig(shadows = {ShadowOfClassWithSomeConstructors.class})
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
