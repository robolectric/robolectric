# start from docker image with android SDK 28 + gradle installed
FROM gcr.io/cloud-builders-community/android:28

RUN \
    # install extra utilities needed
    apt-get update && \
    apt-get -y install maven


