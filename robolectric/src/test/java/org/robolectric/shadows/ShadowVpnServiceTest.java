package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowVpnServiceTest {
  private Context context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void prepare() throws Exception {
    Intent intent = new Intent("foo");
    ShadowVpnService.setPrepareResult(intent);

    assertThat(VpnService.prepare(context)).isEqualTo(intent);
  }
}
