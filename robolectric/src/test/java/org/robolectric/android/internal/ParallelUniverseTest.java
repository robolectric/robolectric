package org.robolectric.android.internal;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.robolectric.BootstrapDeferringRobolectricTestRunner;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.BootstrapWrapper;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.RoboInject;
import org.robolectric.RoboSettings;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.DeviceConfig;
import org.robolectric.android.DeviceConfig.ScreenSize;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.res.ResourceTable;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;

@RunWith(BootstrapDeferringRobolectricTestRunner.class)
public class ParallelUniverseTest {

  @RoboInject BootstrapWrapper bootstrapWrapper;
  private ParallelUniverse pu;

  @Before
  public void setUp() throws InitializationError {
    pu = (ParallelUniverse) bootstrapWrapper.delegate;
  }

  @After
  public void tearDown() throws Exception {
    // reset from weird states created by tests...
    if (!RuntimeEnvironment.isMainThread()) {
      RuntimeEnvironment.setMainThread(Thread.currentThread());
    }
  }

  @Test
  public void setUpApplicationState_configuresGlobalScheduler() {
    bootstrapWrapper.callSetUpApplicationState();

    assertThat(RuntimeEnvironment.getMasterScheduler())
        .isSameAs(ShadowLooper.getShadowMainLooper().getScheduler());
    assertThat(RuntimeEnvironment.getMasterScheduler())
        .isSameAs(ShadowApplication.getInstance().getForegroundThreadScheduler());
  }

  @Test
  public void setUpApplicationState_setsBackgroundScheduler_toBeSameAsForeground_whenAdvancedScheduling() {
    RoboSettings.setUseGlobalScheduler(true);
    try {
      bootstrapWrapper.callSetUpApplicationState();
      final ShadowApplication shadowApplication =
          Shadow.extract(ApplicationProvider.getApplicationContext());
      assertThat(shadowApplication.getBackgroundThreadScheduler())
          .isSameAs(shadowApplication.getForegroundThreadScheduler());
      assertThat(RuntimeEnvironment.getMasterScheduler())
          .isSameAs(RuntimeEnvironment.getMasterScheduler());
    } finally {
      RoboSettings.setUseGlobalScheduler(false);
    }
  }

  @Test
  public void setUpApplicationState_setsBackgroundScheduler_toBeDifferentToForeground_byDefault() {
    bootstrapWrapper.callSetUpApplicationState();
    final ShadowApplication shadowApplication =
        Shadow.extract(ApplicationProvider.getApplicationContext());
    assertThat(shadowApplication.getBackgroundThreadScheduler())
        .isNotSameAs(shadowApplication.getForegroundThreadScheduler());
  }

  @Test
  public void setUpApplicationState_setsMainThread() {
    RuntimeEnvironment.setMainThread(new Thread());
    assertThat(RuntimeEnvironment.isMainThread()).isFalse();
    bootstrapWrapper.callSetUpApplicationState();
    assertThat(RuntimeEnvironment.isMainThread()).isTrue();
  }

  @Test
  public void setUpApplicationState_setsMainThread_onAnotherThread() throws InterruptedException {
    final AtomicBoolean res = new AtomicBoolean();
    Thread t =
        new Thread(() -> {
          bootstrapWrapper.callSetUpApplicationState();
          res.set(RuntimeEnvironment.isMainThread());
        });
    t.start();
    t.join(0);
    assertThat(res.get()).isTrue();
    assertThat(RuntimeEnvironment.isMainThread()).isFalse();
  }

  @Test
  public void ensureBouncyCastleInstalled() throws CertificateException {
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    assertThat(factory.getProvider().getName()).isEqualTo(BouncyCastleProvider.PROVIDER_NAME);
  }

  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfig() {
    String givenQualifiers = "";
    bootstrapWrapper.config = new Config.Builder().setQualifiers(givenQualifiers).build();
    bootstrapWrapper.callSetUpApplicationState();
    assertThat(RuntimeEnvironment.getQualifiers()).contains("v" + Build.VERSION.RESOURCES_SDK_INT);
  }

  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfigWithOtherQualifiers() {
    String givenQualifiers = "large-land";
    bootstrapWrapper.config = new Config.Builder().setQualifiers(givenQualifiers).build();
    bootstrapWrapper.callSetUpApplicationState();

    String optsForO = RuntimeEnvironment.getApiLevel() >= O
        ? "nowidecg-lowdr-"
        : "";
    assertThat(RuntimeEnvironment.getQualifiers())
        .contains("large-notlong-notround-" + optsForO + "land-notnight-mdpi-finger-keyssoft"
            + "-nokeys-navhidden-nonav-v"
            + Build.VERSION.RESOURCES_SDK_INT);
  }

