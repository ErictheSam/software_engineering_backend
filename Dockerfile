FROM frekele/ant

ENV ONTHEDEER=/opt/onthedeer

WORKDIR $ONTHEDEER

COPY . $ONTHEDEER

RUN ant build

FROM tomcat

WORKDIR $CATALINA_HOME/conf/

RUN sed -i 's|"8080"|"80"|' server.xml

RUN rm web.xml

COPY --from=0 /opt/onthedeer/web.xml web.xml

WORKDIR $CATALINA_HOME

COPY --from=0 /opt/onthedeer/tomcatlib lib

COPY --from=0 /opt/onthedeer/WebContent webapps/onthedeer

EXPOSE 80
