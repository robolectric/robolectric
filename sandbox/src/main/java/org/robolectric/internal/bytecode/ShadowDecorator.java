package org.robolectric.internal.bytecode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class ShadowDecorator implements ClassInstrumentor.Decorator {
  private static final String OBJECT_DESC = Type.getDescriptor(Object.class);
  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final String GET_ROBO_DATA_SIGNATURE = "()Ljava/lang/Object;";

  @Override
  public void decorate(ClassInstrumentor.Subject subject) {
    subject.addInterface(Type.getInternalName(ShadowedObject.class));

    subject.addField(0, new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
        ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));

    addRoboGetDataMethod(subject);
  }

  private void addRoboGetDataMethod(ClassInstrumentor.Subject subject) {
    MethodNode initMethodNode = new MethodNode(Opcodes.ACC_PUBLIC, ShadowConstants.GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE, null, null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
    generator.loadThis();                                         // this
    generator.getField(subject.classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
    generator.returnValue();
    generator.endMethod();
    subject.addMethod(initMethodNode);
  }
}
