package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.robolectric.res.builder.XmlBlock;

public class RoutingResourceTable implements ResourceTable {
  private static final PackageResourceTable EMPTY_RESOURCE_TABLE = ResourceTableFactory.newResourceTable("");
  private final Map<String, PackageResourceTable> resourceTables;

  public RoutingResourceTable(PackageResourceTable... resourceTables) {
    this.resourceTables = new HashMap<>();

    for (PackageResourceTable resourceTable : resourceTables) {
      this.resourceTables.put(resourceTable.getPackageName(), resourceTable);
    }
  }

  public InputStream getRawValue(int resId, String qualifiers) {
    return getRawValue(getResName(resId), qualifiers);
  }

  @Override public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return pickFor(resName).getValue(resName, qualifiers);
  }

  public TypedResource getValue(int resId, String qualifiers) {
    ResName resName = pickFor(resId).getResName(resId);
    return resName != null ? getValue(resName, qualifiers) : null;
  }

  public XmlBlock getXml(ResName resName, String qualifiers) {
    return pickFor(resName).getXml(resName, qualifiers);
  }

  public InputStream getRawValue(ResName resName, String qualifiers) {
    return pickFor(resName).getRawValue(resName, qualifiers);
  }

  @Override
  public Integer getResourceId(ResName resName) {
    return pickFor(resName).getResourceId(resName);
  }

  @Override
  public ResName getResName(int resourceId) {
    return pickFor(resourceId).getResName(resourceId);
  }

  @Override
  public void receive(Visitor visitor) {
    for (PackageResourceTable resourceTable : resourceTables.values()) {
      resourceTable.receive(visitor);
    }
  }

  private PackageResourceTable pickFor(int resId) {
    for (PackageResourceTable resourceTable : resourceTables.values()) {
      if (resourceTable.getPackageIdentifier() == ResourceIds.getPackageIdentifier(resId)) {
        return resourceTable;
      }
    }
    return EMPTY_RESOURCE_TABLE;
  }

  private PackageResourceTable pickFor(ResName resName) {
    if (resName == null) return EMPTY_RESOURCE_TABLE;
    return pickFor(resName.packageName);
  }

  private PackageResourceTable pickFor(String namespace) {
    if (namespace.equals("android.internal")) {
      return EMPTY_RESOURCE_TABLE;
    }
    PackageResourceTable resourceTable = resourceTables.get(namespace);
    if (resourceTable == null) {
      resourceTable = whichProvidesFor(namespace);
      return (resourceTable != null) ? resourceTable : EMPTY_RESOURCE_TABLE;
    }
    return resourceTable;
  }

  private PackageResourceTable whichProvidesFor(String namespace) {
    for (PackageResourceTable resourceTable : resourceTables.values()) {
      if (resourceTable.getPackageName().equals(namespace)) {
        return resourceTable;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return resourceTables.keySet().toString();
  }
}
