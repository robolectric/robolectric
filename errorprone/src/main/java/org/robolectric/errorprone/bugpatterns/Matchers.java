package org.robolectric.errorprone.bugpatterns;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import java.lang.annotation.Annotation;

/** Matchers for {@link RobolectricBestPractices}. */
public class Matchers {

  /** Match sub-types or implementations of the given type. */
  public static TypePredicate isCastableTo(Supplier<Type> type) {
    return new CastableTo(type);
  }

  /** Match sub-types or implementations of the given type. */
  public static TypePredicate isCastableTo(String type) {
    return new CastableTo(Suppliers.typeFromString(type));
  }

  /** Matches methods with the specified annotation. */
  public static class AnnotatedMethodMatcher implements Matcher<ExpressionTree> {

    private final Class<? extends Annotation> annotationClass;

    public AnnotatedMethodMatcher(Class<? extends Annotation> annotationClass) {
      this.annotationClass = annotationClass;
    }

    @Override
    public boolean matches(ExpressionTree tree, VisitorState state) {
      Symbol sym = ASTHelpers.getSymbol(tree);
      if (!(sym instanceof MethodSymbol)) {
        return false;
      }

      return sym.getAnnotation(annotationClass) != null;
    }
  }

  /** Matches implementations of the given interface. */
  public static class CastableTo implements TypePredicate {

    public final Supplier<Type> expected;

    public CastableTo(Supplier<Type> type) {
      this.expected = type;
    }

    @Override
    public boolean apply(Type type, VisitorState state) {
      Type bound = expected.get(state);
      if (bound == null || type == null) {
        // TODO(cushon): type suppliers are allowed to return null :(
        return false;
      }
      return ASTHelpers.isCastable(type, bound, state);
    }
  }

}
