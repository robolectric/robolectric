package org.robolectric.res.builder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import org.robolectric.Robolectric;
import org.robolectric.res.Attribute;
import org.robolectric.res.PreferenceNode;
import org.robolectric.res.ResName;
import org.robolectric.shadows.RoboAttributeSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.robolectric.Robolectric.shadowOf;

public class PreferenceBuilder {
  private Class<? extends Preference> loadClass(String className) {
    try {
      //noinspection unchecked
      return (Class<? extends Preference>) getClass().getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }


  public Preference inflate(PreferenceNode preferenceNode, Context context, Preference parent) {
    if ("intent".equals(preferenceNode.getName())) {
      shadowOf(parent).setIntent(createIntent(preferenceNode));
      return null;
    }

    Preference preference = create(preferenceNode, context, (PreferenceGroup) parent);

    for (PreferenceNode child : preferenceNode.getChildren()) {
      inflate(child, context, preference);
    }

    return preference;
  }

  private Preference create(PreferenceNode preferenceNode, Context context, PreferenceGroup parent) {
    Preference preference = constructPreference(preferenceNode, context);
    if (parent != null && parent != preference) {
      parent.addPreference(preference);
    }
    return preference;
  }

  private Preference constructPreference(PreferenceNode preferenceNode, Context context) {
    Class<? extends Preference> clazz = pickViewClass(preferenceNode);

    List<Attribute> attributes = preferenceNode.getAttributes();
    RoboAttributeSet attributeSet = shadowOf(context).createAttributeSet(attributes, null);

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
      try {
        return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class, AttributeSet.class)).newInstance(context, attributeSet);
      } catch (NoSuchMethodException e) {
        try {
          return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class)).newInstance(context);
        } catch (NoSuchMethodException e1) {
          return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class, String.class)).newInstance(context, "");
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

  private Class<? extends Preference> pickViewClass(PreferenceNode preferenceNode) {
    String name = preferenceNode.getName();
    Class<? extends Preference> clazz = loadClass(name);
    if (clazz == null) {
      clazz = loadClass("android.preference." + name);
    }
    if (clazz == null) {
      throw new RuntimeException("couldn't find preference class " + name);
    }
    return clazz;
  }

  private Intent createIntent(PreferenceNode preferenceNode) {
    String targetPackage = getAttribute(preferenceNode, "targetPackage");
    String targetClass =  getAttribute(preferenceNode, "targetClass");
    String mimeType = getAttribute(preferenceNode, "mimeType");
    String data = getAttribute(preferenceNode, "data");
    String action = getAttribute(preferenceNode, "action");

    Intent intent = new Intent();
    if (targetClass != null && targetPackage != null) {
      intent.setComponent(new ComponentName(targetPackage, targetClass));
    }
    if (mimeType != null) {
      intent.setDataAndType(data != null ? Uri.parse(data) : null, mimeType);
    }
    intent.setAction(action);
    return intent;
  }

  private static String getAttribute(PreferenceNode node, String name) {
    Attribute attr = Attribute.find(node.getAttributes(), new ResName("android", "attr", name));
    return attr != null ? attr.value : null;
  }
}
