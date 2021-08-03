#include <jni.h>
#include <log/log.h>
namespace android {

extern int register_android_database_CursorWindow(JNIEnv* env);
extern int register_android_database_SQLiteConnection(JNIEnv* env);

}  // namespace android

/*
 * JNI Initialization
 */
jint JNI_OnLoad(JavaVM* jvm, void* reserved) {
  JNIEnv* e;

  ALOGV("loading JNI\n");
  // Check JNI version
  if (jvm->GetEnv(reinterpret_cast<void**>(&e), JNI_VERSION_1_4)) {
    ALOGE("JNI version mismatch error");
    return JNI_ERR;
  }

  if (android::register_android_database_CursorWindow(e) != JNI_VERSION_1_4 ||
      android::register_android_database_SQLiteConnection(e) !=
          JNI_VERSION_1_4) {
    ALOGE("Failure during registration");
    return JNI_ERR;
  }

  return JNI_VERSION_1_4;
}
