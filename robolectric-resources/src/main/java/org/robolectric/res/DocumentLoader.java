package org.robolectric.res;

import com.ximpleware.VTDNav;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentLoader {
  private static final FsFile.Filter ENDS_WITH_XML = new FsFile.Filter() {
    @Override public boolean accept(@NotNull FsFile fsFile) {
      return fsFile.getName().endsWith(".xml");
    }
  };

  private final FsFile resourceBase;
  private final String packageName;
  private final XmlLoader[] xmlLoaders;
  private final XMLInputFactory factory;
  private final NodeHandler topLevelNodeHandler;

  private static final NodeHandler NO_OP_HANDLER = new NodeHandler();

  public DocumentLoader(String packageName, ResourcePath resourcePath, XmlLoader... xmlLoaders) {
    this.resourceBase = resourcePath.getResourceBase();
    this.packageName = packageName;
    this.xmlLoaders = xmlLoaders;

    topLevelNodeHandler = new NodeHandler();
    for (XmlLoader xmlLoader : xmlLoaders) {
      xmlLoader.addTo(topLevelNodeHandler);
    }

    factory = XMLInputFactory.newInstance();
  }

  public void load(String folderBaseName) {
    FsFile[] files = resourceBase.listFiles(new StartsWithFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.join(folderBaseName) + " is not a directory");
    }
    for (FsFile dir : files) {
      loadFile(dir, xmlLoaders);
    }
  }

  private void loadFile(FsFile dir, XmlLoader[] xmlLoaders) {
    if (!dir.exists()) {
      throw new RuntimeException("no such directory " + dir);
    }

    for (FsFile file : dir.listFiles(ENDS_WITH_XML)) {
      loadResourceXmlFile(file, xmlLoaders);
    }
  }

  private void loadResourceXmlFile(FsFile fsFile, XmlLoader... xmlLoaders) {
    VTDNav vtdNav = parse(fsFile);
    for (XmlLoader xmlLoader : xmlLoaders) {
      xmlLoader.processResourceXml(fsFile, vtdNav, packageName);
    }
  }

  private VTDNav parse(FsFile xmlFile) {
    XmlLoader.XmlContext xmlContext = new XmlLoader.XmlContext(packageName, xmlFile);

    try {
      XMLStreamReader reader =
          factory.createXMLStreamReader(xmlFile.getInputStream());

      doParse(reader, xmlContext);

      return null;
    } catch (XMLStreamException e) {
      throw new RuntimeException("error parsing " + xmlFile, e);
    } catch (IOException e) {
      throw new RuntimeException("error reading " + xmlFile, e);
    }
  }

  protected void doParse(XMLStreamReader reader, XmlLoader.XmlContext xmlContext) throws XMLStreamException {
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

  static class NodeHandler {
    private final String elementName;
    private final Map<String, String> attrs;
    private final Map<String, List<NodeHandler>> subHandlers = new HashMap<>();
    private final List<NodeListener> listeners = new ArrayList<>();

    NodeHandler(String elementName, Map<String, String> attrs) {
      this.elementName = elementName;
      this.attrs = attrs;
    }

    NodeHandler() {
      this.elementName = null;
      this.attrs = Collections.emptyMap();
    }

    NodeHandler findMatchFor(XMLStreamReader xml) {
      String tagName = xml.getLocalName();
      List<NodeHandler> nodeHandlers = subHandlers.get(tagName);
      if (nodeHandlers == null) {
        nodeHandlers = subHandlers.get("*");
      }
      if (nodeHandlers != null) {
        for (NodeHandler subHandler : nodeHandlers) {
          if (subHandler.matches(xml)) {
            return subHandler;
          }
        }
      }

      return null;
//      return elementHandlers.get(xml.getLocalName());
    }

    NodeHandler findMatchFor(String elementName, Map<String, String> attrs) {
      List<NodeHandler> nodeHandlers = null;
      if (elementName != null) {
        nodeHandlers = subHandlers.get(elementName);
      }
      if (nodeHandlers == null) {
        nodeHandlers = subHandlers.get("*");
      }
      if (nodeHandlers != null) {
        for (NodeHandler subHandler : nodeHandlers) {
          if (subHandler.attrs.equals(attrs)) {
            return subHandler;
          }
        }
      }

      NodeHandler nodeHandler = new NodeHandler(elementName, attrs);
      String elementNameOrStar = elementName == null ? "*" : elementName;
      nodeHandlers = subHandlers.get(elementName);
      if (nodeHandlers == null) {
        nodeHandlers = new ArrayList<>();
        subHandlers.put(elementNameOrStar, nodeHandlers);
      }

      nodeHandlers.add(nodeHandler);
      return nodeHandler;
    }

    private boolean matches(XMLStreamReader xml) {
      if (elementName != null && !elementName.equals(xml.getLocalName())) {
        return false;
      }

      int attributeCount = xml.getAttributeCount();
      for (int i = 0; i < attributeCount; i++) {
        String attrName = xml.getAttributeLocalName(i);
        String expectedAttrValue = attrs.get(attrName);
        if (expectedAttrValue != null && !expectedAttrValue.equals(xml.getAttributeValue(i))) {
          return false;
        }
      }
      return true;
    }

    public NodeHandler addHandler(String elementName, Map<String, String> attrs) {
      return findMatchFor(elementName, attrs);
    }

    void addListener(NodeListener nodeListener) {
      listeners.add(nodeListener);
    }

    public void onStart(XMLStreamReader xml, XmlLoader.XmlContext xmlContext) throws XMLStreamException {
      for (NodeListener listener : listeners) {
        listener.onStart(xml, xmlContext);
      }
    }

    public void onCharacters(XMLStreamReader xml, XmlLoader.XmlContext xmlContext) throws XMLStreamException {
      for (NodeListener listener : listeners) {
        listener.onCharacters(xml, xmlContext);
      }
    }

    public void onEnd(XMLStreamReader xml, XmlLoader.XmlContext xmlContext) throws XMLStreamException {
      for (NodeListener listener : listeners) {
        listener.onEnd(xml, xmlContext);
      }
    }

    @Override
    public String toString() {
      return "/" + elementName + "[@" + attrs + "]";
    }
  }

  interface NodeListener {
    void onStart(XMLStreamReader xml, XmlLoader.XmlContext xmlContext) throws XMLStreamException;
    void onCharacters(XMLStreamReader xml, XmlLoader.XmlContext xmlContext) throws XMLStreamException;
    void onEnd(XMLStreamReader xml, XmlLoader.XmlContext xmlContext) throws XMLStreamException;
  }
}
