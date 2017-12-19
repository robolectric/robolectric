package org.robolectric.res;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class ResourceTestUtil {
  void time(String message, Runnable runnable) {
    long startTime = System.nanoTime();
    for (int i = 0; i < 10; i++) {
      runnable.run();
    }
    long elapsed = System.nanoTime() - startTime;
    System.out.println("elapsed " + message + ": " + (elapsed / 1000000.0) + "ms");
  }

  static String stringify(ResourceTable resourceTable) {
    final HashMap<String, List<TypedResource>> map = new HashMap<>();
    resourceTable.receive(new ResourceTable.Visitor() {
      @Override
      public void visit(ResName key, Iterable<TypedResource> values) {
        List<TypedResource> v = new ArrayList<>();
        for (TypedResource value : values) {
          v.add(value);
        }
        map.put(key.getFullyQualifiedName(), v);
      }
    });
    StringBuilder buf = new StringBuilder();
    TreeSet<String> keys = new TreeSet<>(map.keySet());
    for (String key : keys) {
      buf.append(key).append(":\n");
      for (TypedResource typedResource : map.get(key)) {
        Object data = typedResource.getData();
        if (data instanceof List) {
          ArrayList<String> newList = new ArrayList<>();
          for (Object item : ((List) data)) {
            if (item.getClass().equals(TypedResource.class)) {
              TypedResource typedResourceItem = (TypedResource) item;
              newList.add(typedResourceItem.getData().toString() + " (" + typedResourceItem.getResType() + ")");
            } else {
              newList.add(item.toString());
            }
          }
          data = newList.toString();
        } else if (data instanceof StyleData) {
          StyleData styleData = (StyleData) data;
          final Map<String, String> attrs = new TreeMap<>();
          styleData.visit(new StyleData.Visitor() {
            @Override
            public void visit(AttributeResource attributeResource) {
              attrs.put(attributeResource.resName.getFullyQualifiedName(), attributeResource.value);
            }
          });
          data = data.toString() + "^" + styleData.getParent() + " " + attrs;
        }
        buf.append("  ").append(data).append(" {").append(typedResource.getResType())
            .append("/").append(typedResource.getConfig()).append(": ")
            .append(shortContext(typedResource)).append("}").append("\n");
      }
    }
    return buf.toString();
  }

  static String shortContext(TypedResource typedResource) {
    return typedResource.getXmlContext().toString().replaceAll("jar:/usr/local/google/home/.*\\.jar\\!", "jar:");
  }
}
