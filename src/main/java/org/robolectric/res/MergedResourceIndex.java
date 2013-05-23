package org.robolectric.res;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MergedResourceIndex extends ResourceIndex {
  private final ResourceIndex[] subIndexes;
  private final Set<String> actualPackageNames = new HashSet<String>();

  public MergedResourceIndex(ResourceIndex... subIndexes) {
    this.subIndexes = subIndexes;
    for (ResourceIndex subIndex : subIndexes) {
      actualPackageNames.addAll(subIndex.getPackages());
      merge(resourceNameToId, subIndex.resourceNameToId, "resourceNameToId");
      merge(resourceIdToResName, subIndex.resourceIdToResName, "resourceIdToResName");
    }
  }

  private static <K,V> void merge(Map<K, V> map1, Map<K, V> map2, String name) {
    int expected = map1.size() + map2.size();
    map1.putAll(map2);
    if (map1.size() != expected) {
      throw new IllegalStateException("there must have been some overlap for " + name + "! expected " + expected + " but got " + map1.size());
    }
  }

  @Override
  public Integer getResourceId(ResName resName) {
    // todo: this is pretty silly...
    Integer id = resourceNameToId.get(resName);
    if (id == null) {
      for (ResourceIndex subIndex : subIndexes) {
        id = subIndex.getResourceId(resName);
        if (id != null) return id;
      }
    }
    return id;
  }

  @Override
  public ResName getResName(int resourceId) {
    // todo: this is pretty silly...
    ResName resName = resourceIdToResName.get(resourceId);
    if (resName == null) {
      for (ResourceIndex subIndex : subIndexes) {
        resName = subIndex.getResName(resourceId);
        if (resName != null) return resName;
      }
    }
    return resName;
  }

  @Override public Collection<String> getPackages() {
    return actualPackageNames;
  }
}