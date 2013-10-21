package org.robolectric.bytecode;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
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
import static org.objectweb.asm.Type.getType;
import static org.robolectric.util.Util.readBytes;

public class AsmInstrumentingClassLoader extends ClassLoader implements Opcodes, InstrumentingClassLoader {
  private static final String OBJECT_DESC = Type.getDescriptor(Object.class);
  private static final Type OBJECT_TYPE = getType(Object.class);
  private static final Type STRING_TYPE = getType(String.class);
  private static final Type ROBOLECTRIC_INTERNALS_TYPE = Type.getType(RobolectricInternals.class);
  private static final Type PLAN_TYPE = Type.getType(ClassHandler.Plan.class);
  private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
  private static final Method INITIALIZING_METHOD = new Method("initializing", "(Ljava/lang/Object;)Ljava/lang/Object;");
  private static final Method METHOD_INVOKED_METHOD = new Method("methodInvoked", "(Ljava/lang/String;ZLjava/lang/Class;)L" + PLAN_TYPE.getInternalName() + ";");
  private static final Method PLAN_RUN_METHOD = new Method("run", OBJECT_TYPE, new Type[]{OBJECT_TYPE, OBJECT_TYPE, Type.getType(Object[].class)});
  private static final Method HANDLE_EXCEPTION_METHOD = new Method("cleanStackTrace", THROWABLE_TYPE, new Type[]{THROWABLE_TYPE});
  private static final String DIRECT_OBJECT_MARKER_TYPE_DESC = Type.getObjectType(DirectObjectMarker.class.getName().replace('.', '/')).getDescriptor();
  private static final String ROBO_INIT_METHOD_NAME = "$$robo$init";
  static final String GET_ROBO_DATA_METHOD_NAME = "$$robo$getData";
  private static final String GET_ROBO_DATA_SIGNATURE = "()Ljava/lang/Object;";

  private static boolean debug = false;

  private final Setup setup;
  private final URLClassLoader urls;
  private final Map<String, Class> classes = new HashMap<String, Class>();
  private final Set<Setup.MethodRef> methodsToIntercept;
  private final Map<String, String> classesToRemap;
  private int number = 0;


