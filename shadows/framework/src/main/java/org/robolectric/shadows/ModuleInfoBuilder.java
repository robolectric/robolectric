package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.pm.ModuleInfo;
import androidx.annotation.Nullable;

/**
 * Builder for {@link ModuleInfo} as ModuleInfo has hidden constructors, this builder class has been
 * added as a way to make custom ModuleInfo objects when needed.
 */
public final class ModuleInfoBuilder {

  @Nullable private CharSequence name;
  @Nullable private String packageName;
  private boolean hidden = false;

  private ModuleInfoBuilder() {}

  /**
   * Start building a new ModuleInfo
   *
   * @return a new instance of {@link ModuleInfoBuilder}.
   */
  public static ModuleInfoBuilder newBuilder() {
    return new ModuleInfoBuilder();
  }

  /** Sets the public name of the module */
  public ModuleInfoBuilder setName(CharSequence name) {
    this.name = name;
    return this;
  }

  /** Sets the package name of the module */
  public ModuleInfoBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  /** Sets whether or not the module is hidden */
  public ModuleInfoBuilder setHidden(boolean hidden) {
    this.hidden = hidden;
    return this;
  }

  /**
   * Returns a {@link ModuleInfo} with the data that was given. Both name and packageName are
   * mandatory to build, but hidden is optional, if no value was given will default to false
   */
  public ModuleInfo build() {
    // Check mandatory fields.
    checkNotNull(name, "Mandatory field 'name' missing.");
    checkNotNull(packageName, "Mandatory field 'packageName' missing.");

    ModuleInfo moduleInfo = new ModuleInfo();
    moduleInfo.setName(name);
    moduleInfo.setPackageName(packageName);
    moduleInfo.setHidden(hidden);

    return moduleInfo;
  }
}
