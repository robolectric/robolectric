package org.robolectric;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.SandboxConfig;

@RunWith(SandboxTestRunner.class)
public class ClassicSuperHandlingTest {
  @Test
  @SandboxConfig(shadows = {ChildShadow.class, ParentShadow.class, GrandparentShadow.class})
  public void uninstrumentedSubclassesShouldBeAbleToCallSuperWithoutLooping() throws Exception {
    assertEquals("4-3s-2s-1s-boof", new BabiesHavingBabies().method("boof"));
  }

  @Test
  @SandboxConfig(shadows = {ChildShadow.class, ParentShadow.class, GrandparentShadow.class})
  public void shadowInvocationWhenAllAreShadowed() throws Exception {
    assertEquals("3s-2s-1s-boof", new Child().method("boof"));
    assertEquals("2s-1s-boof", new Parent().method("boof"));
    assertEquals("1s-boof", new Grandparent().method("boof"));
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
