#include <jni.h>
#include <log/log.h>

#include "unicode/locid.h"

namespace android {

extern int register_android_database_CursorWindow(JNIEnv* env);
extern int register_android_database_SQLiteConnection(JNIEnv* env);

}  // namespace android

/*
 * JNI Initialization
 */
jint JNI_OnLoad(JavaVM* jvm, void* reserved) {
  JNIEnv* env;

  ALOGV("loading JNI\n");
  // Check JNI version
  if (jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_4)) {
    ALOGE("JNI version mismatch error");
    return JNI_ERR;
  }

  if (android::register_android_database_CursorWindow(env) != JNI_VERSION_1_4 ||
      android::register_android_database_SQLiteConnection(env) !=
          JNI_VERSION_1_4) {
    ALOGE("Failure during registration");
    return JNI_ERR;
  }

  // Configuration is stored as java System properties.
  // Get a reference to System.getProperty
  jclass systemClass = env->FindClass("java/lang/System");
  jmethodID getPropertyMethod = env->GetStaticMethodID(
      systemClass, "getProperty",
      "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");

  // Set the default locale, which is required for e.g. SQLite's 'COLLATE
  // UNICODE'.
  auto stringLanguageTag = (jstring)env->CallStaticObjectMethod(
      systemClass, getPropertyMethod,
      env->NewStringUTF("robolectric.nativeruntime.languageTag"),
      env->NewStringUTF(""));
  const char* languageTag = env->GetStringUTFChars(stringLanguageTag, 0);
  int languageTagLength = env->GetStringLength(stringLanguageTag);
  if (languageTagLength > 0) {
    UErrorCode status = U_ZERO_ERROR;
    icu::Locale locale = icu::Locale::forLanguageTag(languageTag, status);
    if (U_SUCCESS(status)) {
      icu::Locale::setDefault(locale, status);
    }
    if (U_FAILURE(status)) {
      fprintf(stderr,
              "Failed to set the ICU default locale to '%s' (error code %d)\n",
              languageTag, status);
    }
  }
  env->ReleaseStringUTFChars(stringLanguageTag, languageTag);
  return JNI_VERSION_1_4;
}
