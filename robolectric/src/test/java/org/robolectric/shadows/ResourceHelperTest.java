package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.util.TypedValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for {@link ResourceHelper}.
 */
@RunWith(RobolectricTestRunner.class)
public class ResourceHelperTest {

  @Test
  @Config(sdk = Config.NEWEST_SDK)
  public void parseFloatAttribute() {
    TypedValue out = new TypedValue();
    ResourceHelper.parseFloatAttribute(null, "0.16", out, false);
    assertThat(out.getFloat()).isEqualTo(0.16f);

    out = new TypedValue();
    ResourceHelper.parseFloatAttribute(null, ".16", out, false);
    assertThat(out.getFloat()).isEqualTo(0.16f);
  }
}
