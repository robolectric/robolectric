package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import com.example.objects.Dummy;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.processing.Utils;
import org.robolectric.versioning.VersionCalculator.SdkInfo;

/** Tests for {@link ClassNameValidator} */
@RunWith(JUnit4.class)
public class ClassNameValidatorTest {

  private static final SdkInfo sdkInfo = new SdkInfo(10000, false);
  private static final HashMap<String, String> props = new HashMap<>();

  static {
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
  }

  @Test
  public void classNameWithExistingParam_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowClassNameParameterExisting";
    assertAbout(singleClass(props, Utils.getClassRootDir(Dummy.class), sdkInfo))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("Use com.example.objects.Dummy directly");
  }

  @Test
  public void classNameWithNonExistingParam_shouldCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowClassNameParameterNonExisting";
    assertAbout(singleClass(props, Utils.getClassRootDir(Dummy.class), sdkInfo))
        .that(testClass)
        .compilesWithoutError();
  }

  @Test
  public void classNameWithExistingReturnType_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowClassNameReturnTypeExisting";
    assertAbout(singleClass(props, Utils.getClassRootDir(Dummy.class), sdkInfo))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("Use com.example.objects.Dummy directly");
  }

  @Test
  public void classNameWithExistingNestedParam_shouldNotCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowClassNameParameterNestedExisting";
    assertAbout(singleClass(props, Utils.getClassRootDir(Dummy.class), sdkInfo))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining("Use com.example.objects.DummyClassName$PublicNested directly");
  }

  @Test
  public void classNameWithNotAccessibleParam_shouldCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowClassNameParameterNotAccessible";
    assertAbout(singleClass(props, Utils.getClassRootDir(Dummy.class), sdkInfo))
        .that(testClass)
        .compilesWithoutError();
  }

  @Test
  public void classNameWithParamOutsideSdkRange_shouldCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowClassNameParameterOutsideSdkRange";
    assertAbout(singleClass(props, Utils.getClassRootDir(Dummy.class), sdkInfo))
        .that(testClass)
        .compilesWithoutError();
  }

  @Test
  public void classNameOnPublicMethod_shouldCompile() {
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowClassNamePublicMethod";
    assertAbout(singleClass(props, Utils.getClassRootDir(Dummy.class), sdkInfo))
        .that(testClass)
        .compilesWithoutError();
  }
}
