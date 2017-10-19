package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.CompileTester.LineClause;
import com.google.testing.compile.CompileTester.SuccessfulCompilationClause;
import com.google.testing.compile.CompileTester.UnsuccessfulCompilationClause;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.robolectric.annotation.processing.RobolectricProcessor;

public final class SingleClassSubject extends Subject<SingleClassSubject, String> {

  public static SubjectFactory<SingleClassSubject, String> singleClass() {

    return new SubjectFactory<SingleClassSubject, String>() {

      @Override
      public SingleClassSubject getSubject(FailureStrategy failureStrategy, String source) {
        return new SingleClassSubject(failureStrategy, source);
      }
    };
  }


  JavaFileObject source;
  CompileTester tester;
  
  public SingleClassSubject(FailureStrategy failureStrategy, String subject) {
    super(failureStrategy, subject);
    source = JavaFileObjects.forResource(Utils.toResourcePath(subject));
    tester = assertAbout(javaSources())
      .that(ImmutableList.of(source, Utils.ROBO_SOURCE, Utils.SHADOW_EXTRACTOR_SOURCE))
      .processedWith(new RobolectricProcessor());
  }

  public SuccessfulCompilationClause compilesWithoutError() {
    try {
      return tester.compilesWithoutError();
    } catch (AssertionError e) {
      failWithRawMessage(e.getMessage());
    }
    return null;
  }
  
  public SingleFileClause failsToCompile() {
    try {
      return new SingleFileClause(tester.failsToCompile(), source);
    } catch (AssertionError e) {
      failWithRawMessage(e.getMessage());
    }
    return null;
  }
  
  final class SingleFileClause implements CompileTester.ChainingClause<SingleFileClause> {

    UnsuccessfulCompilationClause unsuccessful;
    JavaFileObject source;
    
    public SingleFileClause(UnsuccessfulCompilationClause unsuccessful, JavaFileObject source) {
      this.unsuccessful = unsuccessful;
      this.source = source;
    }
    
    public SingleLineClause withErrorContaining(final String messageFragment) {
      try {
        return new SingleLineClause(unsuccessful.withErrorContaining(messageFragment).in(source));
      } catch (AssertionError e) {
        failWithRawMessage(e.getMessage());
      }
      return null;
    }

    public SingleFileClause withNoErrorContaining(final String messageFragment) {
      try {
        unsuccessful.withErrorContaining(messageFragment);
      } catch (AssertionError e) {
        return this;
      }
      failWithRawMessage(
          "Shouldn't have found any errors containing " + messageFragment + ", but we did");

      return this;
    }
    
    @Override
    public SingleFileClause and() {
      return this;
    }

    final class SingleLineClause implements CompileTester.ChainingClause<SingleFileClause> {

      LineClause lineClause;
      
      public SingleLineClause(LineClause lineClause) {
        this.lineClause = lineClause;
      }
      
      public CompileTester.ChainingClause<SingleFileClause> onLine(long lineNumber) {
        try {
          lineClause.onLine(lineNumber);
          return new CompileTester.ChainingClause<SingleFileClause>() {
            @Override
            public SingleFileClause and() {
              return SingleFileClause.this;
            }
          };
        } catch (AssertionError e) {
          failWithRawMessage(e.getMessage());
        }
        return null;
      }
      
      @Override
      public SingleFileClause and() {
        return SingleFileClause.this;
      }
    
    }
  }
}
