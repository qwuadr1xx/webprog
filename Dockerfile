FROM gradle:8.10-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle war --no-daemon

FROM quay.io/wildfly/wildfly:31.0.1.Final-jdk17
COPY --from=build /home/gradle/src/build/libs/webprog.war /opt/jboss/wildfly/standalone/deployments/
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
