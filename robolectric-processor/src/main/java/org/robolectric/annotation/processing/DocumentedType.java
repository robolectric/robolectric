package org.robolectric.annotation.processing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DocumentedType extends DocumentedElement {
  public final Map<String, DocumentedMethod> methods = new TreeMap<>();

  public List<String> imports;

  DocumentedType(String name) {
    super(name);
  }

  public DocumentedMethod getDocumentedMethod(String desc) {
    DocumentedMethod documentedMethod = methods.get(desc);
    if (documentedMethod == null) {
      documentedMethod = new DocumentedMethod(desc);
      methods.put(desc, documentedMethod);
    }
    return documentedMethod;
  }

  public Collection<DocumentedMethod> getMethods() {
    return methods.values();
  }
}
