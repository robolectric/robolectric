package com.xtremelabs.robolectric.res;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;

/**
 * Loader for xml property files.
 * 
 * <p>Given a resource file a concrete implementation of {@link XmlResourceParser}
 * is returned. The returned implementation is based on the current Android
 * implementation. Please see the android source code for further details.
 * 
 * @author msama (michele@swiftkey.net)
 * 
 * @see https://github.com/android/platform_frameworks_base/blob/master/core/java/android/content/res/XmlBlock.java
 * @see Resources#getXml(int)
 */
public class XmlFileLoader extends XmlLoader {
	
	private Map<String, Document> mXmlDocuments = 
			new HashMap<String, Document>();
	
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
	
	/**
     * Tell is a given feature is supported by android.
     */
	public static boolean isAndroidSupportedFeature(String name) {
    	if (name == null) {
    		return false;
    	}
    	for (String feature: AVAILABLE_FEATURES) {
    		if (feature.equals(name)) {
    			return true;
    		}
    	}
    	return false;
    }

	public XmlFileLoader(ResourceExtractor resourceExtractor) {
		super(resourceExtractor);
	}

	@Override
	protected void processResourceXml(
			File xmlFile, Document document, boolean isSystem)
					throws Exception {
		mXmlDocuments.put(
				"xml/" + xmlFile.getName().replace(".xml", ""), document);
	}

 
	public XmlResourceParser getXml(int resourceId) {
		return getXml(resourceExtractor.getResourceName(resourceId));		
	}
	
	public XmlResourceParser getXml(String key) {
		Document document = mXmlDocuments.get(key);
		if (document == null) {
			return null;
		}
        return new XmlResourceParserImpl(document);
	}

