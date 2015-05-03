package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;

// TODO: Give me a better name
abstract class XResourceLoader implements ResourceLoader {
  final ResBunch data = new ResBunch();
  final ResBundle<PluralResourceLoader.PluralRules> pluralsData = new ResBundle<>();
  final ResBundle<String> stringData = new ResBundle<>();
  final ResBundle<DrawableNode> drawableData = new ResBundle<>();
  final ResBundle<PreferenceNode> preferenceData = new ResBundle<>();
  final ResBundle<XmlBlock> xmlDocuments = new ResBundle<>();
  final ResBundle<FsFile> rawResources = new ResBundle<>();
  private final ResourceIndex resourceIndex;
  boolean isInitialized = false;

  protected XResourceLoader(ResourceIndex resourceIndex) {
    this.resourceIndex = resourceIndex;
  }

  abstract void doInitialize();

  void initialize() {
    if (isInitialized) return;
    doInitialize();
    isInitialized = true;

    makeImmutable();
  }

  protected void makeImmutable() {
    data.makeImmutable();

    pluralsData.makeImmutable();
    stringData.makeImmutable();
    drawableData.makeImmutable();
    preferenceData.makeImmutable();
    xmlDocuments.makeImmutable();
    rawResources.makeImmutable();
  }

  @Override
  public String getNameForId(int id) {
    return resourceIndex.getResourceName(id);
  }

  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    initialize();
    ResBundle.Value<TypedResource> value = data.getValue(resName, qualifiers);
    return value == null ? null : value.getValue();
  }

  @Override
  public Plural getPlural(ResName resName, int quantity, String qualifiers) {
    initialize();
    PluralResourceLoader.PluralRules pluralRules = pluralsData.get(resName, qualifiers);
    if (pluralRules == null) return null;

    return pluralRules.find(quantity);
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    initialize();
    return xmlDocuments.get(resName, qualifiers);
  }

  @Override
  public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
    return drawableData.get(resName, qualifiers);
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
  public PreferenceNode getPreferenceNode(ResName resName, String qualifiers) {
    initialize();

    return preferenceData.get(resName, qualifiers);
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }
}
