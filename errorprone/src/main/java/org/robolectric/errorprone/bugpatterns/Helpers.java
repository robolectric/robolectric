package org.robolectric.errorprone.bugpatterns;

import static com.google.errorprone.util.ASTHelpers.findEnclosingNode;
import static com.google.errorprone.util.ASTHelpers.hasAnnotation;

import com.google.errorprone.VisitorState;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import org.robolectric.annotation.Implements;

/** Matchers for {@link ShadowUsageCheck}. */
public class Helpers {

  /** Match sub-types or implementations of the given type. */
  public static TypePredicate isCastableTo(Supplier<Type> type) {
    return new CastableTo(type);
  }

  /** Match sub-types or implementations of the given type. */
  public static TypePredicate isCastableTo(String type) {
    return new CastableTo(Suppliers.typeFromString(type));
  }

  public static boolean isInShadowClass(TreePath path, VisitorState state) {
    Tree leaf = path.getLeaf();
    JCClassDecl classDecl = JCClassDecl.class.isInstance(leaf)
        ? (JCClassDecl) leaf
        : findEnclosingNode(state.getPath(), JCClassDecl.class);

    return hasAnnotation(classDecl, Implements.class, state);
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
