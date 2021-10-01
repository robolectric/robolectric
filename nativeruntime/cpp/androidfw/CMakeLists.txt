cmake_minimum_required(VERSION 3.10)

project(androidfw)

include_directories(include)

include_directories(../libutils/include)
include_directories(../libcutils/include)
include_directories(../liblog/include)

add_library(androidfw STATIC CursorWindow.cpp)