  @Test
  public void setUpApplicationState_shouldCreateStorageDirs() throws Exception {
    bootstrapWrapper.callSetUpApplicationState();
    ApplicationInfo applicationInfo = ApplicationProvider.getApplicationContext()
        .getApplicationInfo();

    assertThat(applicationInfo.sourceDir).isNotNull();
    assertThat(new File(applicationInfo.sourceDir).exists()).isTrue();

    assertThat(applicationInfo.publicSourceDir).isNotNull();
    assertThat(new File(applicationInfo.publicSourceDir).exists()).isTrue();

    assertThat(applicationInfo.dataDir).isNotNull();
    assertThat(new File(applicationInfo.dataDir).isDirectory()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void setUpApplicationState_shouldCreateStorageDirs_Nplus() throws Exception {
    bootstrapWrapper.callSetUpApplicationState();
    ApplicationInfo applicationInfo = ApplicationProvider.getApplicationContext()
        .getApplicationInfo();

    assertThat(applicationInfo.credentialProtectedDataDir).isNotNull();
    assertThat(new File(applicationInfo.credentialProtectedDataDir).isDirectory()).isTrue();

    assertThat(applicationInfo.deviceProtectedDataDir).isNotNull();
    assertThat(new File(applicationInfo.deviceProtectedDataDir).isDirectory()).isTrue();
  }

  @Test
  public void tearDownApplication_invokesOnTerminate() {
    RuntimeEnvironment.application = mock(Application.class);
    pu.tearDownApplication();
    verify(RuntimeEnvironment.application).onTerminate();
  }

  @Test
  public void testResourceNotFound() {
    // not relevant for binary resources mode
    assumeTrue(pu.isLegacyResourceMode());

    try {
      bootstrapWrapper.appManifest = new ThrowingManifest(bootstrapWrapper.appManifest);
      bootstrapWrapper.callSetUpApplicationState();
      fail("Expected to throw");
    } catch (Resources.NotFoundException expected) {
      // expected
    }
  }

  /** Can't use Mockito for classloader issues */
  static class ThrowingManifest extends AndroidManifest {
    public ThrowingManifest(AndroidManifest androidManifest) {
      super(
          androidManifest.getAndroidManifestFile(),
          androidManifest.getResDirectory(),
          androidManifest.getAssetsDirectory(),
          androidManifest.getLibraryManifests(),
          null,
          androidManifest.getApkFile());
    }

    @Override
    public void initMetaData(ResourceTable resourceTable) throws RoboNotFoundException {
      throw new RoboNotFoundException("This is just a test");
    }
  }

  @Test @Config(qualifiers = "b+fr+Cyrl+UK")
  public void localeIsSet() throws Exception {
    bootstrapWrapper.callSetUpApplicationState();
    assertThat(Locale.getDefault().getLanguage()).isEqualTo("fr");
    assertThat(Locale.getDefault().getScript()).isEqualTo("Cyrl");
    assertThat(Locale.getDefault().getCountry()).isEqualTo("UK");
  }

  @Test @Config(qualifiers = "w123dp-h456dp")
  public void whenNotPrefixedWithPlus_setQualifiers_shouldNotBeBasedOnPreviousConfig() throws Exception {
    bootstrapWrapper.callSetUpApplicationState();
    RuntimeEnvironment.setQualifiers("land");
    assertThat(RuntimeEnvironment.getQualifiers()).contains("w470dp-h320dp");
    assertThat(RuntimeEnvironment.getQualifiers()).contains("-land-");
  }

  @Test @Config(qualifiers = "w100dp-h125dp")
  public void whenDimensAndSizeSpecified_setQualifiers_should() throws Exception {
    bootstrapWrapper.callSetUpApplicationState();
    RuntimeEnvironment.setQualifiers("+xlarge");
    Configuration configuration = Resources.getSystem().getConfiguration();
    assertThat(configuration.screenWidthDp).isEqualTo(ScreenSize.xlarge.width);
    assertThat(configuration.screenHeightDp).isEqualTo(ScreenSize.xlarge.height);
    assertThat(DeviceConfig.getScreenSize(configuration)).isEqualTo(ScreenSize.xlarge);
  }

  @Test @Config(qualifiers = "w123dp-h456dp")
  public void whenPrefixedWithPlus_setQualifiers_shouldBeBasedOnPreviousConfig() throws Exception {
    bootstrapWrapper.callSetUpApplicationState();
    RuntimeEnvironment.setQualifiers("+w124dp");
    assertThat(RuntimeEnvironment.getQualifiers()).contains("w124dp-h456dp");
  }
}
