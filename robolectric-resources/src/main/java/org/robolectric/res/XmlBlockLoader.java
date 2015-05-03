package org.robolectric.res;

import android.content.res.XmlResourceParser;
import org.robolectric.res.builder.XmlBlock;
import org.w3c.dom.Document;

/**
 * Loader for xml property files.
 *
 * <p>Given a resource file a concrete implementation of {@link XmlResourceParser}
 * is returned. The returned implementation is based on the current Android
 * implementation. Please see the android source code for further details.
 */
public class XmlBlockLoader extends XmlLoader {
  private final String attrType;
  private final ResBundle<XmlBlock> resBundle;

  public XmlBlockLoader(ResBundle<XmlBlock> resBundle, String attrType) {
    this.attrType = attrType;
    this.resBundle = resBundle;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    XmlBlock block = XmlBlock.create(parse(xmlFile), xmlFile.getPath(), xmlContext.packageName);
    resBundle.put(attrType, xmlFile.getBaseName(), block, xmlContext);
  }
}
