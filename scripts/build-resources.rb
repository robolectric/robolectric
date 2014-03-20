#!/usr/bin/env ruby
#
# Use this script to generate resource IDs in R.java when you
# add/change/remove resources in the src/test/resources/res folder.
# 

path_to_r = "../src/test/java/org/robolectric/R.java"
if path_to_r =~ /^\/path\/to/
  raise "please change the path to this file!"
else
  original_contents = File.read(path_to_r)
  x = 0xffff
  new_contents = original_contents.gsub(/class|0x[0-9a-fA-F]+;/) do |match|
    if match == "class"
      x += 0x100
      x = x & 0xffffff00
      x -= 1
      "class"
    else
      "0x#{"%x" % (x += 1)};"
    end
  end
  File.open(path_to_r, "w") { |f| f << new_contents }
end
