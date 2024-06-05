package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import com.example.objects.Dummy;
import com.example.objects.ParameterizedDummy;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.processing.DocumentedMethod;
import org.robolectric.annotation.processing.Utils;
import org.robolectric.versioning.AndroidVersions;

/** Tests for {@link ImplementsValidator */
@RunWith(JUnit4.class)
public class ImplementsValidatorTest {
  @Test
  public void implementsWithoutClassOrClassName_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsWithoutClass";
    assertAbout(singleClass())
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("@Implements: must specify <value> or <className>")
        .onLine(5);
  }

  @Test
  public void value_withUnresolvableClassNameAndOldMaxSdk_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowWithUnresolvableClassNameAndOldMaxSdk";
    assertAbout(singleClass()).that(testClass).compilesWithoutError();
  }

  @Test
  public void value_withClassName_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsDummyWithOuterDummyClassName";
    assertAbout(singleClass())
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("@Implements: cannot specify both <value> and <className> attributes")
        .onLine(6);
  }

  @Test
  public void implementsWithParameterMismatch_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsWithParameterMismatch";
    AndroidVersions.AndroidRelease unreleased = AndroidVersions.getUnreleased().get(0);
    HashMap<String, String> props = new HashMap<>();
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
    assertAbout(
            singleClass(
                props, Utils.getClassRootDir(ParameterizedDummy.class), unreleased.getSdkInt()))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining(
            "Shadow type is mismatched, expected <N extends java.lang.Number, T> "
                + "but found <T, N extends java.lang.Number>")
        .onLine(7);
  }

  @Test
  public void implementsWithMissingParameters_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsWithMissingParameters";
    AndroidVersions.AndroidRelease unreleased = AndroidVersions.getUnreleased().get(0);
    HashMap<String, String> props = new HashMap<>();
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
    assertAbout(
            singleClass(
                props, Utils.getClassRootDir(ParameterizedDummy.class), unreleased.getSdkInt()))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining(
            "Shadow type is mismatched, expected  but found <T, N extends java.lang.Number>")
        .onLine(7);
  }

  @Test
  public void implementsWithCorrectParameters_shouldCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsWithCorrectParams";
    AndroidVersions.AndroidRelease unreleased = AndroidVersions.getUnreleased().get(0);
    HashMap<String, String> props = new HashMap<>();
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
    assertAbout(
            singleClass(
                props, Utils.getClassRootDir(ParameterizedDummy.class), unreleased.getSdkInt()))
        .that(testClass)
        .compilesWithoutError();
  }

  @Test
  public void implementsWithExtraParameters_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsWithExtraParameters";
    AndroidVersions.AndroidRelease unreleased = AndroidVersions.getUnreleased().get(0);
    HashMap<String, String> props = new HashMap<>();
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
    assertAbout(singleClass(props, Utils.getClassRootDir(Dummy.class), unreleased.getSdkInt()))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("Shadow type is mismatched, expected <T, S, R> but found  ")
        .onLine(7);
  }

  @Test
  public void constructorShadowWithoutImplementation_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowWithImplementationlessShadowMethods";
    assertAbout(singleClass())
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("Shadow methods must be annotated @Implementation")
        .onLine(8)
        .and()
        .withErrorContaining("Shadow methods must be annotated @Implementation")
        .onLine(10);
  }

  @Test
  public void javadocMarkdownFormatting() throws Exception {
    DocumentedMethod documentedMethod = new DocumentedMethod("name");
    documentedMethod.setDocumentation(
        " First sentence.\n \n Second sentence.\n \n ASCII art:\n   *  *  *\n @return null\n");

    assertThat(documentedMethod.getDocumentation())
        .isEqualTo("First sentence.\n\nSecond sentence.\n\nASCII art:\n  *  *  *\n@return null\n");
  }
}
