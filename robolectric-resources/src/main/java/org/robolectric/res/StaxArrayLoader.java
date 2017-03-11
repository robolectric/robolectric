package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.robolectric.res.StaxLoader.addInnerHandler;

public class StaxArrayLoader extends StaxLoader {
  private final ResType scalarResType;
  private String name;
  private List<TypedResource> items;
  private final StringBuilder buf = new StringBuilder();

  public StaxArrayLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType arrayResType, ResType scalarResType) {
    super(resourceTable, xpathExpr, attrType, arrayResType);
    this.scalarResType = scalarResType;
  }

  @Override
  public StaxDocumentLoader.NodeHandler addTo(StaxDocumentLoader.NodeHandler nodeHandler) {
    StaxDocumentLoader.NodeHandler arrayNodeHandler = super.addTo(nodeHandler);
    StaxDocumentLoader.NodeHandler itemNodeHandler = arrayNodeHandler.addHandler("item", Collections.<String, String>emptyMap());
    itemNodeHandler.addListener(new ItemNodeListener());

    addInnerHandler(itemNodeHandler, buf);
    return arrayNodeHandler;
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    items = new ArrayList<>();
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    resourceTable.addResource(attrType, name, new TypedResource<>(items, resType, xmlContext));
  }

  private class ItemNodeListener implements StaxDocumentLoader.NodeListener {
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
      ResType resType = scalarResType == null ? ResType.inferType(buf.toString()) : scalarResType;
      items.add(new TypedResource<>(buf.toString(), resType, xmlContext));
    }
  }
}
