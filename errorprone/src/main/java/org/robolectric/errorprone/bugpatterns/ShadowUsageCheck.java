package org.robolectric.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.ANDROID;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.util.ASTHelpers.getSymbol;
import static com.google.errorprone.util.ASTHelpers.hasAnnotation;
import static org.robolectric.errorprone.bugpatterns.Helpers.isCastableTo;
import static org.robolectric.errorprone.bugpatterns.Helpers.isInShadowClass;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFix.Builder;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.UnknownType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.Name;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.errorprone.bugpatterns.Helpers.AnnotatedMethodMatcher;

/** @author christianw@google.com (Christian Williams) */
@AutoService(BugChecker.class)
@BugPattern(
    name = "ShadowUsageCheck",
    summary = "Robolectric shadows shouldn't be stored to variables or fields.",
    category = ANDROID,
    severity = SUGGESTION,
    documentSuppression = false,
    tags = StandardTags.REFACTORING,
    link = "http://robolectric.org/migrating/#improper-use-of-shadows",
    linkType = LinkType.CUSTOM,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class ShadowUsageCheck extends BugChecker implements ClassTreeMatcher {

  /** Matches when the shadowOf method is used to obtain a shadow from an instrumented instance. */
  private static final Matcher<MethodInvocationTree> shadowStaticMatcher =
      Matchers.allOf(staticMethod(), new AnnotatedMethodMatcher(Implementation.class));

  /** Matches when the shadowOf method is used to obtain a shadow from an instrumented instance. */
  private static final Matcher<MethodInvocationTree> shadowOfMatcher =
      Matchers.allOf(
          staticMethod()
              .onClass(isCastableTo("org.robolectric.internal.ShadowProvider"))
              .named("shadowOf"),
          argumentCount(1));

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (isInShadowClass(state.getPath(), state)) {
      return NO_MATCH;
    }

    final ShadowInliner shadowInliner = new ShadowInliner(
        (JCCompilationUnit) state.getPath().getCompilationUnit());
    shadowInliner.scan(tree, state);

    Fix fix = shadowInliner.possibleFixes.getFix();
    return fix.isEmpty() ? NO_MATCH : describeMatch(tree, fix);
  }

  static class ShadowInliner extends TreeScanner<Void, VisitorState> {
    private final PossibleFixes possibleFixes;

    private final Set<String> knownFields = new HashSet<>();
    private final Set<String> knownLocalVars = new HashSet<>();
    private final Map<Symbol, String> varRemapping = new HashMap<>();

    private boolean inShadowClass;

    ShadowInliner(JCCompilationUnit compilationUnit) {
      this(new PossibleFixes(SuggestedFix.builder(), compilationUnit));
    }

    ShadowInliner(PossibleFixes possibleFixes) {
      this.possibleFixes = possibleFixes;
    }

    private void matchedShadowOf(MethodInvocationTree shadowOfCall, VisitorState state) {
      ExpressionTree shadowOfArg = shadowOfCall.getArguments().get(0);
      Type shadowOfArgType = getExpressionType(shadowOfArg);

      Tree parent = state.getPath().getParentPath().getLeaf();
      CompilationUnitTree compilationUnit = state.getPath().getCompilationUnit();

      // pointless (ShadowX) shadowOf(x)? drop it.
      if (parent.getKind() == Kind.TYPE_CAST) {
        parent = removeCastIfUnnecessary((JCTypeCast) parent, state);
      }

      switch (parent.getKind()) {
        case VARIABLE: // ShadowType shadowType = shadowOf(type);
          {
            // shadow is being assigned to a variable; don't do that!
            JCVariableDecl variableDecl = (JCVariableDecl) parent;
            String oldVarName = variableDecl.getName().toString();

            // since it's being declared here, no danger of a collision on this var name...
            knownLocalVars.remove(oldVarName);

            String newVarName = pickNewName(shadowOfArg, oldVarName, this);
            varRemapping.put(getSymbol(variableDecl), newVarName);

            // ... but be careful not to collide with it later.
            knownLocalVars.add(newVarName);

            // replace shadow variable declaration with shadowed type and name
            if (!newVarName.equals(shadowOfArg.toString())) {
              Type shadowedType = getUpperBound(shadowOfArgType, state);
              String shadowedTypeName =
                  SuggestedFixes.prettyType(state, possibleFixes.fixBuilder, shadowedType);
              String newAssignment =
                  shadowedTypeName + " " + newVarName + " = " + shadowOfArg + ";";

              // avoid overlapping replacements:
              if (shadowOfArg instanceof JCMethodInvocation) {
                JCExpression jcExpression = ((JCMethodInvocation) shadowOfArg).meth;
                if (jcExpression instanceof JCFieldAccess) {
                  possibleFixes.removeFixFor(((JCFieldAccess) jcExpression).selected);
                }
              }

              possibleFixes.fixByReplacing(parent, newAssignment);
            } else {
              possibleFixes.fixByDeleting(parent);
            }

            // replace shadow variable reference with `nonShadowInstance` or
            // `shadowOf(nonShadowInstance)` as appropriate.
            new TreePathScanner<Void, Void>() {
              @Override
              public Void visitIdentifier(IdentifierTree identifierTreeX, Void aVoid) {
                JCIdent identifierTree = (JCIdent) identifierTreeX;

                Symbol symbol = getSymbol(identifierTree);
                if (variableDecl.sym.equals(symbol) && !isLeftSideOfAssignment(identifierTree)) {
                  TreePath idPath = TreePath.getPath(compilationUnit, identifierTree);
                  Tree parent = idPath.getParentPath().getLeaf();
                  boolean callDirectlyOnFramework = shouldCallDirectlyOnFramework(idPath);

                  JCTree replaceNode;
                  if (parent instanceof JCFieldAccess && !callDirectlyOnFramework) {
                    JCFieldAccess fieldAccess = (JCFieldAccess) parent;
                    JCMethodInvocation newShadowOfCall =
                        createSyntheticShadowAccess(shadowOfCall, newVarName, symbol, state);

                    replaceFieldSelected(fieldAccess, newShadowOfCall, state);
                    replaceNode = newShadowOfCall;
                  } else {
                    identifierTree.name = state.getName(newVarName);
                    identifierTree.sym.name = state.getName(newVarName);
                    replaceNode = identifierTree;
                  }

                  String replaceWith =
                      callDirectlyOnFramework
                          ? newVarName
                          : shadowOfCall.getMethodSelect() + "(" + newVarName + ")";

                  possibleFixes.put(
                      replaceNode, possibleFixes.new ReplacementFix(identifierTree, replaceWith));
                }
                return super.visitIdentifier(identifierTree, aVoid);
              }

              private boolean isLeftSideOfAssignment(IdentifierTree identifierTree) {
                Tree parent = getCurrentPath().getParentPath().getLeaf();
                if (parent instanceof AssignmentTree) {
                  return identifierTree.equals(((AssignmentTree) parent).getVariable());
                }
                return false;
              }
            }.scan(compilationUnit, null);
          }
          break;

        case ASSIGNMENT: // this.shadowType = shadowOf(type);
          {
            // shadow is being assigned to a field or variable; don't do that!
            JCAssign assignment = (JCAssign) parent;
            Symbol fieldSymbol = getSymbol(assignment.lhs);

            String oldFieldName = assignment.lhs.toString();
            String remappedName = varRemapping.get(fieldSymbol);

            // since it's being declared here, no danger of a collision on this var name...
            knownFields.remove(oldFieldName);

            String newFieldName =
                remappedName == null ? pickNewName(shadowOfArg, oldFieldName, this) : remappedName;
            varRemapping.put(fieldSymbol, newFieldName);

            // ... but be careful not to collide with it later.
            knownLocalVars.add(newFieldName);

            // local variable declaration should have been handled above in the VARIABLE case;
            // just strip shadowOf() and assign it to the de-shadowed variable.
            if (fieldSymbol.getKind() == ElementKind.LOCAL_VARIABLE) {
              if (newFieldName.equals(shadowOfArg.toString())) {
                // assigning to self, don't bother
                TreePath assignmentPath = TreePath.getPath(compilationUnit, assignment);
                Tree assignmentParent = assignmentPath.getParentPath().getLeaf();
                if (assignmentParent instanceof ExpressionStatementTree) {
                  possibleFixes.fixByDeleting(assignmentParent);
                }
              } else {
                possibleFixes.fixByReplacing(
                    assignment, newFieldName + " = " + shadowOfArg.toString());
              }
              break;
            }

            Symbol shadowOfArgSym = getSymbol(shadowOfArg);
            ElementKind shadowOfArgDomicile =
                shadowOfArgSym == null
                    ? ElementKind.OTHER // it's probably an expression, not a var...
                    : shadowOfArgSym.getKind();
            boolean namesAreSame = newFieldName.equals(shadowOfArg.toString());

            boolean useExistingField =
                shadowOfArgDomicile == ElementKind.FIELD
                    && namesAreSame
                    && !isMethodParam(ASTHelpers.getSymbol(shadowOfArg), state.getPath());

            if (useExistingField) {
              fixVar(fieldSymbol, state, possibleFixes).delete();

              ExpressionStatementTree enclosingNode =
                  ASTHelpers.findEnclosingNode(
                      TreePath.getPath(compilationUnit, assignment), ExpressionStatementTree.class);
              if (enclosingNode != null) {
                possibleFixes.fixByDeleting(enclosingNode);
              }
            } else {
              Type shadowedType = getUpperBound(shadowOfArgType, state);
              String shadowedTypeName =
                  SuggestedFixes.prettyType(state, possibleFixes.fixBuilder, shadowedType);
              fixVar(fieldSymbol, state, possibleFixes)
                  .setName(newFieldName)
                  .setTypeName(shadowedTypeName)
                  .setRenameUses(false)
                  .modify();

              String thisStr = "";
              if (assignment.lhs.toString().startsWith("this.")
                  || (shadowOfArgDomicile == ElementKind.LOCAL_VARIABLE && namesAreSame)) {
                thisStr = "this.";
              }

              possibleFixes.fixByReplacing(
                  assignment, thisStr + newFieldName + " = " + shadowOfArg);
            }

            TreePath containingBlock = findParentOfKind(state, Kind.BLOCK);
            if (containingBlock != null) {
              // replace shadow field reference with `nonShadowInstance` or
              // `shadowOf(nonShadowInstance)` as appropriate.
              new TreePathScanner<Void, Void>() {
                @Override
                public Void visitMemberSelect(MemberSelectTree memberSelectTree, Void aVoid) {
                  maybeReplaceFieldRef(memberSelectTree.getExpression());

                  return super.visitMemberSelect(memberSelectTree, aVoid);
                }

                @Override
                public Void visitIdentifier(IdentifierTree identifierTree, Void aVoid) {
                  maybeReplaceFieldRef(identifierTree);

                  return super.visitIdentifier(identifierTree, aVoid);
                }

                private void maybeReplaceFieldRef(ExpressionTree subject) {
                  Symbol symbol = getSymbol(subject);
                  if (symbol != null && symbol.getKind() == ElementKind.FIELD) {
                    TreePath subjectPath = TreePath.getPath(compilationUnit, subject);

                    if (symbol.equals(fieldSymbol) && isPartOfMethodInvocation(subjectPath)) {
                      String fieldRef =
                          subject.toString().startsWith("this.")
                              ? "this." + newFieldName
                              : newFieldName;

                      JCTree replaceNode = (JCTree) subject;
                      Tree container = subjectPath.getParentPath().getLeaf();
                      if (container instanceof JCFieldAccess) {
                        JCFieldAccess fieldAccess = (JCFieldAccess) container;
                        JCMethodInvocation newShadowOfCall =
                            createSyntheticShadowAccess(shadowOfCall, newFieldName, symbol, state);
                        replaceFieldSelected(fieldAccess, newShadowOfCall, state);
                        replaceNode = newShadowOfCall;
                      }

                      String replaceWith =
                          shouldCallDirectlyOnFramework(subjectPath)
                              ? fieldRef
                              : shadowOfCall.getMethodSelect() + "(" + fieldRef + ")";
                      possibleFixes.put(
                          replaceNode, possibleFixes.new ReplacementFix(subject, replaceWith));
                    }
                  }
                }
              }.scan(compilationUnit, null);
            }
          }
          break;

        case MEMBER_SELECT: // shadowOf(type).method();
          {
            if (shouldCallDirectlyOnFramework(state.getPath())) {
              if (!isInSyntheticShadowAccess(state)) {
                possibleFixes.fixByReplacing(shadowOfCall, shadowOfArg.toString());
              }
            }
          }
          break;

        case TYPE_CAST:
          System.out.println("WARN: not sure what to do with " + parent.getKind() + ": " + parent);
          break;

        default:
          throw new RuntimeException(
              "not sure what to do with " + parent.getKind() + ": " + parent);
      }
    }

    private void replaceFieldSelected(JCFieldAccess fieldAccess, JCMethodInvocation newShadowOfCall,
        VisitorState state) {
      int priorStartPosition = fieldAccess.selected.getStartPosition();
      int priorEndPosition = getEndPosition(state, fieldAccess.selected);

      fieldAccess.selected = newShadowOfCall;

      newShadowOfCall.pos = priorStartPosition;
      if (newShadowOfCall.meth instanceof JCIdent) {
        ((JCIdent) newShadowOfCall.meth).pos = priorStartPosition;
      }
      setEndPosition(state, newShadowOfCall, priorEndPosition);
    }

    private int getEndPosition(VisitorState state, JCTree node) {
      return state.getEndPosition(node);
    }

    private void setEndPosition(VisitorState state, JCTree tree, int endPosition) {
      JCCompilationUnit compilationUnit = (JCCompilationUnit) state.getPath().getCompilationUnit();
      compilationUnit.endPositions.storeEnd(tree, endPosition);
    }

    @Override
    public Void visitClass(ClassTree classTree, VisitorState visitorState) {
      boolean priorInShadowClass = inShadowClass;
      inShadowClass = hasAnnotation(classTree, Implements.class, visitorState);
      try {
        return super.visitClass(classTree, visitorState);
      } finally {
        inShadowClass = priorInShadowClass;
      }
    }

    @Override
    public Void visitVariable(VariableTree node, VisitorState state) {
      if (getSymbol(node).getKind() == ElementKind.LOCAL_VARIABLE) {
        knownLocalVars.add(node.getName().toString());
      } else {
        knownFields.add(node.getName().toString());
      }
      return super.visitVariable(node, state);
    }

    @Override
    public Void visitMethod(MethodTree node, VisitorState state) {
      knownLocalVars.clear();
      return super.visitMethod(node, state);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      VisitorState nowState = state.withPath(TreePath.getPath(state.getPath(), tree));

      if (!inShadowClass && shadowStaticMatcher.matches(tree, nowState)) {
        // Replace ShadowXxx.method() with Xxx.method() where possible...
        JCFieldAccess methodSelect = (JCFieldAccess) tree.getMethodSelect();
        ClassSymbol owner = (ClassSymbol) methodSelect.sym.owner;

        ClassType shadowedClass = determineShadowedClassName(owner, nowState);
        String shadowedTypeName =
            SuggestedFixes.prettyType(state, possibleFixes.fixBuilder, shadowedClass);
        possibleFixes.fixByReplacing(methodSelect.selected, shadowedTypeName);
      }

      if (!inShadowClass && shadowOfMatcher.matches(tree, nowState)) {
        matchedShadowOf(tree, nowState);
      }

      return super.visitMethodInvocation(tree, nowState);
    }
  }

  private static boolean isInSyntheticShadowAccess(VisitorState state) {
    Tree myParent = state.getPath().getParentPath().getLeaf();
    if (myParent instanceof JCFieldAccess) {
      JCFieldAccess myParentFieldAccess = (JCFieldAccess) myParent;
      return (myParentFieldAccess.selected.type instanceof UnknownType);
    }
    return false;
  }

  private static JCMethodInvocation createSyntheticShadowAccess(
      MethodInvocationTree shadowOfCall,
      String newFieldName,
      Symbol originalSymbol,
      VisitorState state) {
    TreeMaker treeMaker = state.getTreeMaker();

    Symbol newSymbol = createSymbol(originalSymbol, state.getName(newFieldName),
        ((JCExpression) shadowOfCall.getArguments().get(0)).type);

    JCExpression methodSelect = (JCExpression) shadowOfCall.getMethodSelect();
    if (methodSelect instanceof JCIdent) {
      // clone so start pos can be changed...
      methodSelect = treeMaker.Ident(((JCIdent) shadowOfCall.getMethodSelect()).sym);
    }
    JCMethodInvocation callShadowOf =
        treeMaker.Apply(
            null,
            methodSelect,
            com.sun.tools.javac.util.List.of(createIdent(treeMaker, newSymbol)));
    callShadowOf.type = ((JCMethodInvocation) shadowOfCall).type;
    return callShadowOf;
  }

  private static Symbol createSymbol(Symbol oldSymbol, Name newName, Type newType) {
    Symbol newSymbol = oldSymbol.clone(oldSymbol.owner);
    newSymbol.name = newName;
    newSymbol.type = newType;
    return newSymbol;
  }

  private static JCIdent createIdent(TreeMaker treeMaker, Symbol symbol) {
    JCIdent newFieldIdent = treeMaker.Ident(symbol.name);
    newFieldIdent.type = symbol.type;
    newFieldIdent.sym = symbol;
    return newFieldIdent;
  }

  private static boolean isMethodParam(Symbol fieldSymbol, TreePath path) {
    JCMethodDecl enclosingMethodDecl = ASTHelpers.findEnclosingNode(path, JCMethodDecl.class);
    if (enclosingMethodDecl != null) {
      for (JCVariableDecl param : enclosingMethodDecl.getParameters()) {
        if (getSymbol(param).equals(fieldSymbol)) {
          return true;
        }
      }
    }
    return false;
  }

  private static Type getUpperBound(Type type, VisitorState state) {
    return ASTHelpers.getUpperBound(type.tsym.type, Types.instance(state.context));
  }

  private static TreePath findParentOfKind(VisitorState state, Kind kind) {
    TreePath path = state.getPath();
    while (path != null && path.getLeaf().getKind() != kind) {
      path = path.getParentPath();
    }
    return path;
  }

  private static Type getExpressionType(ExpressionTree shadowOfArg) {
    Type shadowOfArgType;
    if (shadowOfArg instanceof JCNewClass) {
      shadowOfArgType = ((JCNewClass) shadowOfArg).type;
    } else if (shadowOfArg instanceof JCTree) {
      shadowOfArgType = ((JCTree) shadowOfArg).type;
    } else {
      throw new RuntimeException("huh? " + shadowOfArg.getClass() + " for " + shadowOfArg);
    }
    return shadowOfArgType;
  }

  private static String pickNewName(
      ExpressionTree shadowOfArg, String oldVarName, ShadowInliner shadowInliner) {
    String newVarName = oldVarName;

    if (shadowOfArg.getKind() == Kind.IDENTIFIER) {
      // no need to worry about a name collision in this case...
      return shadowOfArg.toString();
    } else if (newVarName.equals("shadow")) {
      newVarName = varNameFromType(getExpressionType(shadowOfArg));
    } else if (newVarName.startsWith("shadow")) {
      newVarName = newVarName.substring(6, 7).toLowerCase() + newVarName.substring(7);
    } else if (newVarName.endsWith("Shadow")) {
      newVarName = newVarName.substring(0, newVarName.length() - "Shadow".length());
    }

    // if the new name is already in use, find a unique name...
    String origNewVarName = newVarName;
    for (int i = 2;
        shadowInliner.knownFields.contains(newVarName)
            || shadowInliner.knownLocalVars.contains(newVarName);
        i++) {
      newVarName = origNewVarName + i;
    }

    return newVarName;
  }

  private static String varNameFromType(Type type) {
    String simpleName = type.tsym.name.toString();
    return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
  }

  private static ClassType determineShadowedClassName(ClassSymbol owner, VisitorState state) {
    for (Compound compound : owner.getAnnotationMirrors()) {
      if (Implements.class.getName().equals(compound.getAnnotationType().toString())) {
        for (Entry<MethodSymbol, Attribute> entry : compound.getElementValues().entrySet()) {
          String key = entry.getKey().name.toString();
          Attribute value = entry.getValue();

          if (key.equals("value")) {
            TypeMirror typeMirror = valueVisitor.visit(value);
            if (!typeMirror.equals(state.getTypeFromString("void"))) {
              return (ClassType) typeMirror;
            }
          }

          if (key.equals("className")) {
            String name = classNameVisitor.visit(value);
            if (!name.isEmpty()) {
              return (ClassType) state.getTypeFromString(name);
            }
          }
        }
      }
    }

    throw new RuntimeException("couldn't determine shadowed class for " + owner);
  }

  public static AnnotationValueVisitor<TypeMirror, Void> valueVisitor =
      new SimpleAnnotationValueVisitor6<TypeMirror, Void>() {
        @Override
        public TypeMirror visitType(TypeMirror t, Void arg) {
          return t;
        }
      };

  public static AnnotationValueVisitor<String, Void> classNameVisitor =
      new SimpleAnnotationValueVisitor6<String, Void>() {
        @Override
        public String visitString(String s, Void arg) {
          return s;
        }
      };

  private static boolean isPartOfMethodInvocation(TreePath idPath) {
    Kind parentKind = idPath.getParentPath().getLeaf().getKind();
    if (parentKind == Kind.METHOD_INVOCATION) {
      // must be an argument
      return true;
    }
    if (parentKind == Kind.MEMBER_SELECT) {
      Tree maybeMethodInvocation = idPath.getParentPath().getParentPath().getLeaf();
      // likely the target of the method invocation
      return maybeMethodInvocation.getKind() == Kind.METHOD_INVOCATION;
    }
    return false;
  }

  private static boolean shouldCallDirectlyOnFramework(TreePath idPath) {
    if (idPath.getParentPath().getLeaf().getKind() == Kind.MEMBER_SELECT) {
      Tree maybeMethodInvocation = idPath.getParentPath().getParentPath().getLeaf();
      if (maybeMethodInvocation.getKind() == Kind.METHOD_INVOCATION) {
        MethodInvocationTree methodInvocation = (MethodInvocationTree) maybeMethodInvocation;
        MethodSymbol methodSym = getSymbol(methodInvocation);
        if (methodSym == null) {
          return false;
        }
        Implementation implAnnotation = methodSym.getAnnotation(Implementation.class);
        if (implAnnotation != null) {
          int minSdk = implAnnotation.minSdk();
          int maxSdk = implAnnotation.maxSdk();

          // if minSdk or maxSdk is set (or the method is marked @HiddenApi), this method might
          // not be available at every SDK level (or at all).
          return (minSdk == Implementation.DEFAULT_SDK || minSdk <= 16)
              && maxSdk == Implementation.DEFAULT_SDK
              && methodSym.getAnnotation(HiddenApi.class) == null;
        }
      }
    }
    return false;
  }

  /**
   * Renames the given {@link Symbol} and its usages in the current compilation unit to {@code
   * newName}.
   */
  static VariableFixer fixVar(Symbol symbol, VisitorState state, PossibleFixes possibleFixes) {
    return new VariableFixer(symbol, state, possibleFixes);
  }

  private static class VariableFixer {

    private final Symbol symbol;
    private final VisitorState state;
    private final PossibleFixes possibleFixes;
    private boolean renameUses = true;
    private String newName;
    private String newTypeName;

    public VariableFixer(Symbol symbol, VisitorState state, PossibleFixes possibleFixes) {
      this.symbol = symbol;
      this.state = state;
      this.possibleFixes = possibleFixes;
    }

    VariableFixer setName(String newName) {
      this.newName = newName;
      return this;
    }

    VariableFixer setTypeName(String newTypeName) {
      this.newTypeName = newTypeName;
      return this;
    }

    VariableFixer setRenameUses(boolean renameUses) {
      this.renameUses = renameUses;
      return this;
    }

    void modify() {
      new TreePathScanner<Void, Void>() {
        @Override
        public Void visitVariable(VariableTree variableTree, Void v) {
          if (getSymbol(variableTree).equals(symbol)) {
            String name = variableTree.getName().toString();
            // For a lambda parameter without explicit type, it will return null.
            String source = state.getSourceForNode(variableTree.getType());
            if (newTypeName != null) {
              possibleFixes.fixByReplacing(variableTree.getType(), newTypeName);
            }

            if (newName != null && !newName.equals(name)) {
              int typeLength = source == null ? 0 : source.length();
              int pos =
                  ((JCTree) variableTree).getStartPosition()
                      + state.getSourceForNode(variableTree).indexOf(name, typeLength);
              possibleFixes.fixByReplacing(pos, pos + name.length(), newName);
            }
          }

          return super.visitVariable(variableTree, v);
        }
      }.scan(state.getPath().getCompilationUnit(), null);

      if (newName != null && renameUses) {
        ((JCTree) state.getPath().getCompilationUnit())
            .accept(
                new com.sun.tools.javac.tree.TreeScanner() {
                  @Override
                  public void visitIdent(JCTree.JCIdent tree) {
                    if (symbol.equals(getSymbol(tree))) {
                      possibleFixes.fixByReplacing(tree, newName);
                    }
                  }
                });
      }
    }

    void delete() {
      new TreePathScanner<Void, Void>() {
        @Override
        public Void visitVariable(VariableTree variableTree, Void v) {
          if (getSymbol(variableTree).equals(symbol)) {
            possibleFixes.fixByDeleting(variableTree);
          }

          return super.visitVariable(variableTree, v);
        }
      }.scan(state.getPath().getCompilationUnit(), null);
    }
  }

  private static Tree removeCastIfUnnecessary(JCTypeCast cast, VisitorState state) {
    if (cast.type.tsym.equals(cast.expr.type.tsym)) {
      Tree grandparent = findParent(cast, state);
      switch (grandparent.getKind()) {
        case VARIABLE:
          JCVariableDecl variableDecl = (JCVariableDecl) grandparent;
          variableDecl.init = cast.expr;
          break;
        case ASSIGNMENT:
          JCAssign assignment = (JCAssign) grandparent;
          assignment.rhs = cast.expr;
          break;
        default:
          // ok
      }

      // point to the expression that was previously being cast
      return grandparent;
    } else {
      return cast;
    }
  }

  private static Tree findParent(Tree node, VisitorState state) {
    return TreePath.getPath(state.getPath().getCompilationUnit(), node).getParentPath().getLeaf();
  }

  static class PossibleFixes {

    private final Builder fixBuilder;
    private final JCCompilationUnit compilationUnit;
    private final Map<Tree, PossibleFix> map = new HashMap<>();
    private final List<PossibleFix> list = new ArrayList<>();

    public PossibleFixes(Builder builder, JCCompilationUnit compilationUnit) {
      fixBuilder = builder;
      this.compilationUnit = compilationUnit;
    }

    public void put(Tree tree, PossibleFix possibleFix) {
      if (tree != null) {
        PossibleFix priorFix = map.put(tree, possibleFix);
        if (priorFix != null) {
          list.remove(priorFix);
        }
      }

      list.add(possibleFix);
    }

    public void removeFixFor(Tree tree) {
      PossibleFix possibleFix = map.remove(tree);
      list.remove(possibleFix);
    }

    public void fixByReplacing(Tree tree, String value) {
      put(tree, new ReplacementFix(tree, value));
    }

    public void fixByDeleting(Tree tree) {
      put(tree, new DeletionFix(tree));
    }

    public void fixByReplacing(int startPos, int endPos, String replaceWith) {
      put(null, new PositionalReplacementFix(startPos, endPos, replaceWith));
    }

    public Fix getFix() {
      for (PossibleFix possibleFix : list) {
        possibleFix.applyFix(fixBuilder);
      }

      return fixBuilder.build();
    }

    abstract class PossibleFix {
      protected final Tree tree;
      protected final int startPosition;
      protected final int endPosition;
      private final Throwable trace;

      PossibleFix(Tree tree) {
        this.tree = tree;

        DiagnosticPosition position = (DiagnosticPosition) tree;
        this.startPosition = position.getStartPosition();
        this.endPosition = position.getEndPosition(compilationUnit.endPositions);

        this.trace = new RuntimeException();
      }

      PossibleFix(int startPosition, int endPosition) {
        this.tree = null;

        this.startPosition = startPosition;
        this.endPosition = endPosition;

        this.trace = new RuntimeException();
      }

      abstract void applyFix(Builder fixBuilder);

      @Override
      public String toString() {
        return "PossibleFix{"
            + "tree="
            + tree
            + ", startPosition="
            + startPosition
            + ", endPosition="
            + endPosition
            + '}';
      }
    }

    class ReplacementFix extends PossibleFix {

      private final String replaceWith;

      public ReplacementFix(Tree tree, String replaceWith) {
        super(tree);
        this.replaceWith = replaceWith;
      }

      @Override
      void applyFix(Builder fixBuilder) {
        fixBuilder.replace(tree, replaceWith);
      }

      @Override
      public String toString() {
        return super.toString() + " [replace with " + replaceWith + "]";
      }
    }

    private class PositionalReplacementFix extends PossibleFix {

      private final String replaceWith;

      public PositionalReplacementFix(int startPos, int endPos, String replaceWith) {
        super(startPos, endPos);
        this.replaceWith = replaceWith;
      }

      @Override
      void applyFix(Builder fixBuilder) {
        fixBuilder.replace(startPosition, endPosition, replaceWith);
      }

      @Override
      public String toString() {
        return super.toString() + " [replace with " + replaceWith + "]";
      }
    }

    class DeletionFix extends PossibleFix {

      public DeletionFix(Tree tree) {
        super(tree);
      }

      @Override
      void applyFix(Builder fixBuilder) {
        fixBuilder.delete(tree);
      }

      @Override
      public String toString() {
        return super.toString() + " [delete]";
      }
    }
  }
}
