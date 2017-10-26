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
#  3. You have installed the Android Support Repository and Google Repository libraries from the SDK installer.
#
require 'tmpdir'

def concat_maven_file_segments(repo_root_dir, group_id, artifact_id, version, extension)
  # Also don't move further if we have invalid parameters
  if group_id.to_s == '' || artifact_id.to_s == '' || version.to_s == '' || extension.to_s == ''
    raise ArgumentError, "Group ID, Artifact ID, Version, and/or Extension arguments are invalid. Please check your inputs."
  end
  # Generate dependency path segments
  dep_path_segments = []  
  artifact_file_name = "#{artifact_id}-#{version}.#{extension}"
  # Start with the root repo dir
  dep_path_segments << repo_root_dir

  # Add the split group id segments into the path segments
  dep_path_segments << group_id.split(".")
  
  # Then add the artifact id
  dep_path_segments << artifact_id
  
  # Then add the version ID
  dep_path_segments << version
  
  # Finally, add the version file
  dep_path_segments << artifact_file_name
  
  # Concatenate the segments into the target archive
  dep_path_segments.join("/")
end

def install(group_id, artifact_id, version, archive)
  run("mvn -q install:install-file -DgroupId='#{group_id}' -DartifactId='#{artifact_id}' -Dversion='#{version}' -Dfile='#{archive}' -Dpackaging=jar") || exit(1)
end

def get_dependency(group_id, artifact_id, version, packaging)
  run("mvn -q dependency:get -DremoteRepositories=https://maven.google.com/ -DgroupId='#{group_id}' -DartifactId='#{artifact_id}' -Dversion='#{version}' -Dpackaging='#{packaging}' -Dtransitive=false") || exit(1)
end

def run(args)
  puts "> #{args}"
  system args
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

def install_aar(repo_root_dir, group_id, artifact_id, version, &block)
  return if already_have?(group_id, artifact_id, version, "jar")

  # Don't move further if we have an invalid repo root directory
  unless File.exists?(repo_root_dir)
    puts "Repository #{repo_root_dir} not found!"
    puts "Make sure that the 'ANDROID_HOME' Environment Variable is properly set in your development environment pointing to your SDK installation directory."
    exit 1
  end

  archive = concat_maven_file_segments(repo_root_dir, group_id, artifact_id, version, "aar")

  puts "Installing AAR #{group_id}:#{artifact_id}, version #{version} from \'#{archive}\'."
  Dir.mktmpdir('robolectric-dependencies') do |dir|
    system("cd #{dir}; jar xvf #{archive} > /dev/null")
    install(group_id, artifact_id, version, "#{dir}/classes.jar")
    block.call(dir) if block_given?
  end
end

def already_have?(group_id, artifact_id, version, extension)
  jar_file = concat_maven_file_segments(MVN_LOCAL, group_id, artifact_id, version, extension)
  exists = File.exist?(jar_file)
  puts "Already have #{jar_file}!" if exists
  exists
end

def install_stubs(api)
  return if already_have?("com.google.android", "android-stubs", "#{api}", "jar")

  path  = "#{ANDROID_HOME}/platforms/android-#{api}/android.jar"
  unless File.exists?(path)
    puts "#{path} not found!"
    puts "Make sure that 'SDK Platform' is up to date in the SDK manager for API #{api}."
    exit 1
  end

  puts "Installing stubs jar at com.google.android:android-stubs:#{api}."
  install("com.google.android", "android-stubs", "#{api}", path)
end

def install_from_gmaven(artifact_id)
  return if already_have?(ANDROID_SUPPORT_GROUP_ID, artifact_id, SUPPORT_LIBRARY_VERSION, "jar")

  get_dependency(ANDROID_SUPPORT_GROUP_ID, artifact_id, SUPPORT_LIBRARY_VERSION, "aar")
  install_aar(MVN_LOCAL, ANDROID_SUPPORT_GROUP_ID, artifact_id, SUPPORT_LIBRARY_VERSION)
end

# Local repository paths
ANDROID_HOME = ENV['ANDROID_HOME']
ADDONS = "#{ANDROID_HOME}/add-ons"
GOOGLE_REPO  = "#{ANDROID_HOME}/extras/google/m2repository"
ANDROID_REPO = "#{ANDROID_HOME}/extras/android/m2repository"
MVN_LOCAL = File.expand_path("~/.m2/repository")

