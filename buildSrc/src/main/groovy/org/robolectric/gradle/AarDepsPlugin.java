package org.robolectric.gradle;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE;

import com.android.build.gradle.internal.dependency.ExtractAarTransform;
import com.google.common.base.Joiner;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

/**
 * Resolve aar dependencies into jars for non-Android projects.
 */
public class AarDepsPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project
        .getDependencies()
        .registerTransform(
            ClassesJarExtractor.class,
            reg -> {
              reg.getParameters().getProjectName().set(project.getName());
              reg.getFrom().attribute(ARTIFACT_TYPE_ATTRIBUTE, "aar");
              reg.getTo().attribute(ARTIFACT_TYPE_ATTRIBUTE, "jar");
            });

    project.afterEvaluate(
        p ->
            project
                .getConfigurations()
                .forEach(
                    c -> {
                      // I suspect we're meant to use the org.gradle.usage attribute, but this
                      // works.
                      if (c.getName().endsWith("Classpath")) {
                        c.attributes(
                            cfgAttrs -> cfgAttrs.attribute(ARTIFACT_TYPE_ATTRIBUTE, "jar"));
                      }
                    }));

    // warn if any AARs do make it through somehow; there must be a gradle configuration
    // that isn't matched above.
    //noinspection Convert2Lambda
    project
        .getTasks()
        .withType(JavaCompile.class)
        .all(
            // the following Action<Task needs to remain an anonymous subclass or gradle's
            // incremental compile breaks (run `gradlew -i classes` twice to see impact):
            t -> t.doFirst(new Action<Task>() {
              @Override
              public void execute(Task task) {
                List<File> aarFiles = AarDepsPlugin.this.findAarFiles(t.getClasspath());
                if (!aarFiles.isEmpty()) {
                  throw new IllegalStateException(
                      "AARs on classpath: " + Joiner.on("\n  ").join(aarFiles));
                }
              }
            }));
  }

  private List<File> findAarFiles(FileCollection files) {
    List<File> bad = new ArrayList<>();
    for (File file : files.getFiles()) {
      if (file.getName().toLowerCase().endsWith(".aar")) {
        bad.add(file);
      }
    }
    return bad;
  }

  public static abstract class ClassesJarExtractor extends ExtractAarTransform {
    @Inject
    public ClassesJarExtractor() {
    }

    @Override
    public void transform(@NotNull TransformOutputs outputs) {
      AtomicReference<File> classesJarFile = new AtomicReference<>();
      AtomicReference<File> outJarFile = new AtomicReference<>();
      super.transform(new TransformOutputs() {
        // This is the one that ExtractAarTransform calls.
        @Override
        public File dir(Object o) {
          // ExtractAarTransform needs a place to extract the AAR. We don't really need to
          // register this as an output, but it'd be tricky to avoid it.
          File dir = outputs.dir(o);

          // Also, register our jar file. Its name needs to be quasi-unique or
          // IntelliJ Gradle/Android plugins get confused.
          classesJarFile.set(new File(new File(dir, "jars"), "classes.jar"));
          outJarFile.set(new File(new File(dir, "jars"), o + ".jar"));
          outputs.file(o + "/jars/" + o + ".jar");
          return outputs.dir(o);
        }

        @Override
        public File file(Object o) {
          throw new IllegalStateException("shouldn't be called");
        }
      });

      classesJarFile.get().renameTo(outJarFile.get());
    }
  }
}
