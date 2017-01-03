package org.robolectric.res;

interface XmlLoader {

  void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext);

}
