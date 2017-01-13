package org.robolectric.internal.bytecode;

import org.junit.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class AndroidInterceptorsTest {
  @Test
  public void allMethodRefs() throws Exception {
    assertThat(new AndroidInterceptors().build().getAllMethodRefs())
        .containsExactlyInAnyOrder(
            new MethodRef("java.util.LinkedHashMap", "eldest"),
            new MethodRef("java.lang.System", "loadLibrary"),
            new MethodRef("android.os.StrictMode", "trackActivity"),
            new MethodRef("android.os.StrictMode", "incrementExpectedActivityCount"),
            new MethodRef("java.lang.AutoCloseable", "*"),
            new MethodRef("android.util.LocaleUtil", "getLayoutDirectionFromLocale"),
            new MethodRef("com.android.internal.policy.PolicyManager", "makeNewWindow"),
            new MethodRef("com.android.internal.policy.PolicyManager", "*"),
            new MethodRef("android.view.FallbackEventHandler", "*"),
            new MethodRef("android.view.IWindowSession", "*"),
            new MethodRef("java.lang.System", "nanoTime"),
            new MethodRef("java.lang.System", "currentTimeMillis"),
            new MethodRef("java.lang.System", "arraycopy"),
            new MethodRef("java.lang.System", "logE"),
            new MethodRef("java.util.Locale", "adjustLanguageCode")
        );
  }
}