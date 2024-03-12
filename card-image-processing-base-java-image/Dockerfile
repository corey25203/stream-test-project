##TODO: ARG python jdk opencv versions
FROM python:3.11.7-bookworm

RUN apt-get -y update
RUN apt upgrade
RUN apt  -y install build-essential cmake pkg-config unzip yasm git checkinstall
RUN apt  -y install libjpeg-dev libpng-dev libtiff-dev

RUN apt  -y install libavcodec-dev libavformat-dev libswscale-dev libswresample-dev
RUN apt  -y install libgstreamer1.0-dev libgstreamer-plugins-base1.0-dev
RUN apt  -y install libxvidcore-dev x264 libx264-dev libmp3lame-dev libtheora-dev 
RUN apt  -y install libmp3lame-dev libvorbis-dev 
RUN apt  -y install ffmpeg

RUN apt-get -y install libgtk-3-dev
RUN apt-get -y install libtbb-dev
RUN apt-get -y install libatlas-base-dev gfortran

RUN apt -y install openjdk-17-jdk
RUN apt-get -y install ant
#RUN apt -y install maven


ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
#ENV M2_HOME=/usr/share/maven/


RUN mkdir -p build-maven && cd build-maven
WORKDIR /build-maven
RUN wget -O apache-maven-3.9.6-bin.tar.gz https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
RUN tar xf /build-maven/apache-maven-3.9.6-bin.tar.gz -C /opt
RUN ln -s /opt/apache-maven-3.9.6 /opt/maven
ENV M2_HOME=/opt/maven
ENV MAVEN_HOME=/opt/maven


ENV PATH="${PATH}:${JAVA_HOME}"
ENV PATH="${PATH}:${M2_HOME}/bin"

RUN mkdir -p build-cv && cd build-cv
WORKDIR /build-cv
RUN wget -O opencv.zip https://github.com/opencv/opencv/archive/refs/tags/4.9.0.zip
RUN wget -O opencv_contrib.zip https://github.com/opencv/opencv_contrib/archive/refs/tags/4.9.0.zip

RUN unzip opencv.zip
RUN unzip opencv_contrib.zip

RUN cd opencv-4.9.0
RUN mkdir build
RUN cd build


RUN cmake -D CMAKE_BUILD_TYPE=RELEASE \
-D CMAKE_INSTALL_PREFIX=/usr/local \
#-D OPENCV_PYTHON3_INSTALL_PATH=~/.virtualenvs/cv/lib/python3.11/site-packages \
#-D PYTHON_EXECUTABLE=~/.virtualenvs/cv/bin/python \
-D OPENCV_EXTRA_MODULES_PATH=./opencv_contrib-4.9.0/modules \
-D INSTALL_PYTHON_EXAMPLES=OFF \
-D INSTALL_C_EXAMPLES=OFF \
-D BUILD_EXAMPLES=OFF ./opencv-4.9.0

##TODO: ARG number of VM processors [make -j`nproc` , check  .wslconfig CPU definition ]
RUN make -j2
RUN make install

ENV OCV_OBJ_HOME=/usr/local/share/java/opencv4/
RUN export PATH=$PATH:$OCV_OBJ_HOME
RUN cp /usr/local/share/java/opencv4/libopencv_java490.so /usr/local/share/java/opencv4/opencv-490.jar /usr/lib
RUN cp /usr/local/share/java/opencv4/libopencv_java490.so /lib


##TODO: PUSH core lib image
RUN mkdir /app
RUN mkdir /app/target
ADD ./pom.xml /app
ADD ./src /app/src
WORKDIR /app

ENV APP_PATH /app/

EXPOSE 8080 5000
CMD ["/bin/bash"]