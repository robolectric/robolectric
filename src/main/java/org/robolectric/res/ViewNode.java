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
import com.xtremelabs.robolectric.tester.android.util.ResName;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;

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
        return Collections.unmodifiableList(children);
    }

    public void addChild(ViewNode viewNode) {
        children.add(viewNode);
    }

    boolean isInclude() {
        return name.equals("include");
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    void invokeOnFinishInflate(View view) {
        try {
            Method onFinishInflate = View.class.getDeclaredMethod("onFinishInflate");
            onFinishInflate.setAccessible(true);
            onFinishInflate.invoke(view);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ViewNode{" +
                "name='" + name + '\'' +
                '}';
    }

    View create(Context context, ViewGroup parent) {
        if (isInclude()) {
            throw new IllegalStateException();
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

    private FrameLayout constructFragment(Context context) {
        TestAttributeSet attributeSet = shadowOf(context).createAttributeSet(attributes, View.class);

        Class<? extends Fragment> clazz = loadFragmentClass(Attribute.find(attributes, "android:attr/name").value);
        Fragment fragment = null;
        try {
            fragment = ((Constructor<? extends Fragment>) clazz.getConstructor()).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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

    private View constructView(Context context) {
        Class<? extends View> clazz = pickViewClass();
        try {
            try {
                TestAttributeSet attributeSet = shadowOf(context).createAttributeSet(attributes, View.class);
                return ((Constructor<? extends View>) clazz.getConstructor(Context.class, AttributeSet.class)).newInstance(context, attributeSet);
            } catch (NoSuchMethodException e) {
                try {
                    return ((Constructor<? extends View>) clazz.getConstructor(Context.class)).newInstance(context);
                } catch (NoSuchMethodException e1) {
                    return ((Constructor<? extends View>) clazz.getConstructor(Context.class, String.class)).newInstance(context, "");
                }
            }
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends View> pickViewClass() {
        String name = this.name;

        if ("view".equals(name)) {
            Attribute attribute = Attribute.find(attributes, new ResName("", "attr", "class"));
            if (attribute == null) throw new RuntimeException("no class attr for node " + this);
            name = attribute.value;
        }

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
        if (attributes.size() == 0 || attributes.size() == 1 && attributes.get(0).resName.equals(RoboLayoutInflater.ATTR_LAYOUT)) {
            return this; // don't make a new one if it'll be identical
        }

        List<Attribute> newAttrs = new ArrayList<Attribute>(this.attributes);
        for (Attribute attribute : attributes) {
            if (!attribute.resName.equals(RoboLayoutInflater.ATTR_LAYOUT)) {
                Attribute.put(newAttrs, attribute);
            }
        }

        ViewNode viewNode = new ViewNode(name, newAttrs, xmlContext);
        viewNode.children = children;
        viewNode.requestFocusOverride = requestFocusOverride;
        return viewNode;
    }

    void focusRequested() {
        requestFocusOverride = true;
    }
}
