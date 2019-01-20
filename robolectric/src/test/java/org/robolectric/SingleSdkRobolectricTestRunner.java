package org.robolectric;

import android.os.Build;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.UsesSdk;
import org.robolectric.util.TestUtil;
import org.robolectric.util.inject.Injector;

class SingleSdkRobolectricTestRunner extends RobolectricTestRunner {

  private static final Injector INJECTOR = defaultInjector()
      .register(SdkPicker.class, SingleSdkPicker.class);

  SingleSdkRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass, INJECTOR);
  }

  @Override
  ResourcesMode getResourcesMode() {
    return ResourcesMode.binary;
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
    public List<Sdk> selectSdks(Config config, UsesSdk usesSdk) {
      return Collections.singletonList(sdk);
    }
  }
}
