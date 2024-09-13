package org.robolectric.annotation.processing.validator;

import static org.robolectric.annotation.processing.validator.ImplementationValidator.METHODS_ALLOWED_TO_BE_PUBLIC;

import com.google.auto.common.MoreElements;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.processing.DocumentedMethod;
import org.robolectric.annotation.processing.Helpers;
import org.robolectric.annotation.processing.RobolectricModel;

/** Validator that checks usages of {@link org.robolectric.annotation.Implements}. */
public class ImplementsValidator extends Validator {

  public static final String IMPLEMENTS_CLASS = "org.robolectric.annotation.Implements";
  public static final int MAX_SUPPORTED_ANDROID_SDK = 10000; // Now == Build.VERSION_CODES.O

  public static final String STATIC_INITIALIZER_METHOD_NAME = "__staticInitializer__";
  public static final String CONSTRUCTOR_METHOD_NAME = "__constructor__";

  private final ProcessingEnvironment env;
  private final SdkCheckMode sdkCheckMode;
  private final Kind checkKind;
  private final SdkStore sdkStore;
  private final boolean allowInDev;

  /** Supported modes for validation of {@link Implementation} methods against SDKs. */
  public enum SdkCheckMode {
    OFF,
    WARN,
    ERROR
  }

  public ImplementsValidator(
      RobolectricModel.Builder modelBuilder,
      ProcessingEnvironment env,
      SdkCheckMode sdkCheckMode,
      SdkStore sdkStore,
      boolean allowInDev) {
    super(modelBuilder, env, IMPLEMENTS_CLASS);

    this.env = env;
    this.sdkCheckMode = sdkCheckMode;
    this.checkKind = sdkCheckMode == SdkCheckMode.WARN ? Kind.WARNING : Kind.ERROR;
    this.sdkStore = sdkStore;
    this.allowInDev = allowInDev;
  }

  private TypeElement getClassNameTypeElement(AnnotationValue cv) {
    String className = Helpers.getAnnotationStringValue(cv);
    return elements.getTypeElement(className.replace('$', '.'));
  }

  public String sdkClassNameFq(AnnotationValue valueAttr, AnnotationValue classNameAttr) {
    String sdkClassNameFq;
    if (valueAttr == null) {
      sdkClassNameFq = Helpers.getAnnotationStringValue(classNameAttr);
    } else {
      TypeMirror typeMirror = Helpers.getAnnotationTypeMirrorValue(valueAttr);
      TypeElement typeElement = MoreElements.asType(types.asElement(typeMirror));
      sdkClassNameFq = elements.getBinaryName(typeElement).toString();
    }
    return sdkClassNameFq;
  }