# Android Support libraries maven constants
ANDROID_SUPPORT_GROUP_ID = "com.android.support"
MULTIDEX_ARTIFACT_ID = "multidex"
SUPPORT_V4_ARTIFACT_ID = "support-v4"
SUPPORT_COMPAT_ARTIFACT_ID = "support-compat"
SUPPORT_CORE_UI_ARTIFACT_ID = "support-core-ui"
SUPPORT_CORE_UTILS_ARTIFACT_ID = "support-core-utils"
SUPPORT_FRAGMENT_ARTIFACT_ID = "support-fragment"
APPCOMPAT_V7_ARTIFACT_ID = "appcompat-v7"
INTERNAL_IMPL_ARTIFACT_ID = "internal_impl"

# Android Support library versions (plus trailing version)
# SUPPORT_LIBRARY_TRAILING_VERSION = "23.2.0"
SUPPORT_LIBRARY_VERSION = "26.0.1"
MULTIDEX_TRAILING_VERSION = "1.0.0"
MULTIDEX_VERSION = "1.0.1"

# Play Services constants
PLAY_SERVICES_GROUP_ID = "com.google.android.gms"

# Play Services 6.5.87 version constants, which pulls in all of the play-services
# submodules in classes.jar
PLAY_SERVICES_VERSION_6_5_87 = "6.5.87"
PLAY_SERVICES_LEGACY = "play-services"

# Play Services Base and Basement modules, version 8.4.0 (plus trailing version)
# Current "play-services" artifact no longer references each sub-module directly. When you
# Extract its AAR, it only contains a manifest and blank res folder.
# 
# As a result, we now have to install "play-services-base" and "play-services-basement"
# separately and use those versions instead.
PLAY_SERVICES_TRAILING_VERSION = "8.3.0"
PLAY_SERVICES_VERSION = "8.4.0"
PLAY_SERVICES_BASE = "play-services-base"
PLAY_SERVICES_BASEMENT = "play-services-basement"

# Mavenize all dependencies

install_stubs(27)

install_aar(ANDROID_REPO, ANDROID_SUPPORT_GROUP_ID, MULTIDEX_ARTIFACT_ID, MULTIDEX_TRAILING_VERSION)

install_aar(ANDROID_REPO, ANDROID_SUPPORT_GROUP_ID, MULTIDEX_ARTIFACT_ID, MULTIDEX_VERSION)

# install_aar(ANDROID_REPO, ANDROID_SUPPORT_GROUP_ID, APPCOMPAT_V7_ARTIFACT_ID, SUPPORT_LIBRARY_TRAILING_VERSION)

install_from_gmaven(APPCOMPAT_V7_ARTIFACT_ID)

install_aar(GOOGLE_REPO, PLAY_SERVICES_GROUP_ID, PLAY_SERVICES_LEGACY, PLAY_SERVICES_VERSION_6_5_87)

install_aar(GOOGLE_REPO, PLAY_SERVICES_GROUP_ID, PLAY_SERVICES_BASEMENT, PLAY_SERVICES_TRAILING_VERSION)

install_aar(GOOGLE_REPO, PLAY_SERVICES_GROUP_ID, PLAY_SERVICES_BASEMENT, PLAY_SERVICES_VERSION)

install_aar(GOOGLE_REPO, PLAY_SERVICES_GROUP_ID, PLAY_SERVICES_BASE, PLAY_SERVICES_TRAILING_VERSION)

install_aar(GOOGLE_REPO, PLAY_SERVICES_GROUP_ID, PLAY_SERVICES_BASE, PLAY_SERVICES_VERSION)

# install_aar(MVN_LOCAL, ANDROID_SUPPORT_GROUP_ID, SUPPORT_V4_ARTIFACT_ID, SUPPORT_LIBRARY_TRAILING_VERSION) do |dir|
  # install_jar(ANDROID_SUPPORT_GROUP_ID, INTERNAL_IMPL_ARTIFACT_ID, SUPPORT_LIBRARY_TRAILING_VERSION, "#{dir}/libs/#{INTERNAL_IMPL_ARTIFACT_ID}-#{SUPPORT_LIBRARY_TRAILING_VERSION}.jar")
# end

install_from_gmaven(SUPPORT_V4_ARTIFACT_ID)
install_from_gmaven(SUPPORT_COMPAT_ARTIFACT_ID)
install_from_gmaven(SUPPORT_CORE_UI_ARTIFACT_ID)
install_from_gmaven(SUPPORT_CORE_UTILS_ARTIFACT_ID)
install_from_gmaven(SUPPORT_FRAGMENT_ARTIFACT_ID)
install_from_gmaven('support-media-compat')
