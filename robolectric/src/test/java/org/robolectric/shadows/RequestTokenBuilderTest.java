package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;

import android.os.Binder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VANILLA_ICE_CREAM)
public final class RequestTokenBuilderTest {

  @Test
  public void getRequestToken_returnsToken() {
    assertThat(RequestTokenBuilder.newBuilder().setToken(new Binder()).build()).isNotNull();
  }
}
