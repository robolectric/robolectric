package org.robolectric.res.android;

import org.robolectric.res.android.ResourceTypes.ResTable_map;

public class ResourceTable {
  public static class flag_entry
  {
    public final String name;
    public final int value;
    public final String description;

    public flag_entry(String name, int value, String description) {
      this.name = name;
      this.value = value;
      this.description = description;
    }
  };

  public static flag_entry[] gFormatFlags = {
      new flag_entry("reference", ResTable_map.TYPE_REFERENCE,
          "a reference to another resource, in the form \"<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>\"\n"
          + "or to a theme attribute in the form \"<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>\"."),
      new flag_entry("string", ResTable_map.TYPE_STRING,
          "a string value, using '\\\\;' to escape characters such as '\\\\n' or '\\\\uxxxx' for a unicode character."),
      new flag_entry("integer", ResTable_map.TYPE_INTEGER,
          "an integer value, such as \"<code>100</code>\"."),
      new flag_entry("boolean", ResTable_map.TYPE_BOOLEAN,
          "a boolean value, either \"<code>true</code>\" or \"<code>false</code>\"."),
      new flag_entry("color", ResTable_map.TYPE_COLOR,
          "a color value, in the form of \"<code>#<i>rgb</i></code>\", \"<code>#<i>argb</i></code>\",\n"
          + "\"<code>#<i>rrggbb</i></code>\", or \"<code>#<i>aarrggbb</i></code>\"."),
      new flag_entry("float", ResTable_map.TYPE_FLOAT,
          "a floating point value, such as \"<code>1.2</code>\"."),
      new flag_entry("dimension", ResTable_map.TYPE_DIMENSION,
          "a dimension value, which is a floating point number appended with a unit such as \"<code>14.5sp</code>\".\n"
          + "Available units are: px (pixels), dp (density-independent pixels), sp (scaled pixels based on preferred font size),\n"
          + "in (inches), mm (millimeters)."),
      new flag_entry("fraction", ResTable_map.TYPE_FRACTION,
          "a fractional value, which is a floating point number appended with either % or %p, such as \"<code>14.5%</code>\".\n"
          + "The % suffix always means a percentage of the base size; the optional %p suffix provides a size relative to\n"
          + "some parent container."),
      new flag_entry("enum", ResTable_map.TYPE_ENUM, null),
      new flag_entry("flags", ResTable_map.TYPE_FLAGS, null)
      // new flag_entry(null, 0, null)
  };

}
