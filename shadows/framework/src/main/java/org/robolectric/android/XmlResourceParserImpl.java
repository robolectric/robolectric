package org.robolectric.android;

import static org.robolectric.res.AttributeResource.ANDROID_RES_NS_PREFIX;
import static org.robolectric.res.AttributeResource.RES_AUTO_NS_URI;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.StringResources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Concrete implementation of the {@link XmlResourceParser}.
 *
 * Clients expects a pull parser while the resource loader
 * initialise this object with a {@link Document}.
 * This implementation navigates the dom and emulates a pull
 * parser by raising all the opportune events.
 *
 * Note that the original android implementation is based on
 * a set of native methods calls. Here those methods are
 * re-implemented in java when possible.
 */
public class XmlResourceParserImpl implements XmlResourceParser {

  /**
   * All the parser features currently supported by Android.
   */
  public static final String[] AVAILABLE_FEATURES = {
      XmlResourceParser.FEATURE_PROCESS_NAMESPACES,
      XmlResourceParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES
  };
  /**
   * All the parser features currently NOT supported by Android.
   */
  public static final String[] UNAVAILABLE_FEATURES = {
      XmlResourceParser.FEATURE_PROCESS_DOCDECL,
      XmlResourceParser.FEATURE_VALIDATION
  };

  private final Document document;
  private final String fileName;
  private final String packageName;
  private final ResourceTable resourceTable;
  private final String applicationNamespace;

  private Node currentNode;

  private boolean mStarted = false;
  private boolean mDecNextDepth = false;
  private int mDepth = 0;
  private int mEventType = START_DOCUMENT;

  public XmlResourceParserImpl(Document document, String fileName, String packageName,
                               String applicationPackageName, ResourceTable resourceTable) {
    this.document = document;
    this.fileName = fileName;
    this.packageName = packageName;
    this.resourceTable = resourceTable;
    this.applicationNamespace = ANDROID_RES_NS_PREFIX + applicationPackageName;
  }

  @Override
  public void setFeature(String name, boolean state)
      throws XmlPullParserException {
    if (isAndroidSupportedFeature(name) && state) {
      return;
    }
    throw new XmlPullParserException("Unsupported feature: " + name);
  }

  @Override
  public boolean getFeature(String name) {
    return isAndroidSupportedFeature(name);
  }

  @Override
  public void setProperty(String name, Object value)
      throws XmlPullParserException {
    throw new XmlPullParserException("setProperty() not supported");
  }

  @Override
  public Object getProperty(String name) {
    // Properties are not supported. Android returns null
    // instead of throwing an XmlPullParserException.
    return null;
  }

  @Override
  public void setInput(Reader in) throws XmlPullParserException {
    throw new XmlPullParserException("setInput() not supported");
  }

  @Override
  public void setInput(InputStream inputStream, String inputEncoding)
      throws XmlPullParserException {
    throw new XmlPullParserException("setInput() not supported");
  }

  @Override
  public void defineEntityReplacementText(
      String entityName, String replacementText)
      throws XmlPullParserException {
    throw new XmlPullParserException(
        "defineEntityReplacementText() not supported");
  }

  @Override
  public String getNamespacePrefix(int pos)
      throws XmlPullParserException {
    throw new XmlPullParserException(
        "getNamespacePrefix() not supported");
  }

  @Override
  public String getInputEncoding() {
    return null;
  }

  @Override
  public String getNamespace(String prefix) {
    throw new RuntimeException(
        "getNamespaceCount() not supported");
  }

  @Override
  public int getNamespaceCount(int depth)
      throws XmlPullParserException {
    throw new XmlPullParserException(
        "getNamespaceCount() not supported");
  }

  @Override
  public String getPositionDescription() {
    return "XML file " + fileName + " line #" + getLineNumber() + " (sorry, not yet implemented)";
  }

  @Override
  public String getNamespaceUri(int pos)
      throws XmlPullParserException {
    throw new XmlPullParserException(
        "getNamespaceUri() not supported");
  }

  @Override
  public int getColumnNumber() {
    // Android always returns -1
    return -1;
  }

  @Override
  public int getDepth() {
    return mDepth;
  }

  @Override
  public String getText() {
    if (currentNode == null) {
      return "";
    }
    return StringResources.processStringResources(currentNode.getTextContent());
  }

  @Override
  public int getLineNumber() {
    // TODO(msama): The current implementation is
    //   unable to return line numbers.
    return -1;
  }

