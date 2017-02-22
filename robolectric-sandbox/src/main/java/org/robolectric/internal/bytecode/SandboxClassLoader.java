package org.robolectric.internal.bytecode;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;
import static org.objectweb.asm.Type.VOID;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

/**
 * Class loader that modifies the bytecode of Android classes to insert calls to Robolectric's shadow classes.
 */
public class SandboxClassLoader extends URLClassLoader implements Opcodes {
  private final URLClassLoader systemClassLoader;
  private final URLClassLoader urls;
  private final InstrumentationConfiguration config;
  private final Map<String, String> classesToRemap;
  private final Set<MethodRef> methodsToIntercept;

  public SandboxClassLoader(InstrumentationConfiguration config) {
    this(((URLClassLoader) ClassLoader.getSystemClassLoader()), config);
  }

  public SandboxClassLoader(URLClassLoader systemClassLoader, InstrumentationConfiguration config, URL... urls) {
    super(systemClassLoader.getURLs(), systemClassLoader.getParent());
    this.systemClassLoader = systemClassLoader;

    this.config = config;
    this.urls = new URLClassLoader(urls, null);
    classesToRemap = convertToSlashes(config.classNameTranslations());
    methodsToIntercept = convertToSlashes(config.methodsToIntercept());
    for (URL url : urls) {
      Logger.debug("Loading classes from: %s", url);
    }
  }

