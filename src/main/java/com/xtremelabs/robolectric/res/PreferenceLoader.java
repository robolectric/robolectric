package com.xtremelabs.robolectric.res;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.I18nException;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;

public class PreferenceLoader extends XmlLoader {
	
    private Map<String, PreferenceNode> prefNodesByResourceName = new HashMap<String, PreferenceNode>();

	public PreferenceLoader(ResourceExtractor resourceExtractor) {
		super(resourceExtractor);
	}

	@Override
	protected void processResourceXml(File xmlFile, Document document, boolean isSystem) throws Exception {
		PreferenceNode topLevelNode = new PreferenceNode("top-level", new HashMap<String, String>());
		processChildren(document.getChildNodes(), topLevelNode);
		prefNodesByResourceName.put( "xml/" + xmlFile.getName().replace(".xml", ""), topLevelNode.getChildren().get(0));
	}

    private void processChildren(NodeList childNodes, PreferenceNode parent) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent);
        }
    }
	
    private void processNode(Node node, PreferenceNode parent) {
        String name = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();
        Map<String, String> attrMap = new HashMap<String, String>();
        
        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = attributes.item(i);
                attrMap.put(attr.getNodeName(), attr.getNodeValue());
            }
        }
        
        if (!name.startsWith("#")) {
	        PreferenceNode prefNode = new PreferenceNode(name, attrMap);
	        if (parent != null) parent.addChild(prefNode);
	
	        processChildren(node.getChildNodes(), prefNode);  
        }
    }
 
	public PreferenceScreen inflatePreferences(Context context, int resourceId) {
		return inflatePreferences(context, resourceExtractor.getResourceName(resourceId));		
	}
	
	public PreferenceScreen inflatePreferences(Context context, String key) {
        try {
        	PreferenceNode prefNode = prefNodesByResourceName.get(key);
        	return (PreferenceScreen) prefNode.inflate(context, null);
        } catch (I18nException e) {
        	throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + key, e);
        }
	}

    public class PreferenceNode {
        private String name;
        private final Map<String, String> attributes;
        
        private List<PreferenceNode> children = new ArrayList<PreferenceNode>();

        public PreferenceNode(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        public List<PreferenceNode> getChildren() {
            return children;
        }

        public void addChild(PreferenceNode prefNode) {
            children.add(prefNode);
        }
        
        public Preference inflate(Context context, Preference parent) throws Exception {
        	Preference preference = create(context, (PreferenceGroup) parent);

            for (PreferenceNode child : children) {
                child.inflate(context, preference);
            }
     
        	return preference;
        }
        
        private Preference create(Context context, PreferenceGroup parent) throws Exception {
        	Preference preference = constructPreference(context, parent);
            if (parent != null && parent != preference) {
                parent.addPreference(preference);
            } 
            return preference;
        }
  
        private Preference constructPreference(Context context, PreferenceGroup parent) throws Exception {
        	Class<? extends Preference> clazz = pickViewClass();
        	
           	if (clazz.equals(PreferenceScreen.class)) {
        		return Robolectric.newInstanceOf(PreferenceScreen.class);
        	}
           	
           	try {
                TestAttributeSet attributeSet = new TestAttributeSet(attributes);
                if (strictI18n) {
                	attributeSet.validateStrictI18n();
                }
                return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class, AttributeSet.class)).newInstance(context, attributeSet);
            } catch (NoSuchMethodException e) {
	            try {
	                return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class)).newInstance(context);
	            } catch (NoSuchMethodException e1) {
	                return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class, String.class)).newInstance(context, "");
	            }  
            }
        }
  
        private Class<? extends Preference> pickViewClass() {
            Class<? extends Preference> clazz = loadClass(name);
            if (clazz == null) {
                clazz = loadClass("android.preference." + name);
            }
            if (clazz == null) {
                throw new RuntimeException("couldn't find preference class " + name);
            }
            return clazz;
        }
        
        private Class<? extends Preference> loadClass(String className) {
            try {
                //noinspection unchecked
                return (Class<? extends Preference>) getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }        
    }
}