	/**
	 * Concrete implementation of the {@link XmlResourceParser}.
	 * 
	 * <p>Clients expects a pull parser while the resource loader
	 * initialise this object with a {@link Document}. 
	 * This implementation navigates the dom and emulates a pull
	 * parser by raising all the opportune events.
	 * 
	 * <p>Note that the original android implementation is based on
	 * a set of native methods calls. Here those methods are
	 * re-implemented in java when possible.
	 * 
	 * @see https://github.com/android/platform_frameworks_base/blob/master/core/java/android/content/res/XmlBlock.java
	 */
	final class XmlResourceParserImpl
			implements XmlResourceParser {

        private final Document document;
        private Node currentNode;
        
        private boolean mStarted = false;
        private boolean mDecNextDepth = false;
        private int mDepth = 0;
        private int mEventType = START_DOCUMENT;
        
        XmlResourceParserImpl(Document document) {
        	this.document = document;
        }
        
        public void setFeature(String name, boolean state)
        		throws XmlPullParserException {
        	if (isAndroidSupportedFeature(name) && state) {
        		return;
        	}
            throw new XmlPullParserException("Unsupported feature: " + name);
        }
        
        public boolean getFeature(String name) {
        	return isAndroidSupportedFeature(name);
        }
        
        public void setProperty(String name, Object value)
        		throws XmlPullParserException {
            throw new XmlPullParserException("setProperty() not supported");
        }
        
        public Object getProperty(String name) {
        	// Properties are not supported. Android returns null
        	// instead of throwing an XmlPullParserException.
            return null;
        }
        
        public void setInput(Reader in) throws XmlPullParserException {
            throw new XmlPullParserException("setInput() not supported");
        }
        
        public void setInput(InputStream inputStream, String inputEncoding)
        		throws XmlPullParserException {
            throw new XmlPullParserException("setInput() not supported");
        }
        
        public void defineEntityReplacementText(
        		String entityName, String replacementText)
        				throws XmlPullParserException {
            throw new XmlPullParserException(
            		"defineEntityReplacementText() not supported");
        }
        
        public String getNamespacePrefix(int pos)
        		throws XmlPullParserException {
            throw new XmlPullParserException(
            		"getNamespacePrefix() not supported");
        }
        
        public String getInputEncoding() {
            return null;
        }
        
        public String getNamespace(String prefix) {
        	throw new RuntimeException(
            		"getNamespaceCount() not supported");
        }
        
        public int getNamespaceCount(int depth)
        		throws XmlPullParserException {
            throw new XmlPullParserException(
            		"getNamespaceCount() not supported");
        }
        
        public String getPositionDescription() {
            return "Binary XML file line #" + getLineNumber();
        }
        
        public String getNamespaceUri(int pos)
        		throws XmlPullParserException {
            throw new XmlPullParserException(
            		"getNamespaceUri() not supported");
        }
        
        public int getColumnNumber() {
        	// Android always returns -1
            return -1;
        }
        
        public int getDepth() {
            return mDepth;
        }
        
        public String getText() {
        	if (currentNode == null) {
        		return "";
        	}
        	return currentNode.getTextContent();
        }
        
        public int getLineNumber() {
        	// TODO(msama): The current implementation is 
        	//		unable to return line numbers.
        	return -1;
        }
        
        public int getEventType()
        		throws XmlPullParserException {
            return mEventType;
        }
        
        /*package*/ boolean isWhitespace(String text)
        		throws XmlPullParserException {
            if (text == null) {
            	return false;
            }
            return text.split("\\s").length == 0;
        }
        
        public boolean isWhitespace()
        		throws XmlPullParserException {
            // Note: in android whitespaces are automatically stripped.
        	// Here we have to skip them manually
        	return isWhitespace(getText());
        }
        
        public String getPrefix() {
            throw new RuntimeException("getPrefix not supported");
        }
        
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
        
        public String getNamespace() {
        	if (currentNode == null) {
        		return "";
        	}
        	String namespace = currentNode.getNamespaceURI();
        	if (namespace == null) {
        		return "";
        	}
        	return namespace;
        }
        
        public String getName() {
        	if (currentNode == null) {
        		return "";
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
        
        Node getAttribute(String namespace, String name) {
        	if (currentNode == null) {
        		return null;
        	}
        	NamedNodeMap map = currentNode.getAttributes();
        	// XXX(msama): getNamedItemNS does not work.
        	// 		This is an hack to make this implementation working.
        	//		return map.getNamedItemNS(namespace, name);
        	return map.getNamedItem(name);
        }
        
        public String getAttributeNamespace(int index) {
        	Node attr = getAttributeAt(index);
        	if (attr == null) {
        		return null;
        	}
        	return attr.getNamespaceURI();
        }
        
        public String getAttributeName(int index) {
        	try {
        		Node attr = getAttributeAt(index);
        		return attr.getNodeName();
        	} catch(IndexOutOfBoundsException ex) {
        		return null;
        	}
        }
        
        public String getAttributePrefix(int index) {
            throw new RuntimeException("getAttributePrefix not supported");
        }
        
        public boolean isEmptyElementTag() throws XmlPullParserException {
            // In Android this method is left unimplemented.
        	// This implementation is mirroring that.
            return false;
        }
        
        public int getAttributeCount() {
            if (currentNode == null) {
            	return -1;
            }
            return currentNode.getAttributes().getLength();
        }
        
        public String getAttributeValue(int index) {
        	return getAttributeAt(index).getNodeValue();
        }
        
        public String getAttributeType(int index) {
        	// Android always returns CDATA even if the
        	// node has no attribute.
            return "CDATA";
        }
        
        public boolean isAttributeDefault(int index) {
        	// The android implementation always returns false
            return false;
        }
        
        public int nextToken() throws XmlPullParserException,IOException {
            return next();
        }
        
        public String getAttributeValue(String namespace, String name) {
        	Node attr = getAttribute(namespace, name);
        	if (attr == null) {
        		return null;
        	}
        	return attr.getNodeValue();
        }
        
        public int next() throws XmlPullParserException,IOException {       	
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
         * @throws XmlPullParserException 
         */
        private int nativeNext() throws XmlPullParserException {
        	switch(mEventType) {
        		case(CDSECT): {
        			throw new IllegalArgumentException(
        					"CDSECT is not handled by Android");
        		}
        		case(COMMENT): {
        			throw new IllegalArgumentException(
        					"COMMENT is not handled by Android");
        		}
        		case(DOCDECL): {
        			throw new IllegalArgumentException(
        					"DOCDECL is not handled by Android");
        		}
        		case(ENTITY_REF): {
        			throw new IllegalArgumentException(
        					"ENTITY_REF is not handled by Android");
        		}
        		case(END_DOCUMENT): {
        			// The end document event should have been filtered
        			// from the invoker. This should never happen.
        			throw new IllegalArgumentException(
        					"END_DOCUMENT should not be found here.");
        		}
        		case(END_TAG): {
        			return navigateToNextNode(currentNode);
        		}
        		case(IGNORABLE_WHITESPACE): {
        			throw new IllegalArgumentException(
        					"IGNORABLE_WHITESPACE");
        		}
        		case(PROCESSING_INSTRUCTION): {
        			throw new IllegalArgumentException(
        					"PROCESSING_INSTRUCTION");
        		}
        		case(START_DOCUMENT): {
        			currentNode = document.getDocumentElement();
        			return START_TAG;
        		}
        		case(START_TAG): {
        			if (currentNode.hasChildNodes()) {
        				// The node has children, navigate down
        				return processNextNodeType(
        						currentNode.getFirstChild());
        			} else {
        				// The node has no children
        				return END_TAG;
        			}
        		}
        		case(TEXT): {
        			return navigateToNextNode(currentNode);
        		}
        		default : {
        			// This can only happen if mEventType is
        			// assigned with an unmapped integer.
        			throw new RuntimeException(
                			"Roboletric-> Uknown XML event type: " + mEventType);
        		}
        	}

        }
        
        /*protected*/ int processNextNodeType(Node node)
        		throws XmlPullParserException {
        	switch (node.getNodeType()) {
				case(Node.ATTRIBUTE_NODE): {
					throw new IllegalArgumentException("ATTRIBUTE_NODE");
				}
				case(Node.CDATA_SECTION_NODE): {
					return navigateToNextNode(node);
				}
				case(Node.COMMENT_NODE): {
					return navigateToNextNode(node);
				}
				case(Node.DOCUMENT_FRAGMENT_NODE): {
					throw new IllegalArgumentException("DOCUMENT_FRAGMENT_NODE");
				}
				case(Node.DOCUMENT_NODE): {
					throw new IllegalArgumentException("DOCUMENT_NODE");
				}
				case(Node.DOCUMENT_TYPE_NODE): {
					throw new IllegalArgumentException("DOCUMENT_TYPE_NODE");
				}
				case(Node.ELEMENT_NODE): {
					currentNode = node;
					return START_TAG;
				}
				case(Node.ENTITY_NODE): {
					throw new IllegalArgumentException("ENTITY_NODE");
				}
				case(Node.ENTITY_REFERENCE_NODE): {
					throw new IllegalArgumentException("ENTITY_REFERENCE_NODE");
				}
				case(Node.NOTATION_NODE): {
					throw new IllegalArgumentException("DOCUMENT_TYPE_NODE");
				}
				case(Node.PROCESSING_INSTRUCTION_NODE): {
					throw new IllegalArgumentException("DOCUMENT_TYPE_NODE");
				}
				case(Node.TEXT_NODE): {
					if (isWhitespace(node.getNodeValue())) {
						// Skip whitespaces
						return navigateToNextNode(node);
					} else {
						currentNode = node;
						return TEXT;
					}
				}
				default : {
					throw new RuntimeException(
							"Roboletric -> Unknown node type: " + 
							node.getNodeType() + ".");
				}
        	}
        }
        
        /**
         * Navigate to the next node after a node and all of his 
         * children have been explored.
         * 
         * <p>If the node has unexplored siblings navigate to the
         * next sibling. Otherwise return to its parent.
         * 
         * @param node the node which was just explored.
         * @return {@link XmlPullParserException#START_TAG} if the given 
         * 		node has siblings, {@link XmlPullParserException#END_TAG}
         * 		if the node has no unexplored siblings or 
         * 		{@link XmlPullParserException#END_DOCUMENT} if the explored
         * 		was the root document.
         * @throws XmlPullParserException if the parser fails to 
         * 		parse the next node.
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
        
        public void require(int type, String namespace, String name)
        		throws XmlPullParserException, IOException {
            if (type != getEventType()
            		|| (namespace != null && !namespace.equals(getNamespace()))
            		|| (name != null && !name.equals(getName()))) {
                throw new XmlPullParserException(
                		"expected "+ TYPES[type] + getPositionDescription());
            }
        }
        
        public String nextText() throws XmlPullParserException,IOException {
            if (getEventType() != START_TAG) {
            	throw new XmlPullParserException(
            			getPositionDescription()
            			+ ": parser must be on START_TAG to read next text", this, null);
            }
            int eventType = next();
            if (eventType == TEXT) {
            	String result = getText();
                eventType = next();
                if(eventType != END_TAG) {
                    throw new XmlPullParserException(
                    		getPositionDescription()
                    		+ ": event TEXT it must be immediately followed by END_TAG", this, null);
                }
                return result;
            } else if(eventType == END_TAG) {
            	return "";
            } else {
            	throw new XmlPullParserException(
            			getPositionDescription()
            			+ ": parser must be on START_TAG or TEXT to read text", this, null);
            }
        }
        
        public int nextTag() throws XmlPullParserException,IOException {
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
    
        public int getAttributeNameResource(int index) {
        	throw new RuntimeException("Not implemented yet");
        }
    
        public int getAttributeListValue(String namespace, String attribute,
                String[] options, int defaultValue) {
    		Node attr = getAttribute(namespace, attribute);
    		if (attr == null) {
    			return 0;
    		}
    		List<String> optList = Arrays.asList(options);
        	int index = optList.indexOf(attr.getNodeValue());
        	if (index == -1) {
        		return defaultValue;
        	}
        	return index;
        }
        
        public boolean getAttributeBooleanValue(String namespace, String attribute,
                boolean defaultValue) {
        	Node attr = getAttribute(namespace, attribute);
        	if (attr == null) {
        		return defaultValue;
        	}
        	return Boolean.parseBoolean(attr.getNodeValue());
        }
        
        public int getAttributeResourceValue(String namespace, String attribute,
                int defaultValue) {
        	throw new RuntimeException("Not implemented yet");
        }
        
        public int getAttributeIntValue(String namespace, String attribute,
                int defaultValue) {
        	Node attr = getAttribute(namespace, attribute);
        	if (attr == null) {
        		return defaultValue;
        	}
        	try {
        		return Integer.parseInt(attr.getNodeValue());
        	} catch(NumberFormatException ex) {
        		return defaultValue;
        	}
        }
        
        public int getAttributeUnsignedIntValue(
        		String namespace, String attribute, int defaultValue)
        {
        	int value = getAttributeIntValue(namespace, attribute, defaultValue);
        	if (value < 0) {
        		return defaultValue;
        	}
        	return value;
        }
        
        public float getAttributeFloatValue(String namespace, String attribute,
                float defaultValue) {
        	Node attr = getAttribute(namespace, attribute);
        	if (attr == null) {
        		return defaultValue;
        	}
        	try {
	        	return Float.parseFloat(attr.getNodeValue());
	        } catch(NumberFormatException ex) {
	        	return defaultValue;
	    	}
        }

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
        
        public boolean getAttributeBooleanValue(
        		int idx, boolean defaultValue) {
        	try {
	        	return Boolean.parseBoolean(getAttributeValue(idx));
        	} catch (IndexOutOfBoundsException ex) {
        		return defaultValue;
        	}
        }
        
        public int getAttributeResourceValue(int idx, int defaultValue) {
        	throw new RuntimeException("Not implemented yet");
        }
        
        public int getAttributeIntValue(int idx, int defaultValue) {
        	try {
        		return Integer.parseInt(getAttributeValue(idx));
        	} catch(NumberFormatException ex) {
        		return defaultValue;
        	} catch (IndexOutOfBoundsException ex) {
        		return defaultValue;
        	} 
        }
        
        public int getAttributeUnsignedIntValue(int idx, int defaultValue) {
        	int value = getAttributeIntValue(idx, defaultValue);
            if (value < 0) {
            	return defaultValue;
            }
            return value;
        }
        
        public float getAttributeFloatValue(int idx, float defaultValue) {
        	try {
        		return Float.parseFloat(getAttributeValue(idx));
        	} catch(NumberFormatException ex) {
        		return defaultValue;
        	} catch (IndexOutOfBoundsException ex) {
        		return defaultValue;
        	} 
        }

        public String getIdAttribute() {
        	Node attr = getAttribute(null, "id");
        	if (attr == null) {
        		return null;
        	}
        	return attr.getNodeValue();
        }
        
        public String getClassAttribute() {
        	Node attr = getAttribute(null, "class");
        	if (attr == null) {
        		return null;
        	}
        	return attr.getNodeValue();
        }

        public int getIdAttributeResourceValue(int defaultValue) {
        	String id = getIdAttribute();
        	if (id == null) {
        		return defaultValue;
        	}
        	try {
        		return Integer.parseInt(id);
        	} catch (NumberFormatException ex) {
        		return defaultValue;
        	}
        }

        public int getStyleAttribute() {
        	Node attr = getAttribute(null, "style");
        	if (attr == null) {
        		return 0;
        	}
        	try {
        		return Integer.parseInt(attr.getNodeValue());
        	} catch (NumberFormatException ex) {
        		return 0;
        	}
        }

        public void close() {
            // Nothing to do
        }
        
        protected void finalize() throws Throwable {
            close();
        }
    }
}
