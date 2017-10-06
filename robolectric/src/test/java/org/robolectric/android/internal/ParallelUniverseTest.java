package org.robolectric.android.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.content.res.Resources;
import android.os.Build;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.R;
import org.robolectric.RoboSettings;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.RoboNotFoundException;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.ResourceTableFactory;
import org.robolectric.res.RoutingResourceTable;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
public class ParallelUniverseTest {

  private ParallelUniverse pu;

  private static Config getDefaultConfig() {
    return new Config.Builder().build();
  }

  @Before
  public void setUp() throws InitializationError {
    pu = new ParallelUniverse();
    pu.setSdkConfig(new SdkConfig(Build.VERSION.SDK_INT));
  }

  public void dummyMethodForTest() {}

  private static Method getDummyMethodForTest() {
    try {
      return ParallelUniverseTest.class.getMethod("dummyMethodForTest");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private void setUpApplicationState(Config defaultConfig, AndroidManifest appManifest) {
    ResourceTable sdkResourceProvider = new ResourceTableFactory().newFrameworkResourceTable(new ResourcePath(android.R.class, null, null));
    final RoutingResourceTable routingResourceTable = new RoutingResourceTable(new ResourceTableFactory().newResourceTable("org.robolectric", new ResourcePath(R.class, null, null)));
    Method method = getDummyMethodForTest();
    pu.setUpApplicationState(
        method,
        new DefaultTestLifecycle(),
        appManifest,
        defaultConfig,
        sdkResourceProvider,
        routingResourceTable,
        RuntimeEnvironment.getSystemResourceTable());
  }

  private AndroidManifest dummyManifest() {
    return new AndroidManifest(null, null, null, "package");
  }

  @Test
  public void setUpApplicationState_configuresGlobalScheduler() {
    RuntimeEnvironment.setMasterScheduler(null);
    setUpApplicationState(getDefaultConfig(), dummyManifest());
    assertThat(RuntimeEnvironment.getMasterScheduler())
        .isNotNull()
        .isSameAs(ShadowLooper.getShadowMainLooper().getScheduler())
        .isSameAs(ShadowApplication.getInstance().getForegroundThreadScheduler());
  }

  @Test
  public void setUpApplicationState_setsBackgroundScheduler_toBeSameAsForeground_whenAdvancedScheduling() {
    RoboSettings.setUseGlobalScheduler(true);
    try {
      setUpApplicationState(getDefaultConfig(), dummyManifest());
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
    setUpApplicationState(getDefaultConfig(), dummyManifest());
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    assertThat(shadowApplication.getBackgroundThreadScheduler())
        .isNotSameAs(shadowApplication.getForegroundThreadScheduler());
  }

  @Test
  public void setUpApplicationState_setsMainThread() {
    RuntimeEnvironment.setMainThread(new Thread());
    setUpApplicationState(getDefaultConfig(), dummyManifest());
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
    Thread t =
        new Thread() {
          @Override
          public void run() {
            setUpApplicationState(getDefaultConfig(), ParallelUniverseTest.this.dummyManifest());
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
    setUpApplicationState(c, dummyManifest());
    assertThat(RuntimeEnvironment.getQualifiers()).contains("v" + Build.VERSION.SDK_INT);
  }

  @Test
  public void setUpApplicationState_setsVersionQualifierFromConfigQualifiers() {
    String givenQualifiers = "land-v17";
    Config c = new Config.Builder().setQualifiers(givenQualifiers).build();
    setUpApplicationState(c, dummyManifest());
    assertThat(RuntimeEnvironment.getQualifiers()).contains("land-v17");
  }

  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfigWithOtherQualifiers() {
    String givenQualifiers = "large-land";
    Config c = new Config.Builder().setQualifiers(givenQualifiers).build();
    setUpApplicationState(c, dummyManifest());
    assertThat(RuntimeEnvironment.getQualifiers()).contains("large-land-v" + Build.VERSION.SDK_INT);
  }

  @Test
  public void tearDownApplication_invokesOnTerminate() {
    RuntimeEnvironment.application = mock(Application.class);
    pu.tearDownApplication();
    verify(RuntimeEnvironment.application).onTerminate();
  }

  @Test
  public void testResourceNotFound() {
    try {
      setUpApplicationState(getDefaultConfig(), new ThrowingManifest());
      fail("Expected to throw");
    } catch (Resources.NotFoundException expected) {
      // expected
    }
  }

  /** Can't use Mockito for classloader issues */
  static class ThrowingManifest extends AndroidManifest {
    public ThrowingManifest() {
      super(null, null, null);
    }

    @Override
    public void initMetaData(ResourceTable resourceTable) throws RoboNotFoundException {
      throw new RoboNotFoundException("This is just a test");
    }
  }
}