  @Override
  public Void visitType(TypeElement shadowType, Element parent) {
    captureJavadoc(shadowType);

    // inner class shadows must be static
    if (shadowType.getEnclosingElement().getKind() == ElementKind.CLASS
        && !shadowType.getModifiers().contains(Modifier.STATIC)) {

      error("inner shadow classes must be static");
    }

    // Don't import nested classes because some of them have the same name.
    AnnotationMirror am = getCurrentAnnotation();
    AnnotationValue av = Helpers.getAnnotationTypeMirrorValue(am, "value");
    AnnotationValue cv = Helpers.getAnnotationTypeMirrorValue(am, "className");

    AnnotationValue minSdkVal = Helpers.getAnnotationTypeMirrorValue(am, "minSdk");
    int minSdk = minSdkVal == null ? -1 : Helpers.getAnnotationIntValue(minSdkVal);
    AnnotationValue maxSdkVal = Helpers.getAnnotationTypeMirrorValue(am, "maxSdk");
    int maxSdk = maxSdkVal == null ? -1 : Helpers.getAnnotationIntValue(maxSdkVal);

    AnnotationValue shadowPickerValue = Helpers.getAnnotationTypeMirrorValue(am, "shadowPicker");

    TypeElement shadowPickerTypeElement =
        shadowPickerValue == null
            ? null
            : (TypeElement)
                types.asElement(Helpers.getAnnotationTypeMirrorValue(shadowPickerValue));

    TypeElement actualType = null;
    if (av == null) {
      if (cv == null) {
        error("@Implements: must specify <value> or <className>");
        return null;
      }
      actualType = getClassNameTypeElement(cv);
    } else {
      TypeMirror value = Helpers.getAnnotationTypeMirrorValue(av);
      if (value == null) {
        return null;
      }
      if (cv != null) {
        error("@Implements: cannot specify both <value> and <className> attributes");
      } else {
        actualType = Helpers.getAnnotationTypeMirrorValue(types.asElement(value));
      }
    }
    // Checking for a public modifier here is a bit of a hack to prevent extraneous imports
    // from appearing in the generated files.
    // The version check is even more of a hack, and should be revisited as the
    // output in Robolectric_ShadowPickers.java makes little to no sense.
    if (actualType == null
        || !actualType.getModifiers().contains(Modifier.PUBLIC)
        || (maxSdk != -1 && maxSdk < MAX_SUPPORTED_ANDROID_SDK)) {
      addShadowNotInSdk(shadowType, av, cv, shadowPickerTypeElement);
    } else {
      modelBuilder.addShadowType(shadowType, actualType, shadowPickerTypeElement);
    }

    AnnotationValue looseSignaturesAttr =
        Helpers.getAnnotationTypeMirrorValue(am, "looseSignatures");
    boolean looseSignatures =
        looseSignaturesAttr != null && (Boolean) looseSignaturesAttr.getValue();
    String sdkClassNameFq = sdkClassNameFq(av, cv);
    validateShadow(sdkClassNameFq, shadowType, minSdk, maxSdk, looseSignatures, allowInDev);

    return null;
  }

  private void addShadowNotInSdk(
      TypeElement shadowType,
      AnnotationValue valueAttr,
      AnnotationValue classNameAttr,
      TypeElement shadowPickerTypeElement) {

    String sdkClassNameFq;
    if (valueAttr == null) {
      sdkClassNameFq = Helpers.getAnnotationStringValue(classNameAttr);
    } else {
      TypeMirror typeMirror = Helpers.getAnnotationTypeMirrorValue(valueAttr);
      TypeElement typeElement = MoreElements.asType(types.asElement(typeMirror));
      sdkClassNameFq = elements.getBinaryName(typeElement).toString();
    }

    // there's no such type at the current SDK level, so just use strings...
    // getQualifiedName() uses Outer.Inner and we want Outer$Inner, so:
    String name = getClassFQName(shadowType);
    // SHADOW_MAP currently uses class dot syntax for keys, but SHADOW_PICKER_MAP uses
    // FQ syntax for keys.
    modelBuilder.addExtraShadow(sdkClassNameFq.replace('$', '.'), name);
    if (shadowPickerTypeElement != null) {
      modelBuilder.addExtraShadowPicker(sdkClassNameFq, shadowPickerTypeElement);
    }
  }

  static String getClassFQName(TypeElement elem) {
    StringBuilder name = new StringBuilder();
    while (isClassy(elem.getEnclosingElement().getKind())) {
      name.insert(0, "$" + elem.getSimpleName());
      elem = (TypeElement) elem.getEnclosingElement();
    }
    name.insert(0, elem.getQualifiedName());
    return name.toString();
  }

  private static boolean isClassy(ElementKind kind) {
    return kind == ElementKind.CLASS || kind == ElementKind.INTERFACE;
  }

