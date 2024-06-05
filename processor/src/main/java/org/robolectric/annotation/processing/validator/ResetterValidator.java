package org.robolectric.annotation.processing.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.robolectric.annotation.processing.RobolectricModel;

/** Validator that checks usages of {@link org.robolectric.annotation.Resetter}. */
public class ResetterValidator extends FoundOnImplementsValidator {

  private final Map<TypeElement, ExecutableElement> resetterMethodsByClass = new HashMap<>();

  public ResetterValidator(RobolectricModel.Builder modelBuilder, ProcessingEnvironment env) {
    super(modelBuilder, env, "org.robolectric.annotation.Resetter");
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
      if (resetterMethodsByClass.containsKey(parent)) {
        error(
            String.format(
                Locale.US,
                "Duplicate @Resetter methods found on %s: %s() and %s(). Only one @Resetter method"
                    + " is permitted on each shadow.",
                parent.getQualifiedName(),
                resetterMethodsByClass.get(parent).getSimpleName(),
                elem.getSimpleName()));
        error = true;
      }
      if (!error) {
        resetterMethodsByClass.put(parent, elem);
        modelBuilder.addResetter(parent, elem);
      }
    }
    return null;
  }
}
