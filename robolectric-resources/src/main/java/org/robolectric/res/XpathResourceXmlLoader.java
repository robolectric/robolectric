package org.robolectric.res;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import org.jetbrains.annotations.NotNull;
import org.robolectric.res.DocumentLoader.NodeListener;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class XpathResourceXmlLoader extends XmlLoader implements NodeListener {
  public static final Pattern ATTR_RE = Pattern.compile("([^\\[]*)(?:\\[@(.+)='(.+)'])?");
  private final String expression;

  public XpathResourceXmlLoader(String expression) {
    this.expression = expression;
  }

  @Override
  public DocumentLoader.NodeHandler addTo(DocumentLoader.NodeHandler nodeHandler) {
    for (String s : expression.split("/")) {
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
    return nodeHandler;
  }

  @Override
  public abstract void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException;

  @Override
  public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  @Override protected void processResourceXml(FsFile xmlFile, XmlNode xmlNode, XmlContext xmlContext) {
    try {
      for (XmlNode node : xmlNode.selectByXpath(expression)) {
        String name = node.getAttrValue("name");
        onStart(name, node, xmlContext);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error processing " + xmlFile, e);
    }
  }

  protected abstract void onStart(String name, XmlNode xmlNode, XmlContext xmlContext);

  public static class XmlNode {
    private final VTDNav vtdNav;

    public XmlNode(VTDNav vtdNav) {
      this.vtdNav = vtdNav;
    }

    public String getElementName() {
      try {
        return vtdNav.toString(vtdNav.getCurrentIndex());
      } catch (NavException e) {
        throw new RuntimeException(e);
      }
    }

    public XmlNode getFirstChild() {
      try {
        VTDNav cloneVtdNav = vtdNav.cloneNav();
        if (!cloneVtdNav.toElement(VTDNav.FIRST_CHILD)) return null;
        return new XmlNode(cloneVtdNav);
      } catch (NavException e) {
        throw new RuntimeException(e);
      }
    }

    public String getTextContent() {
      try {
        return vtdNav.getXPathStringVal();
      } catch (NavException e) {
        throw new RuntimeException(e);
      }
    }

    public Iterable<XmlNode> selectByXpath(String expr) throws XPathParseException {
//      VTDNav cloneVtdNav = vtdNav.cloneNav();
//      final AutoPilot ap = new AutoPilot(cloneVtdNav);
//      ap.selectXPath(expr);
//      return returnIterable(new Iterator(ap, cloneVtdNav) {
//        @Override boolean doHasNext() throws XPathEvalException, NavException {
//          int result = ap.evalXPath();
//          if (result == -1) {
//            ap.resetXPath();
//          }
//          return result != -1;
//        }
//      });
      return new ArrayList<>();
    }

    public Iterable<XmlNode> selectElements(String name) {
      VTDNav cloneVtdNav = vtdNav.cloneNav();
      final AutoPilot ap = new AutoPilot(cloneVtdNav);
      ap.selectElement(name);
      return returnIterable(new Iterator(ap, cloneVtdNav) {
        @Override boolean doHasNext() throws XPathEvalException, NavException {
          return ap.iterate();
        }
      });
    }

    private Iterable<XmlNode> returnIterable(final Iterator iterator) {
      return new Iterable<XmlNode>() {
        @NotNull @Override public java.util.Iterator<XmlNode> iterator() {
          return iterator;
        }
      };
    }

    public String getAttrValue(String attrName) {
      try {
        int nameIndex = vtdNav.getAttrVal(attrName);
        return nameIndex == -1 ? null : vtdNav.toNormalizedString(nameIndex);
      } catch (NavException e) {
        throw new RuntimeException(e);
      }
    }

    public void pushLocation() {
      vtdNav.push();
    }

    public void popLocation() {
      vtdNav.pop();
    }

    public boolean moveToParent() {
      try {
        return vtdNav.toElement(VTDNav.PARENT);
      } catch (NavException e) {
        throw new RuntimeException(e);
      }
    }

    private abstract class Iterator implements java.util.Iterator<XmlNode> {
      private final AutoPilot ap;
      private final VTDNav vtdNav;

      public Iterator(AutoPilot ap, VTDNav vtdNav) {
        this.ap = ap;
        this.vtdNav = vtdNav;
      }

      @Override public boolean hasNext() {
        try {
          return doHasNext();
        } catch (XPathEvalException | NavException e) {
          throw new RuntimeException(e);
        }
      }

      abstract boolean doHasNext() throws XPathEvalException, NavException;

      @Override public XmlNode next() {
        return new XmlNode(vtdNav);
      }

      @Override public void remove() {
        throw new UnsupportedOperationException();
      }
    }
  }
}
