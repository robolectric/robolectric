package org.robolectric.res;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PreferenceLoaderTest {

  @Test public void qualifyName_shouldAddPackageIfMissing() throws Exception {
    assertThat(PreferenceLoader.qualifyName("android:id", "android")).isEqualTo("android:id");
    assertThat(PreferenceLoader.qualifyName("my:id", "android")).isEqualTo("my:id");
    assertThat(PreferenceLoader.qualifyName("id", "android")).isEqualTo("android:id");
    assertThat(PreferenceLoader.qualifyName("id", "my.package")).isEqualTo("my.package:id");
  }
}