  private void validateShadow(
      String shadowedClassName,
      TypeElement shadowClassElem,
      int classMinSdk,
      int classMaxSdk,
      boolean looseSignatures,
      boolean allowInDev) {
    Problems problems = new Problems(this.checkKind);
    if (sdkCheckMode != SdkCheckMode.OFF) {
      for (SdkStore.Sdk sdk : sdkStore.sdksMatching(classMinSdk, classMaxSdk)) {
        SdkStore.ClassInfo classInfo = sdk.getClassInfo(shadowedClassName);
        if (classInfo == null) {
          if (!sdk.suppressWarnings(
              shadowClassElem, "robolectric.internal.IgnoreMissingClass", allowInDev)) {
            problems.add("Shadowed type is not found: " + shadowedClassName, sdk.sdkInt);
          }
        } else {
          StringBuilder builder = new StringBuilder();
          helpers.appendParameterList(builder, shadowClassElem.getTypeParameters());
          String shadowParams = builder.toString();
          if (!classInfo.getSignature().equals(shadowParams)
              && !sdk.suppressWarnings(shadowClassElem, "robolectric.mismatchedTypes", allowInDev)
              && !looseSignatures) {
            problems.add(
                "Shadow type is mismatched, expected "
                    + shadowParams
                    + " but found "
                    + classInfo.getSignature(),
                sdk.sdkInt);
          }
        }
      }
    }
    problems.recount(messager, shadowClassElem);
    for (Element memberElement : ElementFilter.methodsIn(shadowClassElem.getEnclosedElements())) {
      ExecutableElement methodElement = MoreElements.asExecutable(memberElement);

      // equals, hashCode, and toString are exempt, because of Robolectric's weird special behavior
      if (METHODS_ALLOWED_TO_BE_PUBLIC.contains(methodElement.getSimpleName().toString())) {
        continue;
      }

      verifySdkMethod(shadowedClassName, methodElement, classMinSdk, classMaxSdk, looseSignatures);
      if (shadowClassElem.getQualifiedName().toString().startsWith("org.robolectric")
          && !methodElement.getModifiers().contains(Modifier.ABSTRACT)) {
        checkForMissingImplementationAnnotation(
            shadowedClassName, methodElement, classMinSdk, classMaxSdk, looseSignatures);
      }

      String methodName = methodElement.getSimpleName().toString();
      if (methodName.equals(CONSTRUCTOR_METHOD_NAME)
          || methodName.equals(STATIC_INITIALIZER_METHOD_NAME)) {
        Implementation implementation = memberElement.getAnnotation(Implementation.class);
        if (implementation == null) {
          messager.printMessage(
              Kind.ERROR, "Shadow methods must be annotated @Implementation", methodElement);
        }
      }
    }
  }

  private void verifySdkMethod(
      String sdkClassName,
      ExecutableElement methodElement,
      int classMinSdk,
      int classMaxSdk,
      boolean looseSignatures) {
    if (sdkCheckMode == SdkCheckMode.OFF) {
      return;
    }

    Implementation implementation = methodElement.getAnnotation(Implementation.class);
    if (implementation != null) {
      Problems problems = new Problems(this.checkKind);

      for (SdkStore.Sdk sdk : sdkStore.sdksMatching(implementation, classMinSdk, classMaxSdk)) {
        String problem = sdk.verifyMethod(sdkClassName, methodElement, looseSignatures, allowInDev);
        if (problem != null) {
          problems.add(problem, sdk.sdkInt);
        }
      }

      if (problems.any()) {
        problems.recount(messager, methodElement);
      }
    }
  }

  /**
   * For the given {@link ExecutableElement}, check to see if it should have a {@link
   * Implementation} tag but is missing one
   */
  private void checkForMissingImplementationAnnotation(
      String sdkClassName,
      ExecutableElement methodElement,
      int classMinSdk,
      int classMaxSdk,
      boolean looseSignatures) {

    if (sdkCheckMode == SdkCheckMode.OFF) {
      return;
    }

    Implementation implementation = methodElement.getAnnotation(Implementation.class);
    if (implementation == null) {
      Kind kind = sdkCheckMode == SdkCheckMode.WARN ? Kind.WARNING : Kind.ERROR;
      Problems problems = new Problems(kind);

      for (SdkStore.Sdk sdk : sdkStore.sdksMatching(implementation, classMinSdk, classMaxSdk)) {
        String problem = sdk.verifyMethod(sdkClassName, methodElement, looseSignatures, allowInDev);
        if (problem == null && sdk.getClassInfo(sdkClassName) != null) {
          problems.add(
              "Missing @Implementation on method " + methodElement.getSimpleName(), sdk.sdkInt);
        }
      }

      if (problems.any()) {
        problems.recount(messager, methodElement);
      }
    }
  }

