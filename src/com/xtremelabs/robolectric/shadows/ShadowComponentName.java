package com.xtremelabs.robolectric.shadows;

import android.content.ComponentName;
import android.content.Context;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

/**
 * Shadows the {@code android.content.ComponentName} class.
 * <p/>
 * Just keeps track of the package and class names, and then gives them back when you ask for them.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(ComponentName.class)
public class ShadowComponentName {
    private String pkg;
    private String cls;

    public void __constructor__(String pkg, String cls) {
        if (pkg == null) throw new NullPointerException("package name is null");
        if (cls == null) throw new NullPointerException("class name is null");
        this.pkg = pkg;
        this.cls = cls;
    }

    public void __constructor__(Context pkg, String cls) {
        if (cls == null) throw new NullPointerException("class name is null");
        this.pkg = pkg.getPackageName();
        this.cls = cls;
    }

    public void __constructor__(Context pkg, Class<?> cls) {
        this.pkg = pkg.getPackageName();
        this.cls = cls.getName();
    }

    @Implementation
    public String getPackageName() {
        return pkg;
    }

    @Implementation
    public String getClassName() {
        return cls;
    }

    @Override @Implementation
    public boolean equals(Object o) {
        if (o == null) return false;
        o = shadowOf_(o);
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        ShadowComponentName that = (ShadowComponentName) o;

        if (cls != null ? !cls.equals(that.cls) : that.cls != null) return false;
        if (pkg != null ? !pkg.equals(that.pkg) : that.pkg != null) return false;

        return true;
    }

    @Override @Implementation
    public int hashCode() {
        int result = pkg != null ? pkg.hashCode() : 0;
        result = 31 * result + (cls != null ? cls.hashCode() : 0);
        return result;
    }

    @Override @Implementation
    public String toString() {
        return "ComponentName{" +
                "pkg='" + pkg + '\'' +
                ", cls='" + cls + '\'' +
                '}';
    }
}
