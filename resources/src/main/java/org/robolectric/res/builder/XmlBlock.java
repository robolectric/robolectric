package org.robolectric.res.builder;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.robolectric.res.FsFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * An XML block is a parsed representation of a resource XML file. Similar in nature
 * to Android's XmlBlock class.
 */
public class XmlBlock {

  private static DocumentBuilder documentBuilder;

  private final Document document;
  private final String filename;
  private final String packageName;

  private synchronized static Document parse(FsFile xmlFile) {
    InputStream inputStream = null;
    try {
      if (documentBuilder == null) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
      }
      inputStream = xmlFile.getInputStream();
      return documentBuilder.parse(inputStream);
    } catch (ParserConfigurationException | IOException | SAXException e) {
      throw new RuntimeException(e);
    } finally {
      if (inputStream != null) try {
        inputStream.close();
      } catch (IOException e) {
        // ignore
      }
    }
  }

  @Nullable
  public static XmlBlock create(FsFile fsFile, String packageName) {
    Document document = parse(fsFile);

    return document == null
        ? null
        : new XmlBlock(document, fsFile.getPath(), packageName);
  }

  private XmlBlock(Document document, String filename, String packageName) {
    this.document = document;
    this.filename = filename;
    this.packageName = packageName;
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
}
