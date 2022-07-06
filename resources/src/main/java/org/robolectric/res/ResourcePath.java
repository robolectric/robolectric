package org.robolectric.res;

import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("NewApi")
public class ResourcePath {
  private final Class<?> rClass;
  private final Path resourceBase;
  private final Path assetsDir;
  private final Class<?> internalRClass;

  public ResourcePath(Class<?> rClass, Path resourceBase, Path assetsDir) {
    this(rClass, resourceBase, assetsDir, null);
  }

  public ResourcePath(Class<?> rClass, Path resourceBase, Path assetsDir, Class<?> internalRClass) {
    this.rClass = rClass;
    this.resourceBase = resourceBase;
    this.assetsDir = assetsDir;
    this.internalRClass = internalRClass;
  }

  public Class<?> getRClass() {
    return rClass;
  }

  public Path getResourceBase() {
    return resourceBase;
  }

  public boolean hasResources() {
    return getResourceBase() != null && Files.exists(getResourceBase());
  }

  public Path getAssetsDir() {
    return assetsDir;
  }

  public Class<?> getInternalRClass() {
    return internalRClass;
  }

  @Override
  public String toString() {
    return "ResourcePath { path=" + resourceBase + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ResourcePath that = (ResourcePath) o;

    if (rClass != null ? !rClass.equals(that.rClass) : that.rClass != null) return false;
    if (resourceBase != null ? !resourceBase.equals(that.resourceBase) : that.resourceBase != null) return false;
    if (assetsDir != null ? !assetsDir.equals(that.assetsDir) : that.assetsDir != null) return false;
    return internalRClass != null ? internalRClass.equals(that.internalRClass) : that.internalRClass == null;

  }

  @Override
  public int hashCode() {
    int result = rClass != null ? rClass.hashCode() : 0;
    result = 31 * result + (resourceBase != null ? resourceBase.hashCode() : 0);
    result = 31 * result + (assetsDir != null ? assetsDir.hashCode() : 0);
    result = 31 * result + (internalRClass != null ? internalRClass.hashCode() : 0);
    return result;
  }
}
