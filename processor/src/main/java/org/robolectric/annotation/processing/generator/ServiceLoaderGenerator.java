package org.robolectric.annotation.processing.generator;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Generator that creates the service loader metadata for a shadow package.
 */
public class ServiceLoaderGenerator extends Generator {
  private final Filer filer;
  private final Messager messager;
  private final String shadowPackage;

  public ServiceLoaderGenerator(ProcessingEnvironment environment, String shadowPackage) {
    this.filer = environment.getFiler();
    this.messager = environment.getMessager();
    this.shadowPackage = shadowPackage;
  }

  @Override
  public void generate() {
    final String fileName = "org.robolectric.internal.ShadowProvider";

    try {
      FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + fileName);
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(file.openOutputStream(), "UTF-8"));
      pw.println(shadowPackage + '.' + GEN_CLASS);
      pw.close();
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write service loader metadata file: " + e);
      throw new RuntimeException(e);
    }
  }
}
