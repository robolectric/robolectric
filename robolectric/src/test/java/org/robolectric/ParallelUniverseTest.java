package org.robolectric;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverse;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.EmptyResourceLoader;
import org.robolectric.res.ResourceExtractor;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(TestRunners.WithDefaults.class)
public class ParallelUniverseTest {

  private ParallelUniverse pu;

  private static Config getDefaultConfig() {
    return new Config.Builder().build();
  }

  @Before
  public void setUp() throws InitializationError {
    pu = new ParallelUniverse(new RobolectricTestRunner(ParallelUniverseTest.class));
    pu.setSdkConfig(new SdkConfig(18));
  }

  private void setUpApplicationState(Config defaultConfig) {
    pu.setUpApplicationState(null, new DefaultTestLifecycle(), RuntimeEnvironment.getSystemResourceLoader(),
        new EmptyResourceLoader("android", new ResourceExtractor(new ResourcePath(android.R.class, "android", null, null))),
        new AndroidManifest(null, null, null, "package"), defaultConfig);
  }

  @Test
  public void setUpApplicationState_configuresGlobalScheduler() {
    RuntimeEnvironment.setMasterScheduler(null);
    setUpApplicationState(getDefaultConfig());
    assertThat(RuntimeEnvironment.getMasterScheduler())
        .isNotNull()
        .isSameAs(ShadowLooper.getShadowMainLooper().getScheduler())
        .isSameAs(ShadowApplication.getInstance().getForegroundThreadScheduler());
  }

  @Test
  public void setUpApplicationState_setsBackgroundScheduler_toBeSameAsForeground_whenAdvancedScheduling() {
    RoboSettings.setUseGlobalScheduler(true);
    try {
      setUpApplicationState(getDefaultConfig());
      final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
      assertThat(shadowApplication.getBackgroundThreadScheduler())
          .isSameAs(shadowApplication.getForegroundThreadScheduler())
          .isSameAs(RuntimeEnvironment.getMasterScheduler());
    } finally {
      RoboSettings.setUseGlobalScheduler(false);
    }
  }

  @Test
  public void setUpApplicationState_setsBackgroundScheduler_toBeDifferentToForeground_byDefault() {
    setUpApplicationState(getDefaultConfig());
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    assertThat(shadowApplication.getBackgroundThreadScheduler())
        .isNotSameAs(shadowApplication.getForegroundThreadScheduler());
  }

  @Test
  public void setUpApplicationState_setsMainThread() {
    RuntimeEnvironment.setMainThread(new Thread());
    setUpApplicationState(getDefaultConfig());
    assertThat(RuntimeEnvironment.isMainThread()).isTrue();
  }

  @Test
  public void resetStaticStatic_setsMainThread(){
    RuntimeEnvironment.setMainThread(new Thread());
    pu.resetStaticState(getDefaultConfig());
    assertThat(RuntimeEnvironment.isMainThread()).isTrue();
  }

  @Test
  public void setUpApplicationState_setsMainThread_onAnotherThread() throws InterruptedException {
    final AtomicBoolean res = new AtomicBoolean();
    Thread t = new Thread() {
      @Override
      public void run() {
        setUpApplicationState(getDefaultConfig());
        res.set(RuntimeEnvironment.isMainThread());
      }
    };
    t.start();
    t.join(1000);
    assertThat(res.get()).isTrue();
  }

  @Test
  public void ensureBouncyCastleInstalled() throws CertificateException {
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    assertThat(factory.getProvider().getName()).isEqualTo(BouncyCastleProvider.PROVIDER_NAME);
  }

  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfig() {
    String givenQualifiers = "";
    Config c = new Config.Builder().setQualifiers(givenQualifiers).build();
    setUpApplicationState(c);
    assertThat(getQualifiersfromSystemResources()).contains("v18");
    assertThat(RuntimeEnvironment.getQualifiers()).contains("v18");
  }
  
  @Test
  public void setUpApplicationState_setsVersionQualifierFromConfigQualifiers() {
    String givenQualifiers = "land-v17";
    Config c = new Config.Builder().setQualifiers(givenQualifiers).build();
    setUpApplicationState(c);
    assertThat(getQualifiersfromSystemResources()).contains("land-v17");
    assertThat(RuntimeEnvironment.getQualifiers()).contains("land-v17");
  }
  
  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfigWithOtherQualifiers() {
    String givenQualifiers = "large-land";
    Config c = new Config.Builder().setQualifiers(givenQualifiers).build();
    setUpApplicationState(c);
    assertThat(getQualifiersfromSystemResources()).contains("large-land-v18");
    assertThat(RuntimeEnvironment.getQualifiers()).contains("large-land-v18");
  }
  
  @Test
  public void tearDownApplication_shouldNotResetPackageManager() {
    RobolectricPackageManager pm = mock(RobolectricPackageManager.class);
    RuntimeEnvironment.setRobolectricPackageManager(pm);
    pu.tearDownApplication();
    verify(pm, never()).reset();
  }
  
  @Test
  public void tearDownApplication_invokesOnTerminate() {
    RuntimeEnvironment.application = mock(Application.class);
    pu.tearDownApplication();
    verify(RuntimeEnvironment.application).onTerminate();
  }
  
  private String getQualifiersfromSystemResources() {
    Resources systemResources = Resources.getSystem();
    Configuration configuration = systemResources.getConfiguration();
    return Shadows.shadowOf(configuration).getQualifiers();
  }
}
