package org.robolectric.res;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * A {@link ResourceTable} for a single package, e.g: "android" / ox01
 */
public class PackageResourceTable implements ResourceTable {

  private static final Logger LOGGER = Logger.getLogger(PackageResourceTable.class.getName());

  private final ResBunch resources = new ResBunch();
  private final BiMap<Integer, ResName> resourceTable = HashBiMap.create();

  private final ResourceIdGenerator androidResourceIdGenerator = new ResourceIdGenerator(0x01);
  private final String packageName;
  private int packageIdentifier;

  PackageResourceTable(String packageName) {
    this.packageName = packageName;
  }

  public String getPackageName() {
    return packageName;
  }

  public synchronized Integer getResourceId(ResName resName) {
    if (resName == null) {
      return null;
    }
    Integer id = resourceTable.inverse().get(resName);
    if (id == null) return 0;

    return id;
  }

  @Override
  public synchronized ResName getResName(int resourceId) {
    return resourceTable.get(resourceId);
  }

  @Override
  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return resources.get(resName, qualifiers);
  }

  @Override
  public TypedResource getValue(int resId, String qualifiers) {
    return resources.get(getResName(resId), qualifiers);
  }

  public XmlBlock getXml(ResName resName, String qualifiers) {
    FileTypedResource typedResource = (FileTypedResource) resources.get(resName, qualifiers);
    if (typedResource == null || !typedResource.isXml()) {
      return null;
    } else {
      return XmlBlock.create(typedResource.getFsFile(), resName.packageName);
    }
  }

  public InputStream getRawValue(ResName resName, String qualifiers) {
    FileTypedResource typedResource = (FileTypedResource) resources.get(resName, qualifiers);
    FsFile file = typedResource == null ? null : typedResource.getFsFile();
    try {
      return file == null ? null : file.getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public InputStream getRawValue(int resId, String qualifiers) {
    return getRawValue(getResName(resId), qualifiers);
  }

  int getPackageIdentifier() {
    return packageIdentifier;
  }

  @Override
  public void receive(Visitor visitor) {
    resources.receive(visitor);
  }

  @Override
  public boolean hasValue(ResName resName, String qualifiers) {
    return getValue(resName, qualifiers) != null
        || getXml(resName, qualifiers) != null
        || getRawValue(resName, qualifiers) != null;
  }

  // TODO: Merge this method with the addResource(String, String, TypedResource) so that the ID is provided by the
  // caller. When we start to read arsc files the ID will already be provided so we want to write the id, name, type and
  // value information in the same call rather than split up like this.
  synchronized void addResource(int id, String type, String name) {
    // We need to record the resource identifiers for Android as they are not generated with AAPT and are only selectively
    // available in the android.R / com.android.internal.R so we will generate identifiers where they don't exist as they
    // are written into the resource table.
    if (ResourceIds.isFrameworkResource(id)) {
      androidResourceIdGenerator.record(id, type, name);
    }
    ResName resName = new ResName(packageName, type, name);
    int resIdPackageIdentifier = ResourceIds.getPackageIdentifier(id);
    if (getPackageIdentifier() == 0) {
      this.packageIdentifier = resIdPackageIdentifier;
    } else if (getPackageIdentifier() != resIdPackageIdentifier) {
      throw new IllegalArgumentException("Attempted to add resId " + resIdPackageIdentifier + " to ResourceIndex with packageIdentifier " + getPackageIdentifier());
    }

    ResName existingEntry = resourceTable.put(id, resName);
    if (existingEntry != null && !existingEntry.equals(resName)) {
      throw new IllegalArgumentException("ResId " + Integer.toHexString(id) + " mapped to both " + resName + " and " + existingEntry);
    }
  }

  void addResource(String type, String name, TypedResource value) {
    if (isAndroidPackage(packageName)) {
      ResName resName = new ResName(packageName, type, name);
      if (!resourceTable.containsValue(resName)) {
        int generatedId = androidResourceIdGenerator.generate(resName.type, resName.name);
        resourceTable.put(generatedId, resName);
        LOGGER.fine("no id mapping found for " + resName.getFullyQualifiedName() + "; assigning ID #0x" + Integer.toHexString(generatedId));
      }
    }

    resources.put(type, name, value);
  }

  private boolean isAndroidPackage(String packageName) {
    return "android".equals(packageName) || "".equals(packageName);
  }
}
