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
 * @author michele@swiftkey.net
 * 
 * @see Resources#getXml(int)
 */
public class XmlFileLoader extends XmlLoader {
	
    private Map<String, Document> xmlDocuments = new HashMap<String, Document>();

	public XmlFileLoader(ResourceExtractor resourceExtractor) {
		super(resourceExtractor);
	}

	@Override
	protected void processResourceXml(
			File xmlFile, Document document, boolean isSystem)
					throws Exception {
		xmlDocuments.put(
				"xml/" + xmlFile.getName().replace(".xml", ""), document);
	}

 
	public XmlResourceParser getXml(int resourceId) {
		return getXml(resourceExtractor.getResourceName(resourceId));		
	}
	
	public XmlResourceParser getXml(String key) {
		Document document = xmlDocuments.get(key);
		if (document == null) {
			return null;
		}
        return new XmlResourceParserImpl(document);
	}

	/*package*/ final class XmlResourceParserImpl
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
            if (FEATURE_PROCESS_NAMESPACES.equals(name) && state) {
                return;
            }
            if (FEATURE_REPORT_NAMESPACE_ATTRIBUTES.equals(name) && state) {
                return;
            }
            throw new XmlPullParserException("Unsupported feature: " + name);
        }
        
        public boolean getFeature(String name) {
            if (FEATURE_PROCESS_NAMESPACES.equals(name)) {
                return true;
            }
            if (FEATURE_REPORT_NAMESPACE_ATTRIBUTES.equals(name)) {
                return true;
            }
            return false;
        }
        
        public void setProperty(String name, Object value)
        		throws XmlPullParserException {
            throw new XmlPullParserException("setProperty() not supported");
        }
        
        public Object getProperty(String name) {
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
            		"getNamespace() not supported");
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
            return -1;
        }
        
        public int getDepth() {
            return mDepth;
        }
        public String getText() {
        	return currentNode.getNodeValue();
        }
        
        public int getLineNumber() {
        	return -1;
        }
        
        public int getEventType()
        		throws XmlPullParserException {
            return mEventType;
        }
        
        public boolean isWhitespace()
        		throws XmlPullParserException {
            // Implemented as in android.
            return false;
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
            return document.getNamespaceURI();
        }
        
        public String getName() {
        	if (currentNode == null) {
        		return "";
        	}
            return currentNode.getNodeName();
        }
        
        private Node getAttributeAt(int index) {
        	if (currentNode == null) {
        		throw new IndexOutOfBoundsException(String.valueOf(index));
        	}
        	NamedNodeMap map = currentNode.getAttributes();
        	if (index >= map.getLength()) {
        		throw new IndexOutOfBoundsException(String.valueOf(index));
        	}
        	return map.item(index);
        }
        
        private Node getAttribute(String namespaceURI, String name) {
        	if (currentNode == null) {
        		return null;
        	}
        	NamedNodeMap map = currentNode.getAttributes();
        	return map.getNamedItemNS(namespaceURI, name);
        }
        
        public String getAttributeNamespace(int index) {
        	return getAttributeAt(index).getNamespaceURI();
        }
        
        public String getAttributeName(int index) {
        	return getAttributeAt(index).getNodeName();
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
            	return 0;
            }
            return currentNode.getAttributes().getLength();
        }
        
        public String getAttributeValue(int index) {
        	return getAttributeAt(index).getNodeValue();
        }
        
        public String getAttributeType(int index) {
            return "CDATA";
        }
        
        public boolean isAttributeDefault(int index) {
            return false;
        }
        
        public int nextToken() throws XmlPullParserException,IOException {
            return next();
        }
        
        public String getAttributeValue(String namespaceURI, String name) {
        	return getAttribute(namespaceURI, name).getNodeValue();
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
         */
        /*package*/ int nativeNext() {
        	switch(mEventType) {
        		case(CDSECT): {
        			throw new IllegalArgumentException("CDSECT");
        		}
        		case(COMMENT): {
        			throw new IllegalArgumentException("COMMENT");
        		}
        		case(DOCDECL): {
        			throw new IllegalArgumentException("DOCDECL");
        		}
        		case(END_DOCUMENT): {
        			throw new IllegalArgumentException("END_DOCUMENT");
        		}
        		case(END_TAG): {
        			return navigateToNextNode(currentNode);
        		}
        		case(ENTITY_REF): {
        			throw new IllegalArgumentException("ENTITY_REF");
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
        	}
        	throw new RuntimeException(
        			"The next event has not been returned.");
        }
        
        /*protected*/ int processNextNodeType(Node node) {
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
					currentNode = node;
					return TEXT;
				}
        	}
        	throw new RuntimeException("The next event has not been returned.");
        }
        
        /*protected*/ int navigateToNextNode(Node node) {
			Node nextNode = node.getNextSibling();
			if (nextNode != null) {
				// Move to the next siblings
				return processNextNodeType(nextNode);
			} else {
				// Goes back to the parent
				if (document.getDocumentElement().equals(node)) {
	        		return END_DOCUMENT;
	        	}
				currentNode = node.getParentNode();
				return END_TAG;
			}
        }
        
        public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
            if (type != getEventType()
                || (namespace != null && !namespace.equals( getNamespace () ) )
                || (name != null && !name.equals( getName() ) ) )
                throw new XmlPullParserException( "expected "+ TYPES[ type ]+getPositionDescription());
        }
        
        public String nextText() throws XmlPullParserException,IOException {
            if(getEventType() != START_TAG) {
               throw new XmlPullParserException(
                 getPositionDescription()
                 + ": parser must be on START_TAG to read next text", this, null);
            }
            int eventType = next();
            if(eventType == TEXT) {
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
            if(eventType == TEXT ) { // && isWhitespace()) {   // skip whitespace
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
        		return defaultValue;
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
        		throw new RuntimeException(
        				"Expected an unsigned int. Found: " + value + ".");
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
	        	throw new RuntimeException(
	        			"Expected a float. Found: " + attr.getNodeValue() + ".");
	    	}
        }

        public int getAttributeListValue(int idx,
                String[] options, int defaultValue) {
        	String value = getAttributeValue(idx);
        	List<String> optList = Arrays.asList(options);
        	int index = optList.indexOf(value);
        	if (index == -1) {
        		return defaultValue;
        	}
        	return index;
        }
        
        public boolean getAttributeBooleanValue(int idx,
                boolean defaultValue) {
        	String value = getAttributeValue(idx);
        	return Boolean.parseBoolean(value);
        }
        
        public int getAttributeResourceValue(int idx, int defaultValue) {
        	throw new RuntimeException("Not implemented yet");
        }
        
        public int getAttributeIntValue(int idx, int defaultValue) {
        	String value = getAttributeValue(idx);
        	try {
        		return Integer.parseInt(value);
        	} catch(NumberFormatException ex) {
        		throw new RuntimeException("Expected an integer found: " + value + ".");
        	}
        }
        
        public int getAttributeUnsignedIntValue(int idx, int defaultValue) {
            int value = getAttributeIntValue(idx, defaultValue);
            if (value < 0) {
            	throw new RuntimeException(
            			"Expected an unsigned int. Found: " + value + ".");
            }
            return value;
        }
        
        public float getAttributeFloatValue(int idx, float defaultValue) {
        	String value = getAttributeValue(idx);
        	try {
        		return Float.parseFloat(value);
        	} catch(NumberFormatException ex) {
        		throw new RuntimeException("Expected a float. Found: " + value + ".");
        	}
        }

        public String getIdAttribute() {
        	throw new RuntimeException("Not implemented yet");
        }
        
        public String getClassAttribute() {
        	throw new RuntimeException("Not implemented yet");
        }

        public int getIdAttributeResourceValue(int defaultValue) {
        	throw new RuntimeException("Not implemented yet");
        }

        public int getStyleAttribute() {
        	throw new RuntimeException("Not implemented yet");
        }

        public void close() {
            // Nothing to do
        }
        
        protected void finalize() throws Throwable {
            close();
        }
    }
}
