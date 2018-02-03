package org.robolectric.internal.bytecode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public abstract class ClassNodeProvider {
  private final Map<String, ClassNode> classNodes = new ConcurrentHashMap<>();

  protected abstract byte[] getClassBytes(String className) throws ClassNotFoundException;

  ClassNode getClassNode(String internalClassName) throws ClassNotFoundException {
    ClassNode classNode = classNodes.get(internalClassName);
    if (classNode == null) {
      classNode = createClassNode(internalClassName);
      classNodes.put(internalClassName, classNode);
    }
    return classNode;
  }

  private ClassNode createClassNode(String internalClassName) throws ClassNotFoundException {
    byte[] byteCode = getClassBytes(internalClassName);
    ClassReader classReader = new ClassReader(byteCode);
    ClassNode classNode = new ClassNode();
    classReader.accept(classNode,
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    return classNode;
  }

}
