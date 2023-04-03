package org.robolectric.shadows;

import android.content.pm.SharedLibraryInfo;
import android.content.pm.VersionedPackage;
import android.os.Build.VERSION_CODES;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link SharedLibraryInfo}. */
public final class SharedLibraryInfoBuilder {
  private String path;
  private String packageName;
  private List<String> codePaths;
  private String name;
  private long version;
  private int type;
  private VersionedPackage declaringPackage;
  private List<VersionedPackage> dependentPackages;
  private List<SharedLibraryInfo> dependencies;
  private boolean isNative;

  private SharedLibraryInfoBuilder() {}

  public static SharedLibraryInfoBuilder newBuilder() {
    return new SharedLibraryInfoBuilder();
  }

  public SharedLibraryInfoBuilder setPath(String path) {
    this.path = path;
    return this;
  }

  public SharedLibraryInfoBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public SharedLibraryInfoBuilder setCodePaths(List<String> codePaths) {
    this.codePaths = codePaths;
    return this;
  }

  public SharedLibraryInfoBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public SharedLibraryInfoBuilder setVersion(long version) {
    this.version = version;
    return this;
  }

  public SharedLibraryInfoBuilder setType(int type) {
    this.type = type;
    return this;
  }

  public SharedLibraryInfoBuilder setDeclaringPackage(VersionedPackage declaringPackage) {
    this.declaringPackage = declaringPackage;
    return this;
  }

  public SharedLibraryInfoBuilder setDependentPackages(List<VersionedPackage> dependentPackages) {
    this.dependentPackages = dependentPackages;
    return this;
  }

  public SharedLibraryInfoBuilder setDependencies(List<SharedLibraryInfo> dependencies) {
    this.dependencies = dependencies;
    return this;
  }

  public SharedLibraryInfoBuilder setIsNative(boolean isNative) {
    this.isNative = isNative;
    return this;
  }

  public SharedLibraryInfo build() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel <= VERSION_CODES.O_MR1) {
      return ReflectionHelpers.callConstructor(
          SharedLibraryInfo.class,
          ClassParameter.from(String.class, name),
          ClassParameter.from(int.class, (int) version),
          ClassParameter.from(int.class, type),
          ClassParameter.from(VersionedPackage.class, declaringPackage),
          ClassParameter.from(List.class, dependentPackages));
    } else if (apiLevel <= VERSION_CODES.P) {
      return ReflectionHelpers.callConstructor(
          SharedLibraryInfo.class,
          ClassParameter.from(String.class, name),
          ClassParameter.from(long.class, version),
          ClassParameter.from(int.class, type),
          ClassParameter.from(VersionedPackage.class, declaringPackage),
          ClassParameter.from(List.class, dependentPackages));
    } else if (apiLevel <= VERSION_CODES.R) {
      return ReflectionHelpers.callConstructor(
          SharedLibraryInfo.class,
          ClassParameter.from(String.class, path),
          ClassParameter.from(String.class, packageName),
          ClassParameter.from(List.class, codePaths),
          ClassParameter.from(String.class, name),
          ClassParameter.from(long.class, version),
          ClassParameter.from(int.class, type),
          ClassParameter.from(VersionedPackage.class, declaringPackage),
          ClassParameter.from(List.class, dependentPackages),
          ClassParameter.from(List.class, dependencies));
    } else {
      return ReflectionHelpers.callConstructor(
          SharedLibraryInfo.class,
          ClassParameter.from(String.class, path),
          ClassParameter.from(String.class, packageName),
          ClassParameter.from(List.class, codePaths),
          ClassParameter.from(String.class, name),
          ClassParameter.from(long.class, version),
          ClassParameter.from(int.class, type),
          ClassParameter.from(VersionedPackage.class, declaringPackage),
          ClassParameter.from(List.class, dependentPackages),
          ClassParameter.from(List.class, dependencies),
          ClassParameter.from(boolean.class, isNative));
    }
  }
}
