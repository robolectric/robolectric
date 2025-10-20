package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.AttributionSource;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(
    minSdk = VERSION_CODES.S,
    shadows = {ShadowAttributionSource.class})
public final class ShadowAttributionSourceTest {

  private static final int CALLING_UID_1 = 10001;
  private static final int CALLING_UID_2 = 10002;

  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @After
  public void tearDown() {
    ShadowAttributionSource.clearTrustedAttributionSources();
  }

  @Test
  public void testIsTrusted_singleAttributionSource_returnExpectedValue() {
    AttributionSource trustedAttributionSource =
        new AttributionSource.Builder(CALLING_UID_1).build();
    AttributionSource unTrustedAttributionSource =
        new AttributionSource.Builder(CALLING_UID_2).build();
    ShadowAttributionSource.addTrustedAttributionSource(trustedAttributionSource);

    assertThat(trustedAttributionSource.isTrusted(context)).isTrue();
    assertThat(unTrustedAttributionSource.isTrusted(context)).isFalse();
  }

  @Test
  public void testIsTrusted_chainedAttributionSource_returnExpectedValue() {
    AttributionSource nestedAttributionSource =
        new AttributionSource.Builder(CALLING_UID_2).build();
    AttributionSource rootAttributionSource =
        new AttributionSource.Builder(CALLING_UID_1).setNext(nestedAttributionSource).build();
    ShadowAttributionSource.addTrustedAttributionSource(rootAttributionSource);
    ShadowAttributionSource.addTrustedAttributionSource(nestedAttributionSource);

    assertThat(nestedAttributionSource.isTrusted(context)).isTrue();
    assertThat(rootAttributionSource.isTrusted(context)).isTrue();
  }
}
