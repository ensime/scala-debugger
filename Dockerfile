FROM ensime/ensime:v2.x-cache
MAINTAINER Chip Senkbeil <chip.senkbeil@gmail.com>

# Install JDK 8 and make it the default
RUN echo "deb http://ftp.debian.org/debian jessie-backports main" >> /etc/apt/sources.list
ENV JAVA_VARIANT java-1.8.0-openjdk-amd64
RUN \
  apt-get update && \
  apt-get install -y openjdk-8-jdk openjdk-8-source icedtea-8-plugin && \
  update-java-alternatives -s ${JAVA_VARIANT} && \
  apt-get clean && \
  java -version && \
  javac -version

ENV SCALA_VARIANTS 2.10.6 2.11.8 2.12.1
RUN \
  mkdir /tmp/sbt && \
  cd /tmp/sbt && \
  mkdir -p project src/main/scala && \
  touch src/main/scala/scratch.scala && \
  echo "sbt.version=0.13.13" > project/build.properties && \
  for SCALA_VERSION in $SCALA_VARIANTS ; do \
    sbt ++$SCALA_VERSION clean updateClassifiers compile ; \
  done && \
  rm -rf /tmp/sbt

# Mark location of project to download
ENV GIT_REPO https://github.com/chipsenkbeil/scala-debugger.git
ENV GIT_BRANCH FixBrokenBuild
ENV GIT_SRC_DIR scala-debugger

# Clone the main repository, build all sources (to get dependencies)
# in a cache directory (so we can copy class files for use in
# incremental compilation)
WORKDIR /cache
RUN git clone $GIT_REPO $GIT_SRC_DIR && \
    cd scala-debugger/ && \
    git checkout $GIT_BRANCH && \
    sbt +compile +test:compile +it:compile

