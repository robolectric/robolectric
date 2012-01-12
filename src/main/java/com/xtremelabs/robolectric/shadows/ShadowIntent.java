package com.xtremelabs.robolectric.shadows;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.util.Join;

import java.io.*;
import java.util.*;

import static android.content.Intent.*;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Intent.class)
public class ShadowIntent {
    @RealObject private Intent realIntent;

    private HashMap<String, Object> extras = new HashMap<String, Object>();
    private String action;
    private ComponentName componentName;
    private String type;
    private Uri data;
    private int flags;
    private Class<?> intentClass;
    private String packageName;
    private Set<String> categories = new HashSet<String>();
    private String uri;

    public void __constructor__(Context packageContext, Class cls) {
        componentName = new ComponentName(packageContext, cls);
        intentClass = cls;
    }

    public void __constructor__(String action, Uri uri) {
        this.action = action;
        data = uri;
    }

    public void __constructor__(String action) {
        __constructor__(action, null);
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
    public Intent setClass(Context packageContext, Class<?> cls) {
        this.intentClass = cls;
        return realIntent;
    }

    @Implementation
    public Intent setClassName(String packageName, String className) {
        componentName = new ComponentName(packageName, className);
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
        ShadowBundle srcShadowBundle = Robolectric.shadowOf_(src);
        extras = new HashMap<String, Object>(srcShadowBundle.map);
        return realIntent;
    }
    
    @Implementation
    public Intent putExtras(Intent src) {
        ShadowIntent srcShadowIntent = shadowOf(src);
        extras = new HashMap<String, Object>(srcShadowIntent.extras);
        return realIntent;
    }

    @Implementation
    public Bundle getExtras() {
        Bundle bundle = new Bundle();
        ((ShadowBundle) Robolectric.shadowOf_(bundle)).map.putAll(extras);
        return bundle;
    }
    
    @Implementation
    public Intent putExtra(String key, int value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, long value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, Serializable value) {
        extras.put(key, serializeCycle(value));
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, Parcelable value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, Parcelable[] value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, String value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, String[] value) {
        extras.put(key, value);
        return realIntent;
    }
    
    @Implementation
    public Intent putExtra(String key, boolean value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, int[] value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public int[] getIntArrayExtra(String name) {
        return (int[]) extras.get(name);
    }

    @Implementation
    public boolean getBooleanExtra(String name, boolean defaultValue) {
        return extras.containsKey(name) ? (Boolean) extras.get(name) : defaultValue;
    }

    @Implementation
    public String[] getStringArrayExtra(String name) {
        return (String[]) extras.get(name);
    }

    @Implementation
    public Intent putExtra(String key, CharSequence value) {
        extras.put(key, value);
        return realIntent;
    }
    
    @Implementation
    public CharSequence getCharSequenceExtra(String name) {
    	return (CharSequence) extras.get(name);
    }

    @Implementation
    public void putExtra(String key, byte[] value) {
        extras.put(key, value);
    }

    @Implementation
    public Intent putStringArrayListExtra(String key, ArrayList<String> value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public ArrayList<String> getStringArrayListExtra(String name) {
        return (ArrayList<String>) extras.get(name);
    }

    @Implementation
    public Intent putParcelableArrayListExtra(String key, ArrayList<Parcelable> value) {
    	extras.put(key, value );
    	return realIntent;
    }

    @Implementation
    public ArrayList<Parcelable> getParcelableArrayListExtra(String key) {
    	return (ArrayList<Parcelable>) extras.get(key);
    }
    
    @Implementation
    public boolean hasExtra(String name) {
	    return extras.containsKey(name);
	}

	@Implementation
    public String getStringExtra(String name) {
        return (String) extras.get(name);
    }

    @Implementation
    public Parcelable getParcelableExtra(String name) {
        return (Parcelable) extras.get(name);
    }

    @Implementation
    public Parcelable[] getParcelableArrayExtra(String name) {
        if (extras.get(name) instanceof Parcelable[]) {
            return (Parcelable[]) extras.get(name);
        }
        return null;
    }

    @Implementation
    public int getIntExtra(String name, int defaultValue) {
        Integer foundValue = (Integer) extras.get(name);
        return foundValue == null ? defaultValue : foundValue;
    }

    @Implementation
    public long getLongExtra(String name, long defaultValue) {
        Long foundValue = (Long) extras.get(name);
        return foundValue == null ? defaultValue : foundValue;
    }
    
    @Implementation
    public byte[] getByteArrayExtra(String name) {
        return (byte[]) extras.get(name);
    }

    @Implementation
    public Serializable getSerializableExtra(String name) {
        return (Serializable) extras.get(name);
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
        return uri;
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
        result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
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

    public void setURI(String uri) {
        this.uri = uri;
    }
}
