# Robolectric build logic

`build-logic` holds the convention plugins that configure Robolectric's modules — the Java and
Kotlin module defaults, the shadow processor wiring, Spotless, the Gradle managed devices used for
instrumentation tests, and publishing. Modules apply them by id, for example
`org.robolectric.gradle.RoboJavaModulePlugin`.

## Error Prone issue report

Error Prone reports its findings as compilation errors, so a build stops at the first module that
has any and never says what the remaining modules hold. That is why `SKIP_ERRORPRONE=true` is the
usual way to work locally — but it hides the issues rather than counting them.

`ErrorProneReportPlugin` collects them instead. It is applied automatically wherever Error Prone
itself is (from `RoboJavaModulePlugin`) and does nothing unless `-PerrorproneReport` is set, so it
has no effect on normal builds.

### Usage

```shell
./gradlew -PerrorproneReport --continue compileTestJava
```

`collectErrorProneIssues` does not need naming: it finalizes every compile task, so it runs once
they have. `--continue` does matter — Error Prone findings fail the compile task, and without it the
build stops at the first module and the report only covers that one.

Do not set `SKIP_ERRORPRONE=true` for this run — it disables Error Prone altogether, and the report
comes back empty.

### Output

Each compile task tees the compiler's messages to
`<module>/build/reports/errorprone/<task>.log`, and `collectErrorProneIssues` parses those into
`build/reports/errorprone/`:

| File | Contents |
| --- | --- |
| `summary.txt` | Totals, the errors listed first, a count per check, then every issue |
| `issues.json` | One object per issue — `check`, `severity`, `file`, `line`, `message` |

Paths are relative to the repository root. `summary.txt` opens with the shape of the problem:

```
1410 Error Prone issue(s): 30 error(s), 1380 warning(s)

Errors (these fail the build):

  robolectric/src/test/java/org/robolectric/android/XmlResourceParserImplTest.java:297
    [NullArgumentForNonNullParameter] Null is not permitted for parameter 'arg0' of method 'isEqualTo'.
...

By check:

  EffectivelyPrivate     346
  MissingSummary         148
  ReferenceEquality      110
```

Only the errors fail a build; the warnings are reported so they can be triaged, not because they
need fixing now.

### Notes

Javac's own lint warnings (`[removal]`, `[deprecation]`) arrive on the same stream as Error Prone's.
They are told apart by case — lowercase is javac, UpperCamelCase is an Error Prone check — and
counted separately at the end of `summary.txt` so they do not inflate the totals.

The per-module logs are written as the compiler emits them rather than when the task ends, because a
compile that reports Error Prone errors fails and never reaches the end of the task.

Do not read those per-module logs as a per-module breakdown. Gradle's task logging is process-global
while tasks overlap, so a parallel build can capture one module's diagnostics into another module's
log. Which module a diagnostic came from is recoverable from its path, so the aggregate simply
discards duplicates; the log files are only a transport.
