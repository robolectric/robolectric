/*
 * Copyright 2016 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robolectric.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.ANDROID;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;

import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.DCTree.DCDocComment;
import com.sun.tools.javac.tree.DCTree.DCReference;
import com.sun.tools.javac.tree.DCTree.DCStartElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** @author christianw@google.com (Christian Williams) */
@BugPattern(
    name = "RobolectricShadow",
    summary = "Robolectric @Implementation methods should be protected.",
    category = ANDROID,
    severity = SUGGESTION,
    documentSuppression = false,
    tags = StandardTags.REFACTORING,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class RobolectricShadow extends BugChecker implements ClassTreeMatcher {
  private static final Matcher<ClassTree> implementsClassMatcher =
      hasAnnotation(Implements.class);

  private static final Matcher<MethodTree> implementationMethodMatcher =
      hasAnnotation(Implementation.class);

  @Override
  public Description matchClass(ClassTree classTree, VisitorState state) {
    List<Optional<SuggestedFix>> fixes = new ArrayList<>();

    if (implementsClassMatcher.matches(classTree, state)) {
      JavacTrees trees = JavacTrees.instance(state.context);

      new TreePathScanner<Void, Void>() {
        @Override
        public Void visitMethod(MethodTree methodTree, Void aVoid) {
          if (implementationMethodMatcher.matches(methodTree, state)) {
            processImplementationMethod(methodTree);
          }
          return super.visitMethod(methodTree, aVoid);
        }

        private void processImplementationMethod(MethodTree methodTree) {
          ModifiersTree modifiersTree = methodTree.getModifiers();
          Set<Modifier> modifiers = modifiersTree.getFlags();
          if (!modifiers.contains(Modifier.PROTECTED)) {
            fixes.add(
                SuggestedFixes.removeModifiers(
                    methodTree, state, Modifier.PUBLIC, Modifier.PRIVATE));
            fixes.add(SuggestedFixes.addModifiers(methodTree, state, Modifier.PROTECTED));
          }

          DocCommentTree commentTree = trees.getDocCommentTree(getCurrentPath());
          if (commentTree != null) {
            DocTreePath docTrees = new DocTreePath(getCurrentPath(), commentTree);
            new DocTreeSymbolScanner(trees, fixes).scan(docTrees, null);
          }
        }
      }.scan(state.getPath(), null);
    }

    SuggestedFix.Builder builder = SuggestedFix.builder();
    for (Optional<SuggestedFix> fix : fixes) {
      fix.ifPresent(builder::merge);
    }

    return describeMatch(classTree, builder.build());
  }

  static final class DocTreeSymbolScanner extends DocTreePathScanner<Void, Void> {
    private final JavacTrees trees;
    private final List<Optional<SuggestedFix>> fixes;

    DocTreeSymbolScanner(JavacTrees trees, List<Optional<SuggestedFix>> fixes) {
      this.trees = trees;
      this.fixes = fixes;
    }

    @Override
    public Void visitStartElement(StartElementTree startElementTree, Void aVoid) {
      if (startElementTree.getName().toString().equalsIgnoreCase("p")) {
        DCStartElement node = (DCStartElement) startElementTree;

        DocTreePath path = getCurrentPath();
        int start = (int) node.getSourcePosition((DCDocComment) path.getDocComment()) + node.pos;
        int end = node.getEndPos((DCDocComment) getCurrentPath().getDocComment());

        fixes.add(Optional.of(SuggestedFix.replace(start, end, "")));

        // int endPos = startPos + node.getSignature().length();
        // String oldName = node.getSignature();
        // int idx = oldName.indexOf('#');
        // if (idx != -1) {
        //   startPos += idx + 1;
        // }
        // state.reportMatch(
        //     describeMatch(
        //         state.getPath().getLeaf(), SuggestedFix.replace(startPos, endPos, with)));

        // int start = element.pos;
        // fixes.add(Optional.of(SuggestedFix.replace(start, element.getEndPos())));
      }
      return super.visitStartElement(startElementTree, aVoid);
    }

    @Override
    public Void visitEndElement(EndElementTree endElementTree, Void aVoid) {
      return super.visitEndElement(endElementTree, aVoid);
    }

    @Override
    public Void visitText(TextTree textTree, Void aVoid) {
      System.out.println("textTree = " + textTree);
      return super.visitText(textTree, aVoid);
    }

    @Override
    public Void visitReference(ReferenceTree referenceTree, Void sink) {
      // do this first, it attributes the referenceTree as a side-effect
      trees.getElement(getCurrentPath());
      com.sun.source.util.TreeScanner<Void, Void> nonRecursiveScanner =
          new com.sun.source.util.TreeScanner<Void, Void>() {
            @Override
            public Void visitIdentifier(IdentifierTree tree, Void sink) {
              Symbol sym = ASTHelpers.getSymbol(tree);
              if (sym != null) {
                System.out.println("sym = " + sym);
              }
              return null;
            }
          };
      DCReference reference = (DCReference) referenceTree;
      nonRecursiveScanner.scan(reference.qualifierExpression, sink);
      nonRecursiveScanner.scan(reference.paramTypes, sink);
      return null;
    }
  }
}
