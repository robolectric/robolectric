package org.robolectric.annotation.processing;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class RealObjectValidator extends FoundOnImplementsValidator {

  public RealObjectValidator(RoboModel model, ProcessingEnvironment env) {
    super(model, env, "org.robolectric.annotation.RealObject");
  }

  @Override
  public Void visitVariable(VariableElement elem, TypeElement parent) {
    TypeMirror impClass = model.getImplementedClass(imp);
    if (impClass != null && !types.isAssignable(impClass, elem.asType())) {
      error("@RealObject with type <" + elem.asType() + ">; expected <" + impClass + '>');
    }
    return null;
  }
}
