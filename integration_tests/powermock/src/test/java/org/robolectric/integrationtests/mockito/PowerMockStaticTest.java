package org.robolectric.integrationtests.mockito;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest(PowerMockStaticTest.Static.class)
public class PowerMockStaticTest {
  @Rule public PowerMockRule rule = new PowerMockRule();

  @Test
  public void testStaticMocking() {
    PowerMockito.mockStatic(Static.class);
    Mockito.when(Static.staticMethod()).thenReturn("hello mock");

    assertThat(Static.staticMethod()).isEqualTo("hello mock");
  }

  public static class Static {
    public static String staticMethod() {
      return "";
    }
  }
}
