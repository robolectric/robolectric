package org.robolectric.annotation.processing.generator;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.processing.ResetterMethod;
import org.robolectric.annotation.processing.RobolectricModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShadowProviderGeneratorTest {

  private RobolectricModel model;
  private ShadowProviderGenerator generator;
  private StringWriter writer;

  @Before
  public void setUp() throws Exception {
    model = mock(RobolectricModel.class);
    generator = new ShadowProviderGenerator(model, mock(ProcessingEnvironment.class), true);
    writer = new StringWriter();
  }

  @Test
  public void resettersAreOnlyCalledIfSdkMatches() throws Exception {
    List<ResetterMethod> resetters = new ArrayList<>();

    resetters.add(new ResetterMethod(type("ShadowThing", 19, 20), element("reset19To20"), parent.getAnnotation(Implements.class).minSdk(), parent.getAnnotation(Implements.class).maxSdk()));
    resetters.add(new ResetterMethod(type("ShadowThing", -1, 18), element("resetMax18"), parent.getAnnotation(Implements.class).minSdk(), parent.getAnnotation(Implements.class).maxSdk()));
    resetters.add(new ResetterMethod(type("ShadowThing", 21, -1), element("resetMin21"), parent.getAnnotation(Implements.class).minSdk(), parent.getAnnotation(Implements.class).maxSdk()));
    when(model.getResetterMethods()).thenReturn(resetters);

    generator.generate("the.package", new PrintWriter(writer));

    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() >= 19 && org.robolectric.RuntimeEnvironment.getApiLevel() <= 20) ShadowThing.reset19To20();");
    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() >= 21) ShadowThing.resetMin21();");
    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() <= 18) ShadowThing.resetMax18();");
  }

  private TypeElement type(String shadowClassName, int minSdk, int maxSdk) {
    TypeElement shadowType = mock(TypeElement.class);
    Name name = mock(Name.class);
    when(name.toString()).thenReturn(shadowClassName);
    when(shadowType.getQualifiedName()).thenReturn(name);
    when(model.getReferentFor(shadowType)).thenReturn(shadowClassName);
    Implements implAnnotation = mock(Implements.class);
    when(implAnnotation.minSdk()).thenReturn(minSdk);
    when(implAnnotation.maxSdk()).thenReturn(maxSdk);
    when(shadowType.getAnnotation(Implements.class)).thenReturn(implAnnotation);
    return shadowType;
  }

  @NotNull
  private ExecutableElement element(String reset) {
    ExecutableElement resetterExecutable = mock(ExecutableElement.class);
    Name mock = mock(Name.class);
    when(mock.toString()).thenReturn(reset);
    when(resetterExecutable.getSimpleName()).thenReturn(mock);
    return resetterExecutable;
  }

}