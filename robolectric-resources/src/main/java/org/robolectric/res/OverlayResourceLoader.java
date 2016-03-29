package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class OverlayResourceLoader extends XResourceLoader {

  private static final int MAX_PARALLELISM = calculateMaxParallelism();

  private final String packageName;
  private final List<PackageResourceLoader> subResourceLoaders;

  public OverlayResourceLoader(String packageName, List<PackageResourceLoader> subResourceLoaders) {
    super(new OverlayResourceIndex(packageName, subResourceLoaders));
    this.packageName = packageName;
    this.subResourceLoaders = subResourceLoaders;
  }

  @Override
  void doInitialize() {
    initialiseResourceLoaders();
    mergeResources();
  }

  /**
   * The number of threads in the executor shouldn't go above 4 because this will cause
   * I/O contention on most machines. Similarly it shouldn't go above half the cores because
   * we don't want to bog down the machine with I/O waits. Finally, we want at least 1 thread
   * to make sure the work gets done.
   */
  private static int calculateMaxParallelism() {
    int threadsToUse = Math.min(4, Runtime.getRuntime().availableProcessors()/2);
    return Math.max(1, threadsToUse);
  }

  private void initialiseResourceLoaders() {
    List<Callable<Void>> initialiseTasks = new ArrayList<Callable<Void>>(subResourceLoaders.size());

    for (final PackageResourceLoader subResourceLoader : subResourceLoaders) {
      initialiseTasks.add(new Callable<Void>() {
        @Override
        public Void call() {
          subResourceLoader.initialize();
          return null;
        }
      });
    }

    runTasksInExecutor(initialiseTasks);
  }

  private void mergeResources() {
    List<Callable<Void>> mergeTasks = new ArrayList<Callable<Void>>(7);
    mergeTasks.add(new Callable<Void>() {
      @Override
      public Void call() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
          pluralsData.mergeLibraryStyle(subResourceLoader.pluralsData, packageName);
        }
        return null;
      }
    });

    mergeTasks.add(new Callable<Void>() {
      @Override
      public Void call() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
          stringData.mergeLibraryStyle(subResourceLoader.stringData, packageName);
        }
        return null;
      }
    });

    mergeTasks.add(new Callable<Void>() {
      @Override
      public Void call() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
          drawableData.mergeLibraryStyle(subResourceLoader.drawableData, packageName);
        }
        return null;
      }
    });

    mergeTasks.add(new Callable<Void>() {
      @Override
      public Void call() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
          preferenceData.mergeLibraryStyle(subResourceLoader.preferenceData, packageName);
        }
        return null;
      }
    });

    mergeTasks.add(new Callable<Void>() {
      @Override
      public Void call() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
          xmlDocuments.mergeLibraryStyle(subResourceLoader.xmlDocuments, packageName);
        }
        return null;
      }
    });

    mergeTasks.add(new Callable<Void>() {
      @Override
      public Void call() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
          rawResources.mergeLibraryStyle(subResourceLoader.rawResources, packageName);
        }
        return null;
      }
    });

    mergeTasks.add(new Callable<Void>() {
      @Override
      public Void call() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
          data.mergeLibraryStyle(subResourceLoader.data, packageName);
        }
        return null;
      }
    });

    runTasksInExecutor(mergeTasks);
  }

  private void runTasksInExecutor(List<Callable<Void>> tasks) {
    ForkJoinPool executorService = new ForkJoinPool(MAX_PARALLELISM);
    try {
      executorService.invokeAll(tasks);
    } finally {
      executorService.shutdown();
      try {
        if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
          throw new RuntimeException(
              "Xml parsing for subdirectories of " + packageName + " took longer than 5 minutes");
        }
      } catch(InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
    return super.getDrawableNode(resName.withPackageName(packageName), qualifiers);
  }

  @Override public Plural getPlural(ResName resName, int quantity, String qualifiers) {
    return super.getPlural(resName.withPackageName(packageName), quantity, qualifiers);
  }

  @Override public PreferenceNode getPreferenceNode(ResName resName, String qualifiers) {
    return super.getPreferenceNode(resName.withPackageName(packageName), qualifiers);
  }

  @Override public InputStream getRawValue(ResName resName) {
    return super.getRawValue(resName.withPackageName(packageName));
  }

  @Override public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return super.getValue(resName.withPackageName(packageName), qualifiers);
  }

  @Override public XmlBlock getXml(ResName resName, String qualifiers) {
    return super.getXml(resName.withPackageName(packageName), qualifiers);
  }

  @Override public boolean providesFor(String namespace) {
    for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
      if (subResourceLoader.providesFor(namespace)) {
        return true;
      }
    }
    return false;
  }
}
