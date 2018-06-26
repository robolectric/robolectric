package org.robolectric.annotation.processing.validator;

import static org.robolectric.annotation.processing.validator.ImplementsValidator.CONSTRUCTOR_METHOD_NAME;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.STATIC_INITIALIZER_METHOD_NAME;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.getClassFQName;

import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.TypeVar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.robolectric.annotation.Implementation;

class SdkStore {

  private final Set<Sdk> sdks = new TreeSet<>();
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

    List<Sdk> matchingSdks = new ArrayList<>();
    for (Sdk sdk : sdks) {
      Integer sdkInt = sdk.sdkInt;
      if (sdkInt >= minSdk && sdkInt <= maxSdk) {
        matchingSdks.add(sdk);
      }
    }
    return matchingSdks;
  }

  synchronized private void checkLoaded() {
    if (!loaded) {
      List<String> sdkList = loadFromSdksFile("/sdks.txt");

      for (String sdkPath : sdkList) {
        sdks.add(new Sdk(sdkPath));
      }

      loaded = true;
    }
  }

  private static List<String> loadFromSdksFile(String resourceFileName) {
    InputStream resIn = SdkStore.class.getResourceAsStream(resourceFileName);
    if (resIn == null) {
      throw new RuntimeException("no such resource " + resourceFileName);
    }

    BufferedReader in = new BufferedReader(new InputStreamReader(resIn, Charset.defaultCharset()));
    ArrayList<String> sdks = new ArrayList<>();
    String line;
    try {
      while ((line = in.readLine()) != null) {
        if (!line.startsWith("#")) {
          sdks.add(line);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("failed reading " + resourceFileName, e);
    }
    return sdks;
  }

  static class Sdk implements Comparable<Sdk> {
    private static final ClassInfo NULL_CLASS_INFO = new ClassInfo();

    private final String path;
    private JarFile jarFile;
    final int sdkInt;
    private final Map<String, ClassInfo> classInfos = new HashMap<>();
    private static File tempDir;

    Sdk(String path) {
      this.path = path;

      this.sdkInt = readSdkInt();
    }

    public String verifyMethod(TypeElement sdkClassElem, ExecutableElement methodElement,
        boolean looseSignatures) {
      String className = getClassFQName(sdkClassElem);
      ClassInfo classInfo = getClassInfo(className);

      if (classInfo == null) {
        return "No such class " + className;
      } else {
        MethodExtraInfo sdkMethod = classInfo.findMethod(methodElement, looseSignatures);

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
      ensureJar();

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

    private int readSdkInt() {
      ensureJar();

      Properties properties = new Properties();
      try (InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry("build.prop"))) {
        properties.load(inputStream);
      } catch (IOException e) {
        throw new RuntimeException("failed to read build.prop from " + path);
      }
      int sdkInt = Integer.parseInt(properties.getProperty("ro.build.version.sdk"));
      String codename = properties.getProperty("ro.build.version.codename");
      if (!"REL".equals(codename)) {
        sdkInt = 10000;
      }

      return sdkInt;
    }

    synchronized private void ensureJar() {
      if (jarFile == null) {
        try {
          URI uri = URI.create(path);
          if ("classpath".equals(uri.getScheme())) {
            jarFile = new JarFile(copyResourceToFile(uri.getSchemeSpecificPart()));
          } else {
            jarFile = new JarFile(path);
          }

        } catch (IOException e) {
          throw new RuntimeException("failed to open SDK " + sdkInt + " at " + path, e);
        }
      }
    }

    private File copyResourceToFile(String resourcePath) throws IOException {
      if (tempDir == null){
        File tempFile = File.createTempFile("prefix", "suffix");
        tempFile.deleteOnExit();
        tempDir = tempFile.getParentFile();
      }
      InputStream jarIn = SdkStore.class.getClassLoader().getResourceAsStream(resourcePath);
      File outFile = new File(tempDir, new File(resourcePath).getName());
      outFile.deleteOnExit();
      try (FileOutputStream jarOut = new FileOutputStream(outFile)) {
        byte[] buffer = new byte[4096];
        int len;
        while ((len = jarIn.read(buffer)) != -1) {
          jarOut.write(buffer, 0, len);
        }
      }

      return outFile;
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

    @Override
    public int compareTo(Sdk sdk) {
      return sdk.sdkInt - sdkInt;
    }
  }

  static class ClassInfo {
    private final Map<MethodInfo, MethodExtraInfo> methods = new HashMap<>();
    private final Map<MethodInfo, MethodExtraInfo> erasedParamTypesMethods = new HashMap<>();

    private ClassInfo() {
    }

    public ClassInfo(ClassNode classNode) {
      for (Object method_ : classNode.methods) {
        MethodNode method = ((MethodNode) method_);
        MethodInfo methodInfo = new MethodInfo(method);
        MethodExtraInfo methodExtraInfo = new MethodExtraInfo(method);
        methods.put(methodInfo, methodExtraInfo);
        erasedParamTypesMethods.put(methodInfo.erase(), methodExtraInfo);
      }
    }

    MethodExtraInfo findMethod(ExecutableElement methodElement, boolean looseSignatures) {
      MethodInfo methodInfo = new MethodInfo(methodElement);

      MethodExtraInfo methodExtraInfo = methods.get(methodInfo);
      if (looseSignatures && methodExtraInfo == null) {
        methodExtraInfo = erasedParamTypesMethods.get(methodInfo.erase());
      }
      return methodExtraInfo;
    }
  }

  static class MethodInfo {
    private final String name;
    private final List<String> paramTypes = new ArrayList<>();

    /** Create a MethodInfo from ASM in-memory representation (an Android framework method). */
    public MethodInfo(MethodNode method) {
      this.name = method.name;
      for (Type type : Type.getArgumentTypes(method.desc)) {
        paramTypes.add(type.getClassName().replace('$', '.'));
      }
    }

    /** Create a MethodInfo with all Object params (for looseSignatures=true). */
    public MethodInfo(String name, int size) {
      this.name = name;
      for (int i = 0; i < size; i++) {
        paramTypes.add("java.lang.Object");
      }
    }

    /** Create a MethodInfo from AST (an @Implementation method in a shadow class). */
    public MethodInfo(ExecutableElement methodElement) {
      this.name = cleanMethodName(methodElement);

      for (VariableElement variableElement : methodElement.getParameters()) {
        TypeMirror varTypeMirror = variableElement.asType();
        String paramType = canonicalize(varTypeMirror);
        String paramTypeWithoutGenerics = paramType.replaceAll("<.*", "");
        paramTypes.add(paramTypeWithoutGenerics);
      }
    }

    private String canonicalize(TypeMirror typeMirror) {
      if (typeMirror instanceof TypeVar) {
        return ((TypeVar) typeMirror).getUpperBound().toString();
      } else if (typeMirror instanceof ArrayType) {
        return canonicalize(((ArrayType) typeMirror).elemtype) + "[]";
      } else {
        return typeMirror.toString();
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

    @Override
    public String toString() {
      return "MethodInfo{" +
          "name='" + name + '\'' +
          ", paramTypes=" + paramTypes +
          '}';
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
