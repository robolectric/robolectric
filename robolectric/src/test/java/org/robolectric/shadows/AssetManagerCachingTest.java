package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.versioning.AndroidVersions.P;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
@Config(minSdk = P.SDK_INT)
public class AssetManagerCachingTest {
  private static final AtomicLong systemNativePtr = new AtomicLong();

  @Test
  public void test1_getAssetManagerPtr() {
    AssetManager systemAssetManager = Resources.getSystem().getAssets();
    systemNativePtr.set(getNativePtr(systemAssetManager));
    assertThat(systemNativePtr.get()).isNotEqualTo(0);
  }

  @Test
  public void test2_verifySamePtr() {
    AssetManager systemAssetManager = Resources.getSystem().getAssets();
    long nativePtr = getNativePtr(systemAssetManager);
    assertThat(nativePtr).isEqualTo(systemNativePtr.get());
  }

  @Test
  public void test3_createApplicationAssets() {
    AssetManager systemAssetManager = Resources.getSystem().getAssets();
    Application application = RuntimeEnvironment.getApplication();
    AssetManager assetManager = application.getAssets();
    long nativePtr = getNativePtr(systemAssetManager);
    long appNativePtr = getNativePtr(assetManager);
    assertThat(nativePtr).isEqualTo(systemNativePtr.get());
    assertThat(nativePtr).isNotEqualTo(appNativePtr);
  }

  private static long getNativePtr(AssetManager assetManager) {
    return ((ShadowAssetManager) Shadow.extract(assetManager)).getNativePtr();
  }
}
