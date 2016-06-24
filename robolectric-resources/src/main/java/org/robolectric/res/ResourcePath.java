package org.robolectric.res;

import java.util.Arrays;

public class ResourcePath {
  public final String packageName;
  public final FsFile resourceBase;
  public final FsFile assetsDir;
  public Class<?>[] rClasses;

  public ResourcePath(String packageName, FsFile resourceBase, FsFile assetsDir, Class<?>... rClasses) {
    this.packageName = packageName;
    this.resourceBase = resourceBase;
    this.assetsDir = assetsDir;
    this.rClasses = rClasses;
  }

  public String getPackageName() {
    return packageName;
  }

  @Override
  public String toString() {
    return "ResourcePath{package=" + getPackageName() + ", path=" + resourceBase + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ResourcePath that = (ResourcePath) o;

    if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;
    if (resourceBase != null ? !resourceBase.equals(that.resourceBase) : that.resourceBase != null) return false;
    if (assetsDir != null ? !assetsDir.equals(that.assetsDir) : that.assetsDir != null) return false;
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Arrays.equals(rClasses, that.rClasses);

  }

  @Override
  public int hashCode() {
    int result = packageName != null ? packageName.hashCode() : 0;
    result = 31 * result + (resourceBase != null ? resourceBase.hashCode() : 0);
    result = 31 * result + (assetsDir != null ? assetsDir.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(rClasses);
    return result;
  }
}
