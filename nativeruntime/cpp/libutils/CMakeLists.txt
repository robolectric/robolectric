cmake_minimum_required(VERSION 3.10)
# Some libutils headers require C++17
set (CMAKE_CXX_STANDARD 17)

project(utils)

include_directories(../base/include)
include_directories(../liblog/include)
include_directories(include)

add_library(utils STATIC
  include/utils/String16.h
  SharedBuffer.cpp
  Unicode.cpp
  String8.cpp
  String16.cpp
)
