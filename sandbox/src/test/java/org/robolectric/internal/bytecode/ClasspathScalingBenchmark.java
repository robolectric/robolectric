package org.robolectric.internal.bytecode;

import static org.junit.Assume.assumeTrue;

import com.google.common.base.Splitter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runners.JUnit4;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.robolectric.util.inject.PluginFinderBenchmarkAccess;

/**
 * Measures how {@link SandboxClassLoader} and related lookups scale with the number of jars on the
 * classpath.
 *
 * <p>Skipped unless {@code -Drobolectric.benchmark=true}. For each classpath size it synthesizes
 * that many jars (three in four of them empty, the rest holding a package of classes, a resource
 * and a service file) and runs the measurements in a child JVM whose {@code java.class.path} starts
 * with them, so the sandbox's parent is the real application class loader as it is under Gradle.
 *
 * <p>Every {@code robolectric.*} property other than the benchmark's own is passed on to the child,
 * so comparing {@code -Drobolectric.sandbox.classpathIndex=false} against {@code =true} compares
 * lookups without and with {@link ClasspathIndex}. {@code -Drobolectric.benchmark.realJars=<file>}
 * appends the jars listed in a file, one path per line, instead of synthesizing them, and measures
 * resource lookups only; {@code -Drobolectric.benchmark.jvmArgs} adds arguments to the child JVM,
 * e.g. to record a profile.
 *
 * <pre>
 * ./gradlew :sandbox:test --rerun --tests '*ClasspathScalingBenchmark' \
 *     -Drobolectric.benchmark=true -Drobolectric.benchmark.sizes=50,500,2000 \
 *     -Drobolectric.sandbox.classpathIndex=true
 * </pre>
 */
@RunWith(JUnit4.class)
public class ClasspathScalingBenchmark {
  private static final int CLASSES_PER_FULL_JAR = 20;
  private static final int SERVICE_TYPES = 10;

  @Test
  public void run() throws Exception {
    assumeTrue(Boolean.getBoolean("robolectric.benchmark"));
    String sizes = System.getProperty("robolectric.benchmark.sizes", "50,500,2000");
    // Real jars, one path per line, e.g. from a Gradle cache: measures resource lookups only.
    String realJarList = System.getProperty("robolectric.benchmark.realJars");
    List<String> realJars = realJarList == null ? null : Files.readAllLines(Paths.get(realJarList));
    // Under the build directory rather than java.io.tmpdir, which is behind a symlink on macOS
    // and would make the application class loader's canonical URLs differ from the sandbox's.
    Path root = Paths.get(System.getProperty("user.dir"), "build", "classpath-bench");
    Files.createDirectories(root);
    Path androidAll = root.resolve("android-all.jar");
    writeJar(androidAll, 0, false);
    for (String size : Splitter.on(',').trimResults().split(sizes)) {
      int n = Integer.parseInt(size);
      Path dir = root.resolve("n" + n);
      Files.createDirectories(dir);
      List<String> cp = new ArrayList<>();
      if (realJars != null) {
        // After the test's own class path, so that none of them shadows what the benchmark runs on.
        cp.add(System.getProperty("java.class.path"));
        cp.addAll(realJars.subList(0, n));
      } else {
        for (int i = 0; i < n; i++) {
          Path jar = dir.resolve(String.format("lib%05d.jar", i));
          writeJar(jar, i, isFull(i));
          cp.add(jar.toString());
        }
        cp.add(System.getProperty("java.class.path"));
      }
      String javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
      List<String> cmd = new ArrayList<>();
      cmd.add(javaBin);
      cmd.add("-Xmx2g");
      cmd.add("-cp");
      cmd.add(String.join(File.pathSeparator, cp));
      for (String prop : System.getProperties().stringPropertyNames()) {
        if (prop.startsWith("robolectric.") && !prop.startsWith("robolectric.benchmark")) {
          cmd.add("-D" + prop + "=" + System.getProperty(prop));
        }
      }
      cmd.add(Child.class.getName());
      cmd.add(Integer.toString(n));
      cmd.add(androidAll.toString());
      cmd.add(realJars != null ? "real" : "synthetic");
      String jvmArgs = System.getProperty("robolectric.benchmark.jvmArgs");
      if (jvmArgs != null) {
        cmd.addAll(1, Splitter.on(' ').omitEmptyStrings().splitToList(jvmArgs));
      }
      Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }
      if (process.waitFor() != 0) {
        throw new AssertionError("benchmark child failed for N=" + n);
      }
    }
  }

  static boolean isFull(int i) {
    return i % 4 == 0;
  }

  private static void writeJar(Path jar, int index, boolean full) throws IOException {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
    try (OutputStream out = Files.newOutputStream(jar);
        JarOutputStream jos = new JarOutputStream(out, manifest)) {
      if (!full) {
        return;
      }
      String pkg = String.format("bench/p%05d/", index);
      for (int c = 0; c < CLASSES_PER_FULL_JAR; c++) {
        jos.putNextEntry(new JarEntry(pkg + "C" + c + ".class"));
        jos.write(classBytes(pkg + "C" + c));
        jos.closeEntry();
      }
      jos.putNextEntry(new JarEntry(pkg + "res.txt"));
      jos.write("hello".getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();
      jos.putNextEntry(
          new JarEntry("META-INF/services/bench.Service" + (index / 4) % SERVICE_TYPES));
      jos.write(("bench.Impl" + index + "\n").getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();
    }
  }

  private static byte[] classBytes(String internalName) {
    ClassWriter cw = new ClassWriter(0);
    cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null);
    cw.visitEnd();
    return cw.toByteArray();
  }

  /** Runs inside a JVM whose classpath starts with the synthesized jars. */
  public static class Child {
    private static final int REPS = 7;

    public static void main(String[] args) throws Exception {
      int n = Integer.parseInt(args[0]);
      URL androidAll = Paths.get(args[1]).toUri().toURL();
      ClassLoader app = ClassLoader.getSystemClassLoader();
      Thread.currentThread().setContextClassLoader(app);
      InstrumentationConfiguration config = InstrumentationConfiguration.newBuilder().build();
      ClassInstrumentor instrumentor = new ClassInstrumentor(new ShadowDecorator());

      // The very first sandbox in the JVM, including any one-time index build, plus its first miss.
      long cold = System.nanoTime();
      SandboxClassLoader first =
          new SandboxClassLoader(app, config, new UrlResourceProvider(androidAll), instrumentor);
      first.getResource("bench/missing/cold.txt");
      report(n, "firstSandboxInJvm_ms", (System.nanoTime() - cold) / 1e6);
      // Keep one sandbox open for the rest of the run, as SandboxManager keeps its sandboxes.
      SandboxClassLoader keepAlive = first;

      if (args.length > 2 && args[2].equals("real")) {
        runRealJars(n, app, config, instrumentor, androidAll);
        keepAlive.close();
        return;
      }

      List<String> classNames = new ArrayList<>();
      List<Integer> fullJars = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        if (isFull(i)) {
          fullJars.add(i);
        }
      }
      // 400 classes spread evenly over the populated jars.
      for (int k = 0; k < 400; k++) {
        int jar = fullJars.get((int) ((long) k * fullJars.size() / 400));
        classNames.add(String.format("bench.p%05d.C%d", jar, k % CLASSES_PER_FULL_JAR));
      }
      String lastRes = String.format("bench/p%05d/res.txt", fullJars.get(fullJars.size() - 1));

      // Warm up the parent's own jar handles and the JIT once, outside any measurement.
      for (int w = 0; w < 3; w++) {
        try (SandboxClassLoader warm =
            new SandboxClassLoader(
                app, config, new UrlResourceProvider(androidAll), instrumentor)) {
          for (String name : classNames) {
            warm.loadClass(name);
          }
          for (int i = 0; i < 50; i++) {
            warm.getResource("bench/missing/warm" + i + ".txt");
            tryLoad(warm, "bench.missing.Warm" + i);
          }
          warm.getResources("META-INF/services/bench.Service1");
          PluginFinderBenchmarkAccess.findPluginNames(warm, "bench.warm.Type" + w);
        }
      }

      report(
          n,
          "construct_us",
          median(() -> {
            long t = System.nanoTime();
            SandboxClassLoader l =
                new SandboxClassLoader(
                    app, config, new UrlResourceProvider(androidAll), instrumentor);
            long d = System.nanoTime() - t;
            l.close();
            return d / 1e3;
          }));

      report(
          n,
          "firstMissOnNewSandbox_us",
          median(() -> {
            try (SandboxClassLoader l =
                new SandboxClassLoader(
                    app, config, new UrlResourceProvider(androidAll), instrumentor)) {
              long t = System.nanoTime();
              l.getResource("bench/missing/first.txt");
              return (System.nanoTime() - t) / 1e3;
            }
          }));

      try (SandboxClassLoader l =
          new SandboxClassLoader(app, config, new UrlResourceProvider(androidAll), instrumentor)) {
        l.getResource("bench/missing/prime.txt");
        int[] counter = {0};
        report(
            n,
            "resourceMiss_us",
            median(() -> {
              long t = System.nanoTime();
              for (int i = 0; i < 200; i++) {
                l.getResource("bench/missing/r" + counter[0]++ + ".txt");
              }
              return (System.nanoTime() - t) / 1e3 / 200;
            }));
        report(
            n,
            "resourceHitLastJar_us",
            median(() -> {
              long t = System.nanoTime();
              for (int i = 0; i < 200; i++) {
                l.getResource(lastRes);
              }
              return (System.nanoTime() - t) / 1e3 / 200;
            }));
        report(
            n,
            "classMiss_us",
            median(() -> {
              long t = System.nanoTime();
              for (int i = 0; i < 200; i++) {
                tryLoad(l, "bench.missing.M" + counter[0]++);
              }
              return (System.nanoTime() - t) / 1e3 / 200;
            }));
        report(
            n,
            "getResourcesServices_us",
            median(() -> {
              long t = System.nanoTime();
              for (int i = 0; i < 20; i++) {
                Enumeration<URL> e = l.getResources("META-INF/services/bench.Service1");
                while (e.hasMoreElements()) {
                  e.nextElement();
                }
              }
              return (System.nanoTime() - t) / 1e3 / 20;
            }));
        java.lang.reflect.Method ensurePackage =
            SandboxClassLoader.class.getDeclaredMethod("ensurePackage", String.class);
        ensurePackage.setAccessible(true);
        report(
            n,
            "ensurePackage_ns",
            median(() -> {
              long t = System.nanoTime();
              for (int i = 0; i < 10000; i++) {
                ensurePackage.invoke(l, classNames.get(i % classNames.size()));
              }
              return (System.nanoTime() - t) / 10000.0;
            }));
      }

      report(
          n,
          "loadClassFreshSandbox_us_per_class",
          median(() -> {
            try (SandboxClassLoader l =
                new SandboxClassLoader(
                    app, config, new UrlResourceProvider(androidAll), instrumentor)) {
              long t = System.nanoTime();
              for (String name : classNames) {
                l.loadClass(name);
              }
              return (System.nanoTime() - t) / 1e3 / classNames.size();
            }
          }));

      report(
          n,
          "classBytesFreshSandbox_us_per_class",
          median(() -> {
            try (SandboxClassLoader l =
                new SandboxClassLoader(
                    app, config, new UrlResourceProvider(androidAll), instrumentor)) {
              long t = System.nanoTime();
              for (String name : classNames) {
                try (java.io.InputStream in =
                    l.getResourceAsStream(name.replace('.', '/') + ".class")) {
                  in.readAllBytes();
                }
              }
              return (System.nanoTime() - t) / 1e3 / classNames.size();
            }
          }));

      // Plugin lookups as each new sandbox's Injector performs them: a fresh finder per sandbox,
      // resolving 20 plugin types that mostly have no service file.
      report(
          n,
          "pluginLookups20NewSandbox_ms",
          median(() -> {
            try (SandboxClassLoader l =
                new SandboxClassLoader(
                    app, config, new UrlResourceProvider(androidAll), instrumentor)) {
              long t = System.nanoTime();
              PluginFinderBenchmarkAccess.findPluginNamesWithNewFinder(l, 20);
              return (System.nanoTime() - t) / 1e6;
            }
          }));

      keepAlive.close();
      report(
          n,
          "sandboxAfterAllClosed_us",
          median(() -> {
            long s = System.nanoTime();
            try (SandboxClassLoader l =
                new SandboxClassLoader(
                    app, config, new UrlResourceProvider(androidAll), instrumentor)) {
              l.getResource("bench/missing/reopen.txt");
              return (System.nanoTime() - s) / 1e3;
            }
          }));

      long t = System.nanoTime();
      long listeners = ServiceLoader.load(RunListener.class, app).stream().count();
      report(n, "runListenerServiceLoaderOnce_us", (System.nanoTime() - t) / 1e3 + 0 * listeners);

      report(
          n,
          "appLoaderResourceMiss_us",
          median(() -> {
            long s = System.nanoTime();
            for (int i = 0; i < 200; i++) {
              app.getResource("bench/missing/app" + i + "_" + s + ".properties");
            }
            return (System.nanoTime() - s) / 1e3 / 200;
          }));
    }

    private static void runRealJars(
        int n,
        ClassLoader app,
        InstrumentationConfiguration config,
        ClassInstrumentor instrumentor,
        URL androidAll)
        throws Exception {
      List<String> entries =
          Splitter.on(File.pathSeparator).splitToList(System.getProperty("java.class.path"));
      List<String> names = new ArrayList<>();
      for (int k = 0; k < 400; k++) {
        String jar = entries.get(entries.size() - n + (int) ((long) k * n / 400));
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jar)) {
          jarFile.stream()
              .map(java.util.zip.ZipEntry::getName)
              .filter(e -> e.endsWith(".class") && !e.startsWith("META-INF/"))
              .filter(e -> !e.endsWith("module-info.class"))
              .skip(k % 7)
              .findFirst()
              .ifPresent(names::add);
        } catch (IOException e) {
          // skip unreadable jars
        }
      }
      report(n, "realSampledNames", names.size());
      report(
          n,
          "realClassResourceHitFreshSandbox_us",
          median(() -> {
            try (SandboxClassLoader l =
                new SandboxClassLoader(
                    app, config, new UrlResourceProvider(androidAll), instrumentor)) {
              long t = System.nanoTime();
              for (String name : names) {
                if (l.getResource(name) == null) {
                  throw new AssertionError("missing " + name);
                }
              }
              return (System.nanoTime() - t) / 1e3 / names.size();
            }
          }));
      try (SandboxClassLoader l =
          new SandboxClassLoader(app, config, new UrlResourceProvider(androidAll), instrumentor)) {
        int[] counter = {0};
        report(
            n,
            "realResourceMiss_us",
            median(() -> {
              long t = System.nanoTime();
              for (int i = 0; i < 200; i++) {
                l.getResource("com/example/missing/r" + counter[0]++ + ".properties");
              }
              return (System.nanoTime() - t) / 1e3 / 200;
            }));
        report(
            n,
            "realClassMiss_us",
            median(() -> {
              long t = System.nanoTime();
              for (int i = 0; i < 200; i++) {
                tryLoad(l, "com.example.missing.M" + counter[0]++);
              }
              return (System.nanoTime() - t) / 1e3 / 200;
            }));
        report(
            n,
            "realGetResourcesServices_us",
            median(() -> {
              long t = System.nanoTime();
              for (int i = 0; i < 20; i++) {
                Enumeration<URL> e =
                    l.getResources("META-INF/services/javax.annotation.processing.Processor");
                while (e.hasMoreElements()) {
                  e.nextElement();
                }
              }
              return (System.nanoTime() - t) / 1e3 / 20;
            }));
      }
      report(
          n,
          "realPluginLookups20NewSandbox_ms",
          median(() -> {
            try (SandboxClassLoader l =
                new SandboxClassLoader(
                    app, config, new UrlResourceProvider(androidAll), instrumentor)) {
              long t = System.nanoTime();
              PluginFinderBenchmarkAccess.findPluginNamesWithNewFinder(l, 20);
              return (System.nanoTime() - t) / 1e6;
            }
          }));
    }

    /** Times a class miss: the class must not exist. */
    private static void tryLoad(ClassLoader l, String name) {
      try {
        l.loadClass(name);
      } catch (ClassNotFoundException expected) {
        return;
      }
      throw new AssertionError("unexpectedly loaded " + name);
    }

    interface Measurement {
      double run() throws Exception;
    }

    private static double median(Measurement m) throws Exception {
      double[] samples = new double[REPS];
      for (int i = 0; i < REPS; i++) {
        samples[i] = m.run();
      }
      Arrays.sort(samples);
      return samples[REPS / 2];
    }

    private static void report(int n, String metric, double value) {
      System.out.printf("BENCH N=%d %s=%.2f%n", n, metric, value);
    }
  }
}
