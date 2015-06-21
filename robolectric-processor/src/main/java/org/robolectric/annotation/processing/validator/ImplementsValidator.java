package org.robolectric.annotation.processing.validator;

import org.robolectric.annotation.processing.RobolectricModel;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

/**
 * Validator that checks usages of {@link org.robolectric.annotation.Implements}.
 */
public class ImplementsValidator extends Validator {

  public static final String IMPLEMENTS_CLASS = "org.robolectric.annotation.Implements";
  
  public ImplementsValidator(RobolectricModel model, ProcessingEnvironment env) {
    super(model, env, IMPLEMENTS_CLASS);
  }

  private TypeElement getClassNameTypeElement(AnnotationValue cv) {
    String className = RobolectricModel.classNameVisitor.visit(cv);
    TypeElement type = elements.getTypeElement(className.replace('$', '.'));
    
    if (type == null) {
      error("@Implements: could not resolve class <" + className + '>', cv);
      return null;
    }
    return type;
  }
  
  @Override
  public Void visitType(TypeElement elem, Element parent) {
    // Don't import nested classes because some of them have the same name.
    AnnotationMirror am = getCurrentAnnotation();
    AnnotationValue av = RobolectricModel.getAnnotationValue(am, "value");
    AnnotationValue cv = RobolectricModel.getAnnotationValue(am, "className");
    TypeElement type = null;
    if (av == null) {
      if (cv == null) {
        error("@Implements: must specify <value> or <className>");
        return null;
      }
      type = getClassNameTypeElement(cv);
    } else {
      TypeMirror value = RobolectricModel.valueVisitor.visit(av);
      if (value == null) {
        return null;
      }

      boolean isAnything = model.ANYTHING_MIRROR != null && types.isSameType(value, model.ANYTHING_MIRROR);
    
      if (isAnything) {
      
        if (cv == null) {
          error("@Implements: Anything class specified but no <className> attribute");
          return null;
        }

        type = getClassNameTypeElement(cv);
      } else if (cv != null) {
        error("@Implements: cannot specify both <value> and <className> attributes");
      } else {
        type = RobolectricModel.typeVisitor.visit(types.asElement(value));
      }
    }
    if (type == null) {
      return null;
    }
    final List<? extends TypeParameterElement> typeTP = type.getTypeParameters();
    final List<? extends TypeParameterElement> elemTP = elem.getTypeParameters();
    if (!model.isSameParameterList(typeTP, elemTP)) {
      StringBuilder message = new StringBuilder();
      if (elemTP.isEmpty()) {
        message.append("Shadow type is missing type parameters, expected <");
        model.appendParameterList(message, type.getTypeParameters());
        message.append('>');
      } else if (typeTP.isEmpty()) {
        message.append("Shadow type has type parameters but real type does not");
      } else {
        message.append("Shadow type must have same type parameters as its real counterpart: expected <");
        model.appendParameterList(message, type.getTypeParameters());
        message.append(">, was <");
        model.appendParameterList(message, elem.getTypeParameters());
        message.append('>');
      }
      messager.printMessage(Kind.ERROR, message, elem);
      return null;
    }
    model.addShadowType(elem, type);
    return null;
  }
}
