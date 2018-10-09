package org.robolectric.annotation.processing.generator;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.processing.RobolectricModel;
import org.robolectric.annotation.processing.RobolectricModel.ResetterInfo;

/** Tests for {@link ShadowProviderGenerator} */
@RunWith(JUnit4.class)
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
    when(model.getVisibleShadowTypes()).thenReturn(Collections.emptyList());

    List<ResetterInfo> resetterInfos = new ArrayList<>();
    resetterInfos.add(resetterInfo("ShadowThing", 19, 20, "reset19To20"));
    resetterInfos.add(resetterInfo("ShadowThing", -1, 18, "resetMax18"));
    resetterInfos.add(resetterInfo("ShadowThing", 21, -1, "resetMin21"));
    when(model.getResetters()).thenReturn(resetterInfos);

    generator.generate(new PrintWriter(writer));

    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() >= 19 && org.robolectric.RuntimeEnvironment.getApiLevel() <= 20) ShadowThing.reset19To20();");
    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() >= 21) ShadowThing.resetMin21();");
    assertThat(writer.toString()).contains("if (org.robolectric.RuntimeEnvironment.getApiLevel() <= 18) ShadowThing.resetMax18();");
  }

  private ResetterInfo resetterInfo(String shadowName, int minSdk, int maxSdk, String methodName) {
    ResetterInfo resetterInfo = mock(ResetterInfo.class);
    when(resetterInfo.getMinSdk()).thenReturn(minSdk);
    when(resetterInfo.getMaxSdk()).thenReturn(maxSdk);
    when(resetterInfo.getMethodCall()).thenReturn(shadowName + "." + methodName + "();");
    return resetterInfo;
  }
}
