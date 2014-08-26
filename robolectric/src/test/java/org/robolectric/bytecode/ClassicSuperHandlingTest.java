package org.robolectric.bytecode;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.Instrument;
import org.robolectric.annotation.RealObject;

import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithoutDefaults.class)
public class ClassicSuperHandlingTest {
  @Test
  @Config(shadows = {ChildShadow.class, ParentShadow.class, GrandparentShadow.class})
  public void uninstrumentedSubclassesShouldBeAbleToCallSuperWithoutLooping() throws Exception {
    assertEquals("4-3s-2s-1s-boof", new BabiesHavingBabies().method("boof"));
    /*
     * Something like:
     *   directlyOn(realObject, Parent.class).method("boof") to call Parent's boof()
     */
  }

  @Test
  @Config(shadows = {ChildShadow.class, ParentShadow.class, GrandparentShadow.class})
  public void shadowInvocationWhenAllAreShadowed() throws Exception {
    assertEquals("3s-2s-1s-boof", new Child().method("boof"));
    assertEquals("2s-1s-boof", new Parent().method("boof"));
    assertEquals("1s-boof", new Grandparent().method("boof"));
  }

  @Ignore("this doesn't make sense until call-through is turned on by default for unshadowed classes")
  @Test
  @Config(shadows = {ParentShadow.class, GrandparentShadow.class})
  public void shadowInvocationWhenChildIsInstrumentedButUnshadowed() throws Exception {
    System.out.println("ShadowWrangler is " + Robolectric.getShadowWrangler() + " from " + RobolectricInternals.class.getClassLoader());
    assertEquals("2s-1s-boof", new Child().method("boof"));
    assertEquals("2s-1s-boof", new Parent().method("boof"));
    assertEquals("1s-boof", new Grandparent().method("boof"));
  }

  @Ignore("this doesn't make sense until call-through is turned on by default for unshadowed classes")
  @Test
  @Config(shadows = {ParentShadow.class})
  public void whenIntermediateIsShadowed() throws Exception {
    assertEquals("2s-1s-boof", new Child().method("boof"));
    assertEquals("2s-1s-boof", new Parent().method("boof"));
    assertEquals(null, new Grandparent().method("boof"));
  }

  @Ignore("this class probably doesn't make much sense anymore...")
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
