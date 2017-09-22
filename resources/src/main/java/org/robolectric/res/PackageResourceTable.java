package org.robolectric.res;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import org.robolectric.res.builder.XmlBlock;

/**
 * A {@link ResourceTable} for a single package, e.g: "android" / ox01
 */
public class PackageResourceTable implements ResourceTable {

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

  int getPackageIdentifier() {
    return packageIdentifier;
  }

  @Override
  public Integer getResourceId(ResName resName) {
    Integer id = resourceTable.inverse().get(resName);
    return id != null ? id : 0;
  }

  @Override
  public ResName getResName(int resourceId) {
    return resourceTable.get(resourceId);
  }

  @Override
  public TypedResource getValue(@Nonnull ResName resName, String qualifiers) {
    return resources.get(resName, qualifiers);
  }

  @Override
  public TypedResource getValue(int resId, String qualifiers) {
    return resources.get(getResName(resId), qualifiers);
  }

  @Override public XmlBlock getXml(ResName resName, String qualifiers) {
    FileTypedResource fileTypedResource = getFileResource(resName, qualifiers);
    if (fileTypedResource == null || !fileTypedResource.isXml()) {
      return null;
    } else {
      return XmlBlock.create(fileTypedResource.getFsFile(), resName.packageName);
    }
  }

  @Override public InputStream getRawValue(ResName resName, String qualifiers) {
    FileTypedResource fileTypedResource = getFileResource(resName, qualifiers);
    if (fileTypedResource == null) {
      return null;
    } else {
      FsFile file = fileTypedResource.getFsFile();
      try {
        return file == null ? null : file.getInputStream();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private FileTypedResource getFileResource(ResName resName, String qualifiers) {
    TypedResource typedResource = resources.get(resName, qualifiers);
    if (!(typedResource instanceof FileTypedResource)) {
      return null;
    } else {
      return (FileTypedResource) typedResource;
    }
  }

  @Override
  public InputStream getRawValue(int resId, String qualifiers) {
    return getRawValue(getResName(resId), qualifiers);
  }

  @Override
  public void receive(Visitor visitor) {
    resources.receive(visitor);
  }

  void addResource(int resId, String type, String name) {
      if (ResourceIds.isFrameworkResource(resId)) {
        androidResourceIdGenerator.record(resId, type, name);
      }
      ResName resName = new ResName(packageName, type, name);
      int resIdPackageIdentifier = ResourceIds.getPackageIdentifier(resId);
      if (getPackageIdentifier() == 0) {
        this.packageIdentifier = resIdPackageIdentifier;
      } else if (getPackageIdentifier() != resIdPackageIdentifier) {
        throw new IllegalArgumentException("Incompatible package for " + packageName + ":" + type + "/" + name + " with resId " + resIdPackageIdentifier + " to ResourceIndex with packageIdentifier " + getPackageIdentifier());
      }

      ResName existingEntry = resourceTable.put(resId, resName);
      if (existingEntry != null && !existingEntry.equals(resName)) {
        throw new IllegalArgumentException("ResId " + Integer.toHexString(resId) + " mapped to both " + resName + " and " + existingEntry);
      }
  }

  void addResource(String type, String name, TypedResource value) {
    ResName resName = new ResName(packageName, type, name);
    Integer id = resourceTable.inverse().get(resName);
    if (id == null && isAndroidPackage(resName)) {
      id = androidResourceIdGenerator.generate(type, name);
      ResName existing = resourceTable.put(id, resName);
      if (existing != null) {
        throw new IllegalStateException(resName + " assigned ID to already existing " + existing);
      }
    }
    resources.put(resName, value);
  }

  private boolean isAndroidPackage(ResName resName) {
    return "android".equals(resName.packageName);
  }
}
