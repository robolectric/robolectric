public @interface Perf {
  int warmUp() default 3;

  int times() default 10;

  int intervalMs() default 20;
}
