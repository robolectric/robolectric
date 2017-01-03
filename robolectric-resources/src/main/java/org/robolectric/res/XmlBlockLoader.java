package org.robolectric.res;

import android.content.res.XmlResourceParser;
import org.robolectric.res.builder.XmlBlock;

/**
 * Loader for xml property files.
 *
 * <p>Given a resource file a concrete implementation of {@link XmlResourceParser}
 * is returned. The returned implementation is based on the current Android
 * implementation. Please see the android source code for further details.
 */
public class XmlBlockLoader extends XmlLoader {
  private PackageResourceTable resourceTable;
  private final String attrType;

  public XmlBlockLoader(PackageResourceTable resourceTable, String attrType) {
    this.resourceTable = resourceTable;
    this.attrType = attrType;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) {
    XmlBlock block = XmlBlock.create(parse(xmlFile), xmlFile.getPath(), xmlContext.getPackageName());
    resourceTable.addXml(attrType, xmlFile.getBaseName(), new TypedResource<>(block, null, xmlContext));

  }
}
