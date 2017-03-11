package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StaxStyleLoader extends StaxLoader {
  private String name;
  private String parent;
  private List<AttributeResource> attributeResources;

  public StaxStyleLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
    super(resourceTable, xpathExpr, attrType, resType);
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    parent = xml.getAttributeValue(null, "parent");
    attributeResources = new ArrayList<>();
  }

  @Override
  protected void addInnerHandlers(StaxDocumentLoader.NodeHandler nodeHandler) {
    nodeHandler.findMatchFor("item", null).addListener(new StaxDocumentLoader.NodeListener() {
      private String attrName;
      private StringBuilder buf = new StringBuilder();

      @Override
      public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        attrName = xml.getAttributeValue(null, "name");
        buf.setLength(0);
      }

      @Override
      public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        buf.append(xml.getText());
      }

      @Override
      public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        ResName attrResName = ResName.qualifyResName(attrName, xmlContext.getPackageName(), "attr");
        attributeResources.add(new AttributeResource(attrResName, buf.toString(), xmlContext.getPackageName()));
      }
    });
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    String styleParent = parent;

    if (styleParent == null) {
      int lastDot = name.lastIndexOf('.');
      if (lastDot != -1) {
        styleParent = name.substring(0, lastDot);
      }
    }

    String styleNameWithUnderscores = underscorize(name);
    StyleData styleData = new StyleData(xmlContext.getPackageName(), styleNameWithUnderscores, underscorize(styleParent), attributeResources);

    resourceTable.addResource("style", styleData.getName(), new TypedResource<>(styleData, resType, xmlContext));
  }

  private String underscorize(String s) {
    return s == null ? null : s.replace('.', '_');
  }
}
