package org.robolectric.annotation.processing.validator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.robolectric.annotation.Implementation;

import static org.robolectric.annotation.processing.validator.ImplementsValidator.CONSTRUCTOR_METHOD_NAME;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.STATIC_INITIALIZER_METHOD_NAME;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.getClassFQName;

class SdkStore {
  private final Map<Integer, Sdk> sdks = new TreeMap<>();
  private boolean loaded = false;

  List<Sdk> sdksMatching(Implementation implementation, int classMinSdk, int classMaxSdk) {
    checkLoaded();

    int minSdk = implementation == null ? -1 : implementation.minSdk();
    if (minSdk == -1) {
      minSdk = 0;
    }
    if (classMinSdk > minSdk) {
      minSdk = classMinSdk;
    }

    int maxSdk = implementation == null ? -1 : implementation.maxSdk();
    if (maxSdk == -1) {
      maxSdk = Integer.MAX_VALUE;
    }
    if (classMaxSdk != -1 && classMaxSdk < maxSdk) {
      maxSdk = classMaxSdk;
    }

    ArrayList<Sdk> matchingSdks = new ArrayList<>();
    for (Map.Entry<Integer, Sdk> entry : sdks.entrySet()) {
      Integer sdkInt = entry.getKey();
      if (sdkInt >= minSdk && sdkInt <= maxSdk) {
        matchingSdks.add(entry.getValue());
      }
    }
    return matchingSdks;
  }

  synchronized private void checkLoaded() {
    if (!loaded) {
      Properties properties = loadFromPropertiesFile("/sdks.properties");

      for (String key : properties.stringPropertyNames()) {
        int sdkInt = Integer.parseInt(key);
        String path = properties.getProperty(key);
        sdks.put(sdkInt, new Sdk(sdkInt, path));
      }

      loaded = true;
    }
  }

  private static Properties loadFromPropertiesFile(String resourceFileName) {
    InputStream in = SdkStore.class.getResourceAsStream(resourceFileName);
    if (in == null) {
      throw new RuntimeException("no such resource " + resourceFileName);
    }

    try {
      Properties properties = new Properties();
      properties.load(in);
      return properties;
    } catch (IOException e) {
      throw new RuntimeException("failed to open " + resourceFileName, e);
    }
  }

  static class Sdk {
    private static final ClassInfo NULL_CLASS_INFO = new ClassInfo();

    final int sdkInt;
    private final String path;
    private JarFile jarFile;
    private final Map<String, ClassInfo> classInfos = new HashMap<>();

    public Sdk(int sdkInt, String path) {
      this.sdkInt = sdkInt;
      this.path = path;
    }

    public String verifyMethod(TypeElement sdkClassElem, ExecutableElement methodElement) {
      String className = getClassFQName(sdkClassElem);
      ClassInfo classInfo = getClassInfo(className);

      if (classInfo == null) {
        return "No such class " + className;
      } else {
        MethodExtraInfo sdkMethod = classInfo.findMethod(methodElement);
        if (sdkMethod == null) {
          return "No such method in " + className;
        } else {
          MethodExtraInfo implMethod = new MethodExtraInfo(methodElement);
          if (sdkMethod.equals(implMethod)) {
            if (implMethod.isStatic != sdkMethod.isStatic) {
              return "@Implementation for " + methodElement.getSimpleName() +
                  " is " + (implMethod.isStatic ? "static" : "not static") +
                  " unlike the SDK method";
            }
            if (!implMethod.returnType.equals(sdkMethod.returnType)) {
              return "@Implementation for " + methodElement.getSimpleName() +
                  " has a return type of " + implMethod.returnType +
                  ", not " + sdkMethod.returnType + " as in the SDK method";
            }
          }
        }
      }
      return null;
    }

