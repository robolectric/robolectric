package org.robolectric.shadows;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.bytecode.RobolectricInternals;
import org.robolectric.util.Join;

import static android.content.Intent.FILL_IN_ACTION;
import static android.content.Intent.FILL_IN_CATEGORIES;
import static android.content.Intent.FILL_IN_COMPONENT;
import static android.content.Intent.FILL_IN_DATA;
import static android.content.Intent.FILL_IN_PACKAGE;
import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Intent.class)
public class ShadowIntent {
  @RealObject private Intent realIntent;

  private final Bundle extras = new Bundle();
  private String action;
  private ComponentName componentName;
  private String type;
  private Uri data;
  private int flags;
  private Class<?> intentClass;
  private String packageName;
  private final Set<String> categories = new HashSet<String>();

  public void __constructor__(String action, Uri uri, Context packageContext, Class cls) {
    componentName = new ComponentName(packageContext, cls);
    intentClass = cls;
    RobolectricInternals.getConstructor(Intent.class, realIntent, String.class, Uri.class, Context.class, Class.class).invoke(action, uri, packageContext, cls);
  }

  public void __constructor__(Context packageContext, Class cls) {
    componentName = new ComponentName(packageContext, cls);
    intentClass = cls;
    RobolectricInternals.getConstructor(Intent.class, realIntent, Context.class, Class.class).invoke(packageContext, cls);
  }

  public void __constructor__(String action, Uri uri) {
    this.action = action;
    data = uri;
    RobolectricInternals.getConstructor(Intent.class, realIntent, String.class, Uri.class).invoke(action, uri);
  }

  public void __constructor__(String action) {
    __constructor__(action, null);
    RobolectricInternals.getConstructor(Intent.class, realIntent, String.class).invoke(action);
  }

  public void __constructor__(Intent intent) {
    ShadowIntent other = shadowOf(intent);
    extras.putAll(other.extras);
    action = other.action;
    componentName = other.componentName;
    type = other.type;
    data = other.data;
    flags = other.flags;
    intentClass = other.intentClass;
    packageName = other.packageName;
    categories.addAll(other.categories);
    RobolectricInternals.getConstructor(Intent.class, realIntent, Intent.class).invoke(intent);
  }

  @Implementation
  public static Intent createChooser(Intent target, CharSequence title) {
    Intent intent = new Intent(Intent.ACTION_CHOOSER);
    intent.putExtra(Intent.EXTRA_INTENT, target);
    if (title != null) {
      intent.putExtra(Intent.EXTRA_TITLE, title);
    }
    return intent;
  }

  @Implementation
  public Intent setAction(String action) {
    this.action = action;
    return realIntent;
  }

  @Implementation
  public String getAction() {
    return action;
  }

  @Implementation
  public Intent setType(String type) {
    this.type = type;
    this.data = null;
    return realIntent;
  }

  @Implementation
  public Intent setDataAndType(Uri data, String type) {
    this.data = data;
    this.type = type;
    return realIntent;
  }

  @Implementation
  public String getType() {
    return type;
  }

  @Implementation
  public Intent addCategory(String category) {
    categories.add(category);
    return realIntent;
  }

  @Implementation
  public void removeCategory(String category) {
    categories.remove(category);
  }

  @Implementation
  public boolean hasCategory(String category) {
    return categories.contains(category);
  }

  @Implementation
  public Set<String> getCategories() {
    return categories;
  }

  @Implementation
  public Intent setPackage(String packageName) {
    this.packageName = packageName;
    return realIntent;
  }

  @Implementation
  public String getPackage() {
    return packageName;
  }

  @Implementation
  public Uri getData() {
    return data;
  }

  @Implementation
  public String getDataString() {
    if (data != null) {
      return data.toString();
    }
    return null;
  }

  @Implementation
  public Intent setClass(Context packageContext, Class<?> cls) {
    componentName = new ComponentName(packageContext, cls);
    this.intentClass = cls;
    return realIntent;
  }

