package org.robolectric.internal.bytecode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

abstract class ClassNodeProvider {
  private final Map<String, ClassNode> classNodes = new ConcurrentHashMap<>();

  abstract byte[] getClassBytes(String className) throws ClassNotFoundException;

  ClassNode getClassNode(String className) throws ClassNotFoundException {
    ClassNode classNode = classNodes.get(className);
    if (classNode == null) {
      classNode = createClassNode(className);
      classNodes.put(className, classNode);
    }
    return classNode;
  }

  private ClassNode createClassNode(String className) throws ClassNotFoundException {
    byte[] byteCode = getClassBytes(className);
    ClassReader classReader = new ClassReader(byteCode);
    ClassNode classNode = new ClassNode();
    classReader.accept(classNode,
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    return classNode;
  }

}
