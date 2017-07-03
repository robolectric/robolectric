package org.robolectric.res.builder;

import org.robolectric.res.ResourceTable;
import org.w3c.dom.Document;

/**
 * @deprecated Use {@link org.robolectric.android.XmlResourceParserImpl} instead.
 */
@Deprecated
public class XmlResourceParserImpl extends org.robolectric.android.XmlResourceParserImpl {
  public XmlResourceParserImpl(Document document, String fileName, String packageName, String applicationPackageName, ResourceTable resourceTable) {
    super(document, fileName, packageName, applicationPackageName, resourceTable);
  }
}