  @Implementation
  public Intent setClassName(String packageName, String className) {
    componentName = new ComponentName(packageName, className);
    try {
      this.intentClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return realIntent;
  }

  @Implementation
  public Intent setClassName(Context packageContext, String className) {
    componentName = new ComponentName(packageContext.getPackageName(), className);
    return realIntent;
  }

  @Implementation
  public Intent setData(Uri data) {
    this.data = data;
    this.type = null;
    return realIntent;
  }

  @Implementation
  public int getFlags() {
    return flags;
  }

  @Implementation
  public Intent setFlags(int flags) {
    this.flags = flags;
    return realIntent;
  }

  @Implementation
  public Intent addFlags(int flags) {
    this.flags |= flags;
    return realIntent;
  }

  @Implementation
  public Intent putExtras(Bundle src) {
    extras.putAll(src);
    return realIntent;
  }

  @Implementation
  public Intent putExtras(Intent src) {
    ShadowIntent srcShadowIntent = shadowOf(src);
    extras.putAll(srcShadowIntent.extras);
    return realIntent;
  }

  @Implementation
  public Bundle getExtras() {
    return extras.isEmpty() ? null : new Bundle(extras);
  }

  @Implementation
  public Intent putExtra(String key, int value) {
    extras.putInt(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, double value) {
    extras.putDouble(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, float value) {
    extras.putFloat(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, long value) {
    extras.putLong(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, Serializable value) {
    extras.putSerializable(key, serializeCycle(value));
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, Parcelable value) {
    extras.putParcelable(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, Parcelable[] value) {
    extras.putParcelableArray(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, String value) {
    extras.putString(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, String[] value) {
    extras.putStringArray(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, Bundle value) {
    extras.putBundle(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, boolean value) {
    extras.putBoolean(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, int[] value) {
    extras.putIntArray(key, value);
    return realIntent;
  }

  @Implementation
  public Intent putExtra(String key, long[] value) {
    extras.putLongArray(key, value);
    return realIntent;
  }

  @Implementation
  public int[] getIntArrayExtra(String name) {
    return extras.getIntArray(name);
  }

  @Implementation
  public long[] getLongArrayExtra(String name) {
    return extras.getLongArray(name);
  }

  @Implementation
  public boolean getBooleanExtra(String name, boolean defaultValue) {
    return extras.getBoolean(name, defaultValue);
  }

  @Implementation
  public String[] getStringArrayExtra(String name) {
    return extras.getStringArray(name);
  }

  @Implementation
  public Intent putExtra(String key, CharSequence value) {
    extras.putCharSequence(key, value);
    return realIntent;
  }

  @Implementation
  public CharSequence getCharSequenceExtra(String name) {
    return extras.getCharSequence(name);
  }

  @Implementation
  public void putExtra(String key, byte[] value) {
    extras.putByteArray(key, value);
  }

  @Implementation
  public Intent putStringArrayListExtra(String key, ArrayList<String> value) {
    extras.putStringArrayList(key, value);
    return realIntent;
  }

  @Implementation
  public ArrayList<String> getStringArrayListExtra(String name) {
    return extras.getStringArrayList(name);
  }

  @Implementation
  public Intent putIntegerArrayListExtra(String key, ArrayList<Integer> value) {
    extras.putIntegerArrayList(key, value);
    return realIntent;
  }

  @Implementation
  public ArrayList<Integer> getIntegerArrayListExtra(String name) {
    return extras.getIntegerArrayList(name);
  }

  @Implementation
  public Intent putParcelableArrayListExtra(String key, ArrayList<Parcelable> value) {
    extras.putParcelableArrayList(key, value);
    return realIntent;
  }

  @Implementation
  public ArrayList<Parcelable> getParcelableArrayListExtra(String key) {
    return extras.getParcelableArrayList(key);
  }

  @Implementation
  public boolean hasExtra(String name) {
    return extras.containsKey(name);
  }

  @Implementation
  public String getStringExtra(String name) {
    return extras.getString(name);
  }

  @Implementation
  public Parcelable getParcelableExtra(String name) {
    return extras.getParcelable(name);
  }

  @Implementation
  public Parcelable[] getParcelableArrayExtra(String name) {
    return extras.getParcelableArray(name);
  }

  @Implementation
  public int getIntExtra(String name, int defaultValue) {
    return extras.getInt(name, defaultValue);
  }

  @Implementation
  public long getLongExtra(String name, long defaultValue) {
    return extras.getLong(name, defaultValue);
  }

  @Implementation
  public double getDoubleExtra(String name, double defaultValue) {
    return extras.getDouble(name, defaultValue);
  }

  @Implementation
  public Bundle getBundleExtra(String name) {
    return extras.getBundle(name);
  }

  @Implementation
  public float getFloatExtra(String name, float defaultValue) {
    return extras.getFloat(name, defaultValue);
  }

  @Implementation
  public byte[] getByteArrayExtra(String name) {
    return extras.getByteArray(name);
  }

  @Implementation
  public Serializable getSerializableExtra(String name) {
    return extras.getSerializable(name);
  }

  @Implementation
  public void removeExtra(String name) {
    extras.remove(name);
  }

  @Implementation
  public Intent setComponent(ComponentName componentName) {
    this.componentName = componentName;
    return realIntent;
  }

  @Implementation
  public ComponentName getComponent() {
    return componentName;
  }

  @Implementation
  public String toURI() {
    return data.toString();
  }

  @Implementation
  public int fillIn(Intent otherIntent, int flags) {
    int changes = 0;
    ShadowIntent other = shadowOf(otherIntent);

    if (other.action != null && (action == null || (flags & FILL_IN_ACTION) != 0)) {
      action = other.action;
      changes |= FILL_IN_ACTION;
    }
    if ((other.data != null || other.type != null)
        && ((data == null && type == null) || (flags & FILL_IN_DATA) != 0)) {
      data = other.data;
      type = other.type;
      changes |= FILL_IN_DATA;
    }
    if (!other.categories.isEmpty()
        && (categories.isEmpty() || (flags & FILL_IN_CATEGORIES) != 0)) {
      categories.addAll(other.categories);
      changes |= FILL_IN_CATEGORIES;
    }
    if (other.packageName != null
        && (packageName == null || (flags & FILL_IN_PACKAGE) != 0)) {
      packageName = other.packageName;
      changes |= FILL_IN_PACKAGE;
    }
    if (other.componentName != null && (flags & FILL_IN_COMPONENT) != 0) {
      componentName = other.componentName;
      changes |= FILL_IN_COMPONENT;
    }

    extras.putAll(other.extras);
    return changes;
  }

  @Implementation
  // cribbed from Android source
  public boolean filterEquals(Intent other) {
    if (other == null) {
      return false;
    }
    if (getAction() != other.getAction()) {
      if (getAction() != null) {
        if (!getAction().equals(other.getAction())) {
          return false;
        }
      } else {
        if (!other.getAction().equals(getAction())) {
          return false;
        }
      }
    }
    if (getData() != other.getData()) {
      if (getData() != null) {
        if (!getData().equals(other.getData())) {
          return false;
        }
      } else {
        if (!other.getData().equals(getData())) {
          return false;
        }
      }
    }
    if (getType() != other.getType()) {
      if (getType() != null) {
        if (!getType().equals(other.getType())) {
          return false;
        }
      } else {
        if (!other.getType().equals(getType())) {
          return false;
        }
      }
    }
    if (getPackage() != other.getPackage()) {
      if (getPackage() != null) {
        if (!getPackage().equals(other.getPackage())) {
          return false;
        }
      } else {
        if (!other.getPackage().equals(getPackage())) {
          return false;
        }
      }
    }
    if (getComponent() != other.getComponent()) {
      if (getComponent() != null) {
        if (!getComponent().equals(other.getComponent())) {
          return false;
        }
      } else {
        if (!other.getComponent().equals(getComponent())) {
          return false;
        }
      }
    }
    if (getCategories() != other.getCategories()) {
      if (getCategories() != null) {
        if (!getCategories().equals(other.getCategories())) {
          return false;
        }
      } else {
        if (!other.getCategories().equals(getCategories())) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Compares an {@code Intent} with a {@code ShadowIntent} (obtained via a call to
   * {@link Robolectric#shadowOf(android.content.Intent)})
   *
   * @param o a {@code ShadowIntent}
   * @return whether they are equivalent
   */
  @Deprecated
  public boolean realIntentEquals(ShadowIntent o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    if (action != null ? !action.equals(o.action) : o.action != null) return false;
    if (packageName != null ? !packageName.equals(o.packageName) : o.packageName != null)
        return false;
    if (componentName != null ? !componentName.equals(o.componentName) : o.componentName != null)
      return false;
    if (data != null ? !data.equals(o.data) : o.data != null) return false;
    if (extras != null ? !extras.equals(o.extras) : o.extras != null) return false;
    if (type != null ? !type.equals(o.type) : o.type != null) return false;
    if (categories != null ? !categories.equals(o.categories) : o.categories != null) return false;

    return true;
  }

  @Override
  @Implementation
  public int hashCode() {
    int result = extras != null ? extras.hashCode() : 0;
    result = 31 * result + (action != null ? action.hashCode() : 0);
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
    result = 31 * result + (data != null ? data.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (categories != null ? categories.hashCode() : 0);
    result = 31 * result + flags;
    return result;
  }

  @Override
  @Implementation
  public boolean equals(Object o) {
    if (!(o instanceof Intent)) return false;
    return realIntentEquals(shadowOf((Intent) o));
  }

  /**
   * Non-Android accessor that returns the {@code Class} object set by
   * {@link #setClass(android.content.Context, Class)}
   *
   * @return the {@code Class} object set by
   *         {@link #setClass(android.content.Context, Class)}
   */
  public Class<?> getIntentClass() {
    return intentClass;
  }

  @Override
  @Implementation
  public String toString() {
    return "Intent{" +
        Join.join(
            ", ",
            ifWeHave(componentName, "componentName"),
            ifWeHave(action, "action"),
            ifWeHave(extras, "extras"),
            ifWeHave(data, "data"),
            ifWeHave(type, "type")
        ) +
        '}';
  }

  private Serializable serializeCycle(Serializable serializable) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream output = new ObjectOutputStream(byteArrayOutputStream);
      output.writeObject(serializable);
      output.close();

      byte[] bytes = byteArrayOutputStream.toByteArray();
      ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes));
      return (Serializable) input.readObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private String ifWeHave(Object o, String name) {
    if (o == null) return null;
    if (o instanceof Map && ((Map) o).isEmpty()) return null;
    return name + "=" + o;
  }

  /**
   * @deprecated Use {@link ShadowIntent#setData(android.net.Uri).}
   */
  public void setURI(String uri) {
    this.data = Uri.parse(uri);
  }
}
