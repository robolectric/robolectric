package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class StaxAttrLoader extends StaxLoader {
  private String name;
  private String format;
  private final List<AttrData.Pair> pairs = new ArrayList<>();

  public StaxAttrLoader(PackageResourceTable resourceTable, String attrType, ResType resType) {
    super(resourceTable, attrType, resType);

    addHandler(
        "*",
        new NodeHandler() {
          @Override
          public void onStart(XMLStreamReader xml, XmlContext xmlContext) {
            String type = xml.getLocalName();
            if (pairs.isEmpty()) {
              if (format == null) {
                format = type;
              } else {
                format = format + "|" + type;
              }
            }
            String name = xml.getAttributeValue(null, "name");
            String value = xml.getAttributeValue(null, "value");
            pairs.add(new AttrData.Pair(name, value));
          }

          @Override
          public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) {}

          @Override
          public void onEnd(XMLStreamReader xml, XmlContext xmlContext) {}
        });
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    format = xml.getAttributeValue(null, "format");
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) {
    AttrData attrData = new AttrData(name, format, new ArrayList<>(pairs));
    pairs.clear();

    //      xmlContext = xmlContext.withLineNumber(xml.getLocation().getLineNumber());
    if (attrData.getFormat() != null) {
      resourceTable.addResource(attrType, name, new TypedResource<>(attrData, resType, xmlContext));
    }
  }
}
