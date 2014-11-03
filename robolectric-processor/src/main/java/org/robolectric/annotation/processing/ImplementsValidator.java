package org.robolectric.annotation.processing;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class ImplementsValidator extends Validator {

  public static final String IMPLEMENTS_CLASS = "org.robolectric.annotation.Implements";
  
  public ImplementsValidator(RoboModel model, ProcessingEnvironment env) {
    super(model, env, IMPLEMENTS_CLASS);
  }

  private TypeElement getClassNameTypeElement(AnnotationValue cv) {
    String className = RoboModel.classNameVisitor.visit(cv);
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
    AnnotationValue av = RoboModel.getAnnotationValue(am, "value");
    AnnotationValue cv = RoboModel.getAnnotationValue(am, "className");
    TypeElement type = null;
    if (av == null) {
      if (cv == null) {
        error("@Implements: must specify <value> or <className>");
        return null;
      }
      type = getClassNameTypeElement(cv);
    } else {
      TypeMirror value = RoboModel.valueVisitor.visit(av);
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
        type = RoboModel.typeVisitor.visit(types.asElement(value));
      }
    }
    if (type != null) {
      model.shadowTypes.put(elem, type);
    }
    return null;
  }
}
