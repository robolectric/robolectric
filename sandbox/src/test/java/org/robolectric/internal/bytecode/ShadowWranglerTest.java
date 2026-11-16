package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.testing.Pony;

@RunWith(JUnit4.class)
public class ShadowWranglerTest {
  @Test
  @SuppressWarnings("deprecation") // isAccessible() reports the flag that setAccessible() writes.
  public void pickShadowMethod_doesNotModifySharedShadowMethod() {
    ShadowWrangler wrangler =
        new ShadowWrangler(
            ShadowMap.EMPTY.newBuilder().addShadowClasses(Pony.ShadowPony.class).build(),
            ShadowMatcher.MATCH_ALL,
            new Interceptors(),
            ImmutableList.of());

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
}
