package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link ResourceTable} for a single package, e.g: "android" / ox01
 */
public class PackageResourceTable implements ResourceTable {
  private final ResBunch values = new ResBunch();
  private final ResBundle xmlDocuments = new ResBundle();
  private final ResBundle rawResources = new ResBundle();
  private final ResourceIndex resourceIndex;

  PackageResourceTable(String packageName) {
    this.resourceIndex = new ResourceIndex(packageName);
  }

  public String getPackageName() {
    return resourceIndex.getPackageName();
  }

  @Override
  public Integer getResourceId(ResName resName) {
    return resourceIndex.getResourceId(resName);
  }

  @Override
  public ResName getResName(int resourceId) {
    return resourceIndex.getResName(resourceId);
  }

  @Override
  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return values.get(resName, qualifiers);
  }

  @Override
  public TypedResource getValue(int resId, String qualifiers) {
    return values.get(getResName(resId), qualifiers);
  }

  public XmlBlock getXml(ResName resName, String qualifiers) {
    TypedResource typedResource = xmlDocuments.get(resName, qualifiers);
    return typedResource == null ? null : (XmlBlock) typedResource.getData();
  }

  public InputStream getRawValue(ResName resName, String qualifiers) {
    TypedResource typedResource = rawResources.get(resName, qualifiers);
    FsFile file = typedResource == null ? null : (FsFile) typedResource.getData();
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

  public int getPackageIdentifier() {
    return resourceIndex.getPackageIdentifier();
  }

  @Override
  public void receive(Visitor visitor) {
    values.receive(visitor);
  }

  @Override
  public boolean hasValue(ResName resName, String qualifiers) {
    return getValue(resName, qualifiers) != null
        || getXml(resName, qualifiers) != null
        || getRawValue(resName, qualifiers) != null;
  }

  void addResource(int resId, String type, String name) {
    resourceIndex.addResource(resId, type, name);
  }

  void addValue(String resourceType, String name, TypedResource valueWithType) {
    values.put(resourceType, name, valueWithType);
  }

  void addXml(String resourceType, String baseName, TypedResource<XmlBlock> xmlBlockTypedResource) {
    xmlDocuments.put(resourceType, baseName, xmlBlockTypedResource);
  }

  void addRaw(String resourceType, String name, TypedResource<FsFile> value) {
    rawResources.put(resourceType, name, value);
  }
}
