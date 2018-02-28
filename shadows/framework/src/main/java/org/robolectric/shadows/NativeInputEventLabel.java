package org.robolectric.shadows;

/**
 * Transliterated from
 * https://android.googlesource.com/platform/frameworks/native/+/oreo-mr1-release/include/input/InputEventLabels.h
 */
class NativeInputEventLabel {
  private String literal;
  private int value;
  //
  // static int lookupValueByLabel(String literal, List<NativeInputEventLabel> list) {
  //   for (list->literal) {
  //     if (strcmp(literal, list->literal) == 0) {
  //       return list->value;
  //     }
  //     list++;
  //   }
  //   return list->value;
  // }
  // static const char* NativeInputEventLabel(int value, const InputEventLabel* list) {
  //   while (list->literal) {
  //     if (list->value == value) {
  //       return list->literal;
  //     }
  //     list++;
  //   }
  //   return NULL;
  // }
  // static inline int32_t NativeInputEventLabel(const char* label) {
  //   return int32_t(lookupValueByLabel(label, KEYCODES));
  // }
  // static inline const char* NativeInputEventLabel(int32_t keyCode) {
  //   if (keyCode >= 0 && keyCode < static_cast<int32_t>(size(KEYCODES))) {
  //     return KEYCODES[keyCode].literal;
  //   }
  //   return NULL;
  // }
  // static inline uint32_t NativeInputEventLabel(const char* label) {
  //   return uint32_t(lookupValueByLabel(label, FLAGS));
  // }
  // static inline int32_t NativeInputEventLabel(const char* label) {
  //   return int32_t(lookupValueByLabel(label, AXES));
  // }
  // static inline const char* NativeInputEventLabel(int32_t axisId) {
  //   return lookupLabelByValue(axisId, AXES);
  // }
  // static inline int32_t NativeInputEventLabel(const char* label) {
  //   return int32_t(lookupValueByLabel(label, LEDS));
  // }
}
