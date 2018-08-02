package org.robolectric.annotation.processing.validator;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import org.robolectric.annotation.processing.Helpers;
import org.robolectric.annotation.processing.RobolectricModel;

/**
 * Base class for validators.
 */
public abstract class Validator implements ElementVisitor<Void, Element> {
  final protected RobolectricModel.Builder modelBuilder;
  final protected Elements elements;
  final protected Types types;
  final protected Messager messager;
  final protected TypeElement annotationType;
  final protected Helpers helpers;
  // This is the easiest way to do it because visit() is final in AbstractEV6
  final ElementVisitor<Void, Element> visitorAdapter = new AbstractElementVisitor6<Void, Element>() {

    @Override
    public Void visitPackage(PackageElement e, Element p) {
      return Validator.this.visitPackage(e, p);
    }

    @Override
    public Void visitType(TypeElement e, Element p) {
      return Validator.this.visitType(e, p);
    }

    @Override
    public Void visitVariable(VariableElement e, Element p) {
      return Validator.this.visitVariable(e, p);
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Element p) {
      return Validator.this.visitExecutable(e, p);
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement e, Element p) {
      return Validator.this.visitTypeParameter(e, p);
    }
  };
  protected Element currentElement;
  protected AnnotationMirror currentAnnotation;

  public Validator(RobolectricModel.Builder modelBuilder, ProcessingEnvironment env, String annotationType) {
    this.modelBuilder = modelBuilder;
    elements = env.getElementUtils();
    types = env.getTypeUtils();
    this.helpers = new Helpers(env);
    messager = env.getMessager();
    // FIXME: Need to test case where type lookup fails
    this.annotationType = elements.getTypeElement(annotationType);
  }

  protected AnnotationMirror getCurrentAnnotation() {
    if (currentAnnotation == null) {
      currentAnnotation = Helpers.getAnnotationMirror(types, currentElement, annotationType);
    }
    return currentAnnotation;
  }

  protected void message(Kind severity, String msg, AnnotationValue av) {
    final AnnotationMirror am = getCurrentAnnotation();
    messager.printMessage(severity, msg, currentElement, am, av);
  }

  protected void message(Kind severity, String msg) {
    final AnnotationMirror am = getCurrentAnnotation();
    messager.printMessage(severity, msg, currentElement, am);
  }

  protected void error(String msg) {
    message(Kind.ERROR, msg);
  }

  protected void error(String msg, AnnotationValue av) {
    message(Kind.ERROR, msg, av);
  }

  public void init(Element e, Element p) {
    currentElement = e;
    currentAnnotation = null;
  }

  public TypeElement getAnnotationType() {
    return annotationType;
  }

  @Override
  public Void visit(Element e, Element p) {
    init(e, p);
    return visitorAdapter.visit(e, p);
  }

  @Override
  public Void visit(Element e) {
    return visit(e, null);
  }

  @Override
  public Void visitPackage(PackageElement e, Element p) {
    return null;
  }

  @Override
  public Void visitType(TypeElement e, Element p) {
    return null;
  }

  @Override
  public Void visitVariable(VariableElement e, Element p) {
    return null;
  }

  @Override
  public Void visitExecutable(ExecutableElement e, Element p) {
    return null;
  }

  @Override
  public Void visitTypeParameter(TypeParameterElement e, Element p) {
    return null;
  }

  @Override
  public Void visitUnknown(Element e, Element p) {
    return null;
  }
}
