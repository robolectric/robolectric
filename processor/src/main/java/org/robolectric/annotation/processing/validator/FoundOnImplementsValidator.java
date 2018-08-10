package org.robolectric.annotation.processing.validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.robolectric.annotation.processing.Helpers;
import org.robolectric.annotation.processing.RobolectricModel;

/**
 * Validator that checks usages of {@link org.robolectric.annotation.Implements}.
 */
public abstract class FoundOnImplementsValidator extends Validator {

  private final TypeElement implementsType =
      elements.getTypeElement(ImplementsValidator.IMPLEMENTS_CLASS);

  protected AnnotationMirror imp;
  
  public FoundOnImplementsValidator(RobolectricModel.Builder modelBuilder,
      ProcessingEnvironment env,
      String annotationType) {
    super(modelBuilder, env, annotationType);
  }

  @Override
  public void init(Element elem, Element p) {
    super.init(elem, p);

    do {
      imp = Helpers.getImplementsMirror(p, types, implementsType);

      // if not found, search on superclasses too...
      if (imp == null) {
        TypeMirror superclass = ((TypeElement) p).getSuperclass();
        p = superclass == null ? null : types.asElement(superclass);
      } else {
        break;
      }
    } while (p != null);

    if (imp == null) {
      error('@' + annotationType.getSimpleName().toString() + " without @Implements");
    }
  }
  
  @Override
  final public Void visitVariable(VariableElement elem, Element parent) {
    return visitVariable(elem, Helpers.getAnnotationTypeMirrorValue(parent));
  }
  
  public Void visitVariable(VariableElement elem, TypeElement parent) {
    return null;
  }

  @Override
  final public Void visitExecutable(ExecutableElement elem, Element parent) {
    return visitExecutable(elem, Helpers.getAnnotationTypeMirrorValue(parent));
  }

  public Void visitExecutable(ExecutableElement elem, TypeElement parent) {
    return null;
  }
}
