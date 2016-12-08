package org.robolectric.res;

import com.ximpleware.VTDNav;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public abstract class XmlLoader {
  private static final DocumentBuilderFactory documentBuilderFactory;
  static {
    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setIgnoringComments(true);
    documentBuilderFactory.setIgnoringElementContentWhitespace(true);
  }

  @NotNull
  static public Document parse(FsFile xmlFile) {
    InputStream inputStream = null;
    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      inputStream = xmlFile.getInputStream();
      return documentBuilder.parse(inputStream);
    } catch (ParserConfigurationException | IOException | SAXException e) {
      throw new RuntimeException(e);
    } finally {
      if (inputStream != null) try {
        inputStream.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected void processResourceXml(FsFile xmlFile, VTDNav vtdNav, String packageName) throws Exception {
    processResourceXml(xmlFile, new XpathResourceXmlLoader.XmlNode(vtdNav), new XmlContext(packageName, xmlFile));
  }

  protected abstract void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception;

}