  @Override
  public URL getResource(String name) {
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
      return maybeInstrumentClass(name);
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
        bytes = getInstrumentedBytes(classNode, config.containsStubs(classInfo));
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

  private String remapParams(String desc) {
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
  private String remapType(String value) {
    String remappedValue = classesToRemap.get(value);
    if (remappedValue != null) {
      value = remappedValue;
    }
    return value;
  }

  private byte[] getInstrumentedBytes(ClassNode classNode, boolean containsStubs) throws ClassNotFoundException {
    if (InvokeDynamic.ENABLED) {
      new InvokeDynamicClassInstrumentor(classNode, containsStubs).instrument();
    } else {
      new OldClassInstrumentor(classNode, containsStubs).instrument();
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

    if (type == Type.VOID_TYPE) {
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
      instructions.add(new MethodInsnNode(INVOKESPECIAL, boxed.getInternalName(), "<init>", "(" + type.getDescriptor() + ")V"));
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

  private boolean shouldIntercept(MethodInsnNode targetMethod) {
    if (targetMethod.name.equals("<init>")) return false; // sorry, can't strip out calls to super() in constructor
    return methodsToIntercept.contains(new MethodRef(targetMethod.owner, targetMethod.name))
        || methodsToIntercept.contains(new MethodRef(targetMethod.owner, "*"));
  }

  abstract class ClassInstrumentor {
    private static final String ROBO_INIT_METHOD_NAME = "$$robo$init";
    static final String GET_ROBO_DATA_SIGNATURE = "()Ljava/lang/Object;";
    final Type OBJECT_TYPE = Type.getType(Object.class);
    private final String OBJECT_DESC = Type.getDescriptor(Object.class);

    final ClassNode classNode;
    private final boolean containsStubs;
    final String internalClassName;
    private final String className;
    final Type classType;

    public ClassInstrumentor(ClassNode classNode, boolean containsStubs) {
      this.classNode = classNode;
      this.containsStubs = containsStubs;

      this.internalClassName = classNode.name;
      this.className = classNode.name.replace('/', '.');
      this.classType = Type.getObjectType(internalClassName);
    }

    //todo javadoc. Extract blocks to separate methods.
    public void instrument() {
      makeClassPublic(classNode);
      classNode.access = classNode.access & ~ACC_FINAL;

      // Need Java version >=7 to allow invokedynamic
      classNode.version = Math.max(classNode.version, V1_7);

      classNode.fields.add(0, new FieldNode(ACC_PUBLIC | ACC_FINAL,
          ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));

      Set<String> foundMethods = instrumentMethods();

      // If there is no constructor, adds one
      addNoArgsConstructor(foundMethods);

      addDirectCallConstructor();

      // Do not override final #equals, #hashCode, and #toString for all classes
      instrumentInheritedObjectMethod(classNode, foundMethods, "equals", "(Ljava/lang/Object;)Z");
      instrumentInheritedObjectMethod(classNode, foundMethods, "hashCode", "()I");
      instrumentInheritedObjectMethod(classNode, foundMethods, "toString", "()Ljava/lang/String;");

      addRoboInitMethod();

      addRoboGetDataMethod();

      doSpecialHandling();
    }

    @NotNull
    private Set<String> instrumentMethods() {
      Set<String> foundMethods = new HashSet<>();
      List<MethodNode> methods = new ArrayList<>(classNode.methods);
      for (MethodNode method : methods) {
        foundMethods.add(method.name + method.desc);

        filterSpecialMethods(method);

        if (method.name.equals("<clinit>")) {
          method.name = ShadowConstants.STATIC_INITIALIZER_METHOD_NAME;
          classNode.methods.add(generateStaticInitializerNotifierMethod());
        } else if (method.name.equals("<init>")) {
          instrumentConstructor(method);
        } else if (!isSyntheticAccessorMethod(method) && !Modifier.isAbstract(method.access)) {
          instrumentNormalMethod(method);
        }
      }
      return foundMethods;
    }

    private void addNoArgsConstructor(Set<String> foundMethods) {
      if (!foundMethods.contains("<init>()V")) {
        MethodNode defaultConstructor = new MethodNode(ACC_PUBLIC, "<init>", "()V", "()V", null);
        RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(defaultConstructor);
        generator.loadThis();
        generator.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", "()V");
        generator.loadThis();
        generator.invokeVirtual(classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
        generator.returnValue();
        classNode.methods.add(defaultConstructor);
      }
    }

    abstract protected void addDirectCallConstructor();

    private void addRoboInitMethod() {
      MethodNode initMethodNode = new MethodNode(ACC_PROTECTED, ROBO_INIT_METHOD_NAME, "()V", null, null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
      Label alreadyInitialized = new Label();
      generator.loadThis();                                         // this
      generator.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
      generator.ifNonNull(alreadyInitialized);
      generator.loadThis();                                         // this
      generator.loadThis();                                         // this, this
      writeCallToInitializing(generator);
      // this, __robo_data__
      generator.putField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
      generator.mark(alreadyInitialized);
      generator.returnValue();
      classNode.methods.add(initMethodNode);
    }

    abstract protected void writeCallToInitializing(RobolectricGeneratorAdapter generator);

    private void addRoboGetDataMethod() {
      MethodNode initMethodNode = new MethodNode(ACC_PUBLIC, ShadowConstants.GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE, null, null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
      generator.loadThis();                                         // this
      generator.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
      generator.returnValue();
      generator.endMethod();
      classNode.methods.add(initMethodNode);
    }

    private void doSpecialHandling() {
      if (className.equals("android.os.Build$VERSION")) {
        for (Object field : classNode.fields) {
          FieldNode fieldNode = (FieldNode) field;
          fieldNode.access &= ~(Modifier.FINAL);
        }
      }
    }

    /**
     * Checks if the given method in the class if overriding, at some point of it's
     * inheritance tree, a final method
     */
    private boolean isOverridingFinalMethod(ClassNode classNode, String methodName, String methodSignature) {
      while (true) {
        List<MethodNode> methods = new ArrayList<>(classNode.methods);

        for (MethodNode method : methods) {
          if (method.name.equals(methodName) && method.desc.equals(methodSignature)) {
            if ((method.access & ACC_FINAL) != 0) {
              return true;
            }
          }
        }

        if (classNode.superName == null) {
          return false;
        }

        try {
          byte[] byteCode = getByteCode(classNode.superName);
          ClassReader classReader = new ClassReader(byteCode);
          classNode = new ClassNode();
          classReader.accept(classNode, 0);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }

      }
    }

    private boolean isSyntheticAccessorMethod(MethodNode method) {
      return (method.access & ACC_SYNTHETIC) != 0;
    }

    /**
     * To be used to instrument methods inherited from the Object class,
     * such as hashCode, equals, and toString.
     * Adds the methods directly to the class.
     */
    private void instrumentInheritedObjectMethod(ClassNode classNode, Set<String> foundMethods, final String methodName, String methodDesc) {
      // Won't instrument if method is overriding a final method
      if (isOverridingFinalMethod(classNode, methodName, methodDesc)) {
        return;
      }

      // if the class doesn't directly override the method, it adds it as a direct invocation and instruments it
      if (!foundMethods.contains(methodName + methodDesc)) {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC, methodName, methodDesc, null, null);
        RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);
        generator.invokeMethod("java/lang/Object", methodNode);
        generator.returnValue();
        generator.endMethod();
        this.classNode.methods.add(methodNode);
        instrumentNormalMethod(methodNode);
      }
    }

    private void instrumentConstructor(MethodNode method) {
      makeMethodPrivate(method);

      if (containsStubs) {
        method.instructions.clear();

        RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(method);
        generator.loadThis();
        generator.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", "()V");
        generator.returnValue();
        generator.endMethod();
      }

      InsnList removedInstructions = extractCallToSuperConstructor(method);
      method.name = new ShadowImpl().directMethodName(ShadowConstants.CONSTRUCTOR_METHOD_NAME);
      classNode.methods.add(redirectorMethod(method, ShadowConstants.CONSTRUCTOR_METHOD_NAME));

      String[] exceptions = exceptionArray(method);
      MethodNode methodNode = new MethodNode(method.access, "<init>", method.desc, method.signature, exceptions);
      makeMethodPublic(methodNode);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);

      methodNode.instructions = removedInstructions;

      generator.loadThis();
      generator.invokeVirtual(classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
      generateShadowCall(method, ShadowConstants.CONSTRUCTOR_METHOD_NAME, generator);

      generator.endMethod();
      classNode.methods.add(methodNode);
    }

    private InsnList extractCallToSuperConstructor(MethodNode ctor) {
      InsnList removedInstructions = new InsnList();
      int startIndex = 0;

      AbstractInsnNode[] insns = ctor.instructions.toArray();
      for (int i = 0; i < insns.length; i++) {
        AbstractInsnNode node = insns[i];

        switch (node.getOpcode()) {
          case ALOAD:
            VarInsnNode vnode = (VarInsnNode) node;
            if (vnode.var == 0) {
              startIndex = i;
            }
            break;

          case INVOKESPECIAL:
            MethodInsnNode mnode = (MethodInsnNode) node;
            if (mnode.owner.equals(internalClassName) || mnode.owner.equals(classNode.superName)) {
              assert mnode.name.equals("<init>");

              // remove all instructions in the range startIndex..i, from aload_0 to invokespecial <init>
              while (startIndex <= i) {
                ctor.instructions.remove(insns[startIndex]);
                removedInstructions.add(insns[startIndex]);
                startIndex++;
              }
              return removedInstructions;
            }
            break;

          case ATHROW:
            ctor.visitCode();
            ctor.visitInsn(RETURN);
            ctor.visitEnd();
            return removedInstructions;
        }
      }

      throw new RuntimeException("huh? " + ctor.name + ctor.desc);
    }

    //TODO javadocs
    private void instrumentNormalMethod(MethodNode method) {
      // if not abstract, set a final modifier
      if ((method.access & ACC_ABSTRACT) == 0) {
        method.access = method.access | ACC_FINAL;
      }
      // if a native method, remove native modifier and force return a default value
      if ((method.access & ACC_NATIVE) != 0) {
        method.access = method.access & ~ACC_NATIVE;

        RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(method);
        Type returnType = generator.getReturnType();
        generator.pushDefaultReturnValueToStack(returnType);
        generator.returnValue();
      }

      // todo figure out
      String originalName = method.name;
      method.name = new ShadowImpl().directMethodName(originalName);

      MethodNode delegatorMethodNode = new MethodNode(method.access, originalName, method.desc, method.signature, exceptionArray(method));
      delegatorMethodNode.visibleAnnotations = method.visibleAnnotations;
      delegatorMethodNode.access &= ~(ACC_NATIVE | ACC_ABSTRACT | ACC_FINAL);

      makeMethodPrivate(method);

      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(delegatorMethodNode);

      generateShadowCall(method, originalName, generator);

      generator.endMethod();

      classNode.methods.add(delegatorMethodNode);
    }

    //todo rename
    private MethodNode redirectorMethod(MethodNode method, String newName) {
      MethodNode redirector = new MethodNode(ASM4, newName, method.desc, method.signature, exceptionArray(method));
      redirector.access = method.access & ~(ACC_NATIVE | ACC_ABSTRACT | ACC_FINAL);
      makeMethodPrivate(redirector);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(redirector);
      generator.invokeMethod(internalClassName, method);
      generator.returnValue();
      return redirector;
    }

    private String[] exceptionArray(MethodNode method) {
      return ((List<String>) method.exceptions).toArray(new String[method.exceptions.size()]);
    }

    /**
     * Filters methods that might need special treatment because of various reasons
     */
    private void filterSpecialMethods(MethodNode callingMethod) {
      ListIterator<AbstractInsnNode> instructions = callingMethod.instructions.iterator();
      while (instructions.hasNext()) {
        AbstractInsnNode node = instructions.next();

        switch (node.getOpcode()) {
          case NEW:
            TypeInsnNode newInsnNode = (TypeInsnNode) node;
            newInsnNode.desc = remapType(newInsnNode.desc);
            break;

          case GETFIELD:
            /* falls through */
          case PUTFIELD:
            /* falls through */
          case GETSTATIC:
            /* falls through */
          case PUTSTATIC:
            FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
            fieldInsnNode.desc = remapType(fieldInsnNode.desc); // todo test
            break;

          case INVOKESTATIC:
            /* falls through */
          case INVOKEINTERFACE:
            /* falls through */
          case INVOKESPECIAL:
            /* falls through */
          case INVOKEVIRTUAL:
            MethodInsnNode targetMethod = (MethodInsnNode) node;
            targetMethod.desc = remapParams(targetMethod.desc);
            if (isGregorianCalendarBooleanConstructor(targetMethod)) {
              replaceGregorianCalendarBooleanConstructor(instructions, targetMethod);
            } else if (shouldIntercept(targetMethod)) {
              interceptInvokeVirtualMethod(instructions, targetMethod);
            }
            break;

          case INVOKEDYNAMIC:
            /* no unusual behavior */
            break;

          default:
            break;
        }
      }
    }

    /**
     * Verifies if the @targetMethod is a <init>(boolean) constructor for {@link java.util.GregorianCalendar}
     */
    private boolean isGregorianCalendarBooleanConstructor(MethodInsnNode targetMethod) {
      return targetMethod.owner.equals("java/util/GregorianCalendar") &&
          targetMethod.name.equals("<init>") &&
          targetMethod.desc.equals("(Z)V");
    }

    /**
     * Replaces the void <init> (boolean) constructor for a call to the void <init> (int, int, int) one
     */
    private void replaceGregorianCalendarBooleanConstructor(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
      // Remove the call to GregorianCalendar(boolean)
      instructions.remove();

      // Discard the already-pushed parameter for GregorianCalendar(boolean)
      instructions.add(new InsnNode(POP));

      // Add parameters values for calling GregorianCalendar(int, int, int)
      instructions.add(new InsnNode(ICONST_0));
      instructions.add(new InsnNode(ICONST_0));
      instructions.add(new InsnNode(ICONST_0));

      // Call GregorianCalendar(int, int, int)
      instructions.add(new MethodInsnNode(INVOKESPECIAL, targetMethod.owner, targetMethod.name, "(III)V", targetMethod.itf));
    }

    /**
     * Decides to call through the appropriate method to intercept the method with an INVOKEVIRTUAL Opcode,
     * depending if the invokedynamic bytecode instruction is available (Java 7+)
     */
    abstract protected void interceptInvokeVirtualMethod(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod);

    /**
     * Replaces protected and private class modifiers with public
     */
    private void makeClassPublic(ClassNode clazz) {
      clazz.access = (clazz.access | ACC_PUBLIC) & ~(ACC_PROTECTED | ACC_PRIVATE);
    }

    /**
     * Replaces protected and private method modifiers with public
     */
    private void makeMethodPublic(MethodNode method) {
      method.access = (method.access | ACC_PUBLIC) & ~(ACC_PROTECTED | ACC_PRIVATE);
    }

    /**
     * Replaces protected and public class modifiers with private
     */
    private void makeMethodPrivate(MethodNode method) {
      method.access = (method.access | ACC_PRIVATE) & ~(ACC_PUBLIC | ACC_PROTECTED);
    }

    private MethodNode generateStaticInitializerNotifierMethod() {
      MethodNode methodNode = new MethodNode(ACC_STATIC, "<clinit>", "()V", "()V", null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);
      generator.push(classType);
      generator.invokeStatic(Type.getType(RobolectricInternals.class), new Method("classInitializing", "(Ljava/lang/Class;)V"));
      generator.returnValue();
      generator.endMethod();
      return methodNode;
    }

    // todo javadocs
    protected abstract void generateShadowCall(MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator);

    int getTag(MethodNode m) {
      return Modifier.isStatic(m.access) ? H_INVOKESTATIC : H_INVOKESPECIAL;
    }
  }

  /**
   * ClassWriter implementation that verifies classes by comparing type information obtained
   * from loading the classes as resources. This was taken from the ASM ClassWriter unit tests.
   */
  private class InstrumentingClassWriter extends ClassWriter {

    /**
     * Preserve stack map frames for V51 and newer bytecode. This fixes class verification errors
     * for JDK7 and JDK8. The option to disable bytecode verification was removed in JDK8.
     * <p>
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
   * GeneratorAdapter implementation specific to generate code for Robolectric purposes
   */
  private static class RobolectricGeneratorAdapter extends GeneratorAdapter {
    private final boolean isStatic;
    private final String desc;

    public RobolectricGeneratorAdapter(MethodNode methodNode) {
      super(Opcodes.ASM4, methodNode, methodNode.access, methodNode.name, methodNode.desc);
      this.isStatic = Modifier.isStatic(methodNode.access);
      this.desc = methodNode.desc;
    }

    public void loadThisOrNull() {
      if (isStatic) {
        loadNull();
      } else {
        loadThis();
      }
    }

    public boolean isStatic() {
      return isStatic;
    }

    public void loadNull() {
      visitInsn(ACONST_NULL);
    }

    public Type getReturnType() {
      return Type.getReturnType(desc);
    }

    /**
     * Forces a return of a default value, depending on the method's return type
     *
     * @param type The method's return type
     */
    public void pushDefaultReturnValueToStack(Type type) {
      if (type.equals(Type.BOOLEAN_TYPE)) {
        push(false);
      } else if (type.equals(Type.INT_TYPE) || type.equals(Type.SHORT_TYPE) || type.equals(Type.BYTE_TYPE) || type.equals(Type.CHAR_TYPE)) {
        push(0);
      } else if (type.equals(Type.LONG_TYPE)) {
        push(0l);
      } else if (type.equals(Type.FLOAT_TYPE)) {
        push(0f);
      } else if (type.equals(Type.DOUBLE_TYPE)) {
        push(0d);
      } else if (type.getSort() == ARRAY || type.getSort() == OBJECT) {
        loadNull();
      }
    }

    private void invokeMethod(String internalClassName, MethodNode method) {
      invokeMethod(internalClassName, method.name, method.desc);
    }

    private void invokeMethod(String internalClassName, String methodName, String methodDesc) {
      if (isStatic()) {
        loadArgs();                                             // this, [args]
        visitMethodInsn(INVOKESTATIC, internalClassName, methodName, methodDesc);
      } else {
        loadThisOrNull();                                       // this
        loadArgs();                                             // this, [args]
        visitMethodInsn(INVOKESPECIAL, internalClassName, methodName, methodDesc);
      }
    }

    public TryCatch tryStart(Type exceptionType) {
      return new TryCatch(this, exceptionType);
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

  public class OldClassInstrumentor extends SandboxClassLoader.ClassInstrumentor {
    private final Type PLAN_TYPE = Type.getType(ClassHandler.Plan.class);
    private final Type THROWABLE_TYPE = Type.getType(Throwable.class);
    private final Method INITIALIZING_METHOD = new Method("initializing", "(Ljava/lang/Object;)Ljava/lang/Object;");
    private final Method METHOD_INVOKED_METHOD = new Method("methodInvoked", "(Ljava/lang/String;ZLjava/lang/Class;)L" + PLAN_TYPE.getInternalName() + ";");
    private final Method PLAN_RUN_METHOD = new Method("run", OBJECT_TYPE, new Type[]{OBJECT_TYPE, OBJECT_TYPE, Type.getType(Object[].class)});
    private final Method HANDLE_EXCEPTION_METHOD = new Method("cleanStackTrace", THROWABLE_TYPE, new Type[]{THROWABLE_TYPE});
    private final String DIRECT_OBJECT_MARKER_TYPE_DESC = Type.getObjectType(DirectObjectMarker.class.getName().replace('.', '/')).getDescriptor();
    private final Type ROBOLECTRIC_INTERNALS_TYPE = Type.getType(RobolectricInternals.class);

    public OldClassInstrumentor(ClassNode classNode, boolean containsStubs) {
      super(classNode, containsStubs);
    }

    @Override
    protected void addDirectCallConstructor() {
      MethodNode directCallConstructor = new MethodNode(ACC_PUBLIC,
          "<init>", "(" + DIRECT_OBJECT_MARKER_TYPE_DESC + classType.getDescriptor() + ")V", null, null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(directCallConstructor);
      generator.loadThis();
      if (classNode.superName.equals("java/lang/Object")) {
        generator.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", "()V");
      } else {
        generator.loadArgs();
        generator.visitMethodInsn(INVOKESPECIAL, classNode.superName,
            "<init>", "(" + DIRECT_OBJECT_MARKER_TYPE_DESC + "L" + classNode.superName + ";)V");
      }
      generator.loadThis();
      generator.loadArg(1);
      generator.putField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
      generator.returnValue();
      classNode.methods.add(directCallConstructor);
    }

    @Override
    protected void writeCallToInitializing(RobolectricGeneratorAdapter generator) {
      generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, INITIALIZING_METHOD);
    }

    @Override
    protected void generateShadowCall(MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator) {
      generateCallToClassHandler(originalMethod, originalMethodName, generator);
    }

    //TODO clean up & javadocs
    private void generateCallToClassHandler(MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator) {
      int planLocalVar = generator.newLocal(PLAN_TYPE);
      int exceptionLocalVar = generator.newLocal(THROWABLE_TYPE);
      Label directCall = new Label();
      Label doReturn = new Label();

      boolean isNormalInstanceMethod = !generator.isStatic && !originalMethodName.equals(ShadowConstants.CONSTRUCTOR_METHOD_NAME);

      // maybe perform proxy call...
      if (isNormalInstanceMethod) {
        Label notInstanceOfThis = new Label();

        generator.loadThis();                                         // this
        generator.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
        generator.instanceOf(classType);                              // __robo_data__, is instance of same class?
        generator.visitJumpInsn(IFEQ, notInstanceOfThis);             // jump if no (is not instance)

        TryCatch tryCatchForProxyCall = generator.tryStart(THROWABLE_TYPE);
        generator.loadThis();                                         // this
        generator.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
        generator.checkCast(classType);                               // __robo_data__ but cast to my class
        generator.loadArgs();                                         // __robo_data__ instance, [args]

        generator.visitMethodInsn(INVOKESPECIAL, internalClassName, originalMethod.name, originalMethod.desc);
        tryCatchForProxyCall.end();

        generator.returnValue();

        // catch(Throwable)
        tryCatchForProxyCall.handler();
        generator.storeLocal(exceptionLocalVar);
        generator.loadLocal(exceptionLocalVar);
        generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
        generator.throwException();

        // callClassHandler...
        generator.mark(notInstanceOfThis);
      }

      // prepare for call to classHandler.methodInvoked(String signature, boolean isStatic)
      generator.push(classType.getInternalName() + "/" + originalMethodName + originalMethod.desc);
      generator.push(generator.isStatic());
      generator.push(classType);                                         // my class
      generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, METHOD_INVOKED_METHOD);
      generator.storeLocal(planLocalVar);

      generator.loadLocal(planLocalVar); // plan
      generator.ifNull(directCall);

      // prepare for call to plan.run(Object instance, Object[] params)
      TryCatch tryCatchForHandler = generator.tryStart(THROWABLE_TYPE);
      generator.loadLocal(planLocalVar); // plan
      generator.loadThisOrNull();        // instance
      if (generator.isStatic()) {        // roboData
        generator.loadNull();
      } else {
        generator.loadThis();
        generator.invokeVirtual(classType, new Method(ShadowConstants.GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE));
      }
      generator.loadArgArray();          // params
      generator.invokeInterface(PLAN_TYPE, PLAN_RUN_METHOD);

      Type returnType = generator.getReturnType();
      int sort = returnType.getSort();
      switch (sort) {
        case VOID:
          generator.pop();
          break;
        case OBJECT:
          /* falls through */
        case ARRAY:
          generator.checkCast(returnType);
          break;
        default:
          int unboxLocalVar = generator.newLocal(OBJECT_TYPE);
          generator.storeLocal(unboxLocalVar);
          generator.loadLocal(unboxLocalVar);
          Label notNull = generator.newLabel();
          Label afterward = generator.newLabel();
          generator.ifNonNull(notNull);
          generator.pushDefaultReturnValueToStack(returnType); // return zero, false, whatever
          generator.goTo(afterward);

          generator.mark(notNull);
          generator.loadLocal(unboxLocalVar);
          generator.unbox(returnType);
          generator.mark(afterward);
          break;
      }
      tryCatchForHandler.end();
      generator.goTo(doReturn);

      // catch(Throwable)
      tryCatchForHandler.handler();
      generator.storeLocal(exceptionLocalVar);
      generator.loadLocal(exceptionLocalVar);
      generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
      generator.throwException();


      if (!originalMethod.name.equals("<init>")) {
        generator.mark(directCall);
        TryCatch tryCatchForDirect = generator.tryStart(THROWABLE_TYPE);
        generator.invokeMethod(classType.getInternalName(), originalMethod.name, originalMethod.desc);
        tryCatchForDirect.end();
        generator.returnValue();

        // catch(Throwable)
        tryCatchForDirect.handler();
        generator.storeLocal(exceptionLocalVar);
        generator.loadLocal(exceptionLocalVar);
        generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
        generator.throwException();
      }

      generator.mark(doReturn);
      generator.returnValue();
    }

    /**
     * Decides to call through the appropriate method to intercept the method with an INVOKEVIRTUAL Opcode,
     * depending if the invokedynamic bytecode instruction is available (Java 7+)
     */
    @Override
    protected void interceptInvokeVirtualMethod(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
      interceptInvokeVirtualMethodWithoutInvokeDynamic(instructions, targetMethod);
    }

    /**
     * Intercepts the method without using the invokedynamic bytecode instruction.
     * Should be called through interceptInvokeVirtualMethod, not directly
     */
    private void interceptInvokeVirtualMethodWithoutInvokeDynamic(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
      boolean isStatic = targetMethod.getOpcode() == INVOKESTATIC;

      instructions.remove(); // remove the method invocation

      Type[] argumentTypes = Type.getArgumentTypes(targetMethod.desc);

      instructions.add(new LdcInsnNode(argumentTypes.length));
      instructions.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));

      // first, move any arguments into an Object[] in reverse order
      for (int i = argumentTypes.length - 1; i >= 0; i--) {
        Type type = argumentTypes[i];
        int argWidth = type.getSize();

        if (argWidth == 1) {                       // A B C []
          instructions.add(new InsnNode(DUP_X1));  // A B [] C []
          instructions.add(new InsnNode(SWAP));    // A B [] [] C
          instructions.add(new LdcInsnNode(i));    // A B [] [] C 2
          instructions.add(new InsnNode(SWAP));    // A B [] [] 2 C
          box(type, instructions);                 // A B [] [] 2 (C)
          instructions.add(new InsnNode(AASTORE)); // A B [(C)]
        } else if (argWidth == 2) {                // A B _C_ []
          instructions.add(new InsnNode(DUP_X2));  // A B [] _C_ []
          instructions.add(new InsnNode(DUP_X2));  // A B [] [] _C_ []
          instructions.add(new InsnNode(POP));     // A B [] [] _C_
          box(type, instructions);                 // A B [] [] (C)
          instructions.add(new LdcInsnNode(i));    // A B [] [] (C) 2
          instructions.add(new InsnNode(SWAP));    // A B [] [] 2 (C)
          instructions.add(new InsnNode(AASTORE)); // A B [(C)]
        }
      }

      if (isStatic) { // []
        instructions.add(new InsnNode(Opcodes.ACONST_NULL)); // [] null
        instructions.add(new InsnNode(Opcodes.SWAP));        // null []
      }

      // instance []
      instructions.add(new LdcInsnNode(targetMethod.owner + "/" + targetMethod.name + targetMethod.desc)); // target method signature
      // instance [] signature
      instructions.add(new InsnNode(DUP_X2));       // signature instance [] signature
      instructions.add(new InsnNode(POP));          // signature instance []

      instructions.add(new LdcInsnNode(classType)); // signature instance [] class
      instructions.add(new MethodInsnNode(INVOKESTATIC,
          Type.getType(RobolectricInternals.class).getInternalName(), "intercept",
          "(Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;"));

      final Type returnType = Type.getReturnType(targetMethod.desc);
      switch (returnType.getSort()) {
        case ARRAY:
          /* falls through */
        case OBJECT:
          instructions.add(new TypeInsnNode(CHECKCAST, remapType(returnType.getInternalName())));
          break;
        case VOID:
          instructions.add(new InsnNode(POP));
          break;
        case Type.LONG:
          instructions.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Long.class)));
          instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Long.class), "longValue", Type.getMethodDescriptor(Type.LONG_TYPE), false));
          break;
        case Type.FLOAT:
          instructions.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Float.class)));
          instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Float.class), "floatValue", Type.getMethodDescriptor(Type.FLOAT_TYPE), false));
          break;
        case Type.DOUBLE:
          instructions.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Double.class)));
          instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Double.class), "doubleValue", Type.getMethodDescriptor(Type.DOUBLE_TYPE), false));
          break;
        case Type.BOOLEAN:
          instructions.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Boolean.class)));
          instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Boolean.class), "booleanValue", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false));
          break;
        case Type.INT:
          instructions.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Integer.class)));
          instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Integer.class), "intValue", Type.getMethodDescriptor(Type.INT_TYPE), false));
          break;
        case Type.SHORT:
          instructions.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Short.class)));
          instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Short.class), "shortValue", Type.getMethodDescriptor(Type.SHORT_TYPE), false));
          break;
        case Type.BYTE:
          instructions.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Byte.class)));
          instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Byte.class), "byteValue", Type.getMethodDescriptor(Type.BYTE_TYPE), false));
          break;
        default:
          throw new RuntimeException("Not implemented: " + getClass().getName() + " cannot intercept methods with return type " + returnType.getClassName());
      }
    }
  }

  public class InvokeDynamicClassInstrumentor extends SandboxClassLoader.ClassInstrumentor {
    private final Handle BOOTSTRAP_INIT;
    private final Handle BOOTSTRAP;
    private final Handle BOOTSTRAP_STATIC;
    private final Handle BOOTSTRAP_INTRINSIC;

    public InvokeDynamicClassInstrumentor(ClassNode classNode, boolean containsStubs) {
      super(classNode, containsStubs);

      String className = Type.getInternalName(InvokeDynamicSupport.class);

      MethodType bootstrap =
          methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class);
      String bootstrapMethod =
          bootstrap.appendParameterTypes(MethodHandle.class).toMethodDescriptorString();
      String bootstrapIntrinsic =
          bootstrap.appendParameterTypes(String.class).toMethodDescriptorString();

      BOOTSTRAP_INIT = new Handle(H_INVOKESTATIC, className, "bootstrapInit", bootstrap.toMethodDescriptorString());
      BOOTSTRAP = new Handle(H_INVOKESTATIC, className, "bootstrap", bootstrapMethod);
      BOOTSTRAP_STATIC = new Handle(H_INVOKESTATIC, className, "bootstrapStatic", bootstrapMethod);
      BOOTSTRAP_INTRINSIC = new Handle(H_INVOKESTATIC, className, "bootstrapIntrinsic", bootstrapIntrinsic);
    }

    @Override
    protected void addDirectCallConstructor() {
      // not needed, for reasons.
    }

    @Override
    protected void writeCallToInitializing(RobolectricGeneratorAdapter generator) {
      generator.invokeDynamic("initializing", Type.getMethodDescriptor(OBJECT_TYPE, classType), BOOTSTRAP_INIT);
    }

    @Override
    protected void generateShadowCall(MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator) {
      generateInvokeDynamic(originalMethod, originalMethodName, generator);
    }

    // todo javadocs
    private void generateInvokeDynamic(MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator) {
      Handle original =
          new Handle(getTag(originalMethod), classType.getInternalName(), originalMethod.name,
              originalMethod.desc);

      if (generator.isStatic()) {
        generator.loadArgs();
        generator.invokeDynamic(originalMethodName, originalMethod.desc, BOOTSTRAP_STATIC, original);
      } else {
        String desc = "(" + classType.getDescriptor() + originalMethod.desc.substring(1);
        generator.loadThis();
        generator.loadArgs();
        generator.invokeDynamic(originalMethodName, desc, BOOTSTRAP, original);
      }

      generator.returnValue();
    }

    @Override
    protected void interceptInvokeVirtualMethod(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
      interceptInvokeVirtualMethodWithInvokeDynamic(instructions, targetMethod);
    }

    /**
     * Intercepts the method using the invokedynamic bytecode instruction available in Java 7+.
     * Should be called through interceptInvokeVirtualMethod, not directly
     */
    private void interceptInvokeVirtualMethodWithInvokeDynamic(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
      instructions.remove();  // remove the method invocation

      Type type = Type.getObjectType(targetMethod.owner);
      String description = targetMethod.desc;
      String owner = type.getClassName();

      if (targetMethod.getOpcode() != INVOKESTATIC) {
        String thisType = type.getDescriptor();
        description = "(" + thisType + description.substring(1, description.length());
      }

      instructions.add(new InvokeDynamicInsnNode(targetMethod.name, description, BOOTSTRAP_INTRINSIC, owner));
    }
  }
}
