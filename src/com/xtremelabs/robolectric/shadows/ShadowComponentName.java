package com.xtremelabs.robolectric.shadows;

import android.content.ComponentName;
import android.content.Context;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        ShadowComponentName that = (ShadowComponentName) Robolectric.shadowOf_(o);

        if (cls != null ? !cls.equals(that.cls) : that.cls != null) return false;
        if (pkg != null ? !pkg.equals(that.pkg) : that.pkg != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pkg != null ? pkg.hashCode() : 0;
        result = 31 * result + (cls != null ? cls.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "ComponentName{" +
                "pkg='" + pkg + '\'' +
                ", cls='" + cls + '\'' +
                '}';
    }
}
