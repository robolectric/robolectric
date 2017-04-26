package org.robolectric.annotation.processing;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class DocumentedPackage extends DocumentedElement {
  private final Map<String, DocumentedType> documentedTypes = new TreeMap<>();

  DocumentedPackage(String name) {
    super(name);
  }

  public Collection<DocumentedType> getDocumentedTypes() {
    return documentedTypes.values();
  }

  public DocumentedType getDocumentedType(String name) {
    DocumentedType documentedType = documentedTypes.get(name);
    if (documentedType == null) {
      documentedType = new DocumentedType(name);
      documentedTypes.put(name, documentedType);
    }
    return documentedType;
  }
}
