package org.robolectric.res;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

class NodeHandler {
  private static final Pattern ATTR_RE = Pattern.compile("([^\\[]*)(?:\\[@(.+)='(.+)'])?");

  private final Map<String, ElementHandler> subElementHandlers = new HashMap<>();

  private static class ElementHandler extends HashMap<String, AttrHandler> {
    final NodeHandler nodeHandler;

    private ElementHandler(NodeHandler nodeHandler) {
      this.nodeHandler = nodeHandler;
    }
  }

  private static class AttrHandler extends HashMap<String, NodeHandler> {
  }

  NodeHandler findMatchFor(XMLStreamReader xml) {
    String tagName = xml.getLocalName();
    ElementHandler elementHandler = subElementHandlers.get(tagName);
    if (elementHandler == null) {
      elementHandler = subElementHandlers.get("*");
    }
    if (elementHandler != null) {
      for (Map.Entry<String, AttrHandler> entry : elementHandler.entrySet()) {
        String attrName = entry.getKey();
        String attributeValue = xml.getAttributeValue(null, attrName);
        if (attributeValue != null) {
          AttrHandler attrHandler = entry.getValue();
          NodeHandler nodeHandler = attrHandler.get(attributeValue);
          if (nodeHandler != null) {
            return nodeHandler;
          }
        }
      }

      return elementHandler.nodeHandler;
    }

    return null;
  }

  public NodeHandler addHandler(String matchExpr, NodeHandler subHandler) {
    Matcher attrMatcher = ATTR_RE.matcher(matchExpr);
    if (attrMatcher.find()) {
      String elementName = attrMatcher.group(1);
      String attrName = attrMatcher.group(2);
      String attrValue = attrMatcher.group(3);

      if (elementName == null || elementName.isEmpty()) {
        elementName = "*";
      }

      ElementHandler elementHandler = subElementHandlers.get(elementName);
      if (elementHandler == null) {
        elementHandler = new ElementHandler(attrName == null ? subHandler : null);
        subElementHandlers.put(elementName, elementHandler);
      }

      if (attrName != null) {
        AttrHandler attrHandler = elementHandler.get(attrName);
        if (attrHandler == null) {
          attrHandler = new AttrHandler();
          elementHandler.put(attrName, attrHandler);
        }
        attrHandler.put(attrValue, subHandler);
      }
    } else {
      throw new RuntimeException("unknown pattern " + matchExpr);
    }

    return this;
  }

  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }
}
