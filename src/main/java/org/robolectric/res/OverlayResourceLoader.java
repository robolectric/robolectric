package org.robolectric.res;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverlayResourceLoader extends XResourceLoader {
    public static final boolean DEBUG = true;

    private final String packageName;
    private final List<PackageResourceLoader> subResourceLoaders;

    public OverlayResourceLoader(String packageName, List<PackageResourceLoader> subResourceLoaders) {
        super(new OverlayResourceIndex(packageName, subResourceLoaders));
        this.packageName = packageName;
        this.subResourceLoaders = subResourceLoaders;
    }

    @Override
    void doInitialize() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
            subResourceLoader.doInitialize();

            booleanResolver.mergeLibraryStyle(subResourceLoader.booleanResolver, packageName);
            colorResolver.mergeLibraryStyle(subResourceLoader.colorResolver, packageName);
            dimenResolver.mergeLibraryStyle(subResourceLoader.dimenResolver, packageName);
            integerResolver.mergeLibraryStyle(subResourceLoader.integerResolver, packageName);
            pluralsResolver.mergeLibraryStyle(subResourceLoader.pluralsResolver, packageName);
            stringResolver.mergeLibraryStyle(subResourceLoader.stringResolver, packageName);
            viewNodes.mergeLibraryStyle(subResourceLoader.viewNodes, packageName);
            menuNodes.mergeLibraryStyle(subResourceLoader.menuNodes, packageName);
            drawableNodes.mergeLibraryStyle(subResourceLoader.drawableNodes, packageName);
        }
    }

    private static class OverlayResourceIndex extends ResourceIndex {
        private final String packageName;

        public OverlayResourceIndex(String packageName, List<PackageResourceLoader> subResourceLoaders) {
            this.packageName = packageName;

            final ResEntries resEntries = new ResEntries();
            for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
                ResourceIndex resourceIndex = subResourceLoader.getResourceIndex();
                for (Map.Entry<ResName, Integer> entry : resourceIndex.resourceNameToId.entrySet()) {
                    ResName resName = entry.getKey();
                    int value = entry.getValue();
                    ResName localResName = resName.withPackageName(packageName);
                    if (DEBUG) resEntries.add(localResName, resName, value);
                    resourceNameToId.put(localResName, value);
                    resourceIdToResName.put(value, localResName);
                }
            }

            if (DEBUG) resEntries.check();
        }

        @Override
        public Integer getResourceId(ResName resName) {
            return resourceNameToId.get(resName.withPackageName(packageName));
        }

        @Override
        public ResName getResName(int resourceId) {
            return resourceIdToResName.get(resourceId).withPackageName(packageName);
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

            public void check() {
                for (Map.Entry<ResName, List<ResEntry>> entries : resEntries.entrySet()) {
                    List<ResEntry> value = entries.getValue();
                    int first = value.get(0).value;
                    for (ResEntry resEntry : value) {
                        if (resEntry.value != first) {
                            System.err.println("*** WARNING!!! resource mismatch!");
                            for (ResEntry entry : value) {
                                System.err.println("* " + entry.resName + " -> " + entry.value);
                            }
                            break;
                        }
                    }
                }
            }
        }

        class ResEntry {
            private final ResName resName;
            private final int value;

            public ResEntry(ResName resName, int value) {
                this.resName = resName;
                this.value = value;
            }
        }
    }
}
