package org.robolectric.internal.bytecode;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

interface ClassNodeProvider {

  byte[] getClassBytes(String className) throws ClassNotFoundException;

  default ClassNode getClassNode(String className) throws ClassNotFoundException {
    byte[] byteCode = getClassBytes(className);
    ClassReader classReader = new ClassReader(byteCode);
    ClassNode classNode = new ClassNode();
    // perf TODO: we should be able to call `accept()` with `ClassReader.SKIP_CODE`:
    classReader.accept(classNode, 0);
    return classNode;
  }

}
