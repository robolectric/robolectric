package org.robolectric.internal.bytecode;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.google.common.base.StandardSystemProperty.PATH_SEPARATOR;
import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.robolectric.util.Logger;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Util;

/**
 * Class loader that modifies the bytecode of Android classes to insert calls to Robolectric's shadow classes.
 */
public class SandboxClassLoader extends URLClassLoader implements Opcodes {
  private final ClassLoader systemClassLoader;
  private final URLClassLoader urls;
  private final InstrumentationConfiguration config;
  private final Map<String, String> classesToRemap;
  private final Set<MethodRef> methodsToIntercept;

  public SandboxClassLoader(InstrumentationConfiguration config) {
    this(ClassLoader.getSystemClassLoader(), config);
  }

  public SandboxClassLoader(
      ClassLoader systemClassLoader, InstrumentationConfiguration config, URL... urls) {
    super(getClassPathUrls(systemClassLoader), systemClassLoader.getParent());
    this.systemClassLoader = systemClassLoader;

    this.config = config;
    this.urls = new URLClassLoader(urls, null);
    classesToRemap = convertToSlashes(config.classNameTranslations());
    methodsToIntercept = convertToSlashes(config.methodsToIntercept());
    for (URL url : urls) {
      Logger.debug("Loading classes from: %s", url);
    }
  }

  private static URL[] getClassPathUrls(ClassLoader classloader) {
    if (classloader instanceof URLClassLoader) {
      return ((URLClassLoader) classloader).getURLs();
    }
    return parseJavaClassPath();
  }

  // TODO(b/65488446): Use a public API once one is available.
  private static URL[] parseJavaClassPath() {
    ImmutableList.Builder<URL> urls = ImmutableList.builder();
    for (String entry : Splitter.on(PATH_SEPARATOR.value()).split(JAVA_CLASS_PATH.value())) {
      try {
        try {
          urls.add(new File(entry).toURI().toURL());
        } catch (SecurityException e) { // File.toURI checks to see if the file is a directory
          urls.add(new URL("file", null, new File(entry).getAbsolutePath()));
        }
      } catch (MalformedURLException e) {
        Logger.strict("malformed classpath entry: " + entry, e);
      }
    }
    return urls.build().toArray(new URL[0]);
  }

  @Override
  public URL getResource(String name) {
    if (config.shouldAcquireResource(name)) {
      return urls.getResource(name);
    }
    URL fromParent = super.getResource(name);
    if (fromParent != null) {
      return fromParent;
    }
    return urls.getResource(name);
  }

  private InputStream getClassBytesAsStreamPreferringLocalUrls(String resName) {
    InputStream fromUrlsClassLoader = urls.getResourceAsStream(resName);
    if (fromUrlsClassLoader != null) {
      return fromUrlsClassLoader;
    }
    return super.getResourceAsStream(resName);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    if (config.shouldAcquire(name)) {
      return PerfStatsCollector.getInstance().measure("load sandboxed class",
          () -> maybeInstrumentClass(name));
    } else {
      return systemClassLoader.loadClass(name);
    }
  }

