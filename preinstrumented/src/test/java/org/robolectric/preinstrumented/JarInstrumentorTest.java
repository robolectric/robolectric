package org.robolectric.preinstrumented;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link JarInstrumentor}. */
@RunWith(JUnit4.class)
public class JarInstrumentorTest {

  private JarInstrumentor spyDummyInstrumentor;

  @Before
  public void setUp() throws Exception {
    JarInstrumentor dummyInstrumentor =
        new JarInstrumentor() {
          @Override
          protected void instrumentJar(File sourceJarFile, File destJarFile) {
            // No-op. We only want to test the command line processing. Stub the actual
            // instrumention.
          }

          @Override
          protected void exit(int status) {
            // No-op. Tests should never call system.exit().
          }
        };
    spyDummyInstrumentor = spy(dummyInstrumentor);
  }

  @Test
  public void processCommandLine_legacyUsage() throws Exception {
    spyDummyInstrumentor.processCommandLine(new String[] {"source.jar", "dest.jar"});
    verify(spyDummyInstrumentor).instrumentJar(new File("source.jar"), new File("dest.jar"));
  }

  @Test
  public void processCommandLine_unknownArguments() throws Exception {
    spyDummyInstrumentor.processCommandLine(new String[] {"source.jar", "dest.jar", "--some-flag"});
    verify(spyDummyInstrumentor, never()).instrumentJar(any(File.class), any(File.class));
    verify(spyDummyInstrumentor).exit(1);
  }
}