  @Override
  public int getEventType()
      throws XmlPullParserException {
    return mEventType;
  }

  /*package*/
  public boolean isWhitespace(String text)
      throws XmlPullParserException {
    if (text == null) {
      return false;
    }
    return text.split("\\s").length == 0;
  }

  @Override
  public boolean isWhitespace()
      throws XmlPullParserException {
    // Note: in android whitespaces are automatically stripped.
    // Here we have to skip them manually
    return isWhitespace(getText());
  }

  @Override
  public String getPrefix() {
    throw new RuntimeException("getPrefix not supported");
  }

  @Override
  public char[] getTextCharacters(int[] holderForStartAndLength) {
    String txt = getText();
    char[] chars = null;
    if (txt != null) {
      holderForStartAndLength[0] = 0;
      holderForStartAndLength[1] = txt.length();
      chars = new char[txt.length()];
      txt.getChars(0, txt.length(), chars, 0);
    }
    return chars;
  }

  @Override
  public String getNamespace() {
    String namespace = currentNode != null ? currentNode.getNamespaceURI() : null;
    if (namespace == null) {
      return "";
    }

    return maybeReplaceNamespace(namespace);
  }

  @Override
  public String getName() {
    if (currentNode == null) {
      return null;
    }
    return currentNode.getNodeName();
  }

  Node getAttributeAt(int index) {
    if (currentNode == null) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    NamedNodeMap map = currentNode.getAttributes();
    if (index >= map.getLength()) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    return map.item(index);
  }

  public String getAttribute(String namespace, String name) {
    if (currentNode == null) {
      return null;
    }

    Element element = (Element) currentNode;
    if (element.hasAttributeNS(namespace, name)) {
      return element.getAttributeNS(namespace, name).trim();
    } else if (applicationNamespace.equals(namespace)
        && element.hasAttributeNS(AttributeResource.RES_AUTO_NS_URI, name)) {
      return element.getAttributeNS(AttributeResource.RES_AUTO_NS_URI, name).trim();
    }

    return null;
  }

  @Override
  public String getAttributeNamespace(int index) {
    Node attr = getAttributeAt(index);
    if (attr == null) {
      return "";
    }
    return maybeReplaceNamespace(attr.getNamespaceURI());
  }

  private String maybeReplaceNamespace(String namespace) {
    if (namespace == null) {
      return "";
    } else if (namespace.equals(applicationNamespace)) {
      return AttributeResource.RES_AUTO_NS_URI;
    } else {
      return namespace;
    }
  }

  @Override
  public String getAttributeName(int index) {
    Node attr = getAttributeAt(index);
    String name = attr.getLocalName();
    return name == null ? attr.getNodeName() : name;
  }

  @Override
  public String getAttributePrefix(int index) {
    throw new RuntimeException("getAttributePrefix not supported");
  }

  @Override
  public boolean isEmptyElementTag() throws XmlPullParserException {
    // In Android this method is left unimplemented.
    // This implementation is mirroring that.
    return false;
  }

  @Override
  public int getAttributeCount() {
    if (currentNode == null) {
      return -1;
    }
    return currentNode.getAttributes().getLength();
  }

  @Override
  public String getAttributeValue(int index) {
    return qualify(getAttributeAt(index).getNodeValue());
  }

  // for testing only...
  public String qualify(String value) {
    if (value == null) return null;
    if (AttributeResource.isResourceReference(value)) {
      return "@" + ResName.qualifyResourceName(value.trim().substring(1).replace("+", ""), packageName, "attr");
    } else if (AttributeResource.isStyleReference(value)) {
      return "?" + ResName.qualifyResourceName(value.trim().substring(1), packageName, "attr");
    } else {
      return StringResources.processStringResources(value);
    }
  }

  @Override
  public String getAttributeType(int index) {
    // Android always returns CDATA even if the
    // node has no attribute.
    return "CDATA";
  }

  @Override
  public boolean isAttributeDefault(int index) {
    // The android implementation always returns false
    return false;
  }

  @Override
  public int nextToken() throws XmlPullParserException, IOException {
    return next();
  }

  @Override
  public String getAttributeValue(String namespace, String name) {
    return qualify(getAttribute(namespace, name));
  }

