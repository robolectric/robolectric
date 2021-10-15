cmake_minimum_required(VERSION 3.10)

project(libcutils)

include_directories(include)
include_directories(../base/include)

add_library(cutils STATIC ashmem.cpp)
