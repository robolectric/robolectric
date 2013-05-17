#!/usr/bin/env ruby

require 'pathname'
require 'yaml'

jar_file = ARGV.shift
android_src = ARGV.shift

java_file_dirs = {}

android_src_path = Pathname.new(android_src)

java_files = `find #{android_src} -name \*.java`.split("\n")
java_files.each do |java_file|
  path = Pathname.new(java_file)
  name = path.basename(".java").to_s

  (java_file_dirs[name] ||= []) << path.relative_path_from(android_src_path).to_s
end

#puts java_file_dirs.to_yaml

classes = `jar tf #{jar_file}`.split("\n")
dirs = {}

classes.each do |clazz|
  next unless clazz =~ /\.class$/
  next if clazz =~ /\$/

  clazz_file = Pathname.new(clazz)
  clazz_as_java_file = clazz_file.to_s.gsub(/\.class/, ".java")
  clazz_pkg = clazz_file.parent
  possible_src_files = java_file_dirs[clazz_file.basename(".class").to_s]
  if possible_src_files.nil?
    puts "Couldn't find a dir for #{clazz_file}..."
    next
  end

  possible_src_files.select { |possible_src_file| possible_src_file.end_with?(clazz_as_java_file) }.map { |f| f[0..(f.length - clazz_as_java_file.length - 2)] }
end

puts "You might want these dirs:"
puts dirs.keys.sort.join("\n")
