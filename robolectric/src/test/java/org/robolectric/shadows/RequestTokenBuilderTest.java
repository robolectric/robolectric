package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Binder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.V;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = V.SDK_INT)
public final class RequestTokenBuilderTest {

  @Test
  public void getRequestToken_returnsToken() {
    assertThat(RequestTokenBuilder.newBuilder().setToken(new Binder()).build()).isNotNull();
  }
}
