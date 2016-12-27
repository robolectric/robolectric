package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;

public class ResourceTable {
  final ResBunch data = new ResBunch();
  final ResBundle xmlDocuments = new ResBundle();
  final ResBundle rawResources = new ResBundle();
  private final ResourceIndex resourceIndex;

  public ResourceTable(String packageName) {
    this.resourceIndex = new ResourceIndex(packageName);;
  }

  public String getPackageName() {
    return resourceIndex.getPackageName();
  }

  public Integer getResourceId(ResName resName) {
    return resourceIndex.getResourceId(resName);
  }

  public ResName getResName(int resourceId) {
    return resourceIndex.getResName(resourceId);
  }

  public Integer getResourceId(ResName resName) {
    return resourceIndex.getResourceId(resName);
  }

  public ResName getResName(int resourceId) {
    return resourceIndex.getResName(resourceId);
  }

  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return data.get(resName, qualifiers);
  }

  XmlBlock getXml(ResName resName, String qualifiers) {
    TypedResource typedResource = xmlDocuments.get(resName, qualifiers);
    return typedResource == null ? null : (XmlBlock) typedResource.getData();
  }

  InputStream getRawValue(ResName resName, String qualifiers) {
    TypedResource typedResource = rawResources.get(resName, qualifiers);
    FsFile file = typedResource == null ? null : (FsFile) typedResource.getData();
    try {
      return file == null ? null : file.getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int getPackageIdentifier() {
    return resourceIndex.getPackageIdentifier();
  }

  public void addResource(int resId, String type, String name) {
    resourceIndex.addResource(resId, type, name);
  }
}
