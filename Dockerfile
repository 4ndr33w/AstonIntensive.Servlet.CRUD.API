#FROM tomcat:7.0.109-jdk8
#FROM tomcat:9.0-jdk17
FROM tomcat:11.0-jdk17

ADD https://jdbc.postgresql.org/download/postgresql-42.6.0.jar /usr/local/tomcat/lib/
ENV JDK_JAVA_OPTIONS ""
WORKDIR /app
ARG WAR_FILE=target/ServletWebApp2.war
COPY target/ServletWebApp2.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ls -la /usr/local/tomcat/webapps/
CMD ["catalina.sh", "run"]
#ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.war"]