  public AsmInstrumentingClassLoader(Setup setup, URL... urls) {
    super(AsmInstrumentingClassLoader.class.getClassLoader());
    this.setup = setup;
    this.urls = new URLClassLoader(urls, null);
    classesToRemap = convertToSlashes(setup.classNameTranslations());
    methodsToIntercept = convertToSlashes(setup.methodsToIntercept());
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

    boolean shouldComeFromThisClassLoader = setup.shouldAcquire(name);

    try {
      if (shouldComeFromThisClassLoader) {
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
    if (setup.shouldAcquire(className)) {
      String classFilename = className.replace('.', '/') + ".class";
      InputStream classBytesStream = urls.getResourceAsStream(classFilename);
      if (classBytesStream == null) {
        classBytesStream = getResourceAsStream(classFilename);
      }
      if (classBytesStream == null) throw new ClassNotFoundException(className);

      byte[] origClassBytes;
      try {
        origClassBytes = readBytes(classBytesStream);
      } catch (IOException e) {
        throw new ClassNotFoundException("couldn't load " + className, e);
      }

      final ClassReader classReader = new ClassReader(origClassBytes);
      ClassNode classNode = new ClassNode() {
        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
          desc = remapParamType(desc);
          return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
          return super.visitMethod(access, name, remapParams(desc), signature, exceptions);
        }
      };
      classReader.accept(classNode, 0);

      try {
        byte[] bytes;
        AsmClassInfo classInfo = new AsmClassInfo(className, classNode);
        if (setup.shouldInstrument(classInfo)) {
          bytes = getInstrumentedBytes(className, classNode, setup.containsStubs(classInfo));
        } else {
          bytes = origClassBytes;
        }
//                System.out.println("[DEBUG] Defining " + classFilename + " (" + bytes.length + ") in " + this + ": class" + number++);
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
//            return super.findClass(className);
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

  private byte[] getInstrumentedBytes(String className, ClassNode classNode, boolean containsStubs) throws ClassNotFoundException {
    new ClassInstrumentor(classNode, containsStubs).instrument();

    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS) {
      @Override
      public int newNameType(String name, String desc) {
        return super.newNameType(name, desc.charAt(0) == ')' ? remapParams(desc) : remapParamType(desc));
      }

      @Override
      public int newClass(String value) {
        value = remapType(value);
        return super.newClass(value);
      }
    };
    classNode.accept(classWriter);

    byte[] classBytes = classWriter.toByteArray();

    if (debug) {
      try {
        FileOutputStream fileOutputStream = new FileOutputStream("tmp/" + className + ".class");
        fileOutputStream.write(classBytes);
        fileOutputStream.close();
        CheckClassAdapter.verify(new ClassReader(classBytes), true, new PrintWriter(new FileWriter("tmp/" + className + ".analysis", false)));
        new ClassReader(classBytes).accept(new TraceClassVisitor(new PrintWriter(new FileWriter("tmp/" + className + ".dis", false))), 0);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return classBytes;
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
    HashMap<String, String> newMap = new HashMap<String, String>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = internalize(entry.getKey());
      String value = internalize(entry.getValue());
      newMap.put(key, value);
      newMap.put("L" + key + ";", "L" + value + ";"); // also the param reference form
    }
    return newMap;
  }

  private Set<Setup.MethodRef> convertToSlashes(Set<Setup.MethodRef> methodRefs) {
    HashSet<Setup.MethodRef> transformed = new HashSet<Setup.MethodRef>();
    for (Setup.MethodRef methodRef : methodRefs) {
      transformed.add(new Setup.MethodRef(internalize(methodRef.className), methodRef.methodName));
    }
    return transformed;
  }

  private String internalize(String className) {
    return className.replace('.', '/');
  }

  private class ClassInstrumentor {
    private final ClassNode classNode;
    private boolean containsStubs;
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

      Set<String> foundMethods = new HashSet<String>();

      List<MethodNode> methods = new ArrayList<MethodNode>(classNode.methods);
      for (MethodNode method : methods) {
        foundMethods.add(method.name + method.desc);

        filterNasties(method);

        if (method.name.equals("<clinit>")) {
          method.name = STATIC_INITIALIZER_METHOD_NAME;
          classNode.methods.add(generateStaticInitializerNotifierMethod());
        } else if (method.name.equals("<init>")) {
          instrumentConstructor(method);
        } else if (!isSyntheticAccessorMethod(method) && !Modifier.isAbstract(method.access)) {
          instrumentNormalMethod(method);
        }
      }

      classNode.fields.add(0, new FieldNode(ACC_PUBLIC, CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));

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
        m.putField(classType, InstrumentingClassLoader.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
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
        m.getField(classType, CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
        m.ifNonNull(alreadyInitialized);
        m.loadThis();                                         // this
        m.loadThis();                                         // this, this
        m.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, INITIALIZING_METHOD); // this, __robo_data__
        m.putField(classType, CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
        m.mark(alreadyInitialized);
        m.returnValue();
        classNode.methods.add(initMethodNode);
      }

      {
        MethodNode initMethodNode = new MethodNode(ACC_PROTECTED, GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE, null, null);
        MyGenerator m = new MyGenerator(initMethodNode);
        m.loadThis();                                         // this
        m.getField(classType, CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
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
      method.name = RobolectricInternals.directMethodName(className, CONSTRUCTOR_METHOD_NAME);
      classNode.methods.add(redirectorMethod(method, CONSTRUCTOR_METHOD_NAME));

      String[] exceptions = exceptionArray(method);
      MethodNode methodNode = new MethodNode(method.access, "<init>", method.desc, method.signature, exceptions);
      makePublic(methodNode);
      MyGenerator m = new MyGenerator(methodNode);

      methodNode.instructions = removedInstructions;

      m.loadThis();
      m.invokeVirtual(classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
      generateCallToClassHandler(method, CONSTRUCTOR_METHOD_NAME, m);

      m.endMethod();
      classNode.methods.add(methodNode);
    }

    private InsnList extractCallToSuperConstructor(MethodNode ctor) {
      InsnList removedInstructions = new InsnList();

      InsnList ins = ctor.instructions;
      ListIterator li = ins.iterator();

      while (li.hasNext()) {
        AbstractInsnNode node = (AbstractInsnNode) li.next();

        li.remove();
        removedInstructions.add(node);

        switch (node.getOpcode()) {
          case INVOKESPECIAL:
            MethodInsnNode mnode = (MethodInsnNode) node;
            if (mnode.owner.equals(internalClassName) || mnode.owner.equals(classNode.superName)) {
              assert mnode.name.equals("<init>");
              return removedInstructions;
            }
            break;

          case ATHROW:
            ctor.visitCode();
            ctor.visitInsn(RETURN);
            ctor.visitEnd();
            System.out.println("ignoring throw in " + ctor.name + ctor.desc);
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
      method.name = RobolectricInternals.directMethodName(className, originalName);
      classNode.methods.add(redirectorMethod(method, RobolectricInternals.directMethodName(originalName)));

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
            if (shouldIntercept(targetMethod)) {
              interceptNastyMethod(instructions, callingMethod, targetMethod);
            }
            break;

          default:
            break;
        }
      }
    }

    private void interceptNastyMethod(ListIterator<AbstractInsnNode> instructions, MethodNode callingMethod, MethodInsnNode targetMethod) {
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
      Type returnType = Type.getReturnType(targetMethod.desc);
      // todo: make this honor the return value if somebody cares about what intercept returns
      switch (returnType.getSort()) {
        case OBJECT:
          instructions.add(new TypeInsnNode(CHECKCAST, remapType(returnType.getInternalName())));
          break;
        case ARRAY:
          // wrong
          instructions.add(new InsnNode(POP));
          instructions.add(new InsnNode(ACONST_NULL));
          break;
        case VOID:
          instructions.add(new InsnNode(POP));
          break;
        case Type.LONG:
          // wrong: should do Long#toLong()
          instructions.add(new InsnNode(POP));
          instructions.add(new InsnNode(LCONST_0));
          break;
        case Type.FLOAT:
          // wrong
          instructions.add(new InsnNode(POP));
          instructions.add(new InsnNode(FCONST_0));
          break;
        case Type.DOUBLE:
          // wrong
          instructions.add(new InsnNode(POP));
          instructions.add(new InsnNode(DCONST_0));
          break;
        default:
          // wrong
          instructions.add(new InsnNode(POP));
          instructions.add(new InsnNode(ICONST_0));
          break;
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

      boolean isNormalInstanceMethod = !m.isStatic && !originalMethodName.equals(InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME);

      // maybe perform proxy call...
      if (isNormalInstanceMethod) {
        Label notInstanceOfThis = new Label();

        m.loadThis();                                         // this
        m.getField(classType, CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
        m.instanceOf(classType);                              // __robo_data__, is instance of same class?
        m.visitJumpInsn(IFEQ, notInstanceOfThis);             // jump if no (is not instance)

        TryCatch tryCatchForProxyCall = m.tryStart(THROWABLE_TYPE);
        m.loadThis();                                         // this
        m.getField(classType, CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
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
        m.invokeVirtual(classType, new Method(GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE));
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
    return methodsToIntercept.contains(new Setup.MethodRef(targetMethod.owner, targetMethod.name))
        || methodsToIntercept.contains(new Setup.MethodRef(targetMethod.owner, "*"));
  }

  public static class AsmClassInfo implements ClassInfo {
    private final String className;
    private ClassNode classNode;

    public AsmClassInfo(String className, ClassNode classNode) {
      this.className = className;
      this.classNode = classNode;
    }

    @Override
    public boolean isInterface() {
      return (classNode.access & ACC_INTERFACE) != 0;
    }

    @Override
    public boolean isAnnotation() {
      return (classNode.access & ACC_ANNOTATION) != 0;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
      String internalName = "L" + annotationClass.getName().replace('.', '/') + ";";
      if (classNode.visibleAnnotations == null) return false;
      for (Object visibleAnnotation : classNode.visibleAnnotations) {
        AnnotationNode annotationNode = (AnnotationNode) visibleAnnotation;
        if (annotationNode.desc.equals(internalName)) return true;
      }
      return false;
    }

    @Override
    public String getName() {
      return className;
    }
  }
}