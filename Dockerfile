FROM ensime/ensime:v2.x-cache
MAINTAINER Chip Senkbeil <chip.senkbeil@gmail.com>

# ensures the scala-debugger dependencies are available for the next build
WORKDIR /cache
RUN git clone https://github.com/chipsenkbeil/scala-debugger.git &&\
    cd scala-debugger &&\
    sbt +update +test:update +it:update
