package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class StaxStyleLoader extends StaxLoader {
  private String name;
  private String parent;
  private List<AttributeResource> attributeResources;

  public StaxStyleLoader(PackageResourceTable resourceTable, String attrType, ResType resType) {
    super(resourceTable, attrType, resType);

    addHandler("item", new NodeHandler() {
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
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    parent = xml.getAttributeValue(null, "parent");
    attributeResources = new ArrayList<>();
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

    StyleData styleData = new StyleData(xmlContext.getPackageName(), name, styleParent, attributeResources);

    resourceTable.addResource("style", styleData.getName(), new TypedResource<>(styleData, resType, xmlContext));
  }
}
