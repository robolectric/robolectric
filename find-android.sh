#!/bin/sh

ANDROID_HOME=`which android | sed 's/\/tools\/android$//'`
echo "sdk.dir=$ANDROID_HOME" >> local.properties
