package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayResourceLoader extends XpathResourceXmlLoader {
  private final ResBunch resBunch;
  private final String attrType;
  private final ResType resType;

  private String name;
  private List<String> items;
  private final StringBuilder buf = new StringBuilder();

  public ArrayResourceLoader(ResBunch resBunch, String xpathExpr, String attrType, ResType resType) {
    super(xpathExpr);
    this.resBunch = resBunch;
    this.attrType = attrType;
    this.resType = resType;
  }

  @Override
  public DocumentLoader.NodeHandler addTo(DocumentLoader.NodeHandler nodeHandler) {
    DocumentLoader.NodeHandler arrayNodeHandler = super.addTo(nodeHandler);
    arrayNodeHandler.addHandler("item", Collections.<String, String>emptyMap()).addListener(
        new DocumentLoader.NodeListener() {
          @Override
          public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
            buf.setLength(0);
          }

          @Override
          public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
            buf.append(xml.getText());
          }

          @Override
          public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
            items.add(buf.toString());
          }
        });
    return arrayNodeHandler;
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    items = new ArrayList<>();
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    resBunch.put(attrType, name, new TypedResource<>(items, resType, xmlContext));
    name = null;
    items = null;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XmlNode xmlNode, XmlContext xmlContext) {
    super.processResourceXml(xmlFile, xmlNode, xmlContext);
  }

  @Override
  protected void onStart(String name, XmlNode xmlNode, XmlContext xmlContext) {
//    resBunch.put(attrType, name, resType.getValueWithType(xmlNode, xmlContext));
  }
}
