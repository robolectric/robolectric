package org.robolectric.res;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PackageResourceIndex implements ResourceIndex {
  private static final Logger LOGGER = Logger.getLogger(ResourceExtractor.class.getName());

  private final BiMap<Integer, ResName> resourceTable = HashBiMap.create();

  private Integer maxUsedInt = null;
  private Integer generatedIdStart = null;
  private String packageName;
  private int packageIdentifier;

  public PackageResourceIndex(String packageName) {
    this.packageName = packageName;
  }

  @Override
  public synchronized Integer getResourceId(ResName resName) {
    Integer id = resourceTable.inverse().get(resName);
    if (id == null && ("android".equals(resName.packageName) || "".equals(resName.packageName))) {
      if (maxUsedInt == null) {
        maxUsedInt = resourceTable.isEmpty() ? 0 : Collections.max(resourceTable.keySet());
        generatedIdStart = maxUsedInt;
      }
      id = ++maxUsedInt;
      resourceTable.put(id, resName);
      LOGGER.fine("no id mapping found for " + resName.getFullyQualifiedName() + "; assigning ID #0x" + Integer.toHexString(id));
    }
    if (id == null) return 0;

    return id;
  }

  @Override
  public synchronized ResName getResName(int resourceId) {
    return resourceTable.get(resourceId);
  }

  @Override
  public void dump() {
    System.out.println(resourceTable);
  }

  public String getPackageName() {
    return packageName;
  }

  int getPackageIdentifier() {
    return packageIdentifier;
  }

  void addResource(int id, String type, String name) {
    ResName resName = new ResName(packageName, type, name);
    int resIdPackageIdentifier = ResourceIds.getPackageIdentifier(id);
    if (getPackageIdentifier() == 0) {
      this.packageIdentifier = resIdPackageIdentifier;
    } else if (getPackageIdentifier() != resIdPackageIdentifier) {
      throw new IllegalArgumentException("Attempted to add resId " + resIdPackageIdentifier + " to PackageResourceIndex with packageIdentifier " + getPackageIdentifier());
    }

    ResName existingEntry = resourceTable.put(id, resName);
    if (existingEntry != null && !existingEntry.equals(resName)) {
      throw new IllegalArgumentException("ResId " + Integer.toHexString(id) + " mapped to both " + resName + " and " + existingEntry);
    }
  }
}
