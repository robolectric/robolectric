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
import static org.robolectric.util.Util.reverse;

public class AsmInstrumentingClassLoader extends ClassLoader implements Opcodes, InstrumentingClassLoader {
    private static final String OBJECT_DESC = Type.getDescriptor(Object.class);
    private static final Type OBJECT_TYPE = getType(Object.class);
    private static final Type STRING_TYPE = getType(String.class);
    private static final Type ROBOLECTRIC_INTERNALS_TYPE = Type.getType(RobolectricInternals.class);

    private static boolean debug = true;

    private final Setup setup;
    private final URLClassLoader urls;
    private final Map<String, Class> classes = new HashMap<String, Class>();
    private Set<Setup.MethodRef> methodsToIntercept;
    private final Map<String, String> classesToRemap;

    public static final String DIRECT_OBJECT_MARKER_TYPE_DESC = Type.getObjectType(DirectObjectMarker.class.getName().replace('.', '/')).getDescriptor();

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
        if (theClass != null) return theClass;

        boolean shouldComeFromThisClassLoader = setup.shouldAcquire(name);
//        System.out.println("loadClass: " + name + (shouldComeFromThisClassLoader ? " acquired!" : ""));

        if (shouldComeFromThisClassLoader) {
            theClass = findClass(name);
        } else {
            theClass = getParent().loadClass(name);
        }

        classes.put(name, theClass);
        return theClass;
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
                return defineClass(className, bytes, 0, bytes.length);
            } catch (Exception e) {
                throw new ClassNotFoundException("couldn't load " + className, e);
            }
        } else {
            throw new IllegalStateException("how did we get here? " + className);
//            return super.findClass(className);
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

//        CheckClassAdapter.verify(new ClassReader(classBytes), false, new PrintWriter(System.out));

        if (debug || className.contains("GeoPoint") || className.contains("ClassWithFunnyConstructors")) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream("tmp/" + className + ".class");
                fileOutputStream.write(classBytes);
                fileOutputStream.close();
                new ClassReader(classBytes).accept(new TraceClassVisitor(new PrintWriter(new FileWriter("tmp/" + className + ".java", true))), 0);
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
            } else throw new IllegalStateException("huh?");
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
    }

    private Map<String, String> convertToSlashes(Map<String, String> map) {
        HashMap<String, String> newMap = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            newMap.put(internalize(entry.getKey()), internalize(entry.getValue()));
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
            fixAccess(classNode);
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
                } else if (!isSyntheticAccessorMethod(method)) {
                    instrumentNormalMethod(method);
                }
            }

            classNode.fields.add(new FieldNode(ACC_PUBLIC, CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));

            if (!foundMethods.contains("<init>()V")) {
                MethodNode defaultConstructor = new MethodNode(ACC_PUBLIC, "<init>", "()V", "()V", null);
                MyGenerator m = new MyGenerator(defaultConstructor);
                m.loadThis();
                m.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", "()V");
                m.returnValue();
                m.endMethod();
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
                m.endMethod();
                classNode.methods.add(directCallConstructor);
            }

            if (!isEnum()) {
                instrumentSpecial(foundMethods, "equals", "(Ljava/lang/Object;)Z");
                instrumentSpecial(foundMethods, "hashCode", "()I");
            }
            instrumentSpecial(foundMethods, "toString", "()Ljava/lang/String;");

//            for (MethodNode method : (List<MethodNode>)classNode.methods) {
//                System.out.println("method = " + method.name + method.desc);
//            }
        }

        private boolean isSyntheticAccessorMethod(MethodNode method) {
            return (method.access & ACC_SYNTHETIC) != 0;
        }

        private void instrumentSpecial(Set<String> foundMethods, final String methodName, String methodDesc) {
            if (!foundMethods.contains(methodName + methodDesc)) {
                MethodNode methodNode = new MethodNode(ACC_PUBLIC, methodName, methodDesc, null, null);
                MyGenerator m = new MyGenerator(methodNode);
                m.loadThis();
                m.loadArgs();
                m.invokeMethod("java/lang/Object", methodNode);
                m.returnValue();
                m.endMethod();
                classNode.methods.add(methodNode);
                instrumentNormalMethod(methodNode);
            }
        }

        private void instrumentConstructor(MethodNode method) {
            fixAccess(method);

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
            MyGenerator m = new MyGenerator(methodNode);

            methodNode.instructions = removedInstructions;

            generateCallToClassHandler(method, CONSTRUCTOR_METHOD_NAME, m);
            m.returnValue();

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

                if (node.getOpcode() == INVOKESPECIAL) {
                    MethodInsnNode mnode = (MethodInsnNode) node;
                    if (mnode.owner.equals(internalClassName) || mnode.owner.equals(classNode.superName)) {
                        assert mnode.name.equals("<init>");
                        return removedInstructions;
                    }
                }

                if (node.getOpcode() == ATHROW) {
//                    removedInstructions.clear();
                    ctor.visitCode();
                    ctor.visitInsn(RETURN);
                    ctor.visitEnd();
                    System.out.println("ignoring throw in " + ctor.name + ctor.desc);
                    return removedInstructions;
                }
            }

            throw new RuntimeException("huh? " + ctor.name + ctor.desc);

