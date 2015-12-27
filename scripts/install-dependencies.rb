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
  system("mvn -q -e install:install-file -DgroupId='#{group_id}' -DartifactId='#{artifact_id}' -Dversion='#{version}' -Dfile='#{archive}' -Dpackaging=jar")
end

def install_jar(group_id, artifact_id, version, archive, &block)
  unless File.exists?(archive)
    puts "#{group_id}:#{artifact_id} not found!"
    puts "Make sure that the 'Android Support Repository' and 'Google Repository' is up to date in the SDK manager."
    exit 1
  end

  puts "Installing JAR #{group_id}:#{artifact_id}, version #{version} from \'#{archive}\'."
  install(group_id, artifact_id, version, archive)
  block.call(dir) if block_given?
end

def install_aar(group_id, artifact_id, version, archive, &block)
  unless File.exists?(archive)
    puts "#{group_id}:#{artifact_id} not found!"
    puts "Make sure that the 'Android Support Repository' and 'Google Repository' is up to date in the SDK manager."
    exit 1
  end

  puts "Installing AAR #{group_id}:#{artifact_id}, version #{version} from \'#{archive}\'."
  Dir.mktmpdir('robolectric-dependencies') do |dir|
    puts "cd'ing into #{dir}, then jarring from #{archive} into /dev/null"
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

  puts "Installing Maps API #{group_id}:#{artifact_id}, API #{api}, version #{version}, revision #{revision}."
  install(group_id, artifact_id, "#{api}_r#{revision}", path)
end

# Local repository paths
ADDONS = "#{ENV['ANDROID_HOME']}/add-ons"
GOOGLE_REPO  = "#{ENV['ANDROID_HOME']}/extras/google/m2repository"
ANDROID_REPO = "#{ENV['ANDROID_HOME']}/extras/android/m2repository"

# Library version constants
SUPPORT_LIBRARY_VERSION = "22.2.1"
MULTIDEX_VERSION = "1.0.0"

PLAY_SERVICES_GROUP_ID = "com.google.android.gms"

# Play Services 6.5.87 version constants, which pulls in all of the play-services
# submodules in classes.jar
PLAY_SERVICES_VERSION_6_5_87 = "6.5.87"
PLAY_SERVICES_LEGACY = "play-services"

# Play Services Base and Basement modules, version 8.3.0
# Current "play-services" artifact no longer references each sub-module directly. When you
# Extract its AAR, it only contains a manifest and blank res folder.
# 
# As a result, we now have to install "play-services-base" and "play-services-basement"
# separately and use those versions instead.
PLAY_SERVICES_VERSION = "8.3.0"
PLAY_SERVICES_BASE = "play-services-base"
PLAY_SERVICES_BASEMENT = "play-services-basement"

# Maps API version constants
MAPS_API_VERSION = "18"
MAPS_REVISION_VERSION = "4"

# Mavenize all dependencies
install_map("com.google.android.maps", "maps", MAPS_API_VERSION, MAPS_REVISION_VERSION)

install_aar("com.android.support", "multidex", MULTIDEX_VERSION,
  "#{ANDROID_REPO}/com/android/support/multidex/#{MULTIDEX_VERSION}/multidex-#{MULTIDEX_VERSION}.aar")

install_aar("com.android.support", "appcompat-v7", SUPPORT_LIBRARY_VERSION,
  "#{ANDROID_REPO}/com/android/support/appcompat-v7/#{SUPPORT_LIBRARY_VERSION}/appcompat-v7-#{SUPPORT_LIBRARY_VERSION}.aar")

install_aar(PLAY_SERVICES_GROUP_ID, PLAY_SERVICES_LEGACY, PLAY_SERVICES_VERSION_6_5_87,
  "#{GOOGLE_REPO}/com/google/android/gms/#{PLAY_SERVICES_LEGACY}/#{PLAY_SERVICES_VERSION_6_5_87}/#{PLAY_SERVICES_LEGACY}-#{PLAY_SERVICES_VERSION_6_5_87}.aar")

install_aar(PLAY_SERVICES_GROUP_ID, PLAY_SERVICES_BASEMENT, PLAY_SERVICES_VERSION,
  "#{GOOGLE_REPO}/com/google/android/gms/#{PLAY_SERVICES_BASEMENT}/#{PLAY_SERVICES_VERSION}/#{PLAY_SERVICES_BASEMENT}-#{PLAY_SERVICES_VERSION}.aar")

install_aar(PLAY_SERVICES_GROUP_ID, PLAY_SERVICES_BASE, PLAY_SERVICES_VERSION,
  "#{GOOGLE_REPO}/com/google/android/gms/#{PLAY_SERVICES_BASE}/#{PLAY_SERVICES_VERSION}/#{PLAY_SERVICES_BASE}-#{PLAY_SERVICES_VERSION}.aar")

install_aar("com.android.support", "support-v4", SUPPORT_LIBRARY_VERSION,
  "#{ANDROID_REPO}/com/android/support/support-v4/#{SUPPORT_LIBRARY_VERSION}/support-v4-#{SUPPORT_LIBRARY_VERSION}.aar") do |dir|

install_jar("com.android.support", "internal_impl", SUPPORT_LIBRARY_VERSION, "#{dir}/libs/internal_impl-#{SUPPORT_LIBRARY_VERSION}.jar")
end