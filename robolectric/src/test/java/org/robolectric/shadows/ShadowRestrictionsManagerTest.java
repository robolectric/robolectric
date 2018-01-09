package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.RestrictionsManager;
import android.os.Bundle;

import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public final class ShadowRestrictionsManagerTest {

  private RestrictionsManager restrictionsManager;
  private Context context;

  @Before
  public void setUp() {
    context = RuntimeEnvironment.application;
    restrictionsManager = (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
  }

  @Test
  @Config(minSdk = LOLLIPOP)  
  public void getApplicationRestrictions() throws Exception {
    assertThat(restrictionsManager.getApplicationRestrictions()).isNull();

    Bundle bundle = new Bundle();
    bundle.putCharSequence("test_key", "test_value");
    shadowOf(restrictionsManager).setApplicationRestrictions(bundle);

    assertThat(restrictionsManager.getApplicationRestrictions().getCharSequence("test_key")).isEqualTo("test_value");
  }
}
