package org.robolectric.annotation.processing.validator;

import static org.robolectric.annotation.processing.validator.ImplementationValidator.METHODS_ALLOWED_TO_BE_PUBLIC;

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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.processing.DocumentedMethod;
import org.robolectric.annotation.processing.Helpers;
import org.robolectric.annotation.processing.RobolectricModel;

/**
 * Validator that checks usages of {@link org.robolectric.annotation.Implements}.
 */
public class ImplementsValidator extends Validator {

  public static final String IMPLEMENTS_CLASS = "org.robolectric.annotation.Implements";
  public static final int MAX_SUPPORTED_ANDROID_SDK = 10000; // Now == Build.VERSION_CODES.O

  public static final String STATIC_INITIALIZER_METHOD_NAME = "__staticInitializer__";
  public static final String CONSTRUCTOR_METHOD_NAME = "__constructor__";

  private static final SdkStore sdkStore = new SdkStore();

  private final ProcessingEnvironment env;
  private final SdkCheckMode sdkCheckMode;

  /**
   * Supported modes for validation of {@link Implementation} methods against SDKs.
   */
  public enum SdkCheckMode {
    OFF,
    WARN,
    ERROR
  }

  public ImplementsValidator(RobolectricModel.Builder modelBuilder, ProcessingEnvironment env,
      SdkCheckMode sdkCheckMode) {
    super(modelBuilder, env, IMPLEMENTS_CLASS);

    this.env = env;
    this.sdkCheckMode = sdkCheckMode;
  }

  private TypeElement getClassNameTypeElement(AnnotationValue cv) {
    String className = Helpers.getAnnotationStringValue(cv);
    return elements.getTypeElement(className.replace('$', '.'));
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

    AnnotationValue shadowPickerValue =
        Helpers.getAnnotationTypeMirrorValue(am, "shadowPicker");
    TypeMirror shadowPickerTypeMirror = shadowPickerValue == null
        ? null
        : Helpers.getAnnotationTypeMirrorValue(shadowPickerValue);

    // This shadow doesn't apply to the current SDK. todo: check each SDK.
    if (maxSdk != -1 && maxSdk < MAX_SUPPORTED_ANDROID_SDK) {
      addShadowNotInSdk(shadowType, av, cv);
      return null;
    }

    TypeElement actualType = null;
    if (av == null) {
      if (cv == null) {
        error("@Implements: must specify <value> or <className>");
        return null;
      }
      actualType = getClassNameTypeElement(cv);

      if (actualType == null
          && !suppressWarnings(shadowType, "robolectric.internal.IgnoreMissingClass")) {
        error("@Implements: could not resolve class <" + cv + '>', cv);
        return null;
      }
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
    if (actualType == null) {
      addShadowNotInSdk(shadowType, av, cv);
      return null;
    }
    final List<? extends TypeParameterElement> typeTP = actualType.getTypeParameters();
    final List<? extends TypeParameterElement> elemTP = shadowType.getTypeParameters();
    if (!helpers.isSameParameterList(typeTP, elemTP)) {
      StringBuilder message = new StringBuilder();
      if (elemTP.isEmpty()) {
        message.append("Shadow type is missing type parameters, expected <");
        helpers.appendParameterList(message, actualType.getTypeParameters());
        message.append('>');
      } else if (typeTP.isEmpty()) {
        message.append("Shadow type has type parameters but real type does not");
      } else {
        message.append("Shadow type must have same type parameters as its real counterpart: expected <");
        helpers.appendParameterList(message, actualType.getTypeParameters());
        message.append(">, was <");
        helpers.appendParameterList(message, shadowType.getTypeParameters());
        message.append('>');
      }
      messager.printMessage(Kind.ERROR, message, shadowType);
      return null;
    }

    AnnotationValue looseSignaturesAttr =
        Helpers.getAnnotationTypeMirrorValue(am, "looseSignatures");
    boolean looseSignatures =
        looseSignaturesAttr == null ? false : (Boolean) looseSignaturesAttr.getValue();
    validateShadowMethods(actualType, shadowType, minSdk, maxSdk, looseSignatures);

    modelBuilder.addShadowType(shadowType, actualType,
        shadowPickerTypeMirror == null
            ? null
            : (TypeElement) types.asElement(shadowPickerTypeMirror));
    return null;
  }

  private void addShadowNotInSdk(TypeElement shadowType, AnnotationValue av, AnnotationValue cv) {
    String sdkClassName;
    if (av == null) {
      sdkClassName = Helpers.getAnnotationStringValue(cv).replace('$', '.');
    } else {
      sdkClassName = av.toString();
    }

    // there's no such type at the current SDK level, so just use strings...
    // getQualifiedName() uses Outer.Inner and we want Outer$Inner, so:
    String name = getClassFQName(shadowType);
    modelBuilder.addExtraShadow(sdkClassName, name);
  }

  private static boolean suppressWarnings(Element element, String warningName) {
    SuppressWarnings[] suppressWarnings = element.getAnnotationsByType(SuppressWarnings.class);
    for (SuppressWarnings suppression : suppressWarnings) {
      for (String name : suppression.value()) {
        if (warningName.equals(name)) {
          return true;
        }
      }
    }
    return false;
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

  private void validateShadowMethods(TypeElement sdkClassElem, TypeElement shadowClassElem,
      int classMinSdk, int classMaxSdk, boolean looseSignatures) {
    for (Element memberElement : ElementFilter.methodsIn(shadowClassElem.getEnclosedElements())) {
      ExecutableElement methodElement = (ExecutableElement) memberElement;

      // equals, hashCode, and toString are exempt, because of Robolectric's weird special behavior
      if (METHODS_ALLOWED_TO_BE_PUBLIC.contains(methodElement.getSimpleName().toString())) {
        continue;
      }

      verifySdkMethod(sdkClassElem, methodElement, classMinSdk, classMaxSdk, looseSignatures);

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

  private void verifySdkMethod(TypeElement sdkClassElem, ExecutableElement methodElement,
      int classMinSdk, int classMaxSdk, boolean looseSignatures) {
    if (sdkCheckMode == SdkCheckMode.OFF) {
      return;
    }

    Implementation implementation = methodElement.getAnnotation(Implementation.class);
    if (implementation != null) {
      Kind kind = sdkCheckMode == SdkCheckMode.WARN
          ? Kind.WARNING
          : Kind.ERROR;
      Problems problems = new Problems(kind);

      for (SdkStore.Sdk sdk : sdkStore.sdksMatching(implementation, classMinSdk, classMaxSdk)) {
        String problem = sdk.verifyMethod(sdkClassElem, methodElement, looseSignatures);
        if (problem != null) {
          problems.add(problem, sdk.sdkInt);
        }
      }

      if (problems.any()) {
        problems.recount(messager, methodElement);
      }
    }
  }

  private void captureJavadoc(TypeElement elem) {
    List<String> imports = new ArrayList<>();
    List<? extends ImportTree> importLines = Trees.instance(env).getPath(elem).getCompilationUnit().getImports();
    for (ImportTree importLine : importLines) {
      imports.add(importLine.getQualifiedIdentifier().toString());
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
        buf.append(problem)
            .append(" for ")
            .append(sdks.size() == 1 ? "SDK " : "SDKs ");

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