  private void captureJavadoc(TypeElement elem) {
    List<String> imports = new ArrayList<>();
    try {
      List<? extends ImportTree> importLines =
          Trees.instance(env).getPath(elem).getCompilationUnit().getImports();
      for (ImportTree importLine : importLines) {
        imports.add(importLine.getQualifiedIdentifier().toString());
      }
    } catch (IllegalArgumentException e) {
      // Trees relies on javac APIs and is not available in all annotation processing
      // implementations
    }

    List<TypeElement> enclosedTypes = ElementFilter.typesIn(elem.getEnclosedElements());
    for (TypeElement enclosedType : enclosedTypes) {
      imports.add(enclosedType.getQualifiedName().toString());
    }

    Elements elementUtils = env.getElementUtils();
    modelBuilder.documentType(elem, elementUtils.getDocComment(elem), imports);

    for (Element memberElement : ElementFilter.methodsIn(elem.getEnclosedElements())) {
      try {
        ExecutableElement methodElement = (ExecutableElement) memberElement;
        Implementation implementation = memberElement.getAnnotation(Implementation.class);

        DocumentedMethod documentedMethod = new DocumentedMethod(memberElement.toString());
        for (Modifier modifier : memberElement.getModifiers()) {
          documentedMethod.modifiers.add(modifier.toString());
        }
        documentedMethod.isImplementation = implementation != null;
        if (implementation != null) {
          documentedMethod.minSdk = sdkOrNull(implementation.minSdk());
          documentedMethod.maxSdk = sdkOrNull(implementation.maxSdk());
        }
        for (VariableElement variableElement : methodElement.getParameters()) {
          documentedMethod.params.add(variableElement.toString());
        }
        documentedMethod.returnType = methodElement.getReturnType().toString();
        for (TypeMirror typeMirror : methodElement.getThrownTypes()) {
          documentedMethod.exceptions.add(typeMirror.toString());
        }
        String docMd = elementUtils.getDocComment(methodElement);
        if (docMd != null) {
          documentedMethod.setDocumentation(docMd);
        }

        modelBuilder.documentMethod(elem, documentedMethod);
      } catch (Exception e) {
        throw new RuntimeException(
            "failed to capture javadoc for " + elem + "." + memberElement, e);
      }
    }
  }

  private Integer sdkOrNull(int sdk) {
    return sdk == -1 ? null : sdk;
  }

  private static class Problems {
    private final Kind kind;
    private final Map<String, Set<Integer>> problems = new HashMap<>();

    public Problems(Kind kind) {
      this.kind = kind;
    }

    void add(String problem, int sdkInt) {
      Set<Integer> sdks = problems.get(problem);
      if (sdks == null) {
        problems.put(problem, sdks = new TreeSet<>());
      }
      sdks.add(sdkInt);
    }

    boolean any() {
      return !problems.isEmpty();
    }

    void recount(Messager messager, Element element) {
      for (Entry<String, Set<Integer>> e : problems.entrySet()) {
        String problem = e.getKey();
        Set<Integer> sdks = e.getValue();

        StringBuilder buf = new StringBuilder();
        buf.append(problem).append(" for ").append(sdks.size() == 1 ? "SDK " : "SDKs ");

        Integer previousSdk = null;
        Integer lastSdk = null;
        for (Integer sdk : sdks) {
          if (previousSdk == null) {
            buf.append(sdk);
          } else {
            if (previousSdk != sdk - 1) {
              buf.append("-").append(previousSdk);
              buf.append("/").append(sdk);
              lastSdk = null;
            } else {
              lastSdk = sdk;
            }
          }

          previousSdk = sdk;
        }

        if (lastSdk != null) {
          buf.append("-").append(lastSdk);
        }

        messager.printMessage(kind, buf.toString(), element);
      }
    }
  }
}
