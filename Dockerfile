FROM navikt/java:8-appdynamics
ENV APPD_ENABLED=true

COPY build/libs/app.jar app.jar