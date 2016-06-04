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
    if (!(o instanceof ResourcePath)) return false;

    ResourcePath that = (ResourcePath) o;

    if (!assetsDir.equals(that.assetsDir)) return false;
    if (!packageName.equals(that.packageName)) return false;
    if (!resourceBase.equals(that.resourceBase)) return false;

    return Arrays.equals(rClasses, that.rClasses);
  }

  @Override
  public int hashCode() {
    int result = 31 * packageName.hashCode();
    result = 31 * result + resourceBase.hashCode();
    result = 31 * result + assetsDir.hashCode();

    result = 31 * result + Arrays.hashCode(rClasses);
    return result;
  }
}
