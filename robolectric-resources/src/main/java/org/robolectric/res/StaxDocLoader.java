package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaxDocLoader {
  private static final FsFile.Filter ENDS_WITH_XML = new FsFile.Filter() {
    @Override public boolean accept(@NotNull FsFile fsFile) {
      return fsFile.getName().endsWith(".xml");
    }
  };
  private static final NodeHandler NO_OP_HANDLER = new NodeHandler();

  private final String packageName;
  private final ResourcePath resourcePath;
  private final boolean parse;
  private final StaxLoader[] staxLoaders;
  private final NodeHandler topLevelNodeHandler;
  private final XMLInputFactory factory;

  public StaxDocLoader(String packageName, ResourcePath resourcePath, StaxLoader... staxLoaders) {
    this(packageName, resourcePath, true, staxLoaders);
  }

  public StaxDocLoader(String packageName, ResourcePath resourcePath, boolean parse, StaxLoader... staxLoaders) {
    this.packageName = packageName;
    this.resourcePath = resourcePath;
    this.parse = parse;
    this.staxLoaders = staxLoaders;

    topLevelNodeHandler = new NodeHandler();
    for (StaxLoader staxLoader : staxLoaders) {
      staxLoader.addTo(topLevelNodeHandler);
    }

    factory = XMLInputFactory.newFactory();
  }

  public void load(String folderBaseName) {
    FsFile resourceBase = resourcePath.getResourceBase();
    FsFile[] files = resourceBase.listFiles(new StartsWithFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.join(folderBaseName) + " is not a directory");
    }
    for (FsFile dir : files) {
      loadFile(dir);
    }
  }

  private void loadFile(FsFile dir) {
    if (!dir.exists()) {
      throw new RuntimeException("no such directory " + dir);
    }

    for (FsFile file : dir.listFiles(ENDS_WITH_XML)) {
      loadResourceXmlFile(new XmlContext(packageName, file));
    }
  }

  protected void loadResourceXmlFile(XmlContext xmlContext) {
    FsFile xmlFile = xmlContext.getXmlFile();
//    if (!xmlFile.getName().contains("strings.xml")) {
//      return;
//    }
//    System.out.println("\n" + xmlFile + ":");

    XMLStreamReader xmlStreamReader = null;
    try {
      if (parse) {
        xmlStreamReader = factory.createXMLStreamReader(xmlFile.getInputStream());
        doParse(xmlStreamReader, xmlContext);
      } else {
        for (StaxLoader staxLoader : staxLoaders) {
          staxLoader.onStart(null, xmlContext);
          staxLoader.onEnd(null, xmlContext);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("error parsing " + xmlFile, e);
    }
    if (xmlStreamReader != null) {
      try {
        xmlStreamReader.close();
      } catch (XMLStreamException e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected void doParse(XMLStreamReader reader, XmlContext xmlContext) throws XMLStreamException {
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

          List<NodeHandler> stack = new ArrayList<>();
          stack.addAll(nodeHandlerStack);
          Collections.reverse(stack);
          stack.remove(0);

//          System.out.println(Join.join(" > ", stack) + " > " + nodeHandler + " because " + reader.getLocalName());

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
      this.elementName = "*".equals(elementName) ? null : elementName;
      this.attrs = attrs == null ? Collections.<String, String>emptyMap() : attrs;
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

    public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      for (NodeListener listener : listeners) {
        listener.onStart(xml, xmlContext);
      }
    }

    public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      for (NodeListener listener : listeners) {
        listener.onCharacters(xml, xmlContext);
      }
    }

    public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
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
    void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException;
    void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException;
    void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException;
  }
  
  
  public static class StaxLoader implements NodeListener {
    public static final Pattern ATTR_RE = Pattern.compile("([^\\[]*)(?:\\[@(.+)='(.+)'])?");

    protected final PackageResourceTable resourceTable;
    private final String xpathExpr;
    protected final String attrType;
    protected final ResType resType;

    private final StringBuilder buf = new StringBuilder();
    protected String name;

    public StaxLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
      this.resourceTable = resourceTable;
      this.xpathExpr = xpathExpr;
      this.attrType = attrType;
      this.resType = resType;
    }

    public NodeHandler addTo(NodeHandler nodeHandler) {
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

      typeSpecific(nodeHandler);

      return nodeHandler;
    }

    protected void typeSpecific(NodeHandler nodeHandler) {
      if (resType == ResType.CHAR_SEQUENCE) {
        addInnerHandler(nodeHandler, buf);
      }
    }

    @Override
    public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      name = xml.getAttributeValue(null, "name");
      buf.setLength(0);
    }

    @Override
    public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      buf.append(xml.getText());
    }

    @Override
    public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      String s = buf.toString();
      if (resType == ResType.CHAR_SEQUENCE) {
        s = StringResources.proccessStringResources(s);
      }
      resourceTable.addResource(attrType, name, new TypedResource<>(s, resType, xmlContext));
    }
  }

  public static class StaxArrayLoader extends StaxLoader {
    private String name;
    private List<TypedResource> items;
    private final StringBuilder buf = new StringBuilder();

    public StaxArrayLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType arrayResType, ResType scalarResType) {
      super(resourceTable, xpathExpr, attrType, arrayResType);
    }

    @Override
    public NodeHandler addTo(NodeHandler nodeHandler) {
      NodeHandler arrayNodeHandler = super.addTo(nodeHandler);
      NodeHandler itemNodeHandler = arrayNodeHandler.addHandler("item", Collections.<String, String>emptyMap());
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

    private class ItemNodeListener implements NodeListener {
      @Override
      public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        buf.setLength(0);
      }

      @Override
      public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
//        System.out.println(xml.getText());
        buf.append(xml.getText());
      }

      @Override
      public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        ResType scalarResType = ResType.inferType(buf.toString());
        items.add(new TypedResource(buf.toString(), scalarResType, xmlContext));
      }
    }
  }

  public static void addInnerHandler(NodeHandler nodeHandler, final StringBuilder buf) {
    final NodeHandler innerNodeHandler = nodeHandler.findMatchFor(null, null);
    innerNodeHandler.addListener(new NodeListener() {
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

  public static class StaxPluralsLoader extends StaxLoader {
    final StringBuilder buf;
    List<Plural> plurals;
    String quantity;

    public StaxPluralsLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType charSequence) {
      super(resourceTable, xpathExpr, attrType, charSequence);
      buf = new StringBuilder();
    }

    @Override
    public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      super.onStart(xml, xmlContext);
      plurals = new ArrayList<>();
    }

    @Override
    protected void typeSpecific(final StaxDocLoader.NodeHandler nodeHandler) {
      final StaxDocLoader.NodeHandler itemNodeHandler = nodeHandler.findMatchFor("item", null);
      itemNodeHandler.addListener(new StaxDocLoader.NodeListener() {
        @Override
        public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
          quantity = xml.getAttributeValue(null, "quantity");
          buf.setLength(0);
          addInnerHandler(itemNodeHandler, buf);
        }

        @Override
        public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
          buf.append(xml.getText());
        }

        @Override
        public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
          plurals.add(new Plural(quantity, buf.toString()));
        }
      });
    }

    @Override
    public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      resourceTable.addResource(attrType, name, new PluralResourceLoader.PluralRules(plurals, resType, xmlContext));
    }
  }

  public static class StaxAttrLoader extends StaxLoader {
    private String format;
    private List<AttrData.Pair> pairs;

    public StaxAttrLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
      super(resourceTable, xpathExpr, attrType, resType);
    }

    @Override
    public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      super.onStart(xml, xmlContext);
      format = xml.getAttributeValue(null, "format");
      pairs = new ArrayList<>();
    }

    @Override
    protected void typeSpecific(StaxDocLoader.NodeHandler nodeHandler) {
      nodeHandler.findMatchFor(null, null).addListener(new StaxDocLoader.NodeListener() {
        private String value;
        private String name;

        @Override
        public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
          String type = xml.getLocalName();
          if (pairs.isEmpty()) {
            if (format == null) {
              format = type;
            } else {
              format = format + "|" + type;
            }
          }
          name = xml.getAttributeValue(null, "name");
          value = xml.getAttributeValue(null, "value");
          pairs.add(new AttrData.Pair(name, value));
        }

        @Override
        public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        }

        @Override
        public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        }
      });
    }

    @Override
    public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      AttrData attrData = new AttrData(name, format, pairs);
