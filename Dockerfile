FROM navikt/java:8-appdynamics

COPY 20-appdynamics.sh /init-scripts/

ADD ./VERSION /app/VERSION
COPY ./target/soknad-kontantstotte-api.jar "/app/app.jar"
