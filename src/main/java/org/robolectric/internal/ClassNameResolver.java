package org.robolectric.internal;

public class ClassNameResolver<T> {
  private String packageName;
  private String className;

  public ClassNameResolver(String packageName, String className) {
    this.packageName = packageName;
    this.className = className;
  }

  public Class<? extends T> resolve() throws ClassNotFoundException {
    Class<? extends T> aClass;
    if (looksFullyQualified(className)) {
      aClass = safeClassForName(className);
    } else {
      if (className.startsWith(".")) {
        aClass = safeClassForName(packageName + className);
      } else {
        aClass = safeClassForName(packageName + "." + className);
      }
    }

    if (aClass == null) {
      throw new ClassNotFoundException("Could not find a class for package: "
          + packageName + " and class name: " + className);
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
