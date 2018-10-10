package org.robolectric.res;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import org.robolectric.res.android.ResTable_config;
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


  public PackageResourceTable(String packageName) {
    this.packageName = packageName;
  }

  @Override
  public String getPackageName() {
    return packageName;
  }

  int getPackageIdentifier() {
    return packageIdentifier;
  }

  @Override
  public Integer getResourceId(ResName resName) {
    Integer id = resourceTable.inverse().get(resName);
    if (id == null && resName != null && resName.name.contains(".")) {
      // try again with underscores (in case we're looking in the compile-time resources, where
      // we haven't read XML declarations and only know what the R.class tells us).
      id =
          resourceTable
              .inverse()
              .get(new ResName(resName.packageName, resName.type, underscorize(resName.name)));
    }
    return id != null ? id : 0;
  }

  @Override
  public ResName getResName(int resourceId) {
    return resourceTable.get(resourceId);
  }

  @Override
  public TypedResource getValue(@Nonnull ResName resName, ResTable_config config) {
    return resources.get(resName, config);
  }

  @Override
  public TypedResource getValue(int resId, ResTable_config config) {
    return resources.get(getResName(resId), config);
  }

  @Override public XmlBlock getXml(ResName resName, ResTable_config config) {
    FileTypedResource fileTypedResource = getFileResource(resName, config);
    if (fileTypedResource == null || !fileTypedResource.isXml()) {
      return null;
    } else {
      return XmlBlock.create(fileTypedResource.getFsFile(), resName.packageName);
    }
  }

  @Override public InputStream getRawValue(ResName resName, ResTable_config config) {
    FileTypedResource fileTypedResource = getFileResource(resName, config);
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

  private FileTypedResource getFileResource(ResName resName, ResTable_config config) {
    TypedResource typedResource = resources.get(resName, config);
    if (!(typedResource instanceof FileTypedResource)) {
      return null;
    } else {
      return (FileTypedResource) typedResource;
    }
  }

  @Override
  public InputStream getRawValue(int resId, ResTable_config config) {
    return getRawValue(getResName(resId), config);
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

    // compound style names were previously registered with underscores (TextAppearance_Small)
    // because they came from R.style; re-register with dots.
    ResName resNameWithUnderscores = new ResName(packageName, type, underscorize(name));
    Integer oldId = resourceTable.inverse().get(resNameWithUnderscores);
    if (oldId != null) {
      resourceTable.forcePut(oldId, resName);
    }

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

  private static String underscorize(String s) {
    return s == null ? null : s.replace('.', '_');
  }
}
