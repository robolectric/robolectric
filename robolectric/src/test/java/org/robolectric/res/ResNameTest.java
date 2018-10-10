package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResNameTest {
  @Test public void shouldQualify() throws Exception {
    assertThat(ResName.qualifyResourceName("some.package:type/name", null, null)).isEqualTo("some.package:type/name");
    assertThat(ResName.qualifyResourceName("some.package:type/name", "default.package", "deftype")).isEqualTo("some.package:type/name");
    assertThat(ResName.qualifyResourceName("*android:type/name", "default.package", "deftype")).isEqualTo("android:type/name");
    assertThat(ResName.qualifyResourceName("some.package:name", "default.package", "deftype")).isEqualTo("some.package:deftype/name");
    assertThat(ResName.qualifyResourceName("type/name", "default.package", "deftype")).isEqualTo("default.package:type/name");
    assertThat(ResName.qualifyResourceName("name", "default.package", "deftype")).isEqualTo("default.package:deftype/name");
    assertThat(ResName.qualifyResourceName("someRawString", "default.package", null)).isNull();
  }

  @Test public void shouldQualifyResNameFromString() throws Exception {
    assertThat(ResName.qualifyResName("some.package:type/name", "default_package", "default_type"))
        .isEqualTo(new ResName("some.package", "type", "name"));
    assertThat(ResName.qualifyResName("some.package:name", "default_package", "default_type"))
        .isEqualTo(new ResName("some.package", "default_type", "name"));
    assertThat(ResName.qualifyResName("type/name", "default_package", "default_type"))
        .isEqualTo(new ResName("default_package", "type", "name"));
    assertThat(ResName.qualifyResName("name", "default_package", "default_type"))
        .isEqualTo(new ResName("default_package", "default_type", "name"));
    assertThat(ResName.qualifyResName("type/package:name", "default_package", "default_type"))
        .isEqualTo(new ResName("package", "type", "name"));
  }

  @Test
  public void qualifyFromFilePathShouldExtractResourceTypeAndNameFromUnqualifiedPath() {
    final ResName actual = ResName.qualifyFromFilePath("some.package", "./res/drawable/icon.png");
    assertThat(actual.getFullyQualifiedName()).isEqualTo("some.package:drawable/icon");
  }

  @Test
  public void qualifyFromFilePathShouldExtractResourceTypeAndNameFromQualifiedPath() {
    final ResName actual = ResName.qualifyFromFilePath("some.package", "./res/drawable-hdpi/icon.png");
    assertThat(actual.getFullyQualifiedName()).isEqualTo("some.package:drawable/icon");
  }

  @Test
  public void hierarchicalNameHandlesWhiteSpace() {
    String name = "TextAppearance.AppCompat.Widget.ActionMode.Subtitle\n" +
        "    ";

    ResName resName = new ResName("org.robolectric.example", "style", name);
    assertThat(resName.name).isEqualTo("TextAppearance.AppCompat.Widget.ActionMode.Subtitle");
    assertThat(resName.type).isEqualTo("style");
    assertThat(resName.packageName).isEqualTo("org.robolectric.example");
  }

  @Test
  public void simpleNameHandlesWhiteSpace() {
    String name = "Subtitle\n" +
        "    ";

    ResName resName = new ResName("org.robolectric.example", "style", name);
    assertThat(resName.name).isEqualTo("Subtitle");
    assertThat(resName.type).isEqualTo("style");
    assertThat(resName.packageName).isEqualTo("org.robolectric.example");
  }

  @Test
  public void fullyQualifiedNameHandlesWhiteSpace() {
    String name = "android:style/TextAppearance.AppCompat.Widget.ActionMode.Subtitle\n" +
        "    ";

    ResName resName = new ResName(name);
    assertThat(resName.name).isEqualTo("TextAppearance.AppCompat.Widget.ActionMode.Subtitle");
    assertThat(resName.type).isEqualTo("style");
    assertThat(resName.packageName).isEqualTo("android");
  }

  @Test
  public void fullyQualifiedNameWithWhiteSpaceInTypeShouldBeHandledCorrectly() {
    String name = "android: string/ok";
    ResName resName = new ResName(name);

    assertThat(resName.name).isEqualTo("ok");
    assertThat(resName.type).isEqualTo("string");
    assertThat(resName.packageName).isEqualTo("android");
  }

  @Test
  public void resourceNameWithWhiteSpaceInTypeShouldBeHandledCorrectly() {
    ResName resName = new ResName("android", " string", "ok");

    assertThat(resName.name).isEqualTo("ok");
    assertThat(resName.type).isEqualTo("string");
    assertThat(resName.packageName).isEqualTo("android");
  }
}
