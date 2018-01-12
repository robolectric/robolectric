package org.robolectric.internal.bytecode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

public class ShadowDecorator implements ClassInstrumentor.Decorator {
  private static final String OBJECT_DESC = Type.getDescriptor(Object.class);

  @Override
  public void decorate(ClassInstrumentor.Subject subject) {
    subject.addInterface(Type.getInternalName(ShadowedObject.class));

    subject.addField(0, new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
        ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));
  }
}
