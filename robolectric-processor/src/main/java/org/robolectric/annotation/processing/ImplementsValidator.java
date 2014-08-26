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
  
  @Override
  public Void visitType(TypeElement elem, Element parent) {
    // Don't import nested classes because some of them have the same name.
    AnnotationMirror am = getCurrentAnnotation();
    AnnotationValue av = RoboModel.getAnnotationValue(am, "value");
    if (av == null) {
      error("@Implements' value attribute could not be found");
      return null;
    }

    TypeMirror value = RoboModel.valueVisitor.visit(av);
    TypeElement type;
    if (value == null) {
      error("@Implements' value attribute was not a class - has the definition changed?", av);
      return null;
    }
    
    if (model.ANYTHING_MIRROR != null && types.isSameType(value, model.ANYTHING_MIRROR)) {
      AnnotationValue cv = RoboModel.getAnnotationValue(am, "className");
      
      if (cv == null) {
        error("@Implements: Anything class specified but no className attribute");
        return null;
      }
      
      String className = RoboModel.classNameVisitor.visit(cv);
      type = elements.getTypeElement(className.replace('$', '.'));
      
      if (type == null) {
        error("@Implements: could not resolve class <" + className + '>', cv);
        return null;
      }
    } else {
      type = RoboModel.typeVisitor.visit(types.asElement(value));
    }
    model.shadowTypes.put(elem, type);
    return null;
  }
}
