package org.robolectric.internal.bytecode;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.robolectric.internal.ShadowedObject;
import org.robolectric.internal.Shadow;
import org.robolectric.internal.ShadowConstants;
import org.objectweb.asm.tree.VarInsnNode;
import org.robolectric.util.Logger;

import java.io.IOException;
import java.io.InputStream;
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

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;
import static org.objectweb.asm.Type.VOID;
import static org.robolectric.util.Util.readBytes;

/**
 * Class loader that modifies the bytecode of Android classes to insert calls to Robolectric's shadow classes.
 */
public class InstrumentingClassLoader extends ClassLoader implements Opcodes {
  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final Type ROBOLECTRIC_INTERNALS_TYPE = Type.getType(RobolectricInternals.class);
  private static final Type PLAN_TYPE = Type.getType(ClassHandler.Plan.class);
  private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
  private static final String OBJECT_DESC = Type.getDescriptor(Object.class);

  private static final Method INITIALIZING_METHOD = new Method("initializing", "(Ljava/lang/Object;)Ljava/lang/Object;");
  private static final Method METHOD_INVOKED_METHOD = new Method("methodInvoked", "(Ljava/lang/String;ZLjava/lang/Class;)L" + PLAN_TYPE.getInternalName() + ";");
  private static final Method PLAN_RUN_METHOD = new Method("run", OBJECT_TYPE, new Type[]{OBJECT_TYPE, OBJECT_TYPE, Type.getType(Object[].class)});
  private static final Method HANDLE_EXCEPTION_METHOD = new Method("cleanStackTrace", THROWABLE_TYPE, new Type[]{THROWABLE_TYPE});
  private static final String DIRECT_OBJECT_MARKER_TYPE_DESC = Type.getObjectType(DirectObjectMarker.class.getName().replace('.', '/')).getDescriptor();
  private static final String ROBO_INIT_METHOD_NAME = "$$robo$init";
  private static final String GET_ROBO_DATA_SIGNATURE = "()Ljava/lang/Object;";

  private final URLClassLoader urls;
  private final InstrumentationConfiguration config;
  private final Map<String, Class> classes = new HashMap<>();
  private final Map<String, String> classesToRemap;
  private final Set<InstrumentationConfiguration.MethodRef> methodsToIntercept;

  public InstrumentingClassLoader(InstrumentationConfiguration config, URL... urls) {
    super(InstrumentingClassLoader.class.getClassLoader());
    this.config = config;
    this.urls = new URLClassLoader(urls, null);
    classesToRemap = convertToSlashes(config.classNameTranslations());
    methodsToIntercept = convertToSlashes(config.methodsToIntercept());
    for (URL url : urls) {
      Logger.debug("Loading classes from: %s", url);
    }
  }

  @Override
  synchronized public Class loadClass(String name) throws ClassNotFoundException {
    Class<?> theClass = classes.get(name);
    if (theClass != null) {
      if (theClass == MissingClassMarker.class) {
        throw new ClassNotFoundException(name);
      } else {
        return theClass;
      }
    }

    try {
      if (config.shouldAcquire(name)) {
        theClass = findClass(name);
      } else {
        theClass = getParent().loadClass(name);
      }
    } catch (ClassNotFoundException e) {
      classes.put(name, MissingClassMarker.class);
      throw e;
    }

    classes.put(name, theClass);
    return theClass;
  }

  private static class MissingClassMarker {
  }

  @Override
  public InputStream getResourceAsStream(String resName) {
    InputStream fromUrlsClassLoader = urls.getResourceAsStream(resName);
    if (fromUrlsClassLoader != null)  {
      return fromUrlsClassLoader;
    }
    return super.getResourceAsStream(resName);
  }

  @Override
  protected Class<?> findClass(final String className) throws ClassNotFoundException {
    if (config.shouldAcquire(className)) {
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
    } else {
      throw new IllegalStateException("how did we get here? " + className);
    }
  }

