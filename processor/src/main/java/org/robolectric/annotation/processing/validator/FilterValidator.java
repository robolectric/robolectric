package org.robolectric.annotation.processing.validator;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.robolectric.annotation.processing.RobolectricModel;

/** Validator that checks usages of {@link org.robolectric.annotation.Filter}. */
public class FilterValidator extends FoundOnImplementsValidator {
  public static final ImmutableSet<String> METHODS_ALLOWED_TO_BE_PUBLIC =
      ImmutableSet.of("toString", "hashCode", "equals");

  public FilterValidator(RobolectricModel.Builder modelBuilder, ProcessingEnvironment env) {
    super(modelBuilder, env, "org.robolectric.annotation.Filter");
  }

  @Override
  public Void visitExecutable(ExecutableElement elem, TypeElement parent) {
    Set<Modifier> modifiers = elem.getModifiers();
    if (!METHODS_ALLOWED_TO_BE_PUBLIC.contains(elem.getSimpleName().toString())) {
      if (!modifiers.contains(Modifier.PROTECTED)) {
        message(Kind.ERROR, "@Filter methods must be protected");
      }
    }

    return null;
  }
}