  @Override
  public int next() throws XmlPullParserException, IOException {
    if (!mStarted) {
      mStarted = true;
      return START_DOCUMENT;
    }
    if (mEventType == END_DOCUMENT) {
      return END_DOCUMENT;
    }
    int ev = nativeNext();
    if (mDecNextDepth) {
      mDepth--;
      mDecNextDepth = false;
    }
    switch (ev) {
      case START_TAG:
        mDepth++;
        break;
      case END_TAG:
        mDecNextDepth = true;
        break;
    }
    mEventType = ev;
    if (ev == END_DOCUMENT) {
      // Automatically close the parse when we reach the end of
      // a document, since the standard XmlPullParser interface
      // doesn't have such an API so most clients will leave us
      // dangling.
      close();
    }
    return ev;
  }

  /**
   * A twin implementation of the native android nativeNext(status)
   *
   * @throws XmlPullParserException
   */
  private int nativeNext() throws XmlPullParserException {
    switch (mEventType) {
      case (CDSECT): {
        throw new IllegalArgumentException(
            "CDSECT is not handled by Android");
      }
      case (COMMENT): {
        throw new IllegalArgumentException(
            "COMMENT is not handled by Android");
      }
      case (DOCDECL): {
        throw new IllegalArgumentException(
            "DOCDECL is not handled by Android");
      }
      case (ENTITY_REF): {
        throw new IllegalArgumentException(
            "ENTITY_REF is not handled by Android");
      }
      case (END_DOCUMENT): {
        // The end document event should have been filtered
        // from the invoker. This should never happen.
        throw new IllegalArgumentException(
            "END_DOCUMENT should not be found here.");
      }
      case (END_TAG): {
        return navigateToNextNode(currentNode);
      }
      case (IGNORABLE_WHITESPACE): {
        throw new IllegalArgumentException(
            "IGNORABLE_WHITESPACE");
      }
      case (PROCESSING_INSTRUCTION): {
        throw new IllegalArgumentException(
            "PROCESSING_INSTRUCTION");
      }
      case (START_DOCUMENT): {
        currentNode = document.getDocumentElement();
        return START_TAG;
      }
      case (START_TAG): {
        if (currentNode.hasChildNodes()) {
          // The node has children, navigate down
          return processNextNodeType(
              currentNode.getFirstChild());
        } else {
          // The node has no children
          return END_TAG;
        }
      }
      case (TEXT): {
        return navigateToNextNode(currentNode);
      }
      default: {
        // This can only happen if mEventType is
        // assigned with an unmapped integer.
        throw new RuntimeException(
            "Robolectric-> Uknown XML event type: " + mEventType);
      }
    }

  }

  /*protected*/ int processNextNodeType(Node node)
      throws XmlPullParserException {
    switch (node.getNodeType()) {
      case (Node.ATTRIBUTE_NODE): {
        throw new IllegalArgumentException("ATTRIBUTE_NODE");
      }
      case (Node.CDATA_SECTION_NODE): {
        return navigateToNextNode(node);
      }
      case (Node.COMMENT_NODE): {
        return navigateToNextNode(node);
      }
      case (Node.DOCUMENT_FRAGMENT_NODE): {
        throw new IllegalArgumentException("DOCUMENT_FRAGMENT_NODE");
      }
      case (Node.DOCUMENT_NODE): {
        throw new IllegalArgumentException("DOCUMENT_NODE");
      }
      case (Node.DOCUMENT_TYPE_NODE): {
        throw new IllegalArgumentException("DOCUMENT_TYPE_NODE");
      }
      case (Node.ELEMENT_NODE): {
        currentNode = node;
        return START_TAG;
      }
      case (Node.ENTITY_NODE): {
        throw new IllegalArgumentException("ENTITY_NODE");
      }
      case (Node.ENTITY_REFERENCE_NODE): {
        throw new IllegalArgumentException("ENTITY_REFERENCE_NODE");
      }
      case (Node.NOTATION_NODE): {
        throw new IllegalArgumentException("DOCUMENT_TYPE_NODE");
      }
      case (Node.PROCESSING_INSTRUCTION_NODE): {
        throw new IllegalArgumentException("DOCUMENT_TYPE_NODE");
      }
      case (Node.TEXT_NODE): {
        if (isWhitespace(node.getNodeValue())) {
          // Skip whitespaces
          return navigateToNextNode(node);
        } else {
          currentNode = node;
          return TEXT;
        }
      }
      default: {
        throw new RuntimeException(
            "Robolectric -> Unknown node type: " +
                node.getNodeType() + ".");
      }
    }
  }

