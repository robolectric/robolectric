package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;

abstract class XResourceLoader implements ResourceLoader {
  final ResBunch data = new ResBunch();
  final ResBundle<PluralResourceLoader.PluralRules> pluralsData = new ResBundle<PluralResourceLoader.PluralRules>();
  final ResBundle<String> stringData = new ResBundle<String>();
  final ResBundle<MenuNode> menuData = new ResBundle<MenuNode>();
  final ResBundle<DrawableNode> drawableData = new ResBundle<DrawableNode>();
  final ResBundle<PreferenceNode> preferenceData = new ResBundle<PreferenceNode>();
  final ResBundle<Document> xmlDocuments = new ResBundle<Document>();
  final ResBundle<FsFile> rawResources = new ResBundle<FsFile>();
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
    menuData.makeImmutable();
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
    ResBunch.Value value = data.getValue(resName, qualifiers);
    return value == null ? null : value.getTypedResource();
  }

  @Override
  public Plural getPlural(ResName resName, int quantity, String qualifiers) {
    initialize();
    PluralResourceLoader.PluralRules pluralRules = pluralsData.get(resName, qualifiers);
    if (pluralRules == null) return null;

    return pluralRules.find(quantity);
  }

  @Override
  public Document getXml(ResName resName, String qualifiers) {
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
  public MenuNode getMenuNode(ResName resName, String qualifiers) {
    initialize();
    if (resName == null) return null;
    return menuData.get(resName, qualifiers);
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }
}
