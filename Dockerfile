FROM ensime/ensime:v2.x-cache
MAINTAINER Chip Senkbeil <chip.senkbeil@gmail.com>

ENV GIT_REPO https://github.com/ensime/scala-debugger.git
ENV GIT_BRANCH master
ENV GIT_SRC_DIR scala-debugger

# Install extra tools needed to check hostname
RUN apt-get update && apt-get install -y host jq

# Clone the main repository, build all sources (to get dependencies), and
# delete the source to clean up the size of the image
RUN git clone $GIT_REPO $GIT_SRC_DIR && \
    cd scala-debugger/ && \
    git checkout $GIT_BRANCH && \
    sbt +compile +test:compile +it:compile && \
    cd ../ && \
    rm -rf $GIT_SRC_DIR

