package org.robolectric.annotation.processing.validator;

import static com.google.auto.common.MoreTypes.asTypeElement;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.robolectric.annotation.processing.RobolectricModel;

/** Validator that checks usages of {@link org.robolectric.annotation.Implementation}. */
public class ImplementationValidator extends FoundOnImplementsValidator {
  public static final ImmutableSet<String> METHODS_ALLOWED_TO_BE_PUBLIC =
      ImmutableSet.of("toString", "hashCode", "equals");

  public ImplementationValidator(RobolectricModel.Builder modelBuilder, ProcessingEnvironment env) {
    super(modelBuilder, env, "org.robolectric.annotation.Implementation");
  }

  @Override
  public Void visitExecutable(ExecutableElement elem, TypeElement parent) {
    Set<Modifier> modifiers = elem.getModifiers();
    if (!METHODS_ALLOWED_TO_BE_PUBLIC.contains(elem.getSimpleName().toString())) {
      if (!modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PROTECTED)) {
        message(
            Kind.ERROR,
            "@Implementation methods should be protected (preferred) or public (deprecated)");
      }
    }

    boolean hasFilterAnnotation = false;

    for (AnnotationMirror mirror : elem.getAnnotationMirrors()) {
      if (asTypeElement(mirror.getAnnotationType())
          .getQualifiedName()
          .contentEquals("org.robolectric.annotation.Filter")) {
        hasFilterAnnotation = true;
      }
    }

    if (hasFilterAnnotation) {
      message(Kind.ERROR, "@Implementation and @Filter cannot be present on the same method");
    }

    // TODO: Check that it has the right signature
    return null;
  }
}
