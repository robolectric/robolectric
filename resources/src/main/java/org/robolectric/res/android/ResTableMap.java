package org.robolectric.res.android;

/**
 * A single name/value mapping that is part of a complex resource entry.
 *
 * <p>frameworks/base/include/androidfw/ResourceTypes.h (struct ResTable_map)
 */
public final class ResTableMap {
  ///////////////////////////////////////////////////////////////
  // Special values for 'name' when defining attribute resources.
  ///////////////////////////////////////////////////////////////

  // This entry holds the attribute's type code.
  static final int ATTR_TYPE = 0x01000000;

  // For integral attributes, this is the minimum value it can hold.
  static final int ATTR_MIN = 0x01000001;

  // For integral attributes, this is the maximum value it can hold.
  static final int ATTR_MAX = 0x01000002;

  // Localization of this resource is can be encouraged or required with
  // an aapt flag if this is set
  static final int ATTR_L10N = 0x01000003;

  // for plural support, see android.content.res.PluralRules#attrForQuantity(int)
  public static final int ATTR_OTHER = 0x01000004;
  public static final int ATTR_ZERO = 0x01000005;
  public static final int ATTR_ONE = 0x01000006;
  public static final int ATTR_TWO = 0x01000007;
  public static final int ATTR_FEW = 0x01000008;
  public static final int ATTR_MANY = 0x01000009;

  /////////////////////////////////////////////////////
  // Bit mask of allowed types, for use with ATTR_TYPE.
  /////////////////////////////////////////////////////

  // No type has been defined for this attribute, use generic
  // type handling.  The low 16 bits are for types that can be
  // handled generically; the upper 16 require additional information
  // in the bag so can not be handled generically for TYPE_ANY.
  static final int TYPE_ANY = 0x0000FFFF;

  // Attribute holds a references to another resource.
  static final int TYPE_REFERENCE = 1 << 0;

  // Attribute holds a generic string.
  static final int TYPE_STRING = 1 << 1;

  // Attribute holds an integer value.  ATTR_MIN and ATTR_MIN can
  // optionally specify a constrained range of possible integer values.
  static final int TYPE_INTEGER = 1 << 2;

  // Attribute holds a boolean integer.
  static final int TYPE_BOOLEAN = 1 << 3;

  // Attribute holds a color value.
  static final int TYPE_COLOR = 1 << 4;

  // Attribute holds a floating point value.
  static final int TYPE_FLOAT = 1 << 5;

  // Attribute holds a dimension value, such as "20px".
  static final int TYPE_DIMENSION = 1 << 6;

  // Attribute holds a fraction value, such as "20%".
  static final int TYPE_FRACTION = 1 << 7;

  // Attribute holds an enumeration.  The enumeration values are
  // supplied as additional entries in the map.
  static final int TYPE_ENUM = 1 << 16;

  // Attribute holds a bitmaks of flags.  The flag bit values are
  // supplied as additional entries in the map.
  static final int TYPE_FLAGS = 1 << 17;

  //////////////////////////////////////////////////////
  // Enum of localization modes, for use with ATTR_L10N.
  //////////////////////////////////////////////////////
  static final int L10N_NOT_REQUIRED = 0;
  static final int L10N_SUGGESTED = 1;

  // The resource identifier defining this mapping's name.  For attribute
  // resources, 'name' can be one of the following special resource types
  // to supply meta-data about the attribute; for all other resource types
  // it must be an attribute resource.
  int nameIdent; // name->ident
  ResValue value;

  ResTableMap(int ident, ResValue value) {
    nameIdent = ident;
    this.value = value;
  }

  // Copy constructor.
  ResTableMap(ResTableMap that) {
    this(that.nameIdent, new ResValue(that.value));
  }
}