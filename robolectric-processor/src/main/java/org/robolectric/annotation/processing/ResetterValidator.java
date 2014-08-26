package org.robolectric.annotation.processing;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class ResetterValidator extends FoundOnImplementsValidator {
  public ResetterValidator(RoboModel model, ProcessingEnvironment env) {
    super(model, env, "org.robolectric.annotation.Resetter");
  }

  @Override
  public Void visitExecutable(ExecutableElement elem, TypeElement parent) {
   if (imp != null) {
      final Set<Modifier> modifiers = elem.getModifiers();
      boolean error = false;
      if (!modifiers.contains(Modifier.STATIC)) {
        error("@Resetter methods must be static");
        error = true;
      }
      if (!modifiers.contains(Modifier.PUBLIC)) {
        error("@Resetter methods must be public");
        error = true;
      }
      List<? extends VariableElement> params = elem.getParameters();
      if (params != null && !params.isEmpty()) {
        error("@Resetter methods must not have parameters");
        error = true;
      }
      if (!error) {
        model.resetterMap.put(parent, elem);
      }
    }
    return null;
  }
}