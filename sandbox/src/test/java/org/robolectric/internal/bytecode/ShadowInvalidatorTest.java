package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MutableCallSite;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ShadowInvalidatorTest {
  private static final MethodHandle BOUND = constant(String.class, "bound");
  private static final MethodHandle REBIND = constant(String.class, "rebind");

  private final ShadowInvalidator invalidator = new ShadowInvalidator();

  @Test
  public void bind_pointsTheCallSiteAtItsTarget() throws Throwable {
    MutableCallSite site = newCallSite();

    invalidator.bind("com.example.Foo", site, BOUND, REBIND);

    assertThat((String) site.dynamicInvoker().invokeExact()).isEqualTo("bound");
  }

  @Test
  public void invalidateClasses_pointsCallSitesOfThoseClassesAtTheirRebindHandles()
      throws Throwable {
    MutableCallSite foo = newCallSite();
    MutableCallSite otherFoo = newCallSite();
    MutableCallSite bar = newCallSite();
    invalidator.bind("com.example.Foo", foo, BOUND, REBIND);
    invalidator.bind("com.example.Foo", otherFoo, BOUND, REBIND);
    invalidator.bind("com.example.Bar", bar, BOUND, REBIND);

    invalidator.invalidateClasses(Arrays.asList("com.example.Foo", "com.example.Unbound"));

    assertThat((String) foo.dynamicInvoker().invokeExact()).isEqualTo("rebind");
    assertThat((String) otherFoo.dynamicInvoker().invokeExact()).isEqualTo("rebind");
    assertThat((String) bar.dynamicInvoker().invokeExact()).isEqualTo("bound");
  }

  @Test
  public void invalidateClasses_invalidatesACallSiteThatWasBoundAgain() throws Throwable {
    MutableCallSite site = newCallSite();
    invalidator.bind("com.example.Foo", site, BOUND, REBIND);
    invalidator.invalidateClasses(Collections.singletonList("com.example.Foo"));
    invalidator.bind("com.example.Foo", site, BOUND, REBIND);
    assertThat((String) site.dynamicInvoker().invokeExact()).isEqualTo("bound");

    invalidator.invalidateClasses(Collections.singletonList("com.example.Foo"));

    assertThat((String) site.dynamicInvoker().invokeExact()).isEqualTo("rebind");
  }

  private static MutableCallSite newCallSite() {
    return new MutableCallSite(methodType(String.class));
  }
}
