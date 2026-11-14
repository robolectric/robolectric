package org.robolectric.integrationtests.mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@Config(application = PowerMockTestApplication.class)
@PrepareForTest(PowerMockApplicationTest.Client.class)
public class PowerMockApplicationTest {
  @Rule public PowerMockRule rule = new PowerMockRule();

  @Test
  public void applicationInstanceIsPreservedWhenIgnoredByPowerMock() throws Exception {
    Client client = PowerMockito.spy(new Client());
    PowerMockito.doReturn(20).when(client, "privateMethod", anyString(), anyInt());

    assertThat(client.publicMethod()).isTrue();
    assertThat(PowerMockTestApplication.getInstance()).isNotNull();
  }

  public static class Client {
    public boolean publicMethod() throws Exception {
      return privateMethod("hello", 1) == 20;
    }

    private int privateMethod(String s, int n) {
      return 10;
    }
  }
}
