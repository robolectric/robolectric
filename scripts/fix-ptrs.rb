#!/usr/bin/env ruby

file = ARGV.shift

def fix(file, out)
  File.read(file).split(/\n/).each do |line|
    re = /(\w+)\(.*long\/\*ptr\*\//
    if line =~ re
      method_name = $1
      out.puts "  XXX FIX"
      out.puts "  @Implementation(to = 20)"
      out.puts line.gsub(/long\/\*ptr\*\//, 'int')
      out.puts "    return #{method_name}((long) );"
      out.puts "  }"
      out.puts ""
      out.puts "  @Implementation(from = 21)"
      out.puts line.gsub(/long\/\*ptr\*\//, 'long')
    else
      out.puts line
    end
  end
end

# fix(file, $stdout)

File.open("#{file}.fix", "w") do |out|
  fix(file, out)
end