  protected Class<?> maybeInstrumentClass(String className) throws ClassNotFoundException {
    final byte[] origClassBytes = getByteCode(className);

    ClassNode classNode = new ClassNode(Opcodes.ASM4) {
      @Override
      public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        desc = remapParamType(desc);
        return super.visitField(access, name, desc, signature, value);
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, remapParams(desc), signature, exceptions);
        return new JSRInlinerAdapter(methodVisitor, access, name, desc, signature, exceptions);
      }
    };

    final ClassReader classReader = new ClassReader(origClassBytes);
    classReader.accept(classNode, 0);

    classNode.interfaces.add(Type.getInternalName(ShadowedObject.class));

    try {
      byte[] bytes;
      ClassInfo classInfo = new ClassInfo(className, classNode);
      if (config.shouldInstrument(classInfo)) {
        bytes = PerfStatsCollector.getInstance().measure("instrument class",
            () -> getInstrumentedBytes(classNode, config.containsStubs(classInfo))
        );
      } else {
        bytes = origClassBytes;
      }
      ensurePackage(className);
      return defineClass(className, bytes, 0, bytes.length);
    } catch (Exception e) {
      throw new ClassNotFoundException("couldn't load " + className, e);
    } catch (OutOfMemoryError e) {
      System.err.println("[ERROR] couldn't load " + className + " in " + this);
      throw e;
    }
  }

  @Override
  protected Package getPackage(String name) {
    Package aPackage = super.getPackage(name);
    if (aPackage != null) {
      return aPackage;
    }

    return ReflectionHelpers.callInstanceMethod(systemClassLoader, "getPackage",
        from(String.class, name));
  }

  protected byte[] getByteCode(String className) throws ClassNotFoundException {
    String classFilename = className.replace('.', '/') + ".class";
    try (InputStream classBytesStream = getClassBytesAsStreamPreferringLocalUrls(classFilename)) {
      if (classBytesStream == null) throw new ClassNotFoundException(className);

      return Util.readBytes(classBytesStream);
    } catch (IOException e) {
      throw new ClassNotFoundException("couldn't load " + className, e);
    }
  }

  private void ensurePackage(final String className) {
    int lastDotIndex = className.lastIndexOf('.');
    if (lastDotIndex != -1) {
      String pckgName = className.substring(0, lastDotIndex);
      Package pckg = getPackage(pckgName);
      if (pckg == null) {
        definePackage(pckgName, null, null, null, null, null, null, null);
      }
    }
  }

  String remapParams(String desc) {
    StringBuilder buf = new StringBuilder();
    buf.append("(");
    for (Type type : Type.getArgumentTypes(desc)) {
      buf.append(remapParamType(type));
    }
    buf.append(")");
    buf.append(remapParamType(Type.getReturnType(desc)));
    return buf.toString();
  }

  // remap Landroid/Foo; to Landroid/Bar;
  private String remapParamType(String desc) {
    return remapParamType(Type.getType(desc));
  }

  private String remapParamType(Type type) {
    String remappedName;
    String internalName;

    switch (type.getSort()) {
      case ARRAY:
        internalName = type.getInternalName();
        int count = 0;
        while (internalName.charAt(count) == '[') count++;

        remappedName = remapParamType(internalName.substring(count));
        if (remappedName != null) {
          return Type.getObjectType(internalName.substring(0, count) + remappedName).getDescriptor();
        }
        break;

      case OBJECT:
        internalName = type.getInternalName();
        remappedName = classesToRemap.get(internalName);
        if (remappedName != null) {
          return Type.getObjectType(remappedName).getDescriptor();
        }
        break;

      default:
        break;
    }
    return type.getDescriptor();
  }

  // remap android/Foo to android/Bar
  String remapType(String value) {
    String remappedValue = classesToRemap.get(value);
    if (remappedValue != null) {
      value = remappedValue;
    }
    return value;
  }

  private byte[] getInstrumentedBytes(ClassNode classNode, boolean containsStubs) throws ClassNotFoundException {
    if (InvokeDynamic.ENABLED) {
      new InvokeDynamicClassInstrumentor(this, classNode, containsStubs).instrument();
    } else {
      new OldClassInstrumentor(this, classNode, containsStubs).instrument();
    }
    ClassWriter writer = new InstrumentingClassWriter(classNode);
    classNode.accept(writer);
    return writer.toByteArray();
  }

  private Map<String, String> convertToSlashes(Map<String, String> map) {
    HashMap<String, String> newMap = new HashMap<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = internalize(entry.getKey());
      String value = internalize(entry.getValue());
      newMap.put(key, value);
      newMap.put("L" + key + ";", "L" + value + ";"); // also the param reference form
    }
    return newMap;
  }

  private Set<MethodRef> convertToSlashes(Set<MethodRef> methodRefs) {
    HashSet<MethodRef> transformed = new HashSet<>();
    for (MethodRef methodRef : methodRefs) {
      transformed.add(new MethodRef(internalize(methodRef.className), methodRef.methodName));
    }
    return transformed;
  }

  private String internalize(String className) {
    return className.replace('.', '/');
  }

  public static void box(final Type type, ListIterator<AbstractInsnNode> instructions) {
    if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
      return;
    }

    if (Type.VOID_TYPE.equals(type)) {
      instructions.add(new InsnNode(ACONST_NULL));
    } else {
      Type boxed = getBoxedType(type);
      instructions.add(new TypeInsnNode(NEW, boxed.getInternalName()));
      if (type.getSize() == 2) {
        // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
        instructions.add(new InsnNode(DUP_X2));
        instructions.add(new InsnNode(DUP_X2));
        instructions.add(new InsnNode(POP));
      } else {
        // p -> po -> opo -> oop -> o
        instructions.add(new InsnNode(DUP_X1));
        instructions.add(new InsnNode(SWAP));
      }
      instructions.add(new MethodInsnNode(INVOKESPECIAL, boxed.getInternalName(), "<init>", "(" + type.getDescriptor() + ")V", false));
    }
  }

  private static Type getBoxedType(final Type type) {
    switch (type.getSort()) {
      case Type.BYTE:
        return Type.getObjectType("java/lang/Byte");
      case Type.BOOLEAN:
        return Type.getObjectType("java/lang/Boolean");
      case Type.SHORT:
        return Type.getObjectType("java/lang/Short");
      case Type.CHAR:
        return Type.getObjectType("java/lang/Character");
      case Type.INT:
        return Type.getObjectType("java/lang/Integer");
      case Type.FLOAT:
        return Type.getObjectType("java/lang/Float");
      case Type.LONG:
        return Type.getObjectType("java/lang/Long");
      case Type.DOUBLE:
        return Type.getObjectType("java/lang/Double");
    }
    return type;
  }

  boolean shouldIntercept(MethodInsnNode targetMethod) {
    if (targetMethod.name.equals("<init>")) return false; // sorry, can't strip out calls to super() in constructor
    return methodsToIntercept.contains(new MethodRef(targetMethod.owner, targetMethod.name))
        || methodsToIntercept.contains(new MethodRef(targetMethod.owner, "*"));
  }

  /**
   * ClassWriter implementation that verifies classes by comparing type information obtained
   * from loading the classes as resources. This was taken from the ASM ClassWriter unit tests.
   */
  private class InstrumentingClassWriter extends ClassWriter {

    /**
     * Preserve stack map frames for V51 and newer bytecode. This fixes class verification errors
     * for JDK7 and JDK8. The option to disable bytecode verification was removed in JDK8.
     *
     * Don't bother for V50 and earlier bytecode, because it doesn't contain stack map frames, and
     * also because ASM's stack map frame handling doesn't support the JSR and RET instructions
     * present in legacy bytecode.
     */
    public InstrumentingClassWriter(ClassNode classNode) {
      super(classNode.version >= 51 ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS);
    }

    @Override
    public int newNameType(String name, String desc) {
      return super.newNameType(name, desc.charAt(0) == ')' ? remapParams(desc) : remapParamType(desc));
    }

    @Override
    public int newClass(String value) {
      value = remapType(value);
      return super.newClass(value);
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
      try {
        ClassReader info1 = typeInfo(type1);
        ClassReader info2 = typeInfo(type2);
        if ((info1.getAccess() & Opcodes.ACC_INTERFACE) != 0) {
          if (typeImplements(type2, info2, type1)) {
            return type1;
          }
          if ((info2.getAccess() & Opcodes.ACC_INTERFACE) != 0) {
            if (typeImplements(type1, info1, type2)) {
              return type2;
            }
          }
          return "java/lang/Object";
        }
        if ((info2.getAccess() & Opcodes.ACC_INTERFACE) != 0) {
          if (typeImplements(type1, info1, type2)) {
            return type2;
          } else {
            return "java/lang/Object";
          }
        }
        StringBuilder b1 = typeAncestors(type1, info1);
        StringBuilder b2 = typeAncestors(type2, info2);
        String result = "java/lang/Object";
        int end1 = b1.length();
        int end2 = b2.length();
        while (true) {
          int start1 = b1.lastIndexOf(";", end1 - 1);
          int start2 = b2.lastIndexOf(";", end2 - 1);
          if (start1 != -1 && start2 != -1
              && end1 - start1 == end2 - start2) {
            String p1 = b1.substring(start1 + 1, end1);
            String p2 = b2.substring(start2 + 1, end2);
            if (p1.equals(p2)) {
              result = p1;
              end1 = start1;
              end2 = start2;
            } else {
              return result;
            }
          } else {
            return result;
          }
        }
      } catch (IOException e) {
        return "java/lang/Object"; // Handle classes that may be obfuscated
      }
    }

    private StringBuilder typeAncestors(String type, ClassReader info) throws IOException {
      StringBuilder b = new StringBuilder();
      while (!"java/lang/Object".equals(type)) {
        b.append(';').append(type);
        type = info.getSuperName();
        info = typeInfo(type);
      }
      return b;
    }

    private boolean typeImplements(String type, ClassReader info, String itf) throws IOException {
      while (!"java/lang/Object".equals(type)) {
        String[] itfs = info.getInterfaces();
        for (String itf2 : itfs) {
          if (itf2.equals(itf)) {
            return true;
          }
        }
        for (String itf1 : itfs) {
          if (typeImplements(itf1, typeInfo(itf1), itf)) {
            return true;
          }
        }
        type = info.getSuperName();
        info = typeInfo(type);
      }
      return false;
    }

    private ClassReader typeInfo(final String type) throws IOException {
      try (InputStream is = getClassBytesAsStreamPreferringLocalUrls(type + ".class")) {
        return new ClassReader(is);
      }
    }
  }

  /**
   * Provides try/catch code generation with a {@link org.objectweb.asm.commons.GeneratorAdapter}
   */
  static class TryCatch {
    private final Label start;
    private final Label end;
    private final Label handler;
    private final GeneratorAdapter generatorAdapter;

    TryCatch(GeneratorAdapter generatorAdapter, Type type) {
      this.generatorAdapter = generatorAdapter;
      this.start = generatorAdapter.mark();
      this.end = new Label();
      this.handler = new Label();
      generatorAdapter.visitTryCatchBlock(start, end, handler, type.getInternalName());
    }

    void end() {
      generatorAdapter.mark(end);
    }

    void handler() {
      generatorAdapter.mark(handler);
    }
  }

}
