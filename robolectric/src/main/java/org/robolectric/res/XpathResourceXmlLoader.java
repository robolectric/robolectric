package org.robolectric.res;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import org.jetbrains.annotations.NotNull;

import javax.xml.xpath.XPathExpressionException;

public abstract class XpathResourceXmlLoader extends XmlLoader {
  private String expression;

  public XpathResourceXmlLoader(String expression) {
    this.expression = expression;
  }

  @Override protected void processResourceXml(FsFile xmlFile, XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    for (XmlNode node : xmlNode.selectByXpath(expression)) {
      String name = node.getAttrValue("name");
      processNode(name, node, xmlContext);
    }
  }

  protected abstract void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) throws XPathExpressionException;

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
      VTDNav cloneVtdNav = vtdNav.cloneNav();
      final AutoPilot ap = new AutoPilot(cloneVtdNav);
      ap.selectXPath(expr);
      return returnIterable(new Iterator(ap, cloneVtdNav) {
        @Override boolean doHasNext() throws XPathEvalException, NavException {
          int result = ap.evalXPath();
          if (result == -1) {
            ap.resetXPath();
          }
          return result != -1;
        }
      });
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
        } catch (XPathEvalException e) {
          throw new RuntimeException(e);
        } catch (NavException e) {
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