//      xmlContext = xmlContext.withLineNumber(xml.getLocation().getLineNumber());
      if (attrData.getFormat() != null) {
        resourceTable.addResource(attrType, name, new TypedResource<>(attrData, resType, xmlContext));
      }
    }
  }

  public static class StaxStyleLoader extends StaxLoader {
    private String name;
    private String parent;
    private StyleData styleData;

    public StaxStyleLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
      super(resourceTable, xpathExpr, attrType, resType);
    }

    @Override
    public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      name = xml.getAttributeValue(null, "name");
      parent = xml.getAttributeValue(null, "parent");

      String styleNameWithUnderscores = underscorize(name);
      styleData = new StyleData(xmlContext.getPackageName(), styleNameWithUnderscores, underscorize(parent));
    }

    @Override
    protected void typeSpecific(StaxDocLoader.NodeHandler nodeHandler) {
      nodeHandler.findMatchFor("item", null).addListener(new StaxDocLoader.NodeListener() {
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
          styleData.add(attrResName, new AttributeResource(attrResName, buf.toString(), xmlContext.getPackageName()));
        }
      });
    }

    @Override
    public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      if (parent == null) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) {
          parent = name.substring(0, lastDot);
        }
      }

      resourceTable.addResource("style", styleData.getName(), new TypedResource<>(styleData, resType, xmlContext));
    }

    private String underscorize(String s) {
      return s == null ? null : s.replace('.', '_');
    }
  }

  public static class OpaqueLoader extends StaxLoader {
    public OpaqueLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
      super(resourceTable, xpathExpr, attrType, resType);
    }

    @Override
    public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    }

    @Override
    public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    }

    @Override
    public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      resourceTable.addResource(attrType, xmlContext.getXmlFile().getBaseName(), new FileTypedResource(xmlContext.getXmlFile(), resType, xmlContext));
    }
  }
}
