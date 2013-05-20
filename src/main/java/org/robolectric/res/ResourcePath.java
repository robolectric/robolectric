package org.robolectric.res;

public class ResourcePath {
  public final Class<?> rClass;
  public final String packageName;
  public final FsFile resourceBase;
  public final FsFile assetsDir;
  public final FsFile rawDir;

  public ResourcePath(Class<?> rClass, String packageName, FsFile resourceBase, FsFile assetsDir) {
    this.rClass = rClass;
    this.packageName = packageName;
    this.resourceBase = resourceBase;
    this.assetsDir = assetsDir;
    FsFile rawDir = resourceBase.join("raw");
    this.rawDir = rawDir.exists() ? rawDir : null;
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
    if (rClass != null ? !rClass.equals(that.rClass) : that.rClass != null) return false;
    if (!rawDir.equals(that.rawDir)) return false;
    if (!resourceBase.equals(that.resourceBase)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = rClass != null ? rClass.hashCode() : 0;
    result = 31 * result + packageName.hashCode();
    result = 31 * result + resourceBase.hashCode();
    result = 31 * result + assetsDir.hashCode();
    result = 31 * result + rawDir.hashCode();
    return result;
  }
}
