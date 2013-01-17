package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.I18nException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class PreferenceLoader extends XmlLoader {

    private final ResourceExtractor resourceExtractor;
    private Map<String, PreferenceNode> prefNodesByResourceName = new HashMap<String, PreferenceNode>();

    public PreferenceLoader(ResourceExtractor resourceExtractor) {
        this.resourceExtractor = resourceExtractor;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {
        PreferenceNode topLevelNode = new PreferenceNode("top-level", new ArrayList<Attribute>());
        processChildren(document.getChildNodes(), topLevelNode, xmlContext);
        prefNodesByResourceName.put(xmlContext.packageName + ":xml/" + xmlFile.getName().replace(".xml", ""), topLevelNode.getChildren().get(0));
    }

    private void processChildren(NodeList childNodes, PreferenceNode parent, XmlContext xmlContext) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent, xmlContext);
        }
    }

    private void processNode(Node node, PreferenceNode parent, XmlContext xmlContext) {
        String name = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();
        List<Attribute> attrList = new ArrayList<Attribute>();

        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = attributes.item(i);
                String attrName = ResourceExtractor.qualifyResourceName(attr.getNodeName(), xmlContext.packageName);
                if (attrName.startsWith("xmlns:")) {
                    // ignore
                } else {
                    attrList.add(new Attribute(Attribute.addType(attrName, "attr"), attr.getNodeValue(), xmlContext.packageName));
                }
            }
        }

        if (!name.startsWith("#")) {
            PreferenceNode prefNode = new PreferenceNode(name, attrList);
            if (parent != null) parent.addChild(prefNode);

            processChildren(node.getChildNodes(), prefNode, xmlContext);
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
        private final List<Attribute> attributes;

        private List<PreferenceNode> children = new ArrayList<PreferenceNode>();

        public PreferenceNode(String name, List<Attribute> attributes) {
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

            TestAttributeSet attributeSet = shadowOf(context).createAttributeSet(attributes, null);

            /**
             * This block is required because the PreferenceScreen(Context, AttributeSet) constructor is somehow hidden
             * from reflection. The only way to set keys/titles/summaries on PreferenceScreens is to set them manually.
             */
               if (clazz.equals(PreferenceScreen.class)) {
                   PreferenceScreen screen = Robolectric.newInstanceOf(PreferenceScreen.class);
                   screen.setKey(Attribute.findValue(attributes, "android:attr/key"));
                   screen.setTitle(Attribute.findValue(attributes, "android:attr/title"));
                   screen.setSummary(Attribute.findValue(attributes, "android:attr/summary"));
                   return screen;
               }

               try {
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
