# Integration tests to verify framework behavior is equivalent on Robolectric Vs Android

load("//tools/build_defs/android:rules.bzl", "android_local_test")

licenses(["notice"])  # MIT

load("//third_party/java_src/robolectric/integration_tests/ctesque:build_defs.bzl", "android_multidevice_test")

DEVICES = [
    "//tools/android/emulated_devices/generic_phone:android_16_x86",
    "//tools/android/emulated_devices/generic_phone:android_17_x86",
    "//tools/android/emulated_devices/generic_phone:android_18_x86",
    "//tools/android/emulated_devices/generic_phone:android_19_x86",
    "//tools/android/emulated_devices/generic_phone:android_21_x86",
    "//tools/android/emulated_devices/generic_phone:android_22_x86",
    "//tools/android/emulated_devices/generic_phone:android_23_x86",
    "//tools/android/emulated_devices/generic_phone:android_24_x86",
    "//tools/android/emulated_devices/generic_phone:android_25_x86",
    # use google image for 26 since android image is not final 26
    "//tools/android/emulated_devices/generic_phone:google_26_x86",
    "//tools/android/emulated_devices/generic_phone:android_27_x86",
    "//tools/android/emulated_devices/generic_phone:google_28_x86",
]

java_library(
    name = "robolectric_prop",
    resources = ["src/test/resources/android/robolectric.properties"],
)

