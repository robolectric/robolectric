package org.robolectric.annotation.processing.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import org.robolectric.annotation.processing.DocumentedPackage;
import org.robolectric.annotation.processing.DocumentedType;
import org.robolectric.annotation.processing.RobolectricModel;
import org.robolectric.annotation.processing.RobolectricModel.ShadowInfo;

public class JavadocJsonGenerator extends Generator {
  private final RobolectricModel model;
  private final Messager messager;
  private final Gson gson;
  private final File jsonDocsDir;

  public JavadocJsonGenerator(
      RobolectricModel model, ProcessingEnvironment environment, File jsonDocsDir) {
    super();

    this.model = model;
    this.messager = environment.getMessager();
    gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    this.jsonDocsDir = jsonDocsDir;
  }

  @Override
  public void generate() {
    Map<String, String> shadowedTypes = new HashMap<>();
    for (ShadowInfo entry : model.getVisibleShadowTypes()) {
      shadowedTypes.put(entry.getShadowName(), entry.getActualName());
    }

    for (Map.Entry<String, String> entry : model.getExtraShadowTypes().entrySet()) {
      String shadowType = entry.getKey();
      String shadowedType = entry.getValue();
      shadowedTypes.put(shadowType, shadowedType);
    }

    for (DocumentedPackage documentedPackage : model.getDocumentedPackages()) {
      for (DocumentedType documentedType : documentedPackage.getDocumentedTypes()) {
        String shadowedType = shadowedTypes.get(documentedType.getName());
        writeJson(documentedType, new File(jsonDocsDir, shadowedType + ".json"));
      }
    }
  }

  private void writeJson(Object object, File file) {
    try {
      file.getParentFile().mkdirs();

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        gson.toJson(object, writer);
      }
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write javadoc JSON file: " + e);
      throw new RuntimeException(e);
    }
  }

}
