package org.robolectric.annotation.processing.validator;

import static org.robolectric.annotation.Implementation.DEFAULT_SDK;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.CONSTRUCTOR_METHOD_NAME;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.STATIC_INITIALIZER_METHOD_NAME;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.getClassFQName;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.robolectric.annotation.Implementation;

/** Encapsulates a collection of Android framework jars. */
public class SdkStore {

  private final Set<Sdk> sdks = new TreeSet<>();
  private boolean loaded = false;
  private final String sdksFile;

  public SdkStore(String sdksFile) {
    this.sdksFile = sdksFile;
  }

  List<Sdk> sdksMatching(Implementation implementation, int classMinSdk, int classMaxSdk) {
    loadSdksOnce();

    int minSdk = implementation == null ? DEFAULT_SDK : implementation.minSdk();
    if (minSdk == DEFAULT_SDK) {
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

  private synchronized void loadSdksOnce() {
    if (!loaded) {
      sdks.addAll(loadFromSdksFile(sdksFile));
      loaded = true;
    }
  }

  private static ImmutableList<Sdk> loadFromSdksFile(String fileName) {
    if (fileName == null || Files.notExists(Paths.get(fileName))) {
      return ImmutableList.of();
    }

    try (InputStream resIn = new FileInputStream(fileName)) {
      if (resIn == null) {
        throw new RuntimeException("no such file " + fileName);
      }

      BufferedReader in =
          new BufferedReader(new InputStreamReader(resIn, Charset.defaultCharset()));
      List<Sdk> sdks = new ArrayList<>();
      String line;
      while ((line = in.readLine()) != null) {
        if (!line.startsWith("#")) {
          sdks.add(new Sdk(line));
        }
      }
      return ImmutableList.copyOf(sdks);
    } catch (IOException e) {
      throw new RuntimeException("failed reading " + fileName, e);
    }
  }

  private static String canonicalize(TypeMirror typeMirror) {
    if (typeMirror instanceof TypeVariable) {
      return ((TypeVariable) typeMirror).getUpperBound().toString();
    } else if (typeMirror instanceof ArrayType) {
      return canonicalize(((ArrayType) typeMirror).getComponentType()) + "[]";
    } else {
      return typeMirror.toString();
    }
  }

  private static String typeWithoutGenerics(String paramType) {
    return paramType.replaceAll("<.*", "");
  }

  static class Sdk implements Comparable<Sdk> {
    private static final ClassInfo NULL_CLASS_INFO = new ClassInfo();

    private final String path;
    private final JarFile jarFile;
    final int sdkInt;
    private final Map<String, ClassInfo> classInfos = new HashMap<>();
    private static File tempDir;

    Sdk(String path) {
      this.path = path;
      this.jarFile = ensureJar();
      this.sdkInt = readSdkInt();
    }

    /**
     * Matches an {@code @Implementation} method against the framework method for this SDK.
     *
     * @param sdkClassElem the framework class being shadowed
     * @param methodElement the {@code @Implementation} method declaration to check
     * @param looseSignatures if true, also match any framework method with the same class, name,
     *     return type, and arity of parameters.
     * @return a string describing any problems with this method, or null if it checks out.
     */
    public String verifyMethod(
        TypeElement sdkClassElem, ExecutableElement methodElement, boolean looseSignatures) {
      String className = getClassFQName(sdkClassElem);
      ClassInfo classInfo = getClassInfo(className);

      if (classInfo == null) {
        return "No such class " + className;
      }

      MethodExtraInfo sdkMethod = classInfo.findMethod(methodElement, looseSignatures);
      if (sdkMethod == null) {
        return "No such method in " + className;
      }

      MethodExtraInfo implMethod = new MethodExtraInfo(methodElement);
      if (!sdkMethod.equals(implMethod)
          && !suppressWarnings(methodElement, "robolectric.ShadowReturnTypeMismatch")) {
        if (implMethod.isStatic != sdkMethod.isStatic) {
          return "@Implementation for " + methodElement.getSimpleName()
              + " is " + (implMethod.isStatic ? "static" : "not static")
              + " unlike the SDK method";
        }
        if (!implMethod.returnType.equals(sdkMethod.returnType)) {
          if (
              (looseSignatures && typeIsOkForLooseSignatures(implMethod, sdkMethod))
                  || (looseSignatures && implMethod.returnType.equals("java.lang.Object[]"))
                  // Number is allowed for int or long return types
                  || typeIsNumeric(sdkMethod, implMethod)) {
            return null;
          } else {
            return "@Implementation for " + methodElement.getSimpleName()
                + " has a return type of " + implMethod.returnType
                + ", not " + sdkMethod.returnType + " as in the SDK method";
          }
        }
      }

      return null;
    }

    private static boolean suppressWarnings(ExecutableElement methodElement, String warningName) {
      SuppressWarnings[] suppressWarnings =
          methodElement.getAnnotationsByType(SuppressWarnings.class);
      for (SuppressWarnings suppression : suppressWarnings) {
        for (String name : suppression.value()) {
          if (warningName.equals(name)) {
            return true;
          }
        }
      }
      return false;
    }

    private static boolean typeIsNumeric(MethodExtraInfo sdkMethod, MethodExtraInfo implMethod) {
      return implMethod.returnType.equals("java.lang.Number")
      && isNumericType(sdkMethod.returnType);
    }

    private static boolean typeIsOkForLooseSignatures(
        MethodExtraInfo implMethod, MethodExtraInfo sdkMethod) {
      return
          // loose signatures allow a return type of Object...
          implMethod.returnType.equals("java.lang.Object")
              // or Object[] for arrays...
              || (implMethod.returnType.equals("java.lang.Object[]")
                  && sdkMethod.returnType.endsWith("[]"));
    }

    private static boolean isNumericType(String type) {
      return type.equals("int") || type.equals("long");
    }

    /**
     * Load and analyze bytecode for the specified class, with caching.
     *
     * @param name the name of the class to analyze
     * @return information about the methods in the specified class
     */
    private synchronized ClassInfo getClassInfo(String name) {
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

    /**
     * Determine the API level for this SDK jar by inspecting its {@code build.prop} file.
     *
     * <p>If the {@code ro.build.version.codename} value isn't {@code REL}, this is an unreleased
     * SDK, which is represented as 10000 (see {@link
     * android.os.Build.VERSION_CODES#CUR_DEVELOPMENT}.
     *
     * @return the API level, or 10000
     */
    private int readSdkInt() {
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

    private JarFile ensureJar() {
      try {
        if (path.startsWith("classpath:")) {
          return new JarFile(copyResourceToFile(URI.create(path).getSchemeSpecificPart()));
        } else {
          return new JarFile(path);
        }

      } catch (IOException e) {
        throw new RuntimeException("failed to open SDK " + sdkInt + " at " + path, e);
      }
    }

    private static File copyResourceToFile(String resourcePath) throws IOException {
      if (tempDir == null){
        File tempFile = File.createTempFile("prefix", "suffix");
        tempFile.deleteOnExit();
        tempDir = tempFile.getParentFile();
      }
      InputStream jarIn = SdkStore.class.getClassLoader().getResourceAsStream(resourcePath);
      if (jarIn == null) {
        throw new RuntimeException("SDK " + resourcePath + " not found");
      }
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
      try (InputStream inputStream = jarFile.getInputStream(entry)) {
        ClassReader classReader = new ClassReader(inputStream);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode,
            ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return classNode;
      } catch (IOException e) {
        throw new RuntimeException("failed to analyze " + classFileName + " in " + path, e);
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
      for (Object aMethod : classNode.methods) {
        MethodNode method = ((MethodNode) aMethod);
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
        paramTypes.add(normalize(type));
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
        String paramTypeWithoutGenerics = typeWithoutGenerics(paramType);
        paramTypes.add(paramTypeWithoutGenerics);
      }
    }

    private static String cleanMethodName(ExecutableElement methodElement) {
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
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MethodInfo that = (MethodInfo) o;
      return Objects.equals(name, that.name)
          && Objects.equals(paramTypes, that.paramTypes);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, paramTypes);
    }
    @Override
    public String toString() {
      return "MethodInfo{"
          + "name='" + name + '\''
          + ", paramTypes=" + paramTypes
          + '}';
    }
  }

  private static String normalize(Type type) {
    return type.getClassName().replace('$', '.');
  }

  static class MethodExtraInfo {
    private final boolean isStatic;
    private final String returnType;

    public MethodExtraInfo(MethodNode method) {
      this.isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
      this.returnType = typeWithoutGenerics(normalize(Type.getReturnType(method.desc)));
    }

    public MethodExtraInfo(ExecutableElement methodElement) {
      this.isStatic = methodElement.getModifiers().contains(Modifier.STATIC);
      this.returnType = typeWithoutGenerics(canonicalize(methodElement.getReturnType()));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MethodExtraInfo that = (MethodExtraInfo) o;
      return isStatic == that.isStatic && Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(isStatic, returnType);
    }
  }
}
