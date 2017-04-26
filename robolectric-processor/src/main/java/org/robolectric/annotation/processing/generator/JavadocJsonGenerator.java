package org.robolectric.annotation.processing.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.robolectric.annotation.processing.DocumentedPackage;
import org.robolectric.annotation.processing.DocumentedType;
import org.robolectric.annotation.processing.RobolectricModel;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JavadocJsonGenerator extends Generator {

  private final RobolectricModel model;
  private final Messager messager;
  private final Gson gson;

  public JavadocJsonGenerator(RobolectricModel model, ProcessingEnvironment environment) {
    super();

    this.model = model;
    this.messager = environment.getMessager();
    gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
  }

  @Override
  public void generate() {
    Map<String, String> shadowedTypes = new HashMap<>();
    for (Map.Entry<TypeElement, TypeElement> entry : model.getShadowOfMap().entrySet()) {
      String shadowType = entry.getKey().getQualifiedName().toString();
      String shadowedType = entry.getValue().getQualifiedName().toString();
      shadowedTypes.put(shadowType, shadowedType);
    }

    for (Map.Entry<String, String> entry : model.getExtraShadowTypes().entrySet()) {
      String shadowType = entry.getKey();
      String shadowedType = entry.getValue();
      shadowedTypes.put(shadowType, shadowedType);
    }

    File docs = new File("build/docs/json");

    for (DocumentedPackage documentedPackage : model.getDocumentedPackages()) {
      for (DocumentedType documentedType : documentedPackage.getDocumentedTypes()) {
        String shadowedType = shadowedTypes.get(documentedType.getName());
        writeJson(documentedType, new File(docs, shadowedType + ".json"));
      }
    }
  }

  private void writeJson(Object object, File file) {
    file.getParentFile().mkdirs();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      gson.toJson(object, writer);
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write javadoc JSON file: " + e);
      throw new RuntimeException(e);
    }
  }

}
