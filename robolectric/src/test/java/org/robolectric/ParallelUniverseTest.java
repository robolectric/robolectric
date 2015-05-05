package org.robolectric;

import android.app.Application;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import android.content.res.Resources;
import android.content.res.Configuration;

import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverse;
import org.robolectric.internal.SdkConfig;
import org.robolectric.res.builder.RobolectricPackageManager;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(TestRunners.WithDefaults.class)
public class ParallelUniverseTest {
  
  private ParallelUniverse pu;

  @Before
  public void setUp() throws InitializationError {
    pu = new ParallelUniverse(new RobolectricTestRunner(ParallelUniverseTest.class));
    pu.setSdkConfig(new SdkConfig(18));
  }

  @Test
  public void ensureBouncyCastleInstalled() throws CertificateException {
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    assertThat(factory.getProvider().getName()).isEqualTo(BouncyCastleProvider.PROVIDER_NAME);
  }

  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfig() {
    String givenQualifiers = "";
    Config c = new Config.Implementation(new int[0], Config.DEFAULT, givenQualifiers, "org.robolectric", "res", "assets", new Class[0], Application.class, new String[0], null);
    pu.setUpApplicationState(null, new DefaultTestLifecycle(), null, null, c);
    assertThat(getQualifiersfromSystemResources()).isEqualTo("v18");
    assertThat(getQualifiersFromAppAssetManager()).isEqualTo("v18");
    assertThat(getQualifiersFromSystemAssetManager()).isEqualTo("v18");
  }
  
  @Test
  public void setUpApplicationState_setsVersionQualifierFromConfigQualifiers() {
    String givenQualifiers = "land-v17";
    Config c = new Config.Implementation(new int[0], Config.DEFAULT, givenQualifiers, "org.robolectric", "res", "assets", new Class[0], Application.class, new String[0], null);
    pu.setUpApplicationState(null, new DefaultTestLifecycle(), null, null, c);
    assertThat(getQualifiersfromSystemResources()).isEqualTo("land-v17");
    assertThat(getQualifiersFromAppAssetManager()).isEqualTo("land-v17");
    assertThat(getQualifiersFromSystemAssetManager()).isEqualTo("land-v17");
  }
  
  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfigWithOtherQualifiers() {
    String givenQualifiers = "large-land";
    Config c = new Config.Implementation(new int[0], Config.DEFAULT, givenQualifiers, "res", "assets", "", new Class[0], Application.class, new String[0], null);
    pu.setUpApplicationState(null, new DefaultTestLifecycle(), null, null, c);
    assertThat(getQualifiersfromSystemResources()).isEqualTo("large-land-v18");
    assertThat(getQualifiersFromAppAssetManager()).isEqualTo("large-land-v18");
    assertThat(getQualifiersFromSystemAssetManager()).isEqualTo("large-land-v18");
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

  private String getQualifiersFromAppAssetManager() {
    return Shadows.shadowOf(RuntimeEnvironment.application.getResources().getAssets()).getQualifiers();
  }

  private String getQualifiersFromSystemAssetManager() {
    return Shadows.shadowOf(Resources.getSystem().getAssets()).getQualifiers();
  }
}
