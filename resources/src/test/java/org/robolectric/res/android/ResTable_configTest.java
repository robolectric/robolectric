package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResTable_configTest {

  @Test
  public void testLocale() {
    ResTable_config resTable_config = new ResTable_config();
    resTable_config.language[0] = 'e';
    resTable_config.language[1] = 'n';
    resTable_config.country[0] = 'u';
    resTable_config.country[1] = 'k';

    assertThat(resTable_config.locale()).isEqualTo(('e' << 24) | ('n' << 16) | ('u' << 8) | 'k');
  }

  @Test
  public void getBcp47Locale_shouldReturnCanonicalizedTag() {
    ResTable_config resTable_config = new ResTable_config();
    resTable_config.language[0] = 'j';
    resTable_config.language[1] = 'a';
    resTable_config.country[0] = 'j';
    resTable_config.country[1] = 'p';

    assertThat(resTable_config.getBcp47Locale(/* canonicalize= */ true)).isEqualTo("ja-jp");
  }

  @Test
  public void getBcp47Locale_philippines_shouldReturnFil() {
    ResTable_config resTable_config = new ResTable_config();
    resTable_config.language[0] = 't';
    resTable_config.language[1] = 'l';
    resTable_config.country[0] = 'p';
    resTable_config.country[1] = 'h';

    assertThat(resTable_config.getBcp47Locale(/* canonicalize= */ true)).isEqualTo("fil-ph");
  }

  @Test
  public void fromDtoH_preservesMnc() {
    ResTable_config config = new ResTable_config();
    config.mnc = 0xFFFF;

    assertThat(ResTable_config.fromDtoH(config).mnc).isEqualTo(0xFFFF);
  }

  @Test
  public void valueKey_isEqualForEqualConfigurations() {
    ResTable_config config = new ResTable_config();
    config.packLanguage("en");
    config.density = 160;
    ResTable_config sameConfig = new ResTable_config();
    sameConfig.packLanguage("en");
    sameConfig.density = 160;

    assertThat(config.valueKey()).isEqualTo(sameConfig.valueKey());
    assertThat(config.valueKey().hashCode()).isEqualTo(sameConfig.valueKey().hashCode());
  }

  @Test
  public void valueKey_differsWhenAnyFieldDiffers() throws Exception {
    for (Field field : ResTable_config.class.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      field.setAccessible(true);
      ResTable_config changed = new ResTable_config();
      change(field, changed);

      assertWithMessage(field.getName())
          .that(changed.valueKey())
          .isNotEqualTo(new ResTable_config().valueKey());
    }
  }

  @Test
  public void valueKey_isNotAffectedByLaterChanges() throws Exception {
    for (Field field : ResTable_config.class.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      field.setAccessible(true);
      ResTable_config config = new ResTable_config();
      if (field.get(config) == null) {
        field.set(config, new byte[1]);
      }
      Object key = config.valueKey();
      Object unchangedKey = config.valueKey();
      change(field, config);

      assertWithMessage(field.getName()).that(key).isEqualTo(unchangedKey);
      assertWithMessage(field.getName()).that(config.valueKey()).isNotEqualTo(key);
    }
  }

  /** Changes the value of a field, changing an array's contents if the field can't be set. */
  private static void change(Field field, ResTable_config config) throws Exception {
    Class<?> type = field.getType();
    if (type == int.class) {
      field.setInt(config, field.getInt(config) + 1);
    } else if (type == short.class) {
      field.setShort(config, (short) (field.getShort(config) + 1));
    } else if (type == byte.class) {
      field.setByte(config, (byte) (field.getByte(config) + 1));
    } else if (type == boolean.class) {
      field.setBoolean(config, !field.getBoolean(config));
    } else if (type == byte[].class) {
      byte[] array = (byte[]) field.get(config);
      if (array == null) {
        field.set(config, new byte[1]);
      } else {
        array[0]++;
      }
    } else {
      throw new AssertionError("Unexpected field type " + type + " for " + field.getName());
    }
  }
}
