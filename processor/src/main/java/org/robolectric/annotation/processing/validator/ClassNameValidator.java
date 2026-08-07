package org.robolectric.annotation.processing.validator;

import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.processing.Helpers;
import org.robolectric.annotation.processing.RobolectricModel;

/**
 * Reports {@code @ClassName} usages whose class could be referenced directly instead.
 *
 * <p>A class can be referenced directly only if it is on the compile classpath, and if it exists
 * and is accessible from the annotated element in every SDK the enclosing shadow applies to.
 * Robolectric resolves all the method signatures of a shadow on each of these SDKs, so referencing
 * a class that is missing from one of them would fail with a {@link NoClassDefFoundError}.
 *
 * <p>Usages on public members are not reported, because these members can be called from projects
 * compiled against the public Android SDK stubs, which don't contain hidden classes.
 */
public class ClassNameValidator extends Validator {
  private final SdkStore sdkStore;

  public ClassNameValidator(
      RobolectricModel.@NonNull Builder modelBuilder,
      @NonNull ProcessingEnvironment env,
      @NonNull SdkStore sdkStore) {
    super(modelBuilder, env, "org.robolectric.annotation.ClassName");
    this.sdkStore = sdkStore;
  }

  @Override
  public Void visitExecutable(ExecutableElement e, Element p) {
    TypeMirror expectedAnnotationType = annotationType.asType();
    for (AnnotationMirror annotationMirror : e.getReturnType().getAnnotationMirrors()) {
      if (types.isSameType(annotationMirror.getAnnotationType(), expectedAnnotationType)) {
        checkClassName(e, annotationMirror);
      }
    }
    return null;
  }

  @Override
  public Void visitVariable(VariableElement e, Element p) {
    checkClassName(e, getCurrentAnnotation());
    return null;
  }

  private void checkClassName(@NonNull Element element, @Nullable AnnotationMirror annotation) {
    String className = getClassNameFromAnnotation(annotation);
    if (className == null || isPublicMember(element)) {
      return;
    }
    if (elements.getTypeElement(className.replace('$', '.')) == null) {
      // The class is not on the compile classpath, so it can't be referenced directly.
      return;
    }
    if (isClassAvailableInAllSdks(element, className)) {
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

  /**
   * Returns whether {@code element} is a public member, or a parameter of a public method. Their
   * signatures are part of the public shadow API.
   */
  private static boolean isPublicMember(@NonNull Element element) {
    Element member =
        element.getKind() == ElementKind.PARAMETER ? element.getEnclosingElement() : element;
    return member.getModifiers().contains(Modifier.PUBLIC);
  }

  /**
   * Returns whether {@code className} exists and is accessible from {@code element} in every SDK
   * the enclosing shadow applies to. Returns {@code false} if no SDK matches, as availability can't
   * be confirmed then.
   */
  private boolean isClassAvailableInAllSdks(@NonNull Element element, @NonNull String className) {
    Implements implementsAnnotation = findImplementsAnnotation(element);
    int minSdk = implementsAnnotation == null ? -1 : implementsAnnotation.minSdk();
    int maxSdk = implementsAnnotation == null ? -1 : implementsAnnotation.maxSdk();
    List<SdkStore.Sdk> sdks = sdkStore.sdksMatching(minSdk, maxSdk);
    if (sdks.isEmpty()) {
      return false;
    }
    String packageName = elements.getPackageOf(element).getQualifiedName().toString();
    for (SdkStore.Sdk sdk : sdks) {
      if (!isClassAccessible(sdk, className, packageName)) {
        return false;
      }
    }
    return true;
  }

  private static @Nullable Implements findImplementsAnnotation(@NonNull Element element) {
    for (Element current = element; current != null; current = current.getEnclosingElement()) {
      Implements implementsAnnotation = current.getAnnotation(Implements.class);
      if (implementsAnnotation != null) {
        return implementsAnnotation;
      }
    }
    return null;
  }

  /**
   * Returns whether {@code className}, and all its enclosing classes, exist in {@code sdk} and are
   * accessible from {@code packageName}.
   */
  private static boolean isClassAccessible(
      SdkStore.@NonNull Sdk sdk, @NonNull String className, @NonNull String packageName) {
    int lastDot = className.lastIndexOf('.');
    String classPackageName = lastDot == -1 ? "" : className.substring(0, lastDot);
    boolean samePackage = classPackageName.equals(packageName);
    String currentClassName = className;
    while (true) {
      SdkStore.ClassInfo classInfo = sdk.getClassInfo(currentClassName);
      if (classInfo == null) {
        return false;
      }
      int access = classInfo.getAccess();
      boolean accessible =
          (access & Opcodes.ACC_PUBLIC) != 0
              || (samePackage && (access & Opcodes.ACC_PRIVATE) == 0);
      if (!accessible) {
        return false;
      }
      int lastDollar = currentClassName.lastIndexOf('$');
      if (lastDollar <= lastDot) {
        return true;
      }
      currentClassName = currentClassName.substring(0, lastDollar);
    }
  }
}
