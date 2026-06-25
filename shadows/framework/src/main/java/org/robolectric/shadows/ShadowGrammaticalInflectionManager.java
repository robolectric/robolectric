package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.app.GrammaticalInflectionManager;
import android.content.res.Configuration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * Shadow for {@link GrammaticalInflectionManager}.
 *
 * <p>This shadow just sets the grammatical gender in the configuration when {@link
 * GrammaticalInflectionManager#setRequestedApplicationGrammaticalGender(int)} is called.
 *
 * <p>This shadow does not yet implement system-level grammatical gender.
 */
@Implements(value = GrammaticalInflectionManager.class, minSdk = UPSIDE_DOWN_CAKE)
public class ShadowGrammaticalInflectionManager {
  /**
   * The Set of valid grammatical gender values.
   *
   * <p>Copied from {@link GrammaticalInflectionManager} because it was renamed from {@code
   * VALID_GENDER_VALUES} in API 34 to {@code VALID_GRAMMATICAL_GENDER_VALUES} in API 35.
   */
  private static final ImmutableSet<Integer> VALID_GRAMMATICAL_GENDER_VALUES =
      ImmutableSet.of(
          Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED,
          Configuration.GRAMMATICAL_GENDER_NEUTRAL,
          Configuration.GRAMMATICAL_GENDER_FEMININE,
          Configuration.GRAMMATICAL_GENDER_MASCULINE);

  /** A List of valid grammatical gender qualifiers. */
  private static final ImmutableList<String> GENDER_QUALIFIERS =
      ImmutableList.of("feminine", "masculine", "neuter");

  @RealObject private GrammaticalInflectionManager realObject;

  @Implementation
  protected void setRequestedApplicationGrammaticalGender(int grammaticalGender) {
    if (!VALID_GRAMMATICAL_GENDER_VALUES.contains(grammaticalGender)) {
      throw new IllegalArgumentException("Unknown grammatical gender");
    }

    if (grammaticalGender == Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED) {
      // We can't clear the grammatical gender by applying a qualifier, because there is no way to
      // specify "not specified" as a gender qualifier.  Further, if we don't specify a gender, then
      // Configuration.updateFrom() will not update the grammatical gender.  So, we have to set
      // the grammatical gender explicitly to NOT_SPECIFIED here, then set the qualifiers to
      // trigger a configuration update.
      Configuration configuration =
          RuntimeEnvironment.getApplication().getResources().getConfiguration();
      configuration.setGrammaticalGender(Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED);
      String qualifiers = RuntimeEnvironment.getQualifiers();
      for (String qualifier : GENDER_QUALIFIERS) {
        qualifiers = qualifiers.replace("-" + qualifier, "");
      }
      RuntimeEnvironment.setQualifiers(qualifiers);
      return;
    }

    // Update the configuration to have the specified grammatical gender
    String qualifiers = convertGrammaticalGenderToQualifier(grammaticalGender);
    RuntimeEnvironment.setQualifiers("+" + qualifiers);
  }

  private static String convertGrammaticalGenderToQualifier(int grammaticalGender) {
    switch (grammaticalGender) {
      case Configuration.GRAMMATICAL_GENDER_FEMININE:
        return "feminine";
      case Configuration.GRAMMATICAL_GENDER_MASCULINE:
        return "masculine";
      case Configuration.GRAMMATICAL_GENDER_NEUTRAL:
        return "neuter";
      default:
        throw new IllegalArgumentException("Unexpected grammatical gender: " + grammaticalGender);
    }
  }
}
