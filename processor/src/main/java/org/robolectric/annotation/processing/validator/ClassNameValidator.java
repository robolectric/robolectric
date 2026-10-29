package org.robolectric.annotation.processing.validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.robolectric.annotation.processing.Helpers;
import org.robolectric.annotation.processing.RobolectricModel;

public class ClassNameValidator extends Validator {
  private final SdkStore.@NonNull Sdk sdk;

  public ClassNameValidator(
      RobolectricModel.@NonNull Builder modelBuilder,
      @NonNull ProcessingEnvironment env,
      @NonNull SdkStore sdkStore) {
    super(modelBuilder, env, "org.robolectric.annotation.ClassName");
    sdk = sdkStore.minSdk();
  }

  @Override
  public Void visitExecutable(ExecutableElement e, Element p) {
    TypeMirror expectedAnnotationType = annotationType.asType();
    for (AnnotationMirror annotationMirror : e.getReturnType().getAnnotationMirrors()) {
      if (types.isSameType(annotationMirror.getAnnotationType(), expectedAnnotationType)) {
        checkClassName(annotationMirror);
      }
    }
    return null;
  }

  @Override
  public Void visitVariable(VariableElement e, Element p) {
    checkClassName(getCurrentAnnotation());
    return null;
  }

  private void checkClassName(@Nullable AnnotationMirror annotation) {
    String className = getClassNameFromAnnotation(annotation);
    if (className == null) {
      return;
    }
    TypeElement typeElement = elements.getTypeElement(className.replace('$', '.'));
    if (isClassAvailable(typeElement)) {
      error("Use " + className + " directly, instead of @ClassName(\"" + className + "\")");
    }
  }

  private @Nullable String getClassNameFromAnnotation(@Nullable AnnotationMirror annotation) {
    if (annotation == null) {
      return null;
    }
    AnnotationValue classNameValue = Helpers.getAnnotationTypeMirrorValue(annotation, "value");
    if (classNameValue == null) {
      return null;
    }
    return Helpers.getAnnotationStringValue(classNameValue);
  }

  private boolean isClassAvailable(Element element) {
    return element != null && sdk.getClassInfo(element.toString()) != null;
  }
}
