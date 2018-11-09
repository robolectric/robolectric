#!/usr/bin/env ruby
#
# This script can be used to regenerate sequential resource ids when the
# R.java file is modified in the src/test/resources/res folder.
#
# Note: this script does *NOT* generate resource ids that are consistent
# with the Android aapt tool. This script will likely be removed at some
# near point in the future and replaced by something that invokes aapt
# directly.

GIT_ROOT = `git rev-parse --show-toplevel`.chomp
START = 0x7f000000
INCR = 0x10000

path_to_r = File.join(GIT_ROOT, "robolectric/src/test/java/org/robolectric/R.java")
if path_to_r =~ /^\/path\/to/
  raise "please change the path to this file!"
else
  original_contents = File.read(path_to_r)
  num_classes = 0
  x = START
  new_contents = original_contents.gsub(/class|0x[0-9a-fA-F]+;/) do |match|
    if match == "class"
      x = START + INCR * num_classes
      num_classes += 1
      "class"
    else
      val = "0x#{"%x"%x};"
      x += 1
      val
    end
  end
  File.open(path_to_r, "w") { |f| f << new_contents }
end
