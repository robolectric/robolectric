#!/usr/bin/env ruby
#
# This script mavenizes all dependencies from the Android SDK required to build Robolectric.
#
# Usage:
#   install-dependencies.rb
#
# Assumptions:
#  1. You've got one or more Android SDKs and Google APIs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install directory.
#  3. You have installed the Android Repository and Google Repository libraries from the SDK installer.
#
require 'tmpdir'

def install(group_id, artifact_id, version, archive)
  system("mvn -q install:install-file -DgroupId='#{group_id}' -DartifactId='#{artifact_id}' -Dversion='#{version}' -Dfile='#{archive}' -Dpackaging=jar")
end

def install_jar(group_id, artifact_id, version, archive, &block)
  unless File.exists?(archive)
    puts "#{group_id}:#{artifact_id} not found!"
    puts "Make sure that the 'Android Support Repository' and 'Google Repository' is up to date in the SDK manager."
    exit 1
  end

  puts "Installing #{group_id}:#{artifact_id}"
  install(group_id, artifact_id, version, archive)
  block.call(dir) if block_given?
end

def install_aar(group_id, artifact_id, version, archive, &block)
  unless File.exists?(archive)
    puts "#{group_id}:#{artifact_id} not found!"
    puts "Make sure that the 'Android Support Repository' and 'Google Repository' is up to date in the SDK manager."
    exit 1
  end

  puts "Installing #{group_id}:#{artifact_id}"
  Dir.mktmpdir('robolectric-dependencies') do |dir|
    system("cd #{dir}; jar xvf #{archive} > /dev/null")
    install(group_id, artifact_id, version, "#{dir}/classes.jar")
    block.call(dir) if block_given?
  end
end

def install_map(group_id, artifact_id, api, revision)
  dir  = "#{ADDONS}/addon-google_apis-google-#{api}"
  path = "#{dir}/libs/maps.jar"

  unless File.exists?(path)
    puts "#{group_id}:#{artifact_id} not found!"
    puts "Make sure that 'Google APIs' is up to date in the SDK manager for API #{api}."
    exit 1
  end

  version = `grep --color=never ^revision= "#{dir}/manifest.ini" | cut -d= -f2`
  if version.strip != revision
    puts "#{group_id}:#{artifact_id} is an incompatible revision!"
    puts "Make sure that 'Google APIs' is up to date in the SDK manager for API #{api}. Expected revision #{revision} but was #{version}."
    exit 1
  end

  puts "Installing #{group_id}:#{artifact_id}"
  install(group_id, artifact_id, version, path)
end

# Local repository paths
ADDONS = "#{ENV['ANDROID_HOME']}/add-ons"
GOOGLE_REPO  = "#{ENV['ANDROID_HOME']}/extras/google/m2repository"
ANDROID_REPO = "#{ENV['ANDROID_HOME']}/extras/android/m2repository"

# Mavenize all dependencies
install_map("com.google.android.maps", "maps", "18", "4")

install_aar("com.android.support", "multidex", "1.0.0",
  "#{ANDROID_REPO}/com/android/support/multidex/1.0.0/multidex-1.0.0.aar")

install_aar("com.android.support", "appcompat-v7", "22.2.0",
  "#{ANDROID_REPO}/com/android/support/appcompat-v7/22.2.0/appcompat-v7-22.2.0.aar")

install_aar("com.google.android.gms", "play-services", "6.5.87",
  "#{GOOGLE_REPO}/com/google/android/gms/play-services/6.5.87/play-services-6.5.87.aar")

install_aar("com.android.support", "support-v4", "22.2.0",
  "#{ANDROID_REPO}/com/android/support/support-v4/22.2.0/support-v4-22.2.0.aar") do |dir|

  install_jar("com.android.support", "internal_impl", "22.2.0", "#{dir}/libs/internal_impl-22.2.0.jar")
end