    synchronized private ClassInfo getClassInfo(String name) {
      if (jarFile == null) {
        try {
          jarFile = new JarFile(path);
        } catch (IOException e) {
          throw new RuntimeException("failed to open SDK " + sdkInt + " at " + path, e);
        }
      }

      ClassInfo classInfo = classInfos.get(name);
      if (classInfo == null) {
        ClassNode classNode = loadClassNode(name);

        if (classNode == null) {
          classInfos.put(name, NULL_CLASS_INFO);
        } else {
          classInfo = new ClassInfo(classNode);
          classInfos.put(name, classInfo);
        }
      }

      return classInfo == NULL_CLASS_INFO ? null : classInfo;
    }

    private ClassNode loadClassNode(String name) {
      String classFileName = name.replace('.', '/') + ".class";
      ZipEntry entry = jarFile.getEntry(classFileName);
      if (entry == null) {
        return null;
      }
      InputStream inputStream;
      try {
        inputStream = jarFile.getInputStream(entry);
      } catch (IOException e) {
        throw new RuntimeException("failed to file " + classFileName + " in " + path, e);
      }

      try {
        ClassReader classReader = new ClassReader(inputStream);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode,
            ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return classNode;
      } catch (IOException e) {
        throw new RuntimeException("failed to load " + classFileName + " in " + path, e);
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  static class ClassInfo {
    private final Map<MethodInfo, MethodExtraInfo> methods = new HashMap<>();

    private ClassInfo() {
    }

    public ClassInfo(ClassNode classNode) {
      for (Object method_ : classNode.methods) {
        MethodNode method = ((MethodNode) method_);
        MethodInfo methodInfo = new MethodInfo(method);
        MethodExtraInfo methodExtraInfo = new MethodExtraInfo(method);
        methods.put(methodInfo, methodExtraInfo);
        methods.put(methodInfo.erase(), methodExtraInfo);
      }
    }

    MethodExtraInfo findMethod(ExecutableElement methodElement) {
      MethodInfo methodInfo = new MethodInfo(methodElement);
      MethodExtraInfo methodExtraInfo = methods.get(methodInfo);
      if (methodExtraInfo == null) {
        methodExtraInfo = methods.get(methodInfo.erase());
      }
      return methodExtraInfo;
    }
  }

  static class MethodInfo {
    private final String name;
    private final List<String> paramTypes = new ArrayList<>();

    public MethodInfo(MethodNode method) {
      this.name = method.name;
      for (Type type : Type.getArgumentTypes(method.desc)) {
        paramTypes.add(type.getClassName());
      }
    }

    public MethodInfo(String name, int size) {
      this.name = name;
      for (int i = 0; i < size; i++) {
        paramTypes.add("java.lang.Object");
      }
    }

    public MethodInfo(ExecutableElement methodElement) {
      this.name = cleanMethodName(methodElement);

      for (VariableElement variableElement : methodElement.getParameters()) {
        paramTypes.add(variableElement.asType().toString());
      }
    }

    private String cleanMethodName(ExecutableElement methodElement) {
      String name = methodElement.getSimpleName().toString();
      if (CONSTRUCTOR_METHOD_NAME.equals(name)) {
        return "<init>";
      } else if (STATIC_INITIALIZER_METHOD_NAME.equals(name)) {
        return "<clinit>";
      } else {
        return name;
      }
    }

    public MethodInfo erase() {
      return new MethodInfo(name, paramTypes.size());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MethodInfo that = (MethodInfo) o;
      return Objects.equals(name, that.name) &&
          Objects.equals(paramTypes, that.paramTypes);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, paramTypes);
    }
  }

  static class MethodExtraInfo {
    private final boolean isStatic;
    private final String returnType;

    public MethodExtraInfo(MethodNode method) {
      this.isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
      this.returnType = Type.getReturnType(method.desc).getClassName();
    }

    public MethodExtraInfo(ExecutableElement methodElement) {
      this.isStatic = methodElement.getModifiers().contains(Modifier.STATIC);
      this.returnType = methodElement.getReturnType().toString();
    }
  }
}
