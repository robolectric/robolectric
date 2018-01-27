package org.robolectric.shadows;

public class ClassNameResolver<T> {

  public static <T> Class<T> resolve(String packageName, String className) throws ClassNotFoundException {
    Class<T> aClass;
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

  private static boolean looksFullyQualified(String className) {
    return className.contains(".") && !className.startsWith(".");
  }

  private static <T> Class<T> safeClassForName(String classNamePath) {
    try {
      return (Class<T>) Class.forName(classNamePath);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