  /**
   * Navigate to the next node after a node and all of his
   * children have been explored.
   *
   * If the node has unexplored siblings navigate to the
   * next sibling. Otherwise return to its parent.
   *
   * @param node the node which was just explored.
   * @return {@link XmlPullParserException#START_TAG} if the given
   *         node has siblings, {@link XmlPullParserException#END_TAG}
   *         if the node has no unexplored siblings or
   *         {@link XmlPullParserException#END_DOCUMENT} if the explored
   *         was the root document.
   * @throws XmlPullParserException if the parser fails to
   *                                parse the next node.
   */
  int navigateToNextNode(Node node)
      throws XmlPullParserException {
    Node nextNode = node.getNextSibling();
    if (nextNode != null) {
      // Move to the next siblings
      return processNextNodeType(nextNode);
    } else {
      // Goes back to the parent
      if (document.getDocumentElement().equals(node)) {
        currentNode = null;
        return END_DOCUMENT;
      }
      currentNode = node.getParentNode();
      return END_TAG;
    }
  }

  @Override
  public void require(int type, String namespace, String name)
      throws XmlPullParserException, IOException {
    if (type != getEventType()
        || (namespace != null && !namespace.equals(getNamespace()))
        || (name != null && !name.equals(getName()))) {
      throw new XmlPullParserException(
          "expected " + TYPES[type] + getPositionDescription());
    }
  }

  @Override
  public String nextText() throws XmlPullParserException, IOException {
    if (getEventType() != START_TAG) {
      throw new XmlPullParserException(
          getPositionDescription()
              + ": parser must be on START_TAG to read next text", this, null);
    }
    int eventType = next();
    if (eventType == TEXT) {
      String result = getText();
      eventType = next();
      if (eventType != END_TAG) {
        throw new XmlPullParserException(
            getPositionDescription()
                + ": event TEXT it must be immediately followed by END_TAG", this, null);
      }
      return result;
    } else if (eventType == END_TAG) {
      return "";
    } else {
      throw new XmlPullParserException(
          getPositionDescription()
              + ": parser must be on START_TAG or TEXT to read text", this, null);
    }
  }

  @Override
  public int nextTag() throws XmlPullParserException, IOException {
    int eventType = next();
    if (eventType == TEXT && isWhitespace()) { // skip whitespace
      eventType = next();
    }
    if (eventType != START_TAG && eventType != END_TAG) {
      throw new XmlPullParserException(
          "Expected start or end tag. Found: " + eventType, this, null);
    }
    return eventType;
  }

  @Override
  public int getAttributeNameResource(int index) {
    String attributeNamespace = getAttributeNamespace(index);
    if (attributeNamespace.equals(RES_AUTO_NS_URI)) {
      attributeNamespace = packageName;
    } else if (attributeNamespace.startsWith(ANDROID_RES_NS_PREFIX)) {
      attributeNamespace = attributeNamespace.substring(ANDROID_RES_NS_PREFIX.length());
    }
    return getResourceId(getAttributeName(index), attributeNamespace, "attr");
  }

  @Override
  public int getAttributeListValue(String namespace, String attribute,
      String[] options, int defaultValue) {
    String attr = getAttribute(namespace, attribute);
    if (attr == null) {
      return 0;
    }
    List<String> optList = Arrays.asList(options);
    int index = optList.indexOf(attr);
    if (index == -1) {
      return defaultValue;
    }
    return index;
  }

  @Override
  public boolean getAttributeBooleanValue(String namespace, String attribute,
      boolean defaultValue) {
    String attr = getAttribute(namespace, attribute);
    if (attr == null) {
      return defaultValue;
    }
    return Boolean.parseBoolean(attr);
  }

