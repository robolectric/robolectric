package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.robolectric.annotation.processing.Utils.DEFAULT_OPTS;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.CompileTester.LineClause;
import com.google.testing.compile.CompileTester.SuccessfulCompilationClause;
import com.google.testing.compile.CompileTester.UnsuccessfulCompilationClause;
import com.google.testing.compile.JavaFileObjects;
import java.util.HashMap;
import java.util.Map;
import javax.tools.JavaFileObject;
import org.robolectric.annotation.processing.RobolectricProcessor;
import org.robolectric.annotation.processing.Utils;

public final class SingleClassSubject extends Subject {

  public static Subject.Factory<SingleClassSubject, String> singleClass() {
    return SingleClassSubject::new;
  }

  public static Subject.Factory<SingleClassSubject, String> singleClass(
      Map<String, String> processorOpts, String sdkLocation, int sdkInt) {
    return (FailureMetadata failureMetadata, String subject) ->
        new SingleClassSubject(failureMetadata, subject, processorOpts, sdkLocation, sdkInt);
  }

  public static Subject.Factory<SingleClassSubject, String> singleClass(
      Map<String, String> processorOpts) {
    return (FailureMetadata failureMetadata, String subject) ->
        new SingleClassSubject(failureMetadata, subject, processorOpts);
  }

  JavaFileObject source;
  CompileTester tester;

  public SingleClassSubject(FailureMetadata failureMetadata, String subject) {
    this(failureMetadata, subject, DEFAULT_OPTS);
  }

  public SingleClassSubject(
      FailureMetadata failureMetadata, String subject, Map<String, String> processorOpts) {
    this(failureMetadata, subject, processorOpts, null, -1);
  }

  public SingleClassSubject(
      FailureMetadata failureMetadata,
      String subject,
      Map<String, String> processorOpts,
      String sdkLocation,
      int sdkInt) {
    super(failureMetadata, subject);
    source = JavaFileObjects.forResource(Utils.toResourcePath(subject));
    Map<String, String> opts = new HashMap<>(DEFAULT_OPTS);
    opts.putAll(processorOpts);
    tester =
        assertAbout(javaSources())
            .that(ImmutableList.of(source, Utils.SHADOW_EXTRACTOR_SOURCE))
            .processedWith(new RobolectricProcessor(opts, sdkLocation, sdkInt));
  }

  public SuccessfulCompilationClause compilesWithoutError() {
    try {
      return tester.compilesWithoutError();
    } catch (AssertionError e) {
      failWithoutActual(simpleFact(e.getMessage()));
    }
    return null;
  }

  public SingleFileClause failsToCompile() {
    try {
      return new SingleFileClause(tester.failsToCompile(), source);
    } catch (AssertionError e) {
      failWithoutActual(simpleFact(e.getMessage()));
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
        failWithoutActual(simpleFact(e.getMessage()));
      }
      return null;
    }

    public SingleFileClause withNoErrorContaining(final String messageFragment) {
      try {
        unsuccessful.withErrorContaining(messageFragment);
      } catch (AssertionError e) {
        return this;
      }
      failWithoutActual(
          simpleFact(
              "Shouldn't have found any errors containing " + messageFragment + ", but we did"));

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
          failWithoutActual(simpleFact(e.getMessage()));
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
