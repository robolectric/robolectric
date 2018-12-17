package org.robolectric.res.builder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.robolectric.res.Fs;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * An XML block is a parsed representation of a resource XML file. Similar in nature
 * to Android's XmlBlock class.
 */
public class XmlBlock {

  private static DocumentBuilder documentBuilder;

  private final Document document;
  private final Path path;
  private final String packageName;

  private static synchronized Document parse(Path xmlFile) {
    InputStream inputStream = null;
    try {
      if (documentBuilder == null) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
      }
      inputStream = Fs.getInputStream(xmlFile);
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
  public static XmlBlock create(Path path, String packageName) {
    Document document = parse(path);

    return document == null ? null : new XmlBlock(document, path, packageName);
  }

  private XmlBlock(Document document, Path path, String packageName) {
    this.document = document;
    this.path = path;
    this.packageName = packageName;
  }

  public Document getDocument() {
    return document;
  }

  public Path getPath() {
    return path;
  }

  public String getPackageName() {
    return packageName;
  }
}