  @Override
  public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
    String attr = getAttribute(namespace, attribute);
    if (attr != null && attr.startsWith("@") && !AttributeResource.isNull(attr)) {
      return getResourceId(attr, packageName, null);
    }
    return defaultValue;
  }

  @Override
  public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
    return XmlUtils.convertValueToInt(this.getAttributeValue(namespace, attribute), defaultValue);
  }

  @Override
  public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
    int value = getAttributeIntValue(namespace, attribute, defaultValue);
    if (value < 0) {
      return defaultValue;
    }
    return value;
  }

  @Override
  public float getAttributeFloatValue(String namespace, String attribute,
      float defaultValue) {
    String attr = getAttribute(namespace, attribute);
    if (attr == null) {
      return defaultValue;
    }
    try {
      return Float.parseFloat(attr);
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }

  @Override
  public int getAttributeListValue(
      int idx, String[] options, int defaultValue) {
    try {
      String value = getAttributeValue(idx);
      List<String> optList = Arrays.asList(options);
      int index = optList.indexOf(value);
      if (index == -1) {
        return defaultValue;
      }
      return index;
    } catch (IndexOutOfBoundsException ex) {
      return defaultValue;
    }
  }

  @Override
  public boolean getAttributeBooleanValue(
      int idx, boolean defaultValue) {
    try {
      return Boolean.parseBoolean(getAttributeValue(idx));
    } catch (IndexOutOfBoundsException ex) {
      return defaultValue;
    }
  }

  @Override
  public int getAttributeResourceValue(int idx, int defaultValue) {
    String attributeValue = getAttributeValue(idx);
    if (attributeValue != null && attributeValue.startsWith("@")) {
      int resourceId = getResourceId(attributeValue.substring(1), packageName, null);
      if (resourceId != 0) {
        return resourceId;
      }
    }
    return defaultValue;
  }

  @Override
  public int getAttributeIntValue(int idx, int defaultValue) {
    try {
      return Integer.parseInt(getAttributeValue(idx));
    } catch (NumberFormatException ex) {
      return defaultValue;
    } catch (IndexOutOfBoundsException ex) {
      return defaultValue;
    }
  }

  @Override
  public int getAttributeUnsignedIntValue(int idx, int defaultValue) {
    int value = getAttributeIntValue(idx, defaultValue);
    if (value < 0) {
      return defaultValue;
    }
    return value;
  }

  @Override
  public float getAttributeFloatValue(int idx, float defaultValue) {
    try {
      return Float.parseFloat(getAttributeValue(idx));
    } catch (NumberFormatException ex) {
      return defaultValue;
    } catch (IndexOutOfBoundsException ex) {
      return defaultValue;
    }
  }

  @Override
  public String getIdAttribute() {
    return getAttribute(null, "id");
  }

  @Override
  public String getClassAttribute() {
    return getAttribute(null, "class");
  }

  @Override
  public int getIdAttributeResourceValue(int defaultValue) {
    return getAttributeResourceValue(null, "id", defaultValue);
  }

  @Override
  public int getStyleAttribute() {
    String attr = getAttribute(null, "style");
    if (attr == null ||
        (!AttributeResource.isResourceReference(attr) && !AttributeResource.isStyleReference(attr))) {
      return 0;
    }

    int style = getResourceId(attr, packageName, "style");
    if (style == 0) {
      // try again with underscores...
      style = getResourceId(attr.replace('.', '_'), packageName, "style");
    }
    return style;
  }

  @Override
  public void close() {
    // Nothing to do
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  private int getResourceId(String possiblyQualifiedResourceName, String defaultPackageName, String defaultType) {

    if (AttributeResource.isNull(possiblyQualifiedResourceName)) return 0;

    if (AttributeResource.isStyleReference(possiblyQualifiedResourceName)) {
      ResName styleReference = AttributeResource.getStyleReference(possiblyQualifiedResourceName, defaultPackageName, "attr");
      Integer resourceId = resourceTable.getResourceId(styleReference);
      if (resourceId == null) {
        throw new Resources.NotFoundException(styleReference.getFullyQualifiedName());
      }
      return resourceId;
    }

    if (AttributeResource.isResourceReference(possiblyQualifiedResourceName)) {
      ResName resourceReference = AttributeResource.getResourceReference(possiblyQualifiedResourceName, defaultPackageName, defaultType);
      Integer resourceId = resourceTable.getResourceId(resourceReference);
      if (resourceId == null) {
        throw new Resources.NotFoundException(resourceReference.getFullyQualifiedName());
      }
      return resourceId;
    }
    possiblyQualifiedResourceName = removeLeadingSpecialCharsIfAny(possiblyQualifiedResourceName);
    ResName resName = ResName.qualifyResName(possiblyQualifiedResourceName, defaultPackageName, defaultType);
    Integer resourceId = resourceTable.getResourceId(resName);
    return resourceId == null ? 0 : resourceId;
  }

  private static String removeLeadingSpecialCharsIfAny(String name){
    if (name.startsWith("@+")) {
      return name.substring(2);
    }
    if (name.startsWith("@")) {
      return name.substring(1);
    }
    return name;
  }

  /**
   * Tell is a given feature is supported by android.
   *
   * @param name Feature name.
   * @return True if the feature is supported.
   */
  private static boolean isAndroidSupportedFeature(String name) {
    if (name == null) {
      return false;
    }
    for (String feature : AVAILABLE_FEATURES) {
      if (feature.equals(name)) {
        return true;
      }
    }
    return false;
  }
}
