package org.robolectric;

import edu.emory.mathcs.backport.java.util.Collections;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceLoaderInjector implements Injector {
  private final InnerLoader<ManifestFactory> manifestFactories = new InnerLoader<>(ManifestFactory.class);

  @Override
  public ManifestFactory getManifestFactories() {
    for (ManifestFactory manifestFactory : manifestFactories.getServices()) {
      ManifestIdentifier identify = manifestFactory.identify(config);
      if (identify) {

      }
    }
  }

  protected static class InnerLoader<T> {
    private final List<T> services = new ArrayList<>();
    private final Class<T> theClass;
    private boolean initialized = false;

    public InnerLoader(Class<T> theClass) {
      this.theClass = theClass;
    }

    private synchronized List<T> getServices() {
      if (!initialized) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(theClass);
        for (T t : serviceLoader) {
          services.add(t);
        }
        Collections.sort(services, new Comparator() {
          @Override
          public int compare(Object o1, Object o2) {
            return ordinal(o1) - ordinal(o2);
          }

          private int ordinal(Object o) {
            if (o instanceof Ordered) {
              return ((Ordered) o).order();
            }
            return 0;
          }
        });

        initialized = true;
      }

      return services;
    }
  }
}
