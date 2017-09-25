package org.robolectric.res.android;

public class XmlAttributeFinder {

  private ResXMLParser xmlParser;

  public XmlAttributeFinder(ResXMLParser xmlParser) {
    this.xmlParser = xmlParser;
  }

  public int find(int curIdent) {
    if (xmlParser == null) {
      return 0;
    }

    int attributeCount = xmlParser.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      if (xmlParser.getAttributeNameResID(i) == curIdent) {
        return i;
      }
    }
    return attributeCount;
  }
}
