package org.robolectric.annotation.processing;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.robolectric.annotation.processing.RobolectricModel.Builder;
import org.robolectric.annotation.processing.generator.Generator;
import org.robolectric.annotation.processing.generator.JavadocJsonGenerator;
import org.robolectric.annotation.processing.generator.ServiceLoaderGenerator;
import org.robolectric.annotation.processing.generator.ShadowProviderGenerator;
import org.robolectric.annotation.processing.validator.ImplementationValidator;
import org.robolectric.annotation.processing.validator.ImplementsValidator;
import org.robolectric.annotation.processing.validator.ImplementsValidator.SdkCheckMode;
import org.robolectric.annotation.processing.validator.RealObjectValidator;
import org.robolectric.annotation.processing.validator.ResetterValidator;
import org.robolectric.annotation.processing.validator.SdkStore;
import org.robolectric.annotation.processing.validator.Validator;

/**
 * Annotation processor entry point for Robolectric annotations.
 */
@SupportedOptions({
  RobolectricProcessor.PACKAGE_OPT, 
  RobolectricProcessor.SHOULD_INSTRUMENT_PKG_OPT})
@SupportedAnnotationTypes("org.robolectric.annotation.*")
public class RobolectricProcessor extends AbstractProcessor {
  static final String PACKAGE_OPT = "org.robolectric.annotation.processing.shadowPackage";
  static final String SHOULD_INSTRUMENT_PKG_OPT = 
      "org.robolectric.annotation.processing.shouldInstrumentPackage";
  static final String JSON_DOCS_DIR = "org.robolectric.annotation.processing.jsonDocsDir";
  static final String JSON_DOCS_ENABLED = "org.robolectric.annotation.processing.jsonDocsEnabled";
  static final String SDK_CHECK_MODE = "org.robolectric.annotation.processing.sdkCheckMode";
  private static final String SDKS_FILE = "org.robolectric.annotation.processing.sdks";
  private static final String PRIORITY = "org.robolectric.annotation.processing.priority";

  private Builder modelBuilder;
  private String shadowPackage;
  private boolean shouldInstrumentPackages;
  private int priority;
  private ImplementsValidator.SdkCheckMode sdkCheckMode;
  private String sdksFile;
  private Map<String, String> options;
  private boolean generated = false;
  private final List<Generator> generators = new ArrayList<>();
  private final Map<TypeElement, Validator> elementValidators = new HashMap<>(13);
  private File jsonDocsDir;
  private boolean jsonDocsEnabled;

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
  @VisibleForTesting
  public RobolectricProcessor(Map<String, String> options) {
    processOptions(options);
  }

  @Override
  public synchronized void init(ProcessingEnvironment environment) {
    super.init(environment);
    processOptions(environment.getOptions());
    modelBuilder = new Builder(environment);

    SdkStore sdkStore = new SdkStore(sdksFile);

    addValidator(new ImplementationValidator(modelBuilder, environment));
    addValidator(new ImplementsValidator(modelBuilder, environment, sdkCheckMode, sdkStore));
    addValidator(new RealObjectValidator(modelBuilder, environment));
    addValidator(new ResetterValidator(modelBuilder, environment));
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

    if (!generated) {
      RobolectricModel model = modelBuilder.build();

      generators.add(
          new ShadowProviderGenerator(
              model, processingEnv, shadowPackage, shouldInstrumentPackages, priority));
      generators.add(new ServiceLoaderGenerator(processingEnv, shadowPackage));
      if (jsonDocsEnabled) {
        generators.add(new JavadocJsonGenerator(model, processingEnv, jsonDocsDir));
      }
      for (Generator generator : generators) {
        generator.generate();
      }
      generated = true;
    }
    return false;
  }

  private void addValidator(Validator v) {
    elementValidators.put(v.getAnnotationType(), v);
  }

  private void processOptions(Map<String, String> options) {
    if (this.options == null) {
      this.options = options;
      this.shadowPackage = options.get(PACKAGE_OPT);
      this.shouldInstrumentPackages =
          !"false".equalsIgnoreCase(options.get(SHOULD_INSTRUMENT_PKG_OPT));
      this.jsonDocsDir = new File(options.getOrDefault(JSON_DOCS_DIR, "build/docs/json"));
      this.jsonDocsEnabled = "true".equalsIgnoreCase(options.get(JSON_DOCS_ENABLED));
      this.sdkCheckMode =
          SdkCheckMode.valueOf(options.getOrDefault(SDK_CHECK_MODE, "WARN").toUpperCase());
      this.sdksFile = getSdksFile(options, SDKS_FILE);
      this.priority =
          Integer.parseInt(options.getOrDefault(PRIORITY, "0"));

      if (this.shadowPackage == null) {
        throw new IllegalArgumentException("no package specified for " + PACKAGE_OPT);
      }
    }
  }

  /**
   * Extendable to support Bazel environments, where the sdks file is generated as a build artifact.
   */
  protected String getSdksFile(Map<String, String> options, String sdksFileParam) {
    return options.get(sdksFileParam);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }
}
