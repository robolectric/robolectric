# Modified from https://github.com/iwahjoedi/android-devcontainer/blob/main/Image/Dockerfile
ARG VARIENT="ubuntu-22.04"
FROM mcr.microsoft.com/vscode/devcontainers/base:0-${VARIENT}

ENV DEVCONTAINER="true"

RUN apt clean && apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y upgrade \
    && apt-get -y install git \
    && apt-get -y install clang cmake ninja-build pkg-config \
    && apt-get -y install wget unzip \
    && apt-get -y install openjdk-17-jdk \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

ARG USERNAME=vscode

USER $USERNAME

RUN export HOME="/home/$USERNAME"
ARG HOME="/home/$USERNAME"
ENV ANDROID_HOME=/home/$USERNAME/Android/Sdk
ENV ANDROID_SDK_ROOT=/home/$USERNAME/Android/Sdk
ENV CMDLINE_HOME="${HOME}/Android/Sdk/cmdline-tools"
ENV CMDLINE="${HOME}/Android/Sdk/cmdline-tools/cmdline-tools/bin"
ENV ANDROID_SDK_ZIP_FILE_VERSION=11076708

# setup flutter sdk
ENV PATH=${PATH}:${ANDROID_HOME}/platform-tools
ENV PATH=${PATH}:${ANDROID_HOME}/platforms
ENV PATH=${PATH}:${ANDROID_HOME}/emulators
ENV PATH=${PATH}:${CMDLINE_HOME}:${CMDLINE}

RUN ls -la ~ && whoami && mkdir -pv ${CMDLINE_HOME} && cd ${CMDLINE_HOME} \
    && wget https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_ZIP_FILE_VERSION}_latest.zip \
    && unzip commandlinetools-linux-${ANDROID_SDK_ZIP_FILE_VERSION}_latest.zip \
    && rm commandlinetools-linux-${ANDROID_SDK_ZIP_FILE_VERSION}_latest.zip

RUN ls -la ${HOME}/ && ls -la ${CMDLINE_HOME} && echo ${CMDLINE_HOME} && chown -Rv $USERNAME:$USERNAME ${HOME}/Android/Sdk
RUN chmod +r+w+x -Rv ${HOME}/Android/Sdk

RUN yes | sdkmanager --licenses
RUN yes | sdkmanager --install "build-tools;34.0.0"
RUN yes | sdkmanager --install "platforms;android-21"
RUN yes | sdkmanager --install "platforms;android-22"
RUN yes | sdkmanager --install "platforms;android-23"
RUN yes | sdkmanager --install "platforms;android-24"
RUN yes | sdkmanager --install "platforms;android-25"
RUN yes | sdkmanager --install "platforms;android-26"
RUN yes | sdkmanager --install "platforms;android-27"
RUN yes | sdkmanager --install "platforms;android-28"
RUN yes | sdkmanager --install "platforms;android-29"
RUN yes | sdkmanager --install "platforms;android-30"
RUN yes | sdkmanager --install "platforms;android-31"
RUN yes | sdkmanager --install "platforms;android-32"
RUN yes | sdkmanager --install "platforms;android-33"
RUN yes | sdkmanager --install "platforms;android-34"
RUN yes | sdkmanager --install "platform-tools" "cmdline-tools;latest"