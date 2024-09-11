package org.robolectric.annotation.processing.validator;

import static org.robolectric.annotation.Implementation.DEFAULT_SDK;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.CONSTRUCTOR_METHOD_NAME;
import static org.robolectric.annotation.processing.validator.ImplementsValidator.STATIC_INITIALIZER_METHOD_NAME;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceSignatureVisitor;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.InDevelopment;
import org.robolectric.versioning.AndroidVersionInitTools;
import org.robolectric.versioning.AndroidVersions;

/** Encapsulates a collection of Android framework jars. */
public class SdkStore {

  private final Set<Sdk> sdks = new TreeSet<>();
  private boolean loaded = false;

  /** Should only ever be needed for android platform development */
  private final boolean loadFromClasspath;

  private final String overrideSdkLocation;
  private final int overrideSdkInt;
  private final String sdksFile;

  /** */
  public SdkStore(
      String sdksFile, boolean loadFromClasspath, String overrideSdkLocation, int overrideSdkInt) {
    this.sdksFile = sdksFile;
    this.loadFromClasspath = loadFromClasspath;
    this.overrideSdkLocation = overrideSdkLocation;
    this.overrideSdkInt = overrideSdkInt;
  }

  /**
   * Used to look up matching sdks for a declared shadow class. Needed to then find the class from
   * the underlying sdks for comparison in the ImplementsValidator.
   */
  List<Sdk> sdksMatching(int classMinSdk, int classMaxSdk) {
    loadSdksOnce();
    List<Sdk> matchingSdks = new ArrayList<>();
    for (Sdk sdk : sdks) {
      int sdkInt = sdk.sdkRelease.getSdkInt();
      if (sdkInt >= classMinSdk && (sdkInt <= classMaxSdk || classMaxSdk == -1)) {
        matchingSdks.add(sdk);
      }
    }
    return matchingSdks;
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
      int sdkInt = sdk.sdkRelease.getSdkInt();
      if (sdkInt >= minSdk && sdkInt <= maxSdk) {
        matchingSdks.add(sdk);
      }
    }
    return matchingSdks;
  }

  private synchronized void loadSdksOnce() {
    if (!loaded) {
      sdks.addAll(
          loadFromSources(loadFromClasspath, sdksFile, overrideSdkLocation, overrideSdkInt));
      loaded = true;
    }
  }

  /**
   * @return a list of sdk_int's to jar locations as a string, one tuple per line.
   */
  @Override
  @SuppressWarnings("JdkCollectors")
  public String toString() {
    loadSdksOnce();
    StringBuilder builder = new StringBuilder();
    builder.append("SdkStore [");
    for (Sdk sdk : sdks.stream().sorted().collect(Collectors.toList())) {
      builder.append("    " + sdk.sdkRelease.getSdkInt() + " : " + sdk.path + "\n");
    }
    builder.append("]");
    return builder.toString();
  }

  /**
   * Scans the jvm properties for the command that executed it, in this command will be the
   * classpath. <br>
   * <br>
   * Scans all jars on the classpath for the first one with a /build.prop on resource. This is
   * assumed to be the sdk that the processor is running with.
   *
   * @return the detected sdk location.
   */
  private static String compilationSdkTarget() {
    String cmd = System.getProperty("sun.java.command");
    Pattern pattern = Pattern.compile("((-cp)|(-classpath))\\s(?<cp>[a-zA-Z-_0-9\\-\\:\\/\\.]*)");
    Matcher matcher = pattern.matcher(cmd);
    if (matcher.find()) {
      String classpathString = matcher.group("cp");
      List<String> cp = Arrays.asList(classpathString.split(":"));
      for (String fileStr : cp) {
        try (JarFile jarFile = new JarFile(fileStr)) {
          ZipEntry entry = jarFile.getEntry("build.prop");
          if (entry != null) {
            return fileStr;
          }
        } catch (IOException ioe) {
          System.out.println("Error detecting compilation SDK: " + ioe.getMessage());
          ioe.printStackTrace();
        }
      }
    }
    return null;
  }

  /**
   * Returns a list of sdks to process, either the compilation's classpaths sdk in a list of size
   * one, or the list of sdks in a sdkFile. This should not be needed unless building in the android
   * codebase. Otherwise, should prefer using the sdks.txt and the released jars.
   *
   * @param localSdk validate sdk found in compile time classpath, takes precedence over sdkFile
   * @param sdkFileName the sdkFile name, may be null, or empty
   * @param overrideSdkLocation if provided overrides the default lookup of the localSdk, iff
   *     localSdk is on.
   * @return a list of sdks to check with annotation processing validators.
   */
  private static ImmutableList<Sdk> loadFromSources(
      boolean localSdk, String sdkFileName, String overrideSdkLocation, int overrideSdkInt) {
    if (localSdk) {
      Sdk sdk = null;
      if (overrideSdkLocation != null) {
        sdk = new Sdk(overrideSdkLocation, overrideSdkInt);
        return sdk == null ? ImmutableList.of() : ImmutableList.of(sdk);
      } else {
        String target = compilationSdkTarget();
        if (target != null) {
          sdk = new Sdk(target);
          // We don't want to test released versions in Android source tree.
          return sdk == null || sdk.sdkRelease.isReleased()
              ? ImmutableList.of()
              : ImmutableList.of(sdk);
        }
      }
    }
    if (sdkFileName == null || Files.notExists(Paths.get(sdkFileName))) {
      return ImmutableList.of();
    }
    try (InputStream resIn = new FileInputStream(sdkFileName)) {
      if (resIn == null) {
        throw new RuntimeException("no such file " + sdkFileName);
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
      throw new RuntimeException("failed reading " + sdkFileName, e);
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
    final AndroidVersions.AndroidRelease sdkRelease;
    final int sdkInt;
    private final Map<String, ClassInfo> classInfos = new HashMap<>();
    private static File tempDir;

    Sdk(String path) {
      this(path, null);
    }

    Sdk(String path, Integer sdkInt) {
      this.path = path;
      if (path.startsWith("classpath:") || path.endsWith(".jar")) {
        this.jarFile = ensureJar();
      } else {
        this.jarFile = null;
      }
      if (sdkInt == null) {
        this.sdkRelease = readSdkVersion();
        this.sdkInt = sdkRelease.getSdkInt();
      } else {
        this.sdkRelease = AndroidVersions.getReleaseForSdkInt(sdkInt);
        this.sdkInt = sdkRelease.getSdkInt();
      }
    }

    /**
     * Matches an {@code @Implementation} method against the framework method for this SDK.
     *
     * @param sdkClassName the framework class being shadowed
     * @param methodElement the {@code @Implementation} method declaration to check
     * @param looseSignatures if true, also match any framework method with the same class, name,
     *     return type, and arity of parameters.
     * @return a string describing any problems with this method, or null if it checks out.
     */
    public String verifyMethod(
        String sdkClassName,
        ExecutableElement methodElement,
        boolean looseSignatures,
        boolean allowInDev) {
      ClassInfo classInfo = getClassInfo(sdkClassName);

      // Probably should not be reachable
      if (classInfo == null
          && !suppressWarnings(methodElement.getEnclosingElement(), null, allowInDev)) {
        return null;
      }

      MethodExtraInfo sdkMethod = classInfo.findMethod(methodElement, looseSignatures);
      if (sdkMethod == null && !suppressWarnings(methodElement, null, allowInDev)) {
        return "No method " + methodElement + " in " + sdkClassName;
      }
      if (sdkMethod != null) {
        MethodExtraInfo implMethod = new MethodExtraInfo(methodElement);
        if (!sdkMethod.equals(implMethod)
            && !suppressWarnings(
                methodElement, "robolectric.ShadowReturnTypeMismatch", allowInDev)) {
          if (implMethod.isStatic != sdkMethod.isStatic) {
            return "@Implementation for "
                + methodElement.getSimpleName()
                + " is "
                + (implMethod.isStatic ? "static" : "not static")
                + " unlike the SDK method";
          }
          if (!implMethod.returnType.equals(sdkMethod.returnType)) {
            if ((looseSignatures && typeIsOkForLooseSignatures(implMethod, sdkMethod))
                || (looseSignatures && implMethod.returnType.equals("java.lang.Object[]"))) {
              return null;
            } else {
              return "@Implementation for "
                  + methodElement.getSimpleName()
                  + " has a return type of "
                  + implMethod.returnType
                  + ", not "
                  + sdkMethod.returnType
                  + " as in the SDK method";
            }
          }
        }
      }

      return null;
    }

    /**
     * Warnings (or potentially Errors, depending on processing flags) can be suppressed in one of
     * two ways, either with @SuppressWarnings("robolectric.<warningName>"), or with
     * the @InDevelopment annotation, if and only the target Sdk is in development.
     *
     * @param annotatedElement element to inspect for annotations
     * @param warningName the name of the warning, if null, @InDevelopment will still be honored.
     * @return true if the warning should be suppressed, else false
     */
    boolean suppressWarnings(Element annotatedElement, String warningName, boolean allowInDev) {
      SuppressWarnings[] suppressWarnings =
          annotatedElement.getAnnotationsByType(SuppressWarnings.class);
      for (SuppressWarnings suppression : suppressWarnings) {
        for (String name : suppression.value()) {
          if (warningName != null && warningName.equals(name)) {
            return true;
          }
        }
      }
      InDevelopment[] inDev = annotatedElement.getAnnotationsByType(InDevelopment.class);
      // Marked in development, sdk is not released, or is the last release (which may still be
      // marked unreleased in g/main aosp/main.
      if (allowInDev
          && inDev.length > 0
          && (!sdkRelease.isReleased()
              || sdkRelease
                  == AndroidVersions.getReleases().stream()
                      .max(AndroidVersions.AndroidRelease::compareTo)
                      .get())) {
        return true;
      }
      return false;
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

    /**
     * Load and analyze bytecode for the specified class, with caching.
     *
     * @param name the name of the class to analyze
     * @return information about the methods in the specified class
     */
    synchronized ClassInfo getClassInfo(String name) {
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
     * @return the API level
     */
    private AndroidVersions.AndroidRelease readSdkVersion() {
      try {
        return AndroidVersionInitTools.computeReleaseVersion(jarFile);
      } catch (IOException e) {
        throw new RuntimeException("failed to read build.prop from " + path);
      }
    }

    private JarFile ensureJar() {
      try {
        if (path.startsWith("classpath:")) {
          return new JarFile(copyResourceToFile(URI.create(path).getSchemeSpecificPart()));
        } else {
          return new JarFile(path);
        }

      } catch (IOException e) {
        throw new RuntimeException(
            "failed to open SDK " + sdkRelease.getSdkInt() + " at " + path, e);
      }
    }

    private static File copyResourceToFile(String resourcePath) throws IOException {
      if (tempDir == null) {
        File tempFile = File.createTempFile("prefix", "suffix");
        tempFile.deleteOnExit();
        tempDir = tempFile.getParentFile();
      }
      try (InputStream jarIn = SdkStore.class.getClassLoader().getResourceAsStream(resourcePath)) {
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
    }

    private ClassNode loadClassNode(String name) {
      String classFileName = name.replace('.', '/') + ".class";
      Supplier<InputStream> inputStreamSupplier = null;

      if (jarFile != null) {
        // working with a jar file.
        ZipEntry entry = jarFile.getEntry(classFileName);
        if (entry == null) {
          return null;
        }
        inputStreamSupplier =
            () -> {
              try {
                return jarFile.getInputStream(entry);
              } catch (IOException ioe) {
                throw new RuntimeException("could not read zip entry", ioe);
              }
            };
      } else {
        // working with an exploded path location.
        Path working = Path.of(path, classFileName);
        File classFile = working.toFile();
        if (classFile.isFile()) {
          inputStreamSupplier =
              () -> {
                try {
                  return new FileInputStream(classFile);
                } catch (IOException ioe) {
                  throw new RuntimeException("could not read file in path " + working, ioe);
                }
              };
        }
      }
      if (inputStreamSupplier == null) {
        return null;
      }
      try (InputStream inputStream = inputStreamSupplier.get()) {
        ClassReader classReader = new ClassReader(inputStream);
        ClassNode classNode = new ClassNode();
        classReader.accept(
            classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return classNode;
      } catch (IOException e) {
        throw new RuntimeException("failed to analyze " + classFileName + " in " + path, e);
      }
    }

    @Override
    public int compareTo(Sdk sdk) {
      return sdk.sdkRelease.getSdkInt() - sdkRelease.getSdkInt();
    }
  }

  static class ClassInfo {
    private final Map<MethodInfo, MethodExtraInfo> methods = new HashMap<>();
    private final Map<MethodInfo, MethodExtraInfo> erasedParamTypesMethods = new HashMap<>();
    private final String signature;

    private ClassInfo() {
      signature = "";
    }

    public ClassInfo(ClassNode classNode) {
      if (classNode.signature != null) {
        TraceSignatureVisitor signatureVisitor = new TraceSignatureVisitor(0);
        new SignatureReader(classNode.signature).accept(signatureVisitor);
        signature = stripExtends(signatureVisitor.getDeclaration());
      } else {
        signature = "";
      }
      for (Object aMethod : classNode.methods) {
        MethodNode method = ((MethodNode) aMethod);
        MethodInfo methodInfo = new MethodInfo(method);
        MethodExtraInfo methodExtraInfo = new MethodExtraInfo(method);
        methods.put(methodInfo, methodExtraInfo);
        erasedParamTypesMethods.put(methodInfo.erase(), methodExtraInfo);
      }
    }

    /**
     * In order to compare typeMirror derived strings of Type parameters, ie `{@code Clazz<X extends
     * Y>}` from a class definition, with a asm bytecode read string of the same, any extends info
     * is not supplied by type parameters, but is by asm class readers `{@code Clazz<X extends Y>
     * extends Clazz1}`.
     *
     * <p>This method can strip any extra information `{@code extends Clazz1}`, from a Generics type
     * parameter string provided by asm byte code readers.
     */
    private static String stripExtends(String asmTypeSuffix) {
      int count = 0;
      for (int loc = 0; loc < asmTypeSuffix.length(); loc++) {
        char c = asmTypeSuffix.charAt(loc);
        if (c == '<') {
          count += 1;
        } else if (c == '>') {
          count -= 1;
        }
        if (count == 0) {
          return asmTypeSuffix.substring(0, loc + 1).trim();
        }
      }
      return "";
    }

    MethodExtraInfo findMethod(ExecutableElement methodElement, boolean looseSignatures) {
      MethodInfo methodInfo = new MethodInfo(methodElement);

      MethodExtraInfo methodExtraInfo = methods.get(methodInfo);
      if (looseSignatures && methodExtraInfo == null) {
        methodExtraInfo = erasedParamTypesMethods.get(methodInfo);
      }
      return methodExtraInfo;
    }

    String getSignature() {
      return signature;
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

        // If parameter is annotated with @ClassName, then use the indicated type instead.
        List<? extends AnnotationMirror> annotationMirrors = variableElement.getAnnotationMirrors();
        for (AnnotationMirror am : annotationMirrors) {
          if (am.getAnnotationType().toString().equals(ClassName.class.getName())) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> annotationEntries =
                am.getElementValues();
            Set<? extends ExecutableElement> keys = annotationEntries.keySet();
            for (ExecutableElement key : keys) {
              if ("value()".equals(key.toString())) {
                AnnotationValue annotationValue = annotationEntries.get(key);
                paramType = annotationValue.getValue().toString().replace('$', '.');
                break;
              }
            }
            break;
          }
        }

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
        Implementation implementation = methodElement.getAnnotation(Implementation.class);
        String methodName = implementation == null ? "" : implementation.methodName();
        methodName = methodName == null ? "" : methodName.trim();
        if (methodName.isEmpty()) {
          return name;
        } else {
          return methodName;
        }
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
      if (!(o instanceof MethodInfo)) {
        return false;
      }
      MethodInfo that = (MethodInfo) o;
      return Objects.equals(name, that.name) && Objects.equals(paramTypes, that.paramTypes);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, paramTypes);
    }

    @Override
    public String toString() {
      return "MethodInfo{" + "name='" + name + '\'' + ", paramTypes=" + paramTypes + '}';
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
      if (!(o instanceof MethodExtraInfo)) {
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
