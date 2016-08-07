package org.robolectric.res;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class OverlayResourceIndex extends ResourceIndex {
  private final String packageName;
  private final Set<String> actualPackageNames = new HashSet<>();

  public OverlayResourceIndex(String packageName, List<PackageResourceLoader> subResourceLoaders) {
    this(packageName, map(subResourceLoaders));
  }

  private static ResourceIndex[] map(List<PackageResourceLoader> subResourceLoaders) {
    ResourceIndex[] resourceIndexes = new ResourceIndex[subResourceLoaders.size()];
    for (int i = 0; i < subResourceLoaders.size(); i++) {
      resourceIndexes[i] = subResourceLoaders.get(i).getResourceIndex();
    }
    return resourceIndexes;
  }

  public OverlayResourceIndex(String packageName, ResourceIndex... subResourceIndexes) {
    this.packageName = packageName;
    actualPackageNames.add(packageName);

    for (ResourceIndex subResourceIndex : subResourceIndexes) {
      actualPackageNames.addAll(subResourceIndex.getPackages());

      for (Map.Entry<ResName, Integer> entry : subResourceIndex.resourceNameToId.entrySet()) {
        ResName resName = entry.getKey();
        int value = entry.getValue();
        ResName localResName = resName.withPackageName(packageName);
        if (!resourceNameToId.containsKey(localResName)) {
          resourceNameToId.put(localResName, value);
          resourceIdToResName.put(value, localResName);
        }
      }
    }
  }

  @Override
  public Integer getResourceId(ResName resName) {
    if (!actualPackageNames.contains(resName.packageName)) {
      return null;
    }
    return resourceNameToId.get(resName.withPackageName(packageName));
  }

  @Override
  public ResName getResName(int resourceId) {
    ResName resName = resourceIdToResName.get(resourceId);
    return resName == null ? null : resName.withPackageName(packageName);
  }

  @Override public Collection<String> getPackages() {
    return actualPackageNames;
  }

  @Override public String toString() {
    return "OverlayResourceIndex{" +
        "package='" + packageName + '\'' +
        '}';
  }
}
