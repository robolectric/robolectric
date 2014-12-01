package org.robolectric.res;

import android.content.res.XmlResourceParser;
import org.w3c.dom.Document;

/**
 * Loader for xml property files.
 *
 * <p>Given a resource file a concrete implementation of {@link XmlResourceParser}
 * is returned. The returned implementation is based on the current Android
 * implementation. Please see the android source code for further details.
 */
public class XmlFileLoader extends XmlLoader {
  private final String attrType;
  private final ResBundle<Document> resBundle;

  public XmlFileLoader(ResBundle<Document> resBundle, String attrType) {
    this.attrType = attrType;
    this.resBundle = resBundle;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    resBundle.put(attrType, xmlFile.getName().replace(".xml", ""), parse(xmlFile), xmlContext);
  }
}
