package com.xtremelabs.robolectric.res;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ViewLoader extends XmlLoader {
    /**
     * Map of "layout/foo" to the View nodes for that layout file
     */
    protected Map<String, ViewNode> viewNodesByLayoutName = new HashMap<String, ViewNode>();
    private AttrResourceLoader attrResourceLoader;

    public ViewLoader(ResourceExtractor resourceExtractor, AttrResourceLoader attrResourceLoader) {
        super(resourceExtractor);
        this.attrResourceLoader = attrResourceLoader;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document, boolean isSystem) throws Exception {
        ViewNode topLevelNode = new ViewNode("top-level", new HashMap<String, String>(), isSystem);
        processChildren(document.getChildNodes(), topLevelNode);
        String parentDir = xmlFile.getParentFile().getName();
        String layoutName = "layout/" + xmlFile.getName().replace(".xml", "");
        String specificLayoutName = parentDir + "/" + xmlFile.getName().replace(".xml", "");
        if (isSystem) {
            layoutName = "android:" + layoutName;
            specificLayoutName = "android:" + specificLayoutName;
        }
        // Check to see if the generic "layout/foo" is already in the map.  If not, add it.
        if (!viewNodesByLayoutName.containsKey(layoutName)) {
            viewNodesByLayoutName.put(layoutName, topLevelNode.getChildren().get(0));
        }
        // Add the specific "layout-land/foo" to the map.  If this happens to be "layout/foo", it's a no-op.
        viewNodesByLayoutName.put(specificLayoutName, topLevelNode.getChildren().get(0));
    }

    private void processChildren(NodeList childNodes, ViewNode parent) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent);
        }
    }

    private void processNode(Node node, ViewNode parent) {
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

        if (name.equals("requestFocus")) {
            parent.attributes.put("android:focus", "true");
            parent.requestFocusOverride = true;
        } else if (!name.startsWith("#")) {
            ViewNode viewNode = new ViewNode(name, attrMap, parent.isSystem);
            parent.addChild(viewNode);

            processChildren(node.getChildNodes(), viewNode);
        }
    }

    public View inflateView(Context context, String key) {
        return inflateView(context, key, null);
    }

    public View inflateView(Context context, String key, View parent) {
        return inflateView(context, key, null, parent);
    }

    public View inflateView(Context context, int resourceId, View parent) {
        return inflateView(context, resourceExtractor.getResourceName(resourceId), parent);
    }

    private View inflateView(Context context, String layoutName, Map<String, String> attributes, View parent) {
        ViewNode viewNode = findViewNode(context, layoutName);
        if (viewNode == null) {
            throw new RuntimeException("Could not find layout " + layoutName);
        }
        try {
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    if (!entry.getKey().equals("layout")) {
                        viewNode.attributes.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            return viewNode.inflate(context, parent);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + layoutName, e);
        }
    }

    /**
     * Load view nodes for the specified layout.  If in an Activity, we'll try to load "layout-land/foo" or
     * "layout-port/foo" first, and fall back to the generic "layout/foo" if not found.  This method returns null if we
     * couldn't find a layout with the specified name.
     */
    private ViewNode findViewNode(Context context, String layoutName) {
        ViewNode viewNode = null;
        if (context instanceof Activity) {
            final int orientation = ((Activity) context).getRequestedOrientation();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                viewNode = viewNodesByLayoutName.get(layoutName.replace("layout/", "layout-land/"));
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                viewNode = viewNodesByLayoutName.get(layoutName.replace("layout/", "layout-port/"));
            }
        }
        if (viewNode == null) {
            viewNode = viewNodesByLayoutName.get(layoutName);
        }
        return viewNode;
    }

    public class ViewNode {
        private String name;
        private final Map<String, String> attributes;

        private List<ViewNode> children = new ArrayList<ViewNode>();
        boolean requestFocusOverride = false;
        boolean isSystem = false;

        public ViewNode(String name, Map<String, String> attributes, boolean isSystem) {
            this.name = name;
            this.attributes = attributes;
            this.isSystem = isSystem;
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

        private View create(Context context, ViewGroup parent) throws Exception {
            if (name.equals("include")) {
                String layout = attributes.get("layout");
                return inflateView(context, layout.substring(1), attributes, parent);
            } else if (name.equals("merge")) {
                return parent;
            } else if (name.equals("fragment")) {
                final String className = attributes.get("android:name");
                // instantiate
                // onCreate
                // return onCreateView;
                // TODO instantiate the fragment?  keep track of it somewhere?
                return null;
            } else {
                applyFocusOverride(parent);
                View view = constructView(context);
                addToParent(parent, view);
                shadowOf(view).applyFocus();
                return view;
            }
        }

        private void addToParent(ViewGroup parent, View view) {
            if (parent != null && parent != view) {
                parent.addView(view);
            }
        }

        private View constructView(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Class<? extends View> clazz = pickViewClass();
            try {
                TestAttributeSet attributeSet = new TestAttributeSet(attributes, resourceExtractor, attrResourceLoader, clazz, isSystem);
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
            Class<? extends View> clazz = loadClass(name);
            if (clazz == null) {
                clazz = loadClass("android.view." + name);
            }
            if (clazz == null) {
                clazz = loadClass("android.widget." + name);
            }
            if (clazz == null) {
                clazz = loadClass("android.webkit." + name);
            }
            if (clazz == null) {
                clazz = loadClass("com.google.android.maps." + name);
            }

            if (clazz == null) {
                throw new RuntimeException("couldn't find view class " + name);
            }
            return clazz;
        }

        private Class<? extends View> loadClass(String className) {
            try {
                //noinspection unchecked
                return (Class<? extends View>) getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
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
    }
}
