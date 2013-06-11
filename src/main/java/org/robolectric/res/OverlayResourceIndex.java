package org.robolectric.res;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class OverlayResourceIndex extends ResourceIndex {
  private final String packageName;
  private final Set<String> actualPackageNames = new HashSet<String>();

  public OverlayResourceIndex(String packageName, List<PackageResourceLoader> subResourceLoaders) {
    this(packageName, map(subResourceLoaders));
  }

  private static ResourceIndex[] map(List<PackageResourceLoader> subResourceLoaders) {
    ResourceIndex[] resourceIndexes = new ResourceIndex[subResourceLoaders.size()];
    for (int i = 0; i < subResourceLoaders.size(); i++) {
      resourceIndexes[i] = subResourceLoaders.get(i).getResourceIndex();
    }
    return resourceIndexes;
  }

  public OverlayResourceIndex(String packageName, ResourceIndex... subResourceIndexes) {
    this.packageName = packageName;
    actualPackageNames.add(packageName);

    for (ResourceIndex subResourceIndex : subResourceIndexes) {
      actualPackageNames.addAll(subResourceIndex.getPackages());

      for (Map.Entry<ResName, Integer> entry : subResourceIndex.resourceNameToId.entrySet()) {
        ResName resName = entry.getKey();
        int value = entry.getValue();
        ResName localResName = resName.withPackageName(packageName);
        if (!resourceNameToId.containsKey(localResName)) {
          resourceNameToId.put(localResName, value);
          resourceIdToResName.put(value, localResName);
        }
      }
    }

//        if (OverlayResourceLoader.DEBUG) resEntries.check(subResourceIndexes);
  }

  @Override
  public Integer getResourceId(ResName resName) {
    if (!actualPackageNames.contains(resName.packageName)) {
      return null;
    }
    return resourceNameToId.get(resName.withPackageName(packageName));
  }

  @Override
  public ResName getResName(int resourceId) {
    ResName resName = resourceIdToResName.get(resourceId);
    return resName == null ? null : resName.withPackageName(packageName);
  }

  @Override public Collection<String> getPackages() {
    return actualPackageNames;
  }

  class ResEntries {
    private final Map<ResName, List<ResEntry>> resEntries = new HashMap<ResName, List<ResEntry>>();

    public void add(ResName localResName, ResName resName, int value) {
      List<ResEntry> resEntryList = resEntries.get(localResName);
      if (resEntryList == null) {
        resEntryList = new ArrayList<ResEntry>();
        resEntries.put(localResName, resEntryList);
      }
      resEntryList.add(new ResEntry(resName, value));
    }

//    public void check(ResourceIndex... subResourceIndex) {
//      for (Map.Entry<ResName, List<ResEntry>> entries : resEntries.entrySet()) {
//        List<ResEntry> value = entries.getValue();
//        int first = value.get(0).value;
//        for (int i = 1, valueSize = value.size(); i < valueSize; i++) {
//          ResEntry resEntry = value.get(i);
//          if (resEntry.value != first) {
//            Class<?> rClass = subResourceIndex[i].resourcePath.rClass;
//            setField(rClass, resEntry.resName, first);
//
//            System.err.println("*** WARNING!!! resource mismatch!");
//            for (ResEntry entry : value) {
//              System.err.println("* " + entry.resName + " -> 0x" + Integer.toHexString(entry.value));
//            }
//            break;
//          }
//        }
//      }
//    }
//
//    private void setField(Class<?> rClass, ResName resName, int value) {
//      Class<?> innerClass = getInnerClass(rClass, resName.type);
//      try {
//        Field field = innerClass.getDeclaredField(resName.name);
//        if (Modifier.isFinal(field.getModifiers())) {
//          System.err.println("*** WARNING!!! " + field + " is final!");
//          Robolectric.Reflection.setFinalStaticField(innerClass, resName.name, value);
//        } else {
//          field.set(null, value);
//        }
//      } catch (NoSuchFieldException e) {
//        throw new RuntimeException(e);
//      } catch (IllegalAccessException e) {
//        throw new RuntimeException(e);
//      }
//
//    }
//
//    private Class<?> getInnerClass(Class<?> rClass, String name) {
//      for (Class<?> aClass : rClass.getClasses()) {
//        if (aClass.getSimpleName().equals(name)) {
//          return aClass;
//        }
//      }
//      throw new RuntimeException("couldn't find " + rClass.getName() + "." + name);
//    }
  }

  class ResEntry {
    private final ResName resName;
    private final int value;

    public ResEntry(ResName resName, int value) {
      this.resName = resName;
      this.value = value;
    }
  }

  @Override public String toString() {
    return "OverlayResourceIndex{" +
        "package='" + packageName + '\'' +
        '}';
  }
}
