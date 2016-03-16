package org.robolectric.shadows.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;

import org.robolectric.fakes.RoboAttributeSet;
import org.robolectric.res.Attribute;
import org.robolectric.res.PreferenceNode;
import org.robolectric.res.ResName;
import org.robolectric.fakes.RoboAttributeSet;

import java.lang.reflect.InvocationTargetException;

import static org.robolectric.Shadows.shadowOf;

// TODO: Consider making these methods static
public class PreferenceBuilder {
  private Class<? extends Preference> loadClass(String className) {
    try {
      //noinspection unchecked
      return (Class<? extends Preference>) getClass().getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public Preference inflate(PreferenceNode preferenceNode, Activity activity, Preference parent) {
    if ("intent".equals(preferenceNode.getName())) {
      parent.setIntent(createIntent(preferenceNode));
      return null;
    }

    Preference preference = create(preferenceNode, activity, (PreferenceGroup) parent);
    shadowOf(preference).callOnAttachedToHierarchy(((PreferenceActivity)activity).getPreferenceManager());

    for (PreferenceNode child : preferenceNode.getChildren()) {
      inflate(child, activity, preference);
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
    AttributeSet attributeSet = RoboAttributeSet.create(context, preferenceNode.getAttributes());

    try {
      try {
        return (clazz.getConstructor(Context.class, AttributeSet.class)).newInstance(context, attributeSet);
      } catch (NoSuchMethodException e) {
        try {
          return (clazz.getConstructor(Context.class)).newInstance(context);
        } catch (NoSuchMethodException e1) {
          return (clazz.getConstructor(Context.class, String.class)).newInstance(context, "");
        }
      }
    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
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
