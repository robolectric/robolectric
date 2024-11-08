package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.ResourcesMode.Mode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowAssetManager._AssetManager28_;

/** Tests that ApkAssets native objects are cached across ClassLoaders. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ResourcesMode(Mode.BINARY)
public class ApkAssetsCacheTest {
  private static String propertyName() {
    return "org.robolectric.ApkAssetsCacheTest.ptrs" + RuntimeEnvironment.getApiLevel();
  }

  @Test
  @LooperMode(LooperMode.Mode.PAUSED)
  public void test1_recordApkAssetsPtrs() {
    String ptrs = collectPtrs();
    assertThat(ptrs).isNotEmpty();
    System.setProperty(propertyName(), ptrs);
  }

  /* The second test needs to run in a separate sandbox. */
  @Test
  @LooperMode(LooperMode.Mode.LEGACY)
  public void test2_verifyPtrsMatchInNewSandbox() {
    String expected = System.getProperty(propertyName(), "");
    assertThat(collectPtrs()).isEqualTo(expected);
  }

  private static String collectPtrs() {
    AssetManager systemAssetManager = AssetManager.getSystem();
    ApkAssets[] apkAssets = reflector(_AssetManager28_.class, systemAssetManager).getApkAssets();
    ArrayList<Long> ptrs = new ArrayList<>(apkAssets.length);
    for (ApkAssets item : apkAssets) {
      ShadowArscApkAssets9 apkAssetsShadow = Shadow.extract(item);
      ptrs.add(apkAssetsShadow.getNativePtr());
    }
    return Joiner.on(" ").join(ptrs);
  }
}
