package org.robolectric.res;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class StaxDocumentLoader extends DocumentLoader {
  private static final NodeHandler NO_OP_HANDLER = new NodeHandler();

  private final NodeHandler topLevelNodeHandler;
  private final XMLInputFactory factory;

  public StaxDocumentLoader(String packageName, FsFile resourceBase, NodeHandler topLevelNodeHandler) {
    super(packageName, resourceBase);

    this.topLevelNodeHandler = topLevelNodeHandler;
    factory = XMLInputFactory.newFactory();
  }

  @Override
  protected void loadResourceXmlFile(XmlContext xmlContext) {
    FsFile xmlFile = xmlContext.getXmlFile();

    XMLStreamReader xmlStreamReader;
    try {
      xmlStreamReader = factory.createXMLStreamReader(xmlFile.getInputStream());
      doParse(xmlStreamReader, xmlContext);
    } catch (Exception e) {
      throw new RuntimeException("error parsing " + xmlFile, e);
    }
    if (xmlStreamReader != null) {
      try {
        xmlStreamReader.close();
      } catch (XMLStreamException e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected void doParse(XMLStreamReader reader, XmlContext xmlContext) throws XMLStreamException {
    NodeHandler nodeHandler = this.topLevelNodeHandler;
    Deque<NodeHandler> nodeHandlerStack = new ArrayDeque<>();

    while (reader.hasNext()) {
      int event = reader.next();
      switch (event) {
        case XMLStreamConstants.START_DOCUMENT:
          break;

        case XMLStreamConstants.START_ELEMENT:
          nodeHandlerStack.push(nodeHandler);
          NodeHandler elementHandler = nodeHandler.findMatchFor(reader);
          nodeHandler = elementHandler == null ? NO_OP_HANDLER : elementHandler;
          nodeHandler.onStart(reader, xmlContext);
          break;

        case XMLStreamConstants.CDATA:
        case XMLStreamConstants.CHARACTERS:
          nodeHandler.onCharacters(reader, xmlContext);
          break;

        case XMLStreamConstants.END_ELEMENT:
          nodeHandler.onEnd(reader, xmlContext);
          nodeHandler = nodeHandlerStack.pop();
          break;

        case XMLStreamConstants.ATTRIBUTE:
      }
    }
  }
}
