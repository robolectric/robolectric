package org.robolectric.res.builder;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ViewNode;
import org.robolectric.shadows.RoboAttributeSet;
import org.robolectric.util.I18nException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.res.ResourceLoader.ANDROID_NS;

public class LayoutBuilder {
  public static final ResName ATTR_LAYOUT = new ResName(":attr/layout");

  private final ResourceLoader resourceLoader;

  public LayoutBuilder(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  private View doInflate(Context context, ViewNode viewNode, ViewGroup parent, String qualifiers) {
    if (viewNode.isInclude()) {
      List<Attribute> viewNodeAttributes = viewNode.getAttributes();
      Attribute layoutAttribute = Attribute.find(viewNodeAttributes, ATTR_LAYOUT);
      return inflateView(context, layoutAttribute.getResourceReference(), viewNodeAttributes, parent, qualifiers);
    } else {
      View view = create(viewNode, context, parent);

      for (ViewNode child : viewNode.getChildren()) {
        doInflate(context, child, (ViewGroup) view, qualifiers);
      }

      if (view != null) {
        invokeOnFinishInflate(view);
      }
      return view;
    }
  }

  public View inflateView(Context context, int resourceId, ViewGroup parent, String qualifiers) {
    ViewNode viewNode = resourceLoader.getLayoutViewNode(resourceLoader.getResourceIndex().getResName(resourceId), qualifiers);
    if (viewNode == null) {
      String name = resourceLoader.getNameForId(resourceId);
      throw new RuntimeException("Could not find layout " + (name == null ? resourceId : name));
    }

    View view = doInflate(context, viewNode, parent, qualifiers);
    if (view != null) return view;

    throw new RuntimeException("Could not find layout " + resourceId);
  }

  public View inflateView(Context context, ResName resName, List<Attribute> attributes, ViewGroup parent, String qualifiers) {
    ViewNode viewNode = resourceLoader.getLayoutViewNode(resName, qualifiers);
    if (viewNode == null) {
      throw new RuntimeException("Could not find layout " + resName.name);
    }

    try {
      return doInflate(context, plusAttributes(viewNode, attributes), parent, qualifiers);
    } catch (I18nException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("error inflating " + resName.name, e);
    }
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

  View create(ViewNode viewNode, Context context, ViewGroup parent) {
    if (viewNode.isInclude()) {
      throw new IllegalStateException();
    } else if (viewNode.getName().equals("merge")) {
      return parent;
    } else if (viewNode.getName().equals("fragment")) {
      View fragment = constructFragment(viewNode, context);
      addToParent(parent, fragment);
      return fragment;
    } else {
      applyFocusOverride(viewNode, parent);
      View view = constructView(viewNode, context);
      addToParent(parent, view);
      shadowOf(view).applyFocus();
      return view;
    }
  }

  private FrameLayout constructFragment(ViewNode viewNode, Context context) {
    List<Attribute> attributes = viewNode.getAttributes();
    AttributeSet attributeSet = shadowOf(context).createAttributeSet(attributes, View.class);

    Class<? extends Fragment> clazz = loadFragmentClass(Attribute.find(attributes, "android:attr/name").value);
    Fragment fragment;
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

    String tag = attributeSet.getAttributeValue(ANDROID_NS, "tag");
    int id = attributeSet.getAttributeResourceValue(ANDROID_NS, "id", 0);
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

  private View constructView(ViewNode viewNode, Context context) {
    Class<? extends View> clazz = pickViewClass(viewNode);
    try {
      Constructor<? extends View> constructor;
      try {
        RoboAttributeSet attributeSet = shadowOf(context).createAttributeSet(viewNode.getAttributes(), View.class);
        constructor = clazz.getConstructor(Context.class, AttributeSet.class);
        return constructor.newInstance(context, attributeSet);
      } catch (NoSuchMethodException e) {
        try {
          constructor = clazz.getConstructor(Context.class);
          return constructor.newInstance(context);
        } catch (NoSuchMethodException e1) {
          constructor = clazz.getConstructor(Context.class, String.class);
          return constructor.newInstance(context, "");
        }
      }
    } catch (InstantiationException e) {
      throw new RuntimeException("Failed to create a " + clazz.getName(), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to create a " + clazz.getName(), e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Failed to create a " + clazz.getName(), e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Failed to create a " + clazz.getName(), e);
    }
  }

  private Class<? extends View> pickViewClass(ViewNode viewNode) {
    String name = viewNode.getName();

    if ("view".equals(name)) {
      Attribute attribute = Attribute.find(viewNode.getAttributes(), new ResName("", "attr", "class"));
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

  public void applyFocusOverride(ViewNode viewNode, ViewParent parent) {
    if (viewNode.shouldRequestFocusOverride()) {
      if (!(parent instanceof View)) return;

      View ancestor = (View) parent;
      while (ancestor.getParent() instanceof View) {
        ancestor = (View) ancestor.getParent();
      }
      ancestor.clearFocus();
    }
  }

  /**
   * Create a new ViewNode with the given attributes merged in. If there's a layout attribute, it'll be excluded.
   */
  public ViewNode plusAttributes(ViewNode viewNode, List<Attribute> attributes) {
    if (attributes.size() == 0 || attributes.size() == 1 && attributes.get(0).resName.equals(LayoutBuilder.ATTR_LAYOUT)) {
      return viewNode; // don't make a new one if it'll be identical
    }

    List<Attribute> newAttrs = new ArrayList<Attribute>(viewNode.getAttributes());
    for (Attribute attribute : attributes) {
      if (!attribute.resName.equals(LayoutBuilder.ATTR_LAYOUT)) {
        Attribute.put(newAttrs, attribute);
      }
    }

    return new ViewNode(viewNode.getName(), newAttrs, viewNode.getXmlContext(),
        viewNode.getChildren(), viewNode.shouldRequestFocusOverride());
  }
}
