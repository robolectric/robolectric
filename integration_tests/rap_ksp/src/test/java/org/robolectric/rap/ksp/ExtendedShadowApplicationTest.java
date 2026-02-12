package org.robolectric.rap.ksp;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
public final class ExtendedShadowApplicationTest {
  @Config(sdk = S)
  @Test
  public void behaviorBeingTested_expectedResult() {
    ExtendedShadowApplication application = Shadow.extract(RuntimeEnvironment.getApplication());
    assertThat(application).isNotNull();
  }
}
