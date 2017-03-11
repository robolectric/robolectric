package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StaxLoader implements StaxDocumentLoader.NodeListener {
  private static final Pattern ATTR_RE = Pattern.compile("([^\\[]*)(?:\\[@(.+)='(.+)'])?");

  protected final PackageResourceTable resourceTable;
  protected final String xpathExpr;
  protected final String attrType;
  protected final ResType resType;

  public StaxLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
    this.resourceTable = resourceTable;
    this.xpathExpr = xpathExpr;
    this.attrType = attrType;
    this.resType = resType;
  }

  public StaxDocumentLoader.NodeHandler addTo(StaxDocumentLoader.NodeHandler nodeHandler) {
    for (String s : xpathExpr.split("/")) {
      if (s.isEmpty()) continue;

      Matcher attrMatcher = ATTR_RE.matcher(s);
      if (attrMatcher.find()) {
        String elementName = attrMatcher.group(1);
        String attrName = attrMatcher.group(2);
        String attrValue = attrMatcher.group(3);

        Map<String, String> attrs = attrName == null
            ? Collections.<String, String>emptyMap()
            : Collections.singletonMap(attrName, attrValue);
        nodeHandler = nodeHandler.addHandler(elementName, attrs);
      } else {
        throw new RuntimeException("unknown pattern " + s);
      }
    }

    nodeHandler.addListener(this);

    addInnerHandlers(nodeHandler);

    return nodeHandler;
  }

  protected void addInnerHandlers(StaxDocumentLoader.NodeHandler nodeHandler) {
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  @Override
  public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  public static void addInnerHandler(StaxDocumentLoader.NodeHandler nodeHandler, final StringBuilder buf) {
    final StaxDocumentLoader.NodeHandler innerNodeHandler = nodeHandler.findMatchFor(null, null);
    innerNodeHandler.addListener(new StaxDocumentLoader.NodeListener() {
      @Override
      public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        addInnerHandler(innerNodeHandler, buf);
      }

      @Override
      public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        buf.append(xml.getText());
      }

      @Override
      public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      }
    });
  }
}
