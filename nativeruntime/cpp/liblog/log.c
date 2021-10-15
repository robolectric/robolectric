#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>

int __android_log_print(int prio, const char* tag, const char* fmt, ...) {
  ((void)prio);
  ((void)tag);
  ((void)fmt);

  if (prio >= 4) {
    va_list args;
    va_start(args, fmt);
    fprintf(stderr, "%s: ", tag);
    fprintf(stderr, fmt, args);
    va_end(args);
  }
  return 0;
}

int __android_log_error_write(int tag, const char* subTag, int32_t uid,
                              const char* data, uint32_t dataLen) {
  ((void)tag);
  return 0;
}

void __android_log_assert(const char* condition, const char* tag,
                                       const char* format, ...) {
  va_list args;
  va_start(args, format);
  fprintf(stderr, "%s: ", tag);
  fprintf(stderr, format, args);
  va_end(args);
  abort();
}
