package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import org.robolectric.annotation.GetInstallerPackageNameMode;
import org.robolectric.pluginapi.config.Configurer;

/**
 * Provides configuration to Robolectric for its &#064;{@link GetInstallerPackageNameMode}
 * annotation.
 */
@AutoService(Configurer.class)
public class GetInstallerPackageNameModeConfigurer
    implements Configurer<GetInstallerPackageNameMode.Mode> {

  @Override
  public Class<GetInstallerPackageNameMode.Mode> getConfigClass() {
    return GetInstallerPackageNameMode.Mode.class;
  }

  @Nonnull
  @Override
  public GetInstallerPackageNameMode.Mode defaultConfig() {
    // TODO: switch to REALISTIC
    return GetInstallerPackageNameMode.Mode.LEGACY;
  }

  @Override
  public GetInstallerPackageNameMode.Mode getConfigFor(@Nonnull String packageName) {
    try {
      Package pkg = Class.forName(packageName + ".package-info").getPackage();
      return valueFrom(pkg.getAnnotation(GetInstallerPackageNameMode.class));
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  @Override
  public GetInstallerPackageNameMode.Mode getConfigFor(@Nonnull Class<?> testClass) {
    return valueFrom(testClass.getAnnotation(GetInstallerPackageNameMode.class));
  }

  @Override
  public GetInstallerPackageNameMode.Mode getConfigFor(@Nonnull Method method) {
    return valueFrom(method.getAnnotation(GetInstallerPackageNameMode.class));
  }

  @Nonnull
  @Override
  public GetInstallerPackageNameMode.Mode merge(
      @Nonnull GetInstallerPackageNameMode.Mode parentConfig,
      @Nonnull GetInstallerPackageNameMode.Mode childConfig) {
    // just take the childConfig - since GetInstallerPackageNameMode only has a single 'value'
    // attribute
    return childConfig;
  }

  private static GetInstallerPackageNameMode.Mode valueFrom(
      GetInstallerPackageNameMode looperMode) {
    return looperMode == null ? null : looperMode.value();
  }
}
