package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.util.TypedValue;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ResourceHelper}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Config.NEWEST_SDK)
public class ResourceHelperTest {

  @Test
  public void parseFloatAttribute() {
    TypedValue out = new TypedValue();
    ResourceHelper.parseFloatAttribute(null, "0.16", out, false);
    assertThat(out.getFloat()).isEqualTo(0.16f);

    out = new TypedValue();
    ResourceHelper.parseFloatAttribute(null, ".16", out, false);
    assertThat(out.getFloat()).isEqualTo(0.16f);
  }

  @Test
  public void parseFloatAttribute_lengthEquals1000_parseSucceed() {
    TypedValue out = new TypedValue();
    boolean parseResult =
        ResourceHelper.parseFloatAttribute(
            null, generateTestFloatAttribute("0.16", 1000), out, false);
    assertThat(parseResult).isTrue();
    assertThat(out.getFloat()).isEqualTo(0.16f);
  }

  @Test
  public void parseFloatAttribute_lengthLargerThan1000_returnsFalse() {
    TypedValue out = new TypedValue();
    boolean parseResult =
        ResourceHelper.parseFloatAttribute(
            null, generateTestFloatAttribute("0.17", 1001), out, false);
    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_lengthLessThan1000_parseSucceed() {
    TypedValue out = new TypedValue();
    boolean parseResult =
        ResourceHelper.parseFloatAttribute(
            null, generateTestFloatAttribute("0.18", 999), out, false);
    assertThat(parseResult).isTrue();
    assertThat(out.getFloat()).isEqualTo(0.18f);
  }

  private static String generateTestFloatAttribute(String prefixAttribute, int length) {
    StringBuilder builder = new StringBuilder(prefixAttribute);
    int usedLength = builder.length();
    for (int i = 0; i < length - usedLength; i++) {
      builder.append("0");
    }
    return builder.toString();
  }
}