  protected byte[] getByteCode(String className) throws ClassNotFoundException {
    String classFilename = className.replace('.', '/') + ".class";
    try (InputStream classBytesStream = getResourceAsStream(classFilename)) {
      if (classBytesStream == null) throw new ClassNotFoundException(className);

      return readBytes(classBytesStream);
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
    new ClassInstrumentor(classNode, containsStubs).instrument();
    ClassWriter writer = new InstrumentingClassWriter(classNode);
    classNode.accept(writer);
    return writer.toByteArray();
  }

  private static class MyGenerator extends GeneratorAdapter {
    private final boolean isStatic;
    private final String desc;

    public MyGenerator(MethodNode methodNode) {
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

    public void pushZero(Type type) {
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

  public static class TryCatch {
    private final Label start;
    private final Label end;
    private final Label handler;
    private final GeneratorAdapter generatorAdapter;

    public TryCatch(GeneratorAdapter generatorAdapter, Type type) {
      this.generatorAdapter = generatorAdapter;
      this.start = generatorAdapter.mark();
      this.end = new Label();
      this.handler = new Label();
      generatorAdapter.visitTryCatchBlock(start, end, handler, type.getInternalName());
    }

    public void end() {
      generatorAdapter.mark(end);
    }

    public void handler() {
      generatorAdapter.mark(handler);
    }
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

  private Set<InstrumentationConfiguration.MethodRef> convertToSlashes(Set<InstrumentationConfiguration.MethodRef> methodRefs) {
    HashSet<InstrumentationConfiguration.MethodRef> transformed = new HashSet<>();
    for (InstrumentationConfiguration.MethodRef methodRef : methodRefs) {
      transformed.add(new InstrumentationConfiguration.MethodRef(internalize(methodRef.className), methodRef.methodName));
    }
    return transformed;
  }

  private String internalize(String className) {
    return className.replace('.', '/');
  }

  private class ClassInstrumentor {
    private final ClassNode classNode;
    private final boolean containsStubs;
    private final String internalClassName;
    private final String className;
    private final Type classType;

    public ClassInstrumentor(ClassNode classNode, boolean containsStubs) {
      this.classNode = classNode;
      this.containsStubs = containsStubs;

      this.internalClassName = classNode.name;
      this.className = classNode.name.replace('/', '.');
      this.classType = Type.getObjectType(internalClassName);
    }

    public void instrument() {
      makePublic(classNode);
      classNode.access = classNode.access & ~ACC_FINAL;

      Set<String> foundMethods = new HashSet<>();
      List<MethodNode> methods = new ArrayList<>(classNode.methods);
      for (MethodNode method : methods) {
        foundMethods.add(method.name + method.desc);

        filterNasties(method);

        if (method.name.equals("<clinit>")) {
          method.name = ShadowConstants.STATIC_INITIALIZER_METHOD_NAME;
          classNode.methods.add(generateStaticInitializerNotifierMethod());
        } else if (method.name.equals("<init>")) {
          instrumentConstructor(method);
        } else if (!isSyntheticAccessorMethod(method) && !Modifier.isAbstract(method.access)) {
          instrumentNormalMethod(method);
        }
      }

      classNode.fields.add(0, new FieldNode(ACC_PUBLIC, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));

      if (!foundMethods.contains("<init>()V")) {
        MethodNode defaultConstructor = new MethodNode(ACC_PUBLIC, "<init>", "()V", "()V", null);
        MyGenerator m = new MyGenerator(defaultConstructor);
        m.loadThis();
        m.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", "()V");
        m.loadThis();
        m.invokeVirtual(classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
        m.returnValue();
        classNode.methods.add(defaultConstructor);
      }

      {
        MethodNode directCallConstructor = new MethodNode(ACC_PUBLIC,
            "<init>", "(" + DIRECT_OBJECT_MARKER_TYPE_DESC + classType.getDescriptor() + ")V", null, null);
        MyGenerator m = new MyGenerator(directCallConstructor);
        m.loadThis();
        if (classNode.superName.equals("java/lang/Object")) {
          m.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", "()V");
        } else {
          m.loadArgs();
          m.visitMethodInsn(INVOKESPECIAL, classNode.superName,
              "<init>", "(" + DIRECT_OBJECT_MARKER_TYPE_DESC + "L" + classNode.superName + ";)V");
        }
        m.loadThis();
        m.loadArg(1);
        m.putField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
        m.returnValue();
        classNode.methods.add(directCallConstructor);
      }

      if (!isEnum()) {
        instrumentSpecial(foundMethods, "equals", "(Ljava/lang/Object;)Z");
        instrumentSpecial(foundMethods, "hashCode", "()I");
      }
      instrumentSpecial(foundMethods, "toString", "()Ljava/lang/String;");

      {
        MethodNode initMethodNode = new MethodNode(ACC_PROTECTED, ROBO_INIT_METHOD_NAME, "()V", null, null);
        MyGenerator m = new MyGenerator(initMethodNode);
        Label alreadyInitialized = new Label();
        m.loadThis();                                         // this
        m.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
        m.ifNonNull(alreadyInitialized);
        m.loadThis();                                         // this
        m.loadThis();                                         // this, this
        m.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, INITIALIZING_METHOD); // this, __robo_data__
        m.putField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
        m.mark(alreadyInitialized);
        m.returnValue();
        classNode.methods.add(initMethodNode);
      }

      {
        MethodNode initMethodNode = new MethodNode(ACC_PUBLIC, ShadowConstants.GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE, null, null);
        MyGenerator m = new MyGenerator(initMethodNode);
        m.loadThis();                                         // this
        m.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
        m.returnValue();
        m.endMethod();
        classNode.methods.add(initMethodNode);
      }

      if (className.equals("android.os.Build$VERSION")) {
        for (Object field : classNode.fields) {
          FieldNode fieldNode = (FieldNode) field;
          fieldNode.access &= ~(Modifier.FINAL);
        }
      }
    }

    private boolean isSyntheticAccessorMethod(MethodNode method) {
      return (method.access & ACC_SYNTHETIC) != 0;
    }

    private void instrumentSpecial(Set<String> foundMethods, final String methodName, String methodDesc) {
      if (!foundMethods.contains(methodName + methodDesc)) {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC, methodName, methodDesc, null, null);
        MyGenerator m = new MyGenerator(methodNode);
        m.invokeMethod("java/lang/Object", methodNode);
        m.returnValue();
        m.endMethod();
        classNode.methods.add(methodNode);
        instrumentNormalMethod(methodNode);
      }
    }

    private void instrumentConstructor(MethodNode method) {
      makePrivate(method);

      if (containsStubs) {
        method.instructions.clear();

        MyGenerator m = new MyGenerator(method);
        m.loadThis();
        m.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", "()V");
        m.returnValue();
        m.endMethod();
      }

      InsnList removedInstructions = extractCallToSuperConstructor(method);
      method.name = Shadow.directMethodName(ShadowConstants.CONSTRUCTOR_METHOD_NAME);
      classNode.methods.add(redirectorMethod(method, ShadowConstants.CONSTRUCTOR_METHOD_NAME));

      String[] exceptions = exceptionArray(method);
      MethodNode methodNode = new MethodNode(method.access, "<init>", method.desc, method.signature, exceptions);
      makePublic(methodNode);
      MyGenerator m = new MyGenerator(methodNode);

      methodNode.instructions = removedInstructions;

      m.loadThis();
      m.invokeVirtual(classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
      generateCallToClassHandler(method, ShadowConstants.CONSTRUCTOR_METHOD_NAME, m);

      m.endMethod();
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

    private void instrumentNormalMethod(MethodNode method) {
      makePrivate(method);
      if ((method.access & ACC_ABSTRACT) == 0) method.access = method.access | ACC_FINAL;
      if ((method.access & ACC_NATIVE) != 0) {
        method.access = method.access & ~ACC_NATIVE;

        MyGenerator myGenerator = new MyGenerator(method);
        Type returnType = myGenerator.getReturnType();
        myGenerator.pushZero(returnType);
        myGenerator.returnValue();
      }

      String originalName = method.name;
      method.name = Shadow.directMethodName(originalName);

      MethodNode delegatorMethodNode = new MethodNode(method.access, originalName, method.desc, method.signature, exceptionArray(method));
      delegatorMethodNode.access &= ~(ACC_NATIVE | ACC_ABSTRACT | ACC_FINAL);
      makePublic(delegatorMethodNode);

      MyGenerator m = new MyGenerator(delegatorMethodNode);

      generateCallToClassHandler(method, originalName, m);

      m.endMethod();

      classNode.methods.add(delegatorMethodNode);
    }

    private MethodNode redirectorMethod(MethodNode method, String newName) {
      MethodNode redirector = new MethodNode(ASM4, newName, method.desc, method.signature, exceptionArray(method));
      redirector.access = method.access & ~(ACC_NATIVE | ACC_ABSTRACT | ACC_FINAL);
      makePrivate(redirector);
      MyGenerator m = new MyGenerator(redirector);
      m.invokeMethod(internalClassName, method);
      m.returnValue();
      return redirector;
    }

    private String[] exceptionArray(MethodNode method) {
      return ((List<String>) method.exceptions).toArray(new String[method.exceptions.size()]);
    }

    private void filterNasties(MethodNode callingMethod) {
      ListIterator<AbstractInsnNode> instructions = callingMethod.instructions.iterator();
      while (instructions.hasNext()) {
        AbstractInsnNode node = instructions.next();

        switch (node.getOpcode()) {
          case NEW:
            TypeInsnNode newInsnNode = (TypeInsnNode) node;
            newInsnNode.desc = remapType(newInsnNode.desc);
            break;

          case GETFIELD:
          case PUTFIELD:
          case GETSTATIC:
          case PUTSTATIC:
            FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
            fieldInsnNode.desc = remapType(fieldInsnNode.desc); // todo test
            break;

          case INVOKESTATIC:
          case INVOKEDYNAMIC:
          case INVOKEINTERFACE:
          case INVOKESPECIAL:
          case INVOKEVIRTUAL:
            MethodInsnNode targetMethod = (MethodInsnNode) node;
            targetMethod.desc = remapParams(targetMethod.desc);
            if (isGregorianCalendar(targetMethod)) {
              replaceNastyGregorianCalendarConstructor(instructions, targetMethod);
            } else if (shouldIntercept(targetMethod)) {
              interceptNastyMethod(instructions, targetMethod);
            }
            break;

          default:
            break;
        }
      }
    }

    private boolean isGregorianCalendar(MethodInsnNode targetMethod) {
      return targetMethod.owner.equals("java/util/GregorianCalendar") && targetMethod.name.equals("<init>") && targetMethod.desc.equals("(Z)V");
    }

    private void replaceNastyGregorianCalendarConstructor(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
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

    private void interceptNastyMethod(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
      boolean isStatic = targetMethod.getOpcode() == INVOKESTATIC;

      instructions.remove(); // remove the method invocation

      Type[] argumentTypes = Type.getArgumentTypes(targetMethod.desc);

      instructions.add(new LdcInsnNode(argumentTypes.length));
      instructions.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));

      // first, move any arguments into an Object[]
      for (int i = argumentTypes.length - 1; i >= 0 ; i--) {
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

    private void makePublic(ClassNode clazz) {
      clazz.access = (clazz.access | ACC_PUBLIC) & ~(ACC_PROTECTED | ACC_PRIVATE);
    }

    private void makePublic(MethodNode method) {
      method.access = (method.access | ACC_PUBLIC) & ~(ACC_PROTECTED | ACC_PRIVATE);
    }

    private void makePrivate(MethodNode method) {
      method.access = (method.access | ACC_PRIVATE) & ~(ACC_PUBLIC | ACC_PROTECTED);
    }

    private MethodNode generateStaticInitializerNotifierMethod() {
      MethodNode methodNode = new MethodNode(ACC_STATIC, "<clinit>", "()V", "()V", null);
      MyGenerator m = new MyGenerator(methodNode);
      m.push(classType);
      m.invokeStatic(Type.getType(RobolectricInternals.class), new Method("classInitializing", "(Ljava/lang/Class;)V"));
      m.returnValue();
      m.endMethod();
      return methodNode;
    }

    private void generateCallToClassHandler(MethodNode originalMethod, String originalMethodName, MyGenerator m) {
      int planLocalVar = m.newLocal(PLAN_TYPE);
      int exceptionLocalVar = m.newLocal(THROWABLE_TYPE);
      Label directCall = new Label();
      Label doReturn = new Label();

      boolean isNormalInstanceMethod = !m.isStatic && !originalMethodName.equals(ShadowConstants.CONSTRUCTOR_METHOD_NAME);

      // maybe perform proxy call...
      if (isNormalInstanceMethod) {
        Label notInstanceOfThis = new Label();

        m.loadThis();                                         // this
        m.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
        m.instanceOf(classType);                              // __robo_data__, is instance of same class?
        m.visitJumpInsn(IFEQ, notInstanceOfThis);             // jump if no (is not instance)

        TryCatch tryCatchForProxyCall = m.tryStart(THROWABLE_TYPE);
        m.loadThis();                                         // this
        m.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
        m.checkCast(classType);                               // __robo_data__ but cast to my class
        m.loadArgs();                                         // __robo_data__ instance, [args]

        m.visitMethodInsn(INVOKESPECIAL, internalClassName, originalMethod.name, originalMethod.desc);
        tryCatchForProxyCall.end();

        m.returnValue();

        // catch(Throwable)
        tryCatchForProxyCall.handler();
        m.storeLocal(exceptionLocalVar);
        m.loadLocal(exceptionLocalVar);
        m.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
        m.throwException();

        // callClassHandler...
        m.mark(notInstanceOfThis);
      }

      // prepare for call to classHandler.methodInvoked(String signature, boolean isStatic)
      m.push(classType.getInternalName() + "/" + originalMethodName + originalMethod.desc);
      m.push(m.isStatic());
      m.push(classType);                                         // my class
      m.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, METHOD_INVOKED_METHOD);
      m.storeLocal(planLocalVar);

      m.loadLocal(planLocalVar); // plan
      m.ifNull(directCall);

      // prepare for call to plan.run(Object instance, Object[] params)
      TryCatch tryCatchForHandler = m.tryStart(THROWABLE_TYPE);
      m.loadLocal(planLocalVar); // plan
      m.loadThisOrNull();        // instance
      if (m.isStatic()) {        // roboData
        m.loadNull();
      } else {
        m.loadThis();
        m.invokeVirtual(classType, new Method(ShadowConstants.GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE));
      }
      m.loadArgArray();          // params
      m.invokeInterface(PLAN_TYPE, PLAN_RUN_METHOD);

      Type returnType = m.getReturnType();
      int sort = returnType.getSort();
      switch (sort) {
        case VOID:
          m.pop();
          break;
        case OBJECT:
        case ARRAY:
          m.checkCast(returnType);
          break;
        default:
          int unboxLocalVar = m.newLocal(OBJECT_TYPE);
          m.storeLocal(unboxLocalVar);
          m.loadLocal(unboxLocalVar);
          Label notNull = m.newLabel();
          Label afterward = m.newLabel();
          m.ifNonNull(notNull);
          m.pushZero(returnType); // return zero, false, whatever
          m.goTo(afterward);

          m.mark(notNull);
          m.loadLocal(unboxLocalVar);
          m.unbox(returnType);
          m.mark(afterward);
          break;
      }
      tryCatchForHandler.end();
      m.goTo(doReturn);

      // catch(Throwable)
      tryCatchForHandler.handler();
      m.storeLocal(exceptionLocalVar);
      m.loadLocal(exceptionLocalVar);
      m.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
      m.throwException();


      if (!originalMethod.name.equals("<init>")) {
        m.mark(directCall);
        TryCatch tryCatchForDirect = m.tryStart(THROWABLE_TYPE);
        m.invokeMethod(classType.getInternalName(), originalMethod.name, originalMethod.desc);
        tryCatchForDirect.end();
        m.returnValue();

        // catch(Throwable)
        tryCatchForDirect.handler();
        m.storeLocal(exceptionLocalVar);
        m.loadLocal(exceptionLocalVar);
        m.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
        m.throwException();
      }

      m.mark(doReturn);
      m.returnValue();
    }

    private boolean isEnum() {
      return (classNode.access & ACC_ENUM) != 0;
    }
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
    return methodsToIntercept.contains(new InstrumentationConfiguration.MethodRef(targetMethod.owner, targetMethod.name))
        || methodsToIntercept.contains(new InstrumentationConfiguration.MethodRef(targetMethod.owner, "*"));
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
      try (InputStream is = getResourceAsStream(type + ".class")) {
        return new ClassReader(is);
      }
    }
  }
}
