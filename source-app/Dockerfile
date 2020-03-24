FROM java:8
MAINTAINER Sam Ma <sam_newyork@163.com>

ENTRYPOINT ["/usr/bin/java", "-jar", "/app/app.jar"]

# mkdir /app directory for staging app.jar
RUN mkdir /app && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/shanghai" > /etc/timezone
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /app/app.jar

# expose 9091 port to external service
EXPOSE 9091