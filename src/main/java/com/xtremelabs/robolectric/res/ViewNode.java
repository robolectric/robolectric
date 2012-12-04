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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ViewNode {
    private final String name;
    private final List<Attribute> attributes;
    private final XmlLoader.XmlContext xmlContext;

    private List<ViewNode> children = new ArrayList<ViewNode>();
    private boolean requestFocusOverride = false;

    public ViewNode(String name, List<Attribute> attributes, XmlLoader.XmlContext xmlContext) {
        this.name = name;
        this.attributes = Collections.unmodifiableList(attributes);
        this.xmlContext = xmlContext;
    }

    public XmlLoader.XmlContext getXmlContext() {
        return xmlContext;
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
            Attribute layoutAttribute = Attribute.find(attributes, ViewLoader.ATTR_LAYOUT);
            String layoutName = layoutAttribute.qualifiedValue();
            ResourceLoader resourceLoader = getResourceLoader(context);
            return resourceLoader.inflateView(context, layoutName, attributes, parent);
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

    private ResourceLoader getResourceLoader(Context context) {
        return shadowOf(context.getResources()).getResourceLoader();
    }

    private FrameLayout constructFragment(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ResourceLoader resourceLoader = getResourceLoader(context);
        TestAttributeSet attributeSet = resourceLoader.createAttributeSet(attributes, View.class);

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
            ResourceLoader resourceLoader = getResourceLoader(context);
            TestAttributeSet attributeSet = resourceLoader.createAttributeSet(attributes, View.class);
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
        if (attributes.size() == 0 || attributes.size() == 1 && attributes.get(0).resName.equals(ViewLoader.ATTR_LAYOUT)) {
            return this; // don't make a new one if it'll be identical
        }

        List<Attribute> newAttrs = new ArrayList<Attribute>(this.attributes);
        for (Attribute attribute : attributes) {
            if (!attribute.resName.equals(ViewLoader.ATTR_LAYOUT)) {
                Attribute.put(newAttrs, attribute);
            }
        }

        ViewNode viewNode = new ViewNode(name, newAttrs, xmlContext);
        viewNode.children = children;
        viewNode.requestFocusOverride = requestFocusOverride;
        return viewNode;
    }

    void focusRequested(XmlLoader.XmlContext xmlContext) {
        requestFocusOverride = true;
//            attributes.add(new Attribute("android:attr/focus", "true", xmlContext.packageName));
    }

    public View inflate(Context context, String layoutName, List<Attribute> attributes, View parent) {
        try {
            return plusAttributes(attributes).inflate(context, parent);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + layoutName, e);
        }
    }
}
