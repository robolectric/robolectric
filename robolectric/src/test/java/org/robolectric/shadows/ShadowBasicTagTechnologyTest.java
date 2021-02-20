package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;

import android.nfc.tech.IsoDep;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowBasicTagTechnology}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = KITKAT)
public final class ShadowBasicTagTechnologyTest {

  // IsoDep extends BasicTagTechnology, which is otherwise a package-protected class.
  private IsoDep basicTagTechnology;

  @Before
  public void setUp() {
    basicTagTechnology = ShadowIsoDep.newInstance();
  }

  @Test
  public void connect() throws Exception {
    assertThat(basicTagTechnology.isConnected()).isFalse();
    basicTagTechnology.connect();
    assertThat(basicTagTechnology.isConnected()).isTrue();
  }

  @Test
  public void close() throws Exception {
    basicTagTechnology.connect();
    basicTagTechnology.close();
    assertThat(basicTagTechnology.isConnected()).isFalse();
  }
}
