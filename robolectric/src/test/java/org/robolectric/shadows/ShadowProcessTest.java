package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowProcessTest {
  @Test
  public void shouldBeZeroWhenNotSet() {
    assertThat(android.os.Process.myPid()).isEqualTo(0);
  }
  
  @Test
  public void shouldGetMyPidAsSet() {
    ShadowProcess.setPid(3);
    assertThat(android.os.Process.myPid()).isEqualTo(3);
  }
}

