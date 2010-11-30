package com.xtremelabs.robolectric.util;

public class ClassNameResolver<T> {
    private String packageName;
    private String className;

    public ClassNameResolver(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }

    public Class<? extends T> resolve() {
        if (looksFullyQualified(className)) {
            return safeClassForName(className);
        }

        Class<? extends T> aClass = safeClassForName(packageName + "." + className);
        if (aClass == null) {
            aClass = safeClassForName(packageName + className);
        }
        return aClass;
    }

    private boolean looksFullyQualified(String className) {
        return className.contains(".") && !className.startsWith(".");
    }

    private Class<? extends T> safeClassForName(String classNamePath) {
        try {
            return (Class<? extends T>) Class.forName(classNamePath);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
