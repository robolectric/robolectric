package org.robolectric.android;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.AttributeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.ResourceTable;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowAssetManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LegacyAttributeSetBuilder implements AttributeSetBuilder {

  private Context context;
  private final Document document;

  protected LegacyAttributeSetBuilder(Context context) {
    this.context = context;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);
    try {
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      document = documentBuilder.newDocument();
      Element dummy = document.createElementNS(
          "http://schemas.android.com/apk/res/" + RuntimeEnvironment.application.getPackageName(),
          "dummy");
      document.appendChild(dummy);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AttributeSetBuilder addAttribute(int resId, String value) {
    String resName = context.getResources().getResourceEntryName(resId);
    String resPkg = context.getResources().getResourcePackageName(resId);
    if ("style".equals(resName)) {
      ((Element)document.getFirstChild()).setAttribute(resName, value);
    } else {
      ((Element)document.getFirstChild()).setAttributeNS("http://schemas.android.com/apk/res/" + resPkg,
          resPkg + ":" + resName, value);
    }
    return this;
  }

  @Override
  public AttributeSetBuilder setStyleAttribute(String value) {
    ((Element)document.getFirstChild()).setAttribute("style", value);
    return this;
  }

  @Override
  public AttributeSetBuilder setClassAttribute(String value) {
    ((Element)document.getFirstChild()).setAttribute("class", value);
    return this;
  }

  @Override
  public AttributeSetBuilder setIdAttribute(String value) {
    ((Element)document.getFirstChild()).setAttribute("id", value);
    return this;
  }

  @Override
  public AttributeSet build() {
    AssetManager assets = context.getResources().getAssets();
    ShadowAssetManager shadowArscAssetManager = Shadow.extract(assets);
    ResourceTable resourceTable = shadowArscAssetManager.getResourceTable();
    XmlResourceParserImpl parser = new XmlResourceParserImpl(document, null,
        context.getPackageName(), context.getPackageName(), resourceTable);
    try {
      parser.next(); // Root document element
      parser.next(); // "dummy" element
    } catch (Exception e) {
      throw new IllegalStateException("Expected single dummy element in the document to contain the attributes.", e);
    }

    return parser;
  }
}
