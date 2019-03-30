"""Common blaze build functions for tests that run on emulator too."""

load("//tools/build_defs/android:rules.bzl", "android_test")

def android_multidevice_test(name, target_devices, **kwargs):
    """Generates a android_test rule for each given device.

    Args:
      name: Name prefix to use for the rules. The name of the generated rules will follow:
        name + target_device[-6:] eg name_15_x86
      target_devices: array of device targets
      **kwargs: arguments to pass to generated android_test rules
    """
    for device in target_devices:
        android_test(
            name = name + "_" + device[-6:],
            aapt_version = "aapt",
            target_devices = [device],
            tags = [name],
            **kwargs
        )

    native.test_suite(name = name, tags = [name])
