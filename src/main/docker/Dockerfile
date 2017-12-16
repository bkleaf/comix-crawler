FROM ubuntu:14.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get -y update

RUN apt-get install -y language-pack-ko

# set locale ko_KR
RUN locale-gen ko_KR.UTF-8

ENV LANG ko_KR.UTF-8
ENV LANGUAGE ko_KR.UTF-8
ENV LC_ALL ko_KR.UTF-8

#set time seoul
RUN mv /etc/localtime /etc/localimte_origin
RUN ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime

RUN apt-get purge openjdk*

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get clean

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

ADD comix-crawler-0.0.1-SNAPSHOT.jar /bleaf/src/comix-crawler.jar

VOLUME ["/bleaf/comix/download","/bleaf/comix/service"]

ENTRYPOINT ["java","-Dspring.profiles.active=product", "-Djava.security.egd=file:/dev/./urandom","-jar","/bleaf/src/comix-crawler.jar"]

EXPOSE 52272
