package org.robolectric.annotation.processing;

import static com.google.common.collect.Lists.newArrayList;

import com.google.common.base.Equivalence;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map.Entry;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.Types;

public class Helpers {

  private static final AnnotationValueVisitor<TypeMirror, Void> TYPE_MIRROR_VISITOR =
      new SimpleAnnotationValueVisitor6<TypeMirror, Void>() {
        @Override
        public TypeMirror visitType(TypeMirror t, Void arg) {
          return t;
        }
      };

  private static final ElementVisitor<TypeElement, Void> TYPE_ELEMENT_VISITOR =
      new SimpleElementVisitor6<TypeElement, Void>() {
        @Override
        public TypeElement visitType(TypeElement e, Void p) {
          return e;
        }
      };

  private static final AnnotationValueVisitor<String, Void> STRING_VISITOR =
      new SimpleAnnotationValueVisitor6<String, Void>() {
        @Override
        public String visitString(String s, Void arg) {
          return s;
        }
      };

  private static final AnnotationValueVisitor<Integer, Void> INT_VISITOR =
      new SimpleAnnotationValueVisitor6<Integer, Void>() {
        @Override
        public Integer visitInt(int i, Void aVoid) {
          return i;
        }
      };

  public static TypeMirror getAnnotationTypeMirrorValue(AnnotationValue av) {
    return TYPE_MIRROR_VISITOR.visit(av);
  }

  public static TypeElement getAnnotationTypeMirrorValue(Element el) {
    return TYPE_ELEMENT_VISITOR.visit(el);
  }

  public static String getAnnotationStringValue(AnnotationValue className) {
    return STRING_VISITOR.visit(className);
  }

  public static int getAnnotationIntValue(AnnotationValue av) {
    return INT_VISITOR.visit(av);
  }

  public static AnnotationMirror getAnnotationMirror(Types types, Element element,
      TypeElement annotation) {
    TypeMirror expectedType = annotation.asType();
    for (AnnotationMirror m : element.getAnnotationMirrors()) {
      if (types.isSameType(expectedType, m.getAnnotationType())) {
        return m;
      }
    }
    return null;
  }

  public static AnnotationMirror getImplementsMirror(Element elem, Types types,
      TypeElement typeElement) {
    return getAnnotationMirror(types, elem, typeElement);
  }

  public void appendParameterList(StringBuilder message,
      List<? extends TypeParameterElement> tpeList) {
    boolean first = true;
    for (TypeParameterElement tpe : tpeList) {
      if (first) {
        first = false;
      } else {
        message.append(',');
      }
      message.append(tpe.toString());
      boolean iFirst = true;
      for (TypeMirror bound : getExplicitBounds(tpe)) {
        if (iFirst) {
          message.append(" extends ");
          iFirst = false;
        } else {
          message.append(',');
        }
        message.append(bound);
      }
    }
  }

  private final Types types;
  private final Elements elements;

  /**
   * TypeMirror representing the Object class.
   */
  private final TypeMirror OBJECT_MIRROR;
  private final Predicate<TypeMirror> notObject;

  public Helpers(Elements elements, Types types) {
    this.types = types;
    this.elements = elements;

    OBJECT_MIRROR = elements.getTypeElement(Object.class.getCanonicalName()).asType();
    notObject = t -> !types.isSameType(t, OBJECT_MIRROR);
  }

  public List<TypeMirror> getExplicitBounds(TypeParameterElement typeParam) {
    return newArrayList(Iterables.filter(typeParam.getBounds(), notObject));
  }


  public static AnnotationValue getAnnotationTypeMirrorValue(AnnotationMirror annotationMirror,
      String key) {
    for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        annotationMirror.getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals(key)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private Equivalence<TypeMirror> typeMirrorEq = new Equivalence<TypeMirror>() {
    @Override
    protected boolean doEquivalent(TypeMirror a, TypeMirror b) {
      return types.isSameType(a, b);
    }

    @Override
    protected int doHash(TypeMirror t) {
      // We're not using the hash.
      return 0;
    }
  };

  private Equivalence<TypeParameterElement> typeEq = new Equivalence<TypeParameterElement>() {
    @Override
    @SuppressWarnings({"unchecked"})
    protected boolean doEquivalent(TypeParameterElement arg0,
        TypeParameterElement arg1) {
      // Casts are necessary due to flaw in pairwise equivalence implementation.
      return typeMirrorEq.pairwise().equivalent((List<TypeMirror>) arg0.getBounds(),
          (List<TypeMirror>) arg1.getBounds());
    }

    @Override
    protected int doHash(TypeParameterElement arg0) {
      // We don't use the hash code.
      return 0;
    }
  };

  @SuppressWarnings({"unchecked"})
  public boolean isSameParameterList(List<? extends TypeParameterElement> l1,
      List<? extends TypeParameterElement> l2) {
    // Cast is necessary because of a flaw in the API design of "PairwiseEquivalent",
    // a flaw that is even acknowledged in the source.
    // Our casts are safe because we're not trying to add elements to the list
    // and therefore can't violate the constraint.
    return typeEq.pairwise().equivalent((List<TypeParameterElement>) l1,
        (List<TypeParameterElement>) l2);
  }

  private TypeMirror getImplementedClassName(AnnotationMirror am) {
    AnnotationValue className = Helpers.getAnnotationTypeMirrorValue(am, "className");
    if (className == null) {
      return null;
    }
    String classNameString = Helpers.getAnnotationStringValue(className);
    if (classNameString == null) {
      return null;
    }
    TypeElement impElement = elements.getTypeElement(classNameString.replace('$', '.'));
    if (impElement == null) {
      return null;
    }
    return impElement.asType();
  }

  public TypeMirror getImplementedClass(AnnotationMirror am) {
    if (am == null) {
      return null;
    }
    // RobolectricWiringTest prefers className (if provided) to value, so we do the same here.
    TypeMirror impType = getImplementedClassName(am);
    if (impType != null) {
      return impType;
    }
    AnnotationValue av = Helpers.getAnnotationTypeMirrorValue(am, "value");
    if (av == null) {
      return null;
    }
    TypeMirror type = Helpers.getAnnotationTypeMirrorValue(av);
    if (type == null) {
      return null;
    }
    return type;
  }

  public String getPackageOf(TypeElement typeElement) {
    return elements.getPackageOf(typeElement).toString();
  }

  public String getBinaryName(TypeElement typeElement) {
    return elements.getBinaryName(typeElement).toString();
  }
}
