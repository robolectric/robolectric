package org.robolectric;

import android.os.Build;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.runners.model.InitializationError;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.UsesSdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.util.TestUtil;
import org.robolectric.util.inject.Injector;

public class SingleSdkRobolectricTestRunner extends RobolectricTestRunner {

  private static final Injector DEFAULT_INJECTOR = defaultInjector().build();

  public static Injector.Builder defaultInjector() {
    return RobolectricTestRunner.defaultInjector()
        .bind(SdkPicker.class, SingleSdkPicker.class);
  }

  public SingleSdkRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass, DEFAULT_INJECTOR);
  }

  public SingleSdkRobolectricTestRunner(Class<?> testClass, Injector injector)
      throws InitializationError {
    super(testClass, injector);
  }

  @Override
  ResModeStrategy getResModeStrategy() {
    return ResModeStrategy.binary;
  }

  public static class SingleSdkPicker implements SdkPicker {

    private final Sdk sdk;

    public SingleSdkPicker() {
      this(Build.VERSION_CODES.P);
    }

    SingleSdkPicker(int apiLevel) {
      this.sdk = TestUtil.getSdkCollection().getSdk(apiLevel);
    }

    @Nonnull
    @Override
    public List<Sdk> selectSdks(Configuration configuration, UsesSdk usesSdk) {
      return Collections.singletonList(sdk);
    }
  }
}
