package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.I18nException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ViewLoader extends XmlLoader {
    public static final String ATTR_LAYOUT = ":attr/layout";
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    /**
     * Map of "layout/foo" to the View nodes for that layout file
     */
    protected Map<String, ViewNode> viewNodesByLayoutName = new HashMap<String, ViewNode>();
    private AttrResourceLoader attrResourceLoader;
    private List<String> qualifierSearchPath = new ArrayList<String>();

    public ViewLoader(ResourceExtractor resourceExtractor, AttrResourceLoader attrResourceLoader) {
        super(resourceExtractor);
        this.attrResourceLoader = attrResourceLoader;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {
        ViewNode topLevelNode = new ViewNode("top-level", new ArrayList<Attribute>(), xmlContext);
        processChildren(document.getChildNodes(), topLevelNode, xmlContext);
        String parentDir = xmlFile.getParentFile().getName();
        String layoutName = xmlContext.packageName + ":layout/" + xmlFile.getName().replace(".xml", "");
        String specificLayoutName = xmlContext.packageName + ":" + parentDir + "/" + xmlFile.getName().replace(".xml", "");
        // Check to see if the generic "layout/foo" is already in the map.  If not, add it.
        if (!viewNodesByLayoutName.containsKey(layoutName)) {
            viewNodesByLayoutName.put(layoutName, topLevelNode.getChildren().get(0));
        }
        // Add the specific "layout-land/foo" to the map.  If this happens to be "layout/foo", it's a no-op.
        viewNodesByLayoutName.put(specificLayoutName, topLevelNode.getChildren().get(0));
    }

    private void processChildren(NodeList childNodes, ViewNode parent, XmlContext xmlContext) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent, xmlContext);
        }
    }

    private void processNode(Node node, ViewNode parent, XmlContext xmlContext) {
        String name = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();
        List<Attribute> attrList = new ArrayList<Attribute>();
        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = attributes.item(i);
                if (!XMLNS_URI.equals(attr.getNamespaceURI())) {
                    attrList.add(new Attribute(attr, xmlContext));
                }
            }
        }

        if (name.equals("requestFocus")) {
            parent.focusRequested(xmlContext);
        } else if (!name.startsWith("#")) {
            ViewNode viewNode = new ViewNode(name, attrList, parent.xmlContext);
            parent.addChild(viewNode);

            processChildren(node.getChildNodes(), viewNode, xmlContext);
        }
    }

    public View inflateView(Context context, String key, View parent) {
        return inflateView(context, key, new ArrayList<Attribute>(), parent);
    }

    public View inflateView(Context context, int resourceId, View parent) {
        return inflateView(context, resourceExtractor.getResourceName(resourceId), parent);
    }

    private View inflateView(Context context, String layoutName, List<Attribute> attributes, View parent) {
        ViewNode viewNode = getViewNodeByLayoutName(layoutName);
        if (viewNode == null) {
            throw new RuntimeException("Could not find layout " + layoutName);
        }

        try {
            return viewNode.plusAttributes(attributes).inflate(context, parent);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + layoutName, e);
        }
    }

    private ViewNode getViewNodeByLayoutName(String layoutName) {
        String[] parts = layoutName.split("/");
        if (parts[0].endsWith(":layout") && !qualifierSearchPath.isEmpty()) {
            String rawLayoutName = parts[1];
            for (String location : qualifierSearchPath) {
                ViewNode foundNode = viewNodesByLayoutName.get(parts[0] + "-" + location + "/" + rawLayoutName);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return viewNodesByLayoutName.get(layoutName);
    }

    public void setLayoutQualifierSearchPath(String... locations) {
        qualifierSearchPath = Arrays.asList(locations);
    }

    public class ViewNode {
        private final String name;
        private final List<Attribute> attributes;
        private final XmlContext xmlContext;

        private List<ViewNode> children = new ArrayList<ViewNode>();
        private boolean requestFocusOverride = false;

        public ViewNode(String name, List<Attribute> attributes, XmlContext xmlContext) {
            this.name = name;
            this.attributes = Collections.unmodifiableList(attributes);
            this.xmlContext = xmlContext;
        }

        public List<ViewNode> getChildren() {
            return children;
        }

        public void addChild(ViewNode viewNode) {
            children.add(viewNode);
        }

        public View inflate(Context context, View parent) throws Exception {
            View view = create(context, (ViewGroup) parent);

            for (ViewNode child : children) {
                child.inflate(context, view);
            }

            if (view != null) {
                invokeOnFinishInflate(view);
            }
            return view;
        }

        private void invokeOnFinishInflate(View view) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Method onFinishInflate = View.class.getDeclaredMethod("onFinishInflate");
            onFinishInflate.setAccessible(true);
            onFinishInflate.invoke(view);
        }

        @Override
        public String toString() {
            return "ViewNode{" +
                    "name='" + name + '\'' +
                    '}';
        }

        private View create(Context context, ViewGroup parent) throws Exception {
            if (name.equals("include")) {
                Attribute layoutAttribute = Attribute.find(attributes, ATTR_LAYOUT);
                String layoutName = layoutAttribute.qualifiedValue();
                return inflateView(context, layoutName, attributes, parent);
            } else if (name.equals("merge")) {
                return parent;
            } else if (name.equals("fragment")) {
                View fragment = constructFragment(context);
                addToParent(parent, fragment);
                return fragment;
            } else {
                applyFocusOverride(parent);
                View view = constructView(context);
                addToParent(parent, view);
                shadowOf(view).applyFocus();
                return view;
            }
        }

        private FrameLayout constructFragment(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            TestAttributeSet attributeSet = new TestAttributeSet(attributes, resourceExtractor, attrResourceLoader, View.class);
            if (strictI18n) {
                attributeSet.validateStrictI18n();
            }

            Class<? extends Fragment> clazz = loadFragmentClass(Attribute.find(attributes, "android:attr/name").value);
            Fragment fragment = ((Constructor<? extends Fragment>) clazz.getConstructor()).newInstance();
            if (!(context instanceof FragmentActivity)) {
                throw new RuntimeException("Cannot inflate a fragment unless the activity is a FragmentActivity");
            }

            FragmentActivity activity = (FragmentActivity) context;

            String tag = attributeSet.getAttributeValue("android", "tag");
            int id = attributeSet.getAttributeResourceValue("android", "id", 0);
            // TODO: this should probably be changed to call TestFragmentManager.addFragment so that the
            // inflated fragments don't get started twice (once in the commit, and once in ShadowFragmentActivity's
            // onStart()
            activity.getSupportFragmentManager().beginTransaction().add(id, fragment, tag).commit();

            View view = fragment.getView();

            FrameLayout container = new FrameLayout(context);
            container.setId(id);
            container.addView(view);
            return container;
        }

        private void addToParent(ViewGroup parent, View view) {
            if (parent != null && parent != view) {
                parent.addView(view);
            }
        }

        private View constructView(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Class<? extends View> clazz = pickViewClass();
            try {
                TestAttributeSet attributeSet = new TestAttributeSet(attributes, resourceExtractor, attrResourceLoader, clazz);
                if (strictI18n) {
                    attributeSet.validateStrictI18n();
                }
                return ((Constructor<? extends View>) clazz.getConstructor(Context.class, AttributeSet.class)).newInstance(context, attributeSet);
            } catch (NoSuchMethodException e) {
                try {
                    return ((Constructor<? extends View>) clazz.getConstructor(Context.class)).newInstance(context);
                } catch (NoSuchMethodException e1) {
                    return ((Constructor<? extends View>) clazz.getConstructor(Context.class, String.class)).newInstance(context, "");
                }
            }
        }

        private Class<? extends View> pickViewClass() {
            Class<? extends View> clazz = loadViewClass(name);
            if (clazz == null) {
                clazz = loadViewClass("android.view." + name);
            }
            if (clazz == null) {
                clazz = loadViewClass("android.widget." + name);
            }
            if (clazz == null) {
                clazz = loadViewClass("android.webkit." + name);
            }
            if (clazz == null) {
                clazz = loadViewClass("com.google.android.maps." + name);
            }

            if (clazz == null) {
                throw new RuntimeException("couldn't find view class " + name);
            }
            return clazz;
        }

        private Class loadClass(String className) {
            try {
                return getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private Class<? extends View> loadViewClass(String className) {
            // noinspection unchecked
            return (Class<? extends View>) loadClass(className);
        }

        private Class<? extends Fragment> loadFragmentClass(String className) {
            // noinspection unchecked
            return (Class<? extends Fragment>) loadClass(className);
        }

        public void applyFocusOverride(ViewParent parent) {
            if (requestFocusOverride) {
                View ancestor = (View) parent;
                while (ancestor.getParent() != null) {
                    ancestor = (View) ancestor.getParent();
                }
                ancestor.clearFocus();
            }
        }

        /**
         * Create a new ViewLoader with the given attributes merged in. If there's a layout attribute, it'll be excluded.
         */
        public ViewNode plusAttributes(List<Attribute> attributes) {
            if (attributes.size() == 0 || attributes.size() == 1 && attributes.get(0).fullyQualifiedName.equals(":attr/layout")) {
                return this; // don't make a new one if it'll be identical
            }

            List<Attribute> newAttrs = new ArrayList<Attribute>(this.attributes);
            for (Attribute attribute : attributes) {
                if (!attribute.fullyQualifiedName.equals(ATTR_LAYOUT)) {
                    Attribute.put(newAttrs, attribute);
                }
            }

            ViewNode viewNode = new ViewNode(name, newAttrs, xmlContext);
            viewNode.children = children;
            viewNode.requestFocusOverride = requestFocusOverride;
            return viewNode;
        }

        private void focusRequested(XmlContext xmlContext) {
            requestFocusOverride = true;
//            attributes.add(new Attribute("android:attr/focus", "true", xmlContext.packageName));
        }
    }
}
