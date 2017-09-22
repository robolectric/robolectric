package org.robolectric.annotation.processing.generator;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.processing.RobolectricModel;

public class ShadowProviderGeneratorTest {

  private RobolectricModel model;
  private ShadowProviderGenerator generator;
  private StringWriter writer;

  @Before
  public void setUp() throws Exception {
    model = mock(RobolectricModel.class);
    generator = new ShadowProviderGenerator(model, mock(ProcessingEnvironment.class), "the.package", true);
    writer = new StringWriter();
  }

  @Test
  public void resettersAreOnlyCalledIfSdkMatches() throws Exception {
    HashMap<TypeElement, ExecutableElement> resetters = new HashMap<>();

    resetters.put(type("ShadowThing", 19, 20), element("reset19To20"));
    resetters.put(type("ShadowThing", -1, 18), element("resetMax18"));
    resetters.put(type("ShadowThing", 21, -1), element("resetMin21"));
    when(model.getResetters()).thenReturn(resetters);

    generator.generate(new PrintWriter(writer));

    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() >= 19 && org.robolectric.RuntimeEnvironment.getApiLevel() <= 20) ShadowThing.reset19To20();");
    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() >= 21) ShadowThing.resetMin21();");
    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() <= 18) ShadowThing.resetMax18();");
  }

  private TypeElement type(String shadowClassName, int minSdk, int maxSdk) {
    TypeElement shadowType = mock(TypeElement.class);
    when(model.getReferentFor(shadowType)).thenReturn(shadowClassName);
    Implements implAnnotation = mock(Implements.class);
    when(implAnnotation.minSdk()).thenReturn(minSdk);
    when(implAnnotation.maxSdk()).thenReturn(maxSdk);
    when(shadowType.getAnnotation(Implements.class)).thenReturn(implAnnotation);
    return shadowType;
  }

  @Nonnull
  private ExecutableElement element(String reset) {
    ExecutableElement resetterExecutable = mock(ExecutableElement.class);
    Name mock = mock(Name.class);
    when(mock.toString()).thenReturn(reset);
    when(resetterExecutable.getSimpleName()).thenReturn(mock);
    return resetterExecutable;
  }

}