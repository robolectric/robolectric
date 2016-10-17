package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

// TODO: Give me a better name
abstract class XResourceLoader extends ResourceLoader {
  final ResBunch data = new ResBunch();
  final ResBundle<XmlBlock> xmlDocuments = new ResBundle<>();
  final ResBundle<FsFile> rawResources = new ResBundle<>();
  
  private final ResourceIndex resourceIndex;
  private boolean isInitialized = false;

  XResourceLoader(ResourceIndex resourceIndex) {
    this.resourceIndex = resourceIndex;
  }

  abstract void doInitialize();

  synchronized void initialize() {
    if (isInitialized) return;
    doInitialize();
    isInitialized = true;

    makeImmutable();
  }

  private void makeImmutable() {
    data.makeImmutable();

    xmlDocuments.makeImmutable();
    rawResources.makeImmutable();
  }

  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    initialize();
    ResBundle.Value<TypedResource> value = data.getValue(resName, qualifiers);
    return value == null ? null : value.getValue();
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    initialize();
    return xmlDocuments.get(resName, qualifiers);
  }

  @Override
  public InputStream getRawValue(ResName resName) {
    initialize();

    FsFile file = rawResources.get(resName, "");
    try {
      return file == null ? null : file.getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  @Override
  public int getResourceId(ResName resName) {
    Integer resourceId = resourceIndex.getResourceId(resName);
    return resourceId == null ? 0 : resourceId;
  }

  @Override
  public ResName getResName(int resourceId) {
    return resourceIndex.getResName(resourceId);
  }

  @Override
  public Collection<String> getPackages() {
    return resourceIndex.getPackages();
  }

  @Override
  public void receive(Visitor visitor) {
    initialize();
    data.receive(visitor);
  }
}
