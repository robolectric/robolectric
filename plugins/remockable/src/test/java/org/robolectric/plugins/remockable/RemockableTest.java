package org.robolectric.plugins.remockable;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.plugins.remockable.Remockable.mockOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.SandboxConfig;

@RunWith(SandboxTestRunner.class)
public class RemockableTest {
  @Test
  @SandboxConfig(shadows = {ShadowThing.class})
  public void testRealMock() throws Exception {
    Thing mockThing = mock(Thing.class);
    when(mockThing.doThing("value"))
        .thenReturn("from mock");
    assertThat(mockThing.doThing("value"))
        .isEqualTo("from mock");
  }

  @Test
  @SandboxConfig(shadows = {ShadowThing.class})
  public void testRoboMock() throws Exception {
    Thing thing = new Thing();

    assertThat(thing.doThing("value"))
        .isEqualTo("from shadow: value (from real: value)");

    when(mockOf(thing).doThing(eq("value")))
        .thenReturn("from mock");
    assertThat(thing.doThing("value"))
        .isEqualTo("from mock");
    assertThat(thing.doThing("value"))
        .isEqualTo("from mock");

    assertThat(thing.doThing("other value"))
        .isEqualTo("from shadow: other value (from real: other value)");
  }

  @Instrument
  static class Thing {
    public String doThing(String arg) {
      return "from real: " + arg;
    }
  }

  @Implements(Thing.class)
  public static class ShadowThing {
    static boolean wasCalled = false;

    @RealObject Thing realThing;

    @Implementation
    public String doThing(String arg) {
      wasCalled = true;
      return "from shadow: " + arg
          + " (" + directlyOn(realThing, Thing.class).doThing(arg) + ")";
    }
  }
}
