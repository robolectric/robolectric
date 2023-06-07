package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.Files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link NativeCallHandler}. */
@RunWith(JUnit4.class)
public class NativeCallHandlerTest {
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void jarInstrumentorLegacyUsage() throws IOException {
    // CUJ: Legacy jarInstrumentor usage; there is no exemption file, native methods do not throw.

    File exemptionsFile = tempFolder.newFile("natives.txt");
    assertThat(exemptionsFile.delete()).isTrue();

    // Create handler, which loads exemptions from file. It's fine for the file to be missing.
    NativeCallHandler handler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ false, /* throwOnNatives= */ false);

    // No method descriptor should throw.
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod()V")).isFalse();
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod(II)V")).isFalse();
  }

  @Test
  public void jarInstrumentorUsage_throwOnNativesEnabled() throws IOException {
    // CUJ: jarInstrumentor usage with an exemption list and non-exempted native methods should
    // throw.

    File exemptionsFile = tempFolder.newFile("natives.txt");
    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(exemptionsFile.getPath(), UTF_8))) {
      writer.write("android.app.ActivityThread#dumpGraphicsInfo(Ljava/io/FileDescriptor;)V\n");
      writer.write("libcore.io.Linux#chmod(Ljava/lang/String;I)V\n");
      writer.write("libcore.io.Linux#fchmod(Ljava/io/FileDescriptor;I)V\n");
      writer.write("android.graphics.fonts.Font^Builder#nAddAxis(JIF)V\n");
      writer.write("org.example.MyClass#someOtherMethod()V\n");
      // empty or white-space lines are ignored
      writer.write("\n");
      writer.write("  \t \n");
      // A # prefix denotes a comment and is ignored too
      writer.write("# org.example.Ignored#comment()V\n");
      writer.write("  # org.example.Ignored#thisIsACommentToo()V  \n");
    }

    // Create handler, which loads exemptions from file. ThrowOnNatives is enabled.
    NativeCallHandler handler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ false, /* throwOnNatives= */ true);

    // Test exempted methods
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod()V")).isFalse();

    // Test non-exempted methods
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod(II)V")).isTrue();

    // Empty lines and comments are ignored and not present in the exemption list.
    assertThat(handler.shouldThrow("")).isTrue();
    assertThat(handler.shouldThrow("  \t ")).isTrue();
    assertThat(handler.shouldThrow("# org.example.Ignored#comment()V")).isTrue();
    assertThat(handler.shouldThrow("  # org.example.Ignored#thisIsACommentToo()V  ")).isTrue();
  }

  @Test
  public void jarInstrumentorUsage_throwOnNativesDisabled() throws IOException {
    // CUJ: jarInstrumentor usage with an exemption list and non-exempted native methods should
    // throw.

    File exemptionsFile = tempFolder.newFile("natives.txt");
    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(exemptionsFile.getPath(), UTF_8))) {
      writer.write("android.app.ActivityThread#dumpGraphicsInfo(Ljava/io/FileDescriptor;)V\n");
      writer.write("libcore.io.Linux#chmod(Ljava/lang/String;I)V\n");
      writer.write("libcore.io.Linux#fchmod(Ljava/io/FileDescriptor;I)V\n");
      writer.write("android.graphics.fonts.Font^Builder#nAddAxis(JIF)V\n");
      writer.write("org.example.MyClass#someOtherMethod()V\n");
    }

    // Create handler, which loads exemptions from file. ThrowOnNatives is disabled.
    NativeCallHandler handler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ false, /* throwOnNatives= */ false);

    // Test exempted methods
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod()V")).isFalse();

    // Test non-exempted methods
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod(II)V")).isFalse();
  }

  @Test
  public void jarInstrumentorUsage_logNativeCall_ignored() throws IOException {
    // When not writing the exemption list, logNativeCall calls are no-op.

    File exemptionsFile = tempFolder.newFile("natives.txt");

    // Create handler, which loads exemptions from file. ThrowOnNatives is enabled.
    NativeCallHandler handler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ false, /* throwOnNatives= */ true);

    // No methods are exempted -- initial list is empty.
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod()V")).isTrue();
    assertThat(handler.shouldThrow("android.graphics.fonts.Font$Builder#nAddAxis(JIF)V")).isTrue();

    handler.logNativeCall("org.example.MyClass#someOtherMethod()V");
    handler.logNativeCall("android.graphics.fonts.Font$Builder#nAddAxis(JIF)V");

    // LogNativeCall did not capture. These methods are still not exempted.
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod()V")).isTrue();
    assertThat(handler.shouldThrow("android.graphics.fonts.Font$Builder#nAddAxis(JIF)V")).isTrue();
  }

  @Test
  public void exemptionListGeneratorUsage_logNativeCall_capturesCalls() throws IOException {
    // CUJ: jarInstrumentor called to generate the exemption list.

    File exemptionsFile = tempFolder.newFile("natives.txt");

    // Create handler, which loads exemptions from file. ThrowOnNatives is enabled.
    NativeCallHandler handler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ true, /* throwOnNatives= */ true);

    // No methods are exempted -- initial list is empty.
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod()V")).isTrue();
    assertThat(handler.shouldThrow("android.graphics.fonts.Font$Builder#nAddAxis(JIF)V")).isTrue();

    handler.logNativeCall("org.example.MyClass#someOtherMethod()V");
    handler.logNativeCall("android.graphics.fonts.Font$Builder#nAddAxis(JIF)V");

    // These methods are now exempted.
    assertThat(handler.shouldThrow("org.example.MyClass#someOtherMethod()V")).isFalse();
    assertThat(handler.shouldThrow("android.graphics.fonts.Font$Builder#nAddAxis(JIF)V")).isFalse();
  }

  @Test
  public void exemptionListGeneratorUsage_writeExemptionFile() throws IOException {
    // CUJ: jarInstrumentor called to generate the exemption list.

    File exemptionsFile = tempFolder.newFile("natives.txt");
    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(exemptionsFile.getPath(), UTF_8))) {
      writer.write("android.app.ActivityThread#dumpGraphicsInfo(Ljava/io/FileDescriptor;)V\n");
      writer.write("libcore.io.Linux#fchmod(Ljava/io/FileDescriptor;I)V\n");
    }

    // Create handler, which loads exemptions from file. ThrowOnNatives is disabled.
    NativeCallHandler handler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ true, /* throwOnNatives= */ false);

    handler.logNativeCall("org.example.MyClass#someOtherMethod()V");
    // Multiple calls with same value are idempotent.
    handler.logNativeCall("org.example.MyClass#someOtherMethod(I)V");
    handler.logNativeCall("org.example.MyClass#someOtherMethod(I)V");
    handler.logNativeCall("org.example.MyClass#someOtherMethod(I)V");
    handler.logNativeCall("org.example.MyClass#someOtherMethod(II)V");
    handler.logNativeCall("libcore.io.Linux#chmod(Ljava/lang/String;I)V");
    // Case of a nested class with $ in the FQCN.
    handler.logNativeCall("android.graphics.fonts.Font$Builder#nAddAxis(JIF)V");

    handler.writeExemptionsList();

    // Note: due to how the generated files are manipulated in the shell/makefile build system,
    // '$' characters are a problem and would need to be escaped (and potentially differently for
    // shell vs makefiles). The workaround is to have '$' rewritten as '^'.

    assertThat(Files.asCharSource(exemptionsFile, UTF_8).read())
        .isEqualTo(
            "android.app.ActivityThread#dumpGraphicsInfo(Ljava/io/FileDescriptor;)V\n"
                // Font$Builder gets written as Font^Builder.
                + "android.graphics.fonts.Font^Builder#nAddAxis(JIF)V\n"
                + "libcore.io.Linux#chmod(Ljava/lang/String;I)V\n"
                + "libcore.io.Linux#fchmod(Ljava/io/FileDescriptor;I)V\n"
                + "org.example.MyClass#someOtherMethod()V\n"
                + "org.example.MyClass#someOtherMethod(I)V\n"
                + "org.example.MyClass#someOtherMethod(II)V\n");
  }

  @Test
  public void getExceptionMessage() throws IOException {
    File exemptionsFile = tempFolder.newFile("natives.txt");
    NativeCallHandler handler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ false, /* throwOnNatives= */ true);

    // Test generated exception message for non-exempted methods.
    assertThat(
            handler.getExceptionMessage(
                "org.example.MyClass$1#someOtherMethod(II)V",
                "org.example.MyClass$1",
                "someOtherMethod"))
        .isEqualTo(
            "Unexpected Robolectric native method call to"
                + " 'org.example.MyClass$1#someOtherMethod()'.\n"
                + "Option 1: If customizing this method is useful, add an implementation in"
                + " ShadowMyClass.java.\n"
                + "Option 2: If this method just needs to trivially return 0 or null, please add an"
                + " exemption entry for\n"
                + "   org.example.MyClass^1#someOtherMethod(II)V\n"
                + "to exemption file natives.txt");
  }
}
