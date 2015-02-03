package org.robolectric.annotation.processing;

import org.robolectric.annotation.processing.generator.Generator;
import org.robolectric.annotation.processing.generator.ServiceLoaderGenerator;
import org.robolectric.annotation.processing.generator.ShadowProviderGenerator;
import org.robolectric.annotation.processing.validator.ImplementationValidator;
import org.robolectric.annotation.processing.validator.ImplementsValidator;
import org.robolectric.annotation.processing.validator.RealObjectValidator;
import org.robolectric.annotation.processing.validator.ResetterValidator;
import org.robolectric.annotation.processing.validator.Validator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotation processor entry point for Robolectric annotations.
 */
@SupportedOptions(RobolectricProcessor.PACKAGE_OPT)
@SupportedAnnotationTypes("org.robolectric.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RobolectricProcessor extends AbstractProcessor {
  static final String PACKAGE_OPT = "org.robolectric.annotation.processing.shadowPackage";

  private RobolectricModel model;
  private String shadowPackage;
  private Map<String, String> options;
  private boolean generated = false;
  private final List<Generator> generators = new ArrayList<>();
  private final Map<TypeElement, Validator> elementValidators = new HashMap<>(13);

  /**
   * Default constructor.
   */
  public RobolectricProcessor() {
  }

  /**
   * Constructor to use for testing passing options in. Only
   * necessary until compile-testing supports passing options
   * in.
   *
   * @param options simulated options that would ordinarily
   *                be passed in the {@link ProcessingEnvironment}.
   */
  RobolectricProcessor(Map<String, String> options) {
    processOptions(options);
  }

  @Override
  public void init(ProcessingEnvironment environment) {
    super.init(environment);
    processOptions(environment.getOptions());
    model = new RobolectricModel(environment.getElementUtils(), environment.getTypeUtils());

    Messager messager = processingEnv.getMessager();
    messager.printMessage(Kind.NOTE, "Initialising Robolectric annotation processor");

    addValidator(new ImplementationValidator(model, environment));
    addValidator(new ImplementsValidator(model, environment));
    addValidator(new RealObjectValidator(model, environment));
    addValidator(new ResetterValidator(model, environment));

    generators.add(new ShadowProviderGenerator(model, environment));
    generators.add(new ServiceLoaderGenerator(model, environment));
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      Validator validator = elementValidators.get(annotation);
      if (validator != null) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(annotation)) {
          validator.visit(elem, elem.getEnclosingElement());
        }
      }
    }

    if (!generated && shadowPackage != null) {
      model.prepare();
      for (Generator generator : generators) {
        generator.generate(shadowPackage);
      }
      generated = true;
    }
    return true;
  }

  private void addValidator(Validator v) {
    elementValidators.put(v.getAnnotationType(), v);
  }

  private void processOptions(Map<String, String> options) {
    if (this.options == null) {
      this.options = options;
      shadowPackage = options.get(PACKAGE_OPT);
    }
  }
}
