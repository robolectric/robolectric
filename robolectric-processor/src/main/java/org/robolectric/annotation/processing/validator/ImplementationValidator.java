package org.robolectric.annotation.processing.validator;

import org.robolectric.annotation.processing.RoboModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class ImplementationValidator extends FoundOnImplementsValidator {
  public ImplementationValidator(RoboModel model, ProcessingEnvironment env) {
    super(model, env, "org.robolectric.annotation.Implementation");
  }

  @Override
  public Void visitExecutable(ExecutableElement elem, TypeElement parent) {
    // TODO: Check that it has the right signature
    return null;
  }
}
