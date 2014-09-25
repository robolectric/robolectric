package org.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import android.content.res.Resources;
import android.content.res.Configuration;
import org.robolectric.internal.ParallelUniverse;
import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.*;

@RunWith(TestRunners.WithDefaults.class)
public class ParallelUniverseTest {
  
  private ParallelUniverse pu;

  @Before
  public void setUp() {
    pu = new ParallelUniverse(mock(RobolectricTestRunner.class));
    pu.setSdkConfig(new SdkConfig(18));
  }
  
  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfig() {
    String givenQualifiers = "";
    Config c = new Config.Implementation(-1, Config.DEFAULT, givenQualifiers, "res", -1, new Class[0], Application.class, new String[0]);
    pu.setUpApplicationState(null, new DefaultTestLifecycle(), false, null, null, c);
    assertThat(getQualifiersfromSystemResources()).isEqualTo("v18");
    assertThat(getQualifiersFromAppAssetManager()).isEqualTo("v18");
    assertThat(getQualifiersFromSystemAssetManager()).isEqualTo("v18");
  }
  
  @Test
  public void setUpApplicationState_setsVersionQualifierFromConfigQualifiers() {
    String givenQualifiers = "land-v17";
    Config c = new Config.Implementation(-1, Config.DEFAULT, givenQualifiers, "res", -1, new Class[0], Application.class, new String[0]);
    pu.setUpApplicationState(null, new DefaultTestLifecycle(), false, null, null, c);
    assertThat(getQualifiersfromSystemResources()).isEqualTo("land-v17");
    assertThat(getQualifiersFromAppAssetManager()).isEqualTo("land-v17");
    assertThat(getQualifiersFromSystemAssetManager()).isEqualTo("land-v17");
  }
  
  @Test
  public void setUpApplicationState_setsVersionQualifierFromSdkConfigWithOtherQualifiers() {
    String givenQualifiers = "large-land";
    Config c = new Config.Implementation(-1, Config.DEFAULT, givenQualifiers, "res", -1, new Class[0], Application.class, new String[0]);
    pu.setUpApplicationState(null, new DefaultTestLifecycle(), false, null, null, c);
    assertThat(getQualifiersfromSystemResources()).isEqualTo("large-land-v18");
    assertThat(getQualifiersFromAppAssetManager()).isEqualTo("large-land-v18");
    assertThat(getQualifiersFromSystemAssetManager()).isEqualTo("large-land-v18");
  }
  
  private String getQualifiersfromSystemResources() {
    Resources systemResources = Resources.getSystem();
    Configuration configuration = systemResources.getConfiguration();
    return Robolectric.shadowOf(configuration).getQualifiers();
  }

  private String getQualifiersFromAppAssetManager() {
    return Robolectric.shadowOf(Robolectric.application.getResources().getAssets()).getQualifiers();
  }

  private String getQualifiersFromSystemAssetManager() {
    return Robolectric.shadowOf(Resources.getSystem().getAssets()).getQualifiers();
  }
}
