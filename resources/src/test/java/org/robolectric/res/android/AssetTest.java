package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AssetTest {
  @Test
  @SuppressWarnings("deprecation")
  public void excludedAsset_retainsCompatibilityField() {
    assertThat(Asset.EXCLUDED_ASSET).isNotNull();
    assertThat(Asset.excludedAsset()).isSameInstanceAs(Asset.EXCLUDED_ASSET);
    assertThat(CppAssetManager.kExcludedAsset).isSameInstanceAs(Asset.EXCLUDED_ASSET);
    assertThat(Asset.EXCLUDED_ASSET.getLength()).isEqualTo(0);
  }
}