android_local_test(
    name = "ResourcesTest",
    size = "medium",
    srcs = ["src/test/java/android/content/res/ResourcesTest.java"],
    custom_package = "android.content.res",
    jvm_flags = ["-Drobolectric.resourcesMode=\"both\""],
    deps = [
        ":robolectric_prop",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/android_libs/guava_jdk5:collect",
        "//third_party/java/junit",
        "//third_party/java/truth",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_multidevice_test(
    name = "ResourcesTest_android",
    size = "medium",
    srcs = ["src/test/java/android/content/res/ResourcesTest.java"],
    binary_under_test = "//third_party/java_src/robolectric/robolectric:test_app",
    custom_package = "org.robolectric.integration_tests.ctesque",
    manifest = "AndroidManifest.xml",
    shard_count = 4,
    target_devices = DEVICES,
    deps = [
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/android_libs/guava_jdk5:collect",
        "//third_party/java/junit:junit-android",
        "//third_party/java/truth:truth-android",
        "//third_party/java_src/robolectric/annotations",
        "//third_party/java_src/robolectric/shadowapi",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_local_test(
    name = "AssetManagerTest",
    size = "medium",
    srcs = ["src/test/java/android/content/res/AssetManagerTest.java"],
    custom_package = "android.content.res",
    deps = [
        ":robolectric_prop",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/android_libs/guava_jdk5:io",
        "//third_party/java/junit",
        "//third_party/java/truth",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_multidevice_test(
    name = "AssetManagerTest_android",
    size = "medium",
    srcs = ["src/test/java/android/content/res/AssetManagerTest.java"],
    binary_under_test = "//third_party/java_src/robolectric/robolectric:test_app",
    custom_package = "org.robolectric.integration_tests.ctesque",
    manifest = "AndroidManifest.xml",
    target_devices = DEVICES,
    deps = [
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/android_libs/guava_jdk5:io",
        "//third_party/java/junit:junit-android",
        "//third_party/java/truth:truth-android",
        "//third_party/java_src/robolectric/annotations",
        "//third_party/java_src/robolectric/shadowapi",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_local_test(
    name = "ThemeTest",
    size = "medium",
    srcs = ["src/test/java/android/content/res/ThemeTest.java"],
    custom_package = "android.content.res",
    deps = [
        ":robolectric_prop",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/junit",
        "//third_party/java/truth",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_multidevice_test(
    name = "ThemeTest_android",
    size = "medium",
    srcs = ["src/test/java/android/content/res/ThemeTest.java"],
    binary_under_test = "//third_party/java_src/robolectric/robolectric:test_app",
    custom_package = "org.robolectric.integration_tests.ctesque",
    manifest = "AndroidManifest.xml",
    target_devices = DEVICES,
    deps = [
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/android_libs/guava_jdk5:io",
        "//third_party/java/junit:junit-android",
        "//third_party/java/truth:truth-android",
        "//third_party/java_src/robolectric/annotations",
        "//third_party/java_src/robolectric/shadowapi",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_local_test(
    name = "MotionEventTest",
    size = "medium",
    srcs = [
        "src/test/java/android/view/MotionEventTest.java",
    ],
    custom_package = "android.view",
    manifest = "AndroidManifest.xml",
    deps = [
        ":robolectric_prop",
        "//third_party/android/androidx_test/core",
        "//third_party/android/androidx_test/ext/truth",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/junit",
        "//third_party/java/truth",
    ],
)

android_multidevice_test(
    name = "MotionEventTest_android",
    size = "medium",
    srcs = [
        "src/test/java/android/view/MotionEventTest.java",
    ],
    binary_under_test = "//third_party/java_src/robolectric/robolectric:test_app",
    custom_package = "org.robolectric.integration_tests.ctesque",
    manifest = "AndroidManifest.xml",
    target_devices = DEVICES,
    deps = [
        "//third_party/android/androidx_test/core",
        "//third_party/android/androidx_test/ext/truth",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/junit:junit-android",
        "//third_party/java/truth:truth-android",
        "//third_party/java_src/robolectric/annotations",
        "//third_party/java_src/robolectric/shadowapi",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_local_test(
    name = "SettingsTest",
    size = "medium",
    srcs = [
        "src/test/java/android/provider/SettingsTest.java",
    ],
    custom_package = "android.view",
    manifest = "AndroidManifest.xml",
    deps = [
        ":robolectric_prop",
        "//third_party/android/androidx_test/core",
        "//third_party/android/androidx_test/ext/truth",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/junit",
        "//third_party/java/truth",
    ],
)

# TODO(jongerrish): Why is android.permission.WRITE_SECURE_SETTINGS not working on forge?
#android_multidevice_test(
#    name = "SettingsTest_android",
#    size = "medium",
#    srcs = [
#        "src/test/java/android/provider/SettingsTest.java",
#    ],
#    args = [
#        "--clear_package_data",
#    ],
#    binary_under_test = "//third_party/java_src/robolectric/robolectric:test_app",
#    custom_package = "org.robolectric.integration_tests.ctesque",
#    manifest = "AndroidManifest.xml",
#    target_devices = DEVICES,
#    deps = [
#        "//third_party/android/androidx_test/core",
#        "//third_party/android/androidx_test/ext/truth",
#        "//third_party/android/androidx_test/runner/android_junit_runner",
#        "//third_party/java/jsr305_annotations",
#        "//third_party/java/junit:junit-android",
#        "//third_party/java/truth:truth-android",
#        "//third_party/java_src/robolectric/annotations",
#        "//third_party/java_src/robolectric/shadowapi",
#        "//third_party/java_src/robolectric/testapp",
#    ],
#)

android_local_test(
    name = "PathTest",
    srcs = ["src/test/java/android/graphics/PathTest.java"],
    custom_package = "android.graphics",
    deps = [
        ":robolectric_prop",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/android_libs/guava_jdk5:collect",
        "//third_party/java/junit",
        "//third_party/java/truth",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_multidevice_test(
    name = "PathTest_android",
    size = "medium",
    srcs = [
        "src/test/java/android/graphics/PathTest.java",
    ],
    binary_under_test = "//third_party/java_src/robolectric/robolectric:test_app",
    custom_package = "org.robolectric.integration_tests.ctesque",
    manifest = "AndroidManifest.xml",
    target_devices = DEVICES,
    deps = [
        "//third_party/android/androidx_test/core",
        "//third_party/android/androidx_test/ext/truth",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/junit:junit-android",
        "//third_party/java/truth:truth-android",
        "//third_party/java_src/robolectric/annotations",
        "//third_party/java_src/robolectric/shadowapi",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_local_test(
    name = "BitmapTest",
    srcs = ["src/test/java/android/graphics/BitmapTest.java"],
    custom_package = "android.graphics",
    deps = [
        ":robolectric_prop",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/android_libs/guava_jdk5:collect",
        "//third_party/java/junit",
        "//third_party/java/truth",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_multidevice_test(
    name = "BitmapTest_android",
    size = "medium",
    srcs = [
        "src/test/java/android/graphics/BitmapTest.java",
    ],
    binary_under_test = "//third_party/java_src/robolectric/robolectric:test_app",
    custom_package = "org.robolectric.integration_tests.ctesque",
    manifest = "AndroidManifest.xml",
    target_devices = DEVICES,
    deps = [
        "//third_party/android/androidx_test/core",
        "//third_party/android/androidx_test/ext/truth",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/junit:junit-android",
        "//third_party/java/truth:truth-android",
        "//third_party/java_src/robolectric/annotations",
        "//third_party/java_src/robolectric/shadowapi",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_local_test(
    name = "PackageManagerTest",
    srcs = ["src/test/java/android/content/pm/PackageManagerTest.java"],
    custom_package = "android.content.pm",
    deps = [
        ":robolectric_prop",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/android_libs/guava_jdk5:collect",
        "//third_party/java/junit",
        "//third_party/java/truth",
        "//third_party/java_src/robolectric/testapp",
    ],
)

android_multidevice_test(
    name = "PackageManagerTest_android",
    size = "medium",
    srcs = [
        "src/test/java/android/content/pm/PackageManagerTest.java",
    ],
    binary_under_test = "//third_party/java_src/robolectric/robolectric:test_app",
    custom_package = "org.robolectric.integration_tests.ctesque",
    manifest = "AndroidManifest.xml",
    target_devices = DEVICES,
    deps = [
        "//third_party/android/androidx_test/core",
        "//third_party/android/androidx_test/ext/truth",
        "//third_party/android/androidx_test/runner/android_junit_runner",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/junit:junit-android",
        "//third_party/java/truth:truth-android",
        "//third_party/java_src/robolectric/annotations",
        "//third_party/java_src/robolectric/shadowapi",
        "//third_party/java_src/robolectric/testapp",
    ],
)
