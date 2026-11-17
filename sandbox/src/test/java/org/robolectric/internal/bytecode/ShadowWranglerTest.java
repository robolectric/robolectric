package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.testing.Pony;

@RunWith(JUnit4.class)
public class ShadowWranglerTest {
  private ShadowWrangler wrangler;

  @Before
  public void setUp() {
    wrangler =
        new ShadowWrangler(
            ShadowMap.EMPTY.newBuilder().addShadowClasses(Pony.ShadowPony.class).build(),
            ShadowMatcher.MATCH_ALL,
            new Interceptors(),
            ImmutableList.of());
  }

  @Test
  @SuppressWarnings("deprecation") // isAccessible() reports the flag that setAccessible() writes.
  public void pickShadowMethod_doesNotModifySharedShadowMethod() {
    Method method = wrangler.pickShadowMethod(Pony.class, "prance", new Class<?>[] {String.class});
    assertThat(method.getDeclaringClass()).isEqualTo(Pony.ShadowPony.class);
    assertThat(method.isAccessible()).isTrue();

    // The shadow's Method objects are cached and shared by every thread that links a call site, so
    // looking one up must only read it.
    method.setAccessible(false);
    assertThat(wrangler.pickShadowMethod(Pony.class, "prance", new Class<?>[] {String.class}))
        .isSameInstanceAs(method);
    assertThat(method.isAccessible()).isFalse();
  }

  @Test
  @SuppressWarnings("deprecation") // isAccessible() reports the flag that setAccessible() writes.
  public void findShadowMethodHandle_doesNotModifySharedShadowMethod() throws Exception {
    Method method = wrangler.pickShadowMethod(Pony.class, "prance", new Class<?>[] {String.class});

    // ShadowPony.prance is protected, so it can only be unreflected because indexing made it
    // accessible.
    assertThat(
            wrangler.findShadowMethodHandle(
                Pony.class, "prance", methodType(String.class, String.class), true, false))
        .isNotNull();

    // Linking a call site must rely on that and not write the shared Method's flag again.
    method.setAccessible(false);
    assertThrows(
        IllegalAccessException.class,
        () ->
            wrangler.findShadowMethodHandle(
                Pony.class, "prance", methodType(String.class, String.class), true, false));
    assertThat(method.isAccessible()).isFalse();
  }
}
