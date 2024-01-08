package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.StringNetworkSpecifier;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.O)
public final class NetworkSpecifierFactoryTest {

  private static final String SUBSCRIPTION_ID_SPECIFIER = "1";
  private static final String ETHERNET_SPECIFIER = "eth0";

  @Test
  public void newStringNetworkSpecifier() {
    assertThat(NetworkSpecifierFactory.newStringNetworkSpecifier(SUBSCRIPTION_ID_SPECIFIER))
        .isInstanceOf(StringNetworkSpecifier.class);
    assertThat(NetworkSpecifierFactory.newStringNetworkSpecifier(ETHERNET_SPECIFIER))
        .isInstanceOf(StringNetworkSpecifier.class);
  }
}
