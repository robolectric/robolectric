package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.res.builder.XmlBlock;

public class RoutingResourceProvider extends ResourceProvider {
  private static final ResourceTable EMPTY_RESOURCE_TABLE = ResourceTableFactory.newResourceTable("");
  private final Map<String, ResourceTable> resourceTables;

  public RoutingResourceProvider(ResourceTable... resourceTables) {
    this.resourceTables = new HashMap<>();

    for (ResourceTable resourceTable : resourceTables) {
      this.resourceTables.put(resourceTable.getPackageName(), resourceTable);
    }
  }

  @Override public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return pickFor(resName).getValue(resName, qualifiers);
  }

  @Override
  public TypedResource getValue(int resId, String qualifiers) {
    ResName resName = pickFor(resId).getResName(resId);
    return resName != null ? getValue(resName, qualifiers) : null;
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    return pickFor(resName).getXml(resName, qualifiers);
  }

  @Override
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
    for (ResourceTable resourceTable : resourceTables.values()) {
      resourceTable.data.receive(visitor);
    }
  }

  private ResourceTable pickFor(int resId) {
    for (ResourceTable resourceTable : resourceTables.values()) {
      if (resourceTable.getPackageIdentifier() == ResourceIds.getPackageIdentifier(resId)) {
        return resourceTable;
      }
    }
    return EMPTY_RESOURCE_TABLE;
  }

  private ResourceTable pickFor(ResName resName) {
    if (resName == null) return EMPTY_RESOURCE_TABLE;
    return pickFor(resName.packageName);
  }

  private ResourceTable pickFor(String namespace) {
    if (namespace.equals("android.internal")) {
      return EMPTY_RESOURCE_TABLE;
    }
    ResourceTable resourceTable = resourceTables.get(namespace);
    if (resourceTable == null) {
      resourceTable = whichProvidesFor(namespace);
      return (resourceTable != null) ? resourceTable : EMPTY_RESOURCE_TABLE;
    }
    return resourceTable;
  }

  private ResourceTable whichProvidesFor(String namespace) {
    for (ResourceTable resourceTable : resourceTables.values()) {
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