//            // Look for the ALOAD 0 (i.e., push this on the stack)
//            while (li.hasNext()) {
//                AbstractInsnNode node = (AbstractInsnNode) li.next();
//                if (node.getOpcode() == ALOAD) {
//                    VarInsnNode varNode = (VarInsnNode) node;
//                    assert varNode.var == 0;
//                    // Remove the ALOAD
//                    li.remove();
//                    break;
//                }
//            }
//
//            // Look for the call to the super-class, an INVOKESPECIAL
//            while (li.hasNext()) {
//                AbstractInsnNode node = (AbstractInsnNode) li.next();
//                if (node.getOpcode() == INVOKESPECIAL) {
//                    MethodInsnNode mnode = (MethodInsnNode) node;
////                assert mnode.owner.equals(methodNo.superName);
//                    assert mnode.name.equals("<init>");
////                assert mnode.desc.equals(cons.desc);
//
//                    li.remove();
//                    return removedInstructions;
//                }
//            }
//
////        throw new AssertionError("Could not convert constructor " + classNode.name + ctor.name + " to simple method.");
//            return removedInstructions;
        }

        private void instrumentNormalMethod(MethodNode method) {
            fixAccess(method);
            method.access = method.access & ~ACC_FINAL;

            String originalName = method.name;
            method.name = RobolectricInternals.directMethodName(className, originalName);
            classNode.methods.add(redirectorMethod(method, RobolectricInternals.directMethodName(originalName)));

            MethodNode delegatorMethodNode = new MethodNode(method.access, originalName, method.desc, method.signature, exceptionArray(method));
            delegatorMethodNode.access &= ~(ACC_NATIVE | ACC_ABSTRACT | ACC_FINAL);

            MyGenerator m = new MyGenerator(delegatorMethodNode);

            Label callDirect = new Label();
            Label callClassHandler = new Label();

            if (!m.isStatic) {
                m.loadThis();                                         // this
                m.getField(classType, "__robo_data__", OBJECT_TYPE);  // contents of __robo_data__
                m.instanceOf(classType);                              // is instance of same class?
                m.visitJumpInsn(IFNE, callDirect); // jump if yes (is instance)
            }

            if (m.isStatic) {
                m.push(classType);                                    // my class
            } else {
                m.loadThis();                                         // this
            }
            m.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, new Method("shouldCallDirectly", "(Ljava/lang/Object;)Z"));
            // args, should call directly?
            m.visitJumpInsn(IFEQ, callClassHandler); // jump if no (should not call directly)

            m.invokeMethod(internalClassName, method);
            m.returnValue();

            // callDirect...
            m.mark(callDirect);

            // call direct method and return
            if (m.isStatic()) {
                m.loadArgs();                                             // this, [args]
                m.visitMethodInsn(INVOKESTATIC, internalClassName, method.name, method.desc);
            } else {
                m.loadThis();
                m.getField(classType, "__robo_data__", OBJECT_TYPE);      // __robo_data__ instance
                m.checkCast(classType);
                m.loadArgs();                                             // __robo_data__ instance, [args]
                m.visitMethodInsn(INVOKESPECIAL, internalClassName, method.name, method.desc);
            }
            m.returnValue();

            // callClassHandler...
            m.mark(callClassHandler);
            generateCallToClassHandler(method, originalName, m);
            m.returnValue();

            m.endMethod();

            classNode.methods.add(delegatorMethodNode);
        }

        private MethodNode redirectorMethod(MethodNode method, String newName) {
            MethodNode redirector = new MethodNode(ASM4, newName, method.desc, method.signature, exceptionArray(method));
            redirector.access = method.access & ~(ACC_NATIVE | ACC_ABSTRACT | ACC_FINAL);
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
                        fieldInsnNode.desc = remapType(fieldInsnNode.desc);//todo test
                        break;

                    case INVOKESTATIC:
                    case INVOKEDYNAMIC:
                    case INVOKEINTERFACE:
                    case INVOKESPECIAL:
                    case INVOKEVIRTUAL:
                        MethodInsnNode targetMethod = (MethodInsnNode) node;
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

            instructions.remove();

            // first, throw away arguments (ugh)
            for (Type type : reverse(Type.getArgumentTypes(targetMethod.desc))) {
                instructions.add(type.getSize() == 2 ? new InsnNode(POP2) : new InsnNode(POP));
            }

            instructions.add(new LdcInsnNode(targetMethod.owner)); // class name
            if (!isStatic) instructions.add(new InsnNode(SWAP));

            instructions.add(new LdcInsnNode(targetMethod.name));  // method name
            if (isStatic) {
                instructions.add(new InsnNode(ACONST_NULL));    // target object or null for static
            } else {
                instructions.add(new InsnNode(SWAP));
            }

            instructions.add(new LdcInsnNode(0));
            instructions.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
            instructions.add(new LdcInsnNode(0));
            instructions.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
            instructions.add(new MethodInsnNode(INVOKESTATIC,
                    Type.getType(RobolectricInternals.class).getInternalName(), "intercept",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));
            Type returnType = Type.getReturnType(targetMethod.desc);
            // todo: make this honor the return value if somebody cares about what intercept returns
            switch (returnType.getSort()) {
                case OBJECT:
                    instructions.add(new TypeInsnNode(CHECKCAST, remapType(returnType.getInternalName())));
                    break;
                case ARRAY:
                    instructions.add(new InsnNode(POP));
                    instructions.add(new InsnNode(ACONST_NULL));
                    break;
                case VOID:
                    instructions.add(new InsnNode(POP));
                    break;
                case Type.LONG:
                    instructions.add(new InsnNode(POP));
                    instructions.add(new InsnNode(LCONST_0));
                    break;
                case Type.FLOAT:
                    instructions.add(new InsnNode(POP));
                    instructions.add(new InsnNode(FCONST_0));
                    break;
                case Type.DOUBLE:
                    instructions.add(new InsnNode(POP));
                    instructions.add(new InsnNode(DCONST_0));
                    break;
                default:
                    instructions.add(new InsnNode(POP));
                    instructions.add(new InsnNode(ICONST_0));
                break;
            }
        }

        private void fixAccess(ClassNode clazz) {
            clazz.access = (clazz.access | ACC_PUBLIC) & ~(ACC_PROTECTED | ACC_PRIVATE);
        }

        private void fixAccess(MethodNode method) {
            method.access = (method.access | ACC_PUBLIC) & ~(ACC_PROTECTED | ACC_PRIVATE);
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

        private void generateCallToClassHandler(MethodNode method, String originalMethodName, MyGenerator m) {
            // prepare for call to classHandler.methodInvoked()
            m.push(classType);                                         // my class
            m.push(originalMethodName);                                // my class, method name
            m.loadThisOrNull();                                        // my class, method name, this
//
//            // load param types
            Type[] argumentTypes = Type.getArgumentTypes(method.desc);
            m.push(argumentTypes.length);
            m.newArray(STRING_TYPE);                                   // my class, method name, this, String[n]{nulls}
            for (int i = 0; i < argumentTypes.length; i++) {
                Type argumentType = argumentTypes[i];
                m.dup();
                m.push(i);
                m.push(argumentType.getClassName());
                m.arrayStore(STRING_TYPE);
            }
            // my class, method name, this, String[n]{param class names}

            m.loadArgArray();

            m.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, new Method("methodInvoked", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"));

            Type returnType = m.getReturnType();
            int sort = returnType.getSort();
            if (sort != VOID)
                if (sort == OBJECT || sort == ARRAY) {
                    m.checkCast(returnType);
                } else {
                    Label notNull = m.newLabel();
                    Label finished = m.newLabel();
                    m.dup();
                    m.ifNonNull(notNull);
//                if (returnType.getSize() == 2) {
//                    m.pop2();
//                } else {
                    m.pop();
//                }
                    m.pushZero(returnType);
                    m.goTo(finished);
                    m.visitLabel(notNull);
                    m.unbox(returnType);
                    m.visitLabel(finished);
                }
        }

        private boolean isEnum() {
            return (classNode.access & ACC_ENUM) != 0;
        }
    }

    private boolean shouldIntercept(MethodInsnNode targetMethod) {
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