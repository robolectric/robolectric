package org.robolectric.res.builder;

import org.robolectric.res.Attribute;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An XML block is a parsed representation of a resource XML file. Similar in nature
 * to Android's XmlBlock class.
 */
public class XmlBlock {
  private final Document document;
  private final String filename;
  private final String packageName;

  public static XmlBlock create(Document document, String file, String packageName) {
    replaceResAutoNamespace(document, packageName);
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

  /**
   * Replaces all instances of "http://schemas.android.com/apk/res-auto" with
   * "http://schemas.android.com/apk/res/packageName" in the given Document.
   */
  private static void replaceResAutoNamespace(Document document, String packageName) {
    String autoNs = Attribute.RES_AUTO_NS_URI;
    String newNs = Attribute.ANDROID_RES_NS_PREFIX + packageName;
    replaceAttributeNamespace(document, document.getDocumentElement(), autoNs, newNs);
  }

  private static void replaceAttributeNamespace(Document document, Node n, String oldNs, String newNs) {
    NamedNodeMap attrs = n.getAttributes();
    if (attrs != null) {
      for (int i = 0; i < attrs.getLength(); i++) {
        replaceNamespace(document, attrs.item(i), oldNs, newNs);
      }
    }
    if (n.hasChildNodes()) {
      NodeList list = n.getChildNodes();
      for (int i = 0; i < list.getLength(); i++) {
        replaceAttributeNamespace(document, list.item(i), oldNs, newNs);
      }
    }
  }

  private static void replaceNamespace(Document document, Node n, String oldNs, String newNs) {
    if (oldNs.equals(n.getNamespaceURI())) {
      document.renameNode(n, newNs, n.getNodeName());
    }
  }

  private XmlBlock(Document document, String filename, String packageName) {
    this.document = document;
    this.filename = filename;
    this.packageName = packageName;
  }
}
