package org.robolectric.res.builder;

import org.w3c.dom.Document;

/**
 * An XML block is a parsed representation of a resource XML file. Similar in nature
 * to Android's XmlBlock class.
 */
public class XmlBlock {
  private final Document document;
  private final String filename;
  private final String packageName;

  public static XmlBlock create(Document document, String file, String packageName) {
    return new XmlBlock(document, file, packageName);
  }

  public Document getDocument() {
    return document;
  }

  public String getFilename() {
    return filename;
  }

  public String getPackageName() {
    return packageName;
  }

  private XmlBlock(Document document, String filename, String packageName) {
    this.document = document;
    this.filename = filename;
    this.packageName = packageName;
  }
}
