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

  private final ResourceIdGenerator androidResourceIdGenerator = new ResourceIdGenerator(0x01);
  private final String packageName;
  private int packageIdentifier;

  public PackageResourceIndex(String packageName) {
    this.packageName = packageName;
  }

  @Override
  public synchronized Integer getResourceId(ResName resName) {
    if (resName == null) {
      return null;
    }
    Integer id = resourceTable.inverse().get(resName);
    if (id == null && isAndroidPackage(resName)) {
      id = androidResourceIdGenerator.generate(resName.type, resName.name);
      resourceTable.put(id, resName);
      LOGGER.fine("no id mapping found for " + resName.getFullyQualifiedName() + "; assigning ID #0x" + Integer.toHexString(id));
    }
    if (id == null) return 0;

    return id;
  }

  private boolean isAndroidPackage(ResName resName) {
    return "android".equals(resName.packageName) || "".equals(resName.packageName);
  }

  @Override
  public synchronized ResName getResName(int resourceId) {
    return resourceTable.get(resourceId);
  }

  @Override
  public synchronized void dump() {
    System.out.println(resourceTable);
  }

  public String getPackageName() {
    return packageName;
  }

  int getPackageIdentifier() {
    return packageIdentifier;
  }

  synchronized void addResource(int id, String type, String name) {
    if (ResourceIds.isFrameworkResource(id)) {
      androidResourceIdGenerator.record(id, type, name);
    }
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
