package org.robolectric.android.internal;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.robolectric.annotation.ConscryptMode.Mode.OFF;
import static org.robolectric.annotation.ConscryptMode.Mode.ON;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import androidx.test.core.app.ApplicationProvider;
import java.io.File;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.Cipher;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BootstrapDeferringRobolectricTestRunner;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.BootstrapWrapperI;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.RoboInject;
import org.robolectric.RoboSettings;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.DeviceConfig;
import org.robolectric.android.DeviceConfig.ScreenSize;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.plugins.HierarchicalConfigurationStrategy.ConfigurationImpl;
import org.robolectric.res.ResourceTable;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;

@RunWith(BootstrapDeferringRobolectricTestRunner.class)
@LooperMode(LEGACY)
public class AndroidTestEnvironmentTest {

  @RoboInject BootstrapWrapperI bootstrapWrapper;

  @Test
  public void setUpApplicationState_configuresGlobalScheduler() {
    bootstrapWrapper.callSetUpApplicationState();

    assertThat(RuntimeEnvironment.getMasterScheduler())
        .isSameInstanceAs(ShadowLooper.getShadowMainLooper().getScheduler());
    assertThat(RuntimeEnvironment.getMasterScheduler())
        .isSameInstanceAs(ShadowApplication.getInstance().getForegroundThreadScheduler());
  }

  @Test
  public void setUpApplicationState_setsBackgroundScheduler_toBeSameAsForeground_whenAdvancedScheduling() {
    RoboSettings.setUseGlobalScheduler(true);
    try {
      bootstrapWrapper.callSetUpApplicationState();
      final ShadowApplication shadowApplication =
          Shadow.extract(ApplicationProvider.getApplicationContext());
      assertThat(shadowApplication.getBackgroundThreadScheduler())
          .isSameInstanceAs(shadowApplication.getForegroundThreadScheduler());
      assertThat(RuntimeEnvironment.getMasterScheduler())
          .isSameInstanceAs(RuntimeEnvironment.getMasterScheduler());
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
        .isNotSameInstanceAs(shadowApplication.getForegroundThreadScheduler());
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
    t.join();
    assertThat(res.get()).isTrue();
    assertThat(RuntimeEnvironment.isMainThread()).isFalse();
  }

  /**
   * Checks that crypto primitives that are available in an Android environment are also available
   * in Robolectric via {@link BouncyCastleProvider}.
   */
  @Test
  @ConscryptMode(ON)
  public void testWhenConscryptModeOn_ConscryptInstalled()
      throws CertificateException, NoSuchAlgorithmException {

    bootstrapWrapper.callSetUpApplicationState();
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    assertThat(factory.getProvider().getName()).isEqualTo("Conscrypt");

    MessageDigest digest = MessageDigest.getInstance("SHA256");
    assertThat(digest.getProvider().getName()).isEqualTo("Conscrypt");
  }

  @Test
  @ConscryptMode(ON)
  public void testWhenConscryptModeOn_BouncyCastleInstalled() throws GeneralSecurityException {
    bootstrapWrapper.callSetUpApplicationState();
    Cipher aesCipher = Cipher.getInstance("RSA/None/OAEPWithSHA-256AndMGF1Padding");
    assertThat(aesCipher.getProvider().getName()).isEqualTo(BouncyCastleProvider.PROVIDER_NAME);
  }

  @Test
  @ConscryptMode(OFF)
  public void testWhenConscryptModeOff_ConscryptNotInstalled()
      throws CertificateException, NoSuchAlgorithmException {

    bootstrapWrapper.callSetUpApplicationState();
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    assertThat(factory.getProvider().getName()).isNotEqualTo("Conscrypt");

    MessageDigest digest = MessageDigest.getInstance("SHA256");
    assertThat(digest.getProvider().getName()).isNotEqualTo("Conscrypt");
  }

  @Test
  @ConscryptMode(OFF)
  public void testWhenConscryptModeOff_BouncyCastleInstalled() throws GeneralSecurityException {

    bootstrapWrapper.callSetUpApplicationState();
    MessageDigest digest = MessageDigest.getInstance("SHA256");
    assertThat(digest.getProvider().getName()).isEqualTo(BouncyCastleProvider.PROVIDER_NAME);

    Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
    assertThat(aesCipher.getProvider().getName()).isEqualTo(BouncyCastleProvider.PROVIDER_NAME);
  }

  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdk() {
    String givenQualifiers = "";
    ConfigurationImpl config = new ConfigurationImpl();
    config.put(Config.class, new Config.Builder().setQualifiers(givenQualifiers).build());
    config.put(LooperMode.Mode.class, LEGACY);
    bootstrapWrapper.changeConfig(config);
    bootstrapWrapper.callSetUpApplicationState();
    assertThat(RuntimeEnvironment.getQualifiers()).contains("v" + Build.VERSION.RESOURCES_SDK_INT);
  }

  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkWithOtherQualifiers() {
    String givenQualifiers = "large-land";
    ConfigurationImpl config = new ConfigurationImpl();
    config.put(Config.class, new Config.Builder().setQualifiers(givenQualifiers).build());
    config.put(LooperMode.Mode.class, LEGACY);
    bootstrapWrapper.changeConfig(config);

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
    List<String> events = new ArrayList<>();
    RuntimeEnvironment.application =
        new Application() {
          @Override
          public void onTerminate() {
            super.onTerminate();
            events.add("terminated");
          }
        };
    bootstrapWrapper.tearDownApplication();
    assertThat(events).containsExactly("terminated");
  }

  @Test
  public void testResourceNotFound() {
    // not relevant for binary resources mode
    assumeTrue(bootstrapWrapper.isLegacyResources());

    try {
      bootstrapWrapper.changeAppManifest(new ThrowingManifest(bootstrapWrapper.getAppManifest()));
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

  @LazyApplication(LazyLoad.ON)
  @Test
  public void resetState_doesNotLoadApplication() {
    RuntimeEnvironment.application = null;
    assertThat(RuntimeEnvironment.application).isNull();
    bootstrapWrapper.resetState();
    assertThat(RuntimeEnvironment.application).isNull();
  }

  @LazyApplication(LazyLoad.ON)
  @Test
  public void tearDownApplication_doesNotLoadApplication() {
    bootstrapWrapper.callSetUpApplicationState();
    RuntimeEnvironment.application = null;
    bootstrapWrapper.tearDownApplication();
    assertThat(RuntimeEnvironment.application).isNull();
  }

  @LazyApplication(LazyLoad.ON)
  @Test
  @Config(qualifiers = "w480dp-h640dp-land-hdpi")
  public void systemResources_getDisplayMetrics_correctValues() {
    bootstrapWrapper.callSetUpApplicationState();
    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    assertThat(displayMetrics.densityDpi).isEqualTo(DisplayMetrics.DENSITY_HIGH);
    assertThat(RuntimeEnvironment.getQualifiers()).contains("w640dp-h480dp");
    assertThat(RuntimeEnvironment.getQualifiers()).contains("land");
  }
}
