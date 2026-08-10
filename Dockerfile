FROM gcr.io/distroless/java21-debian12

USER nonroot

COPY build/install/*/lib /lib

ENTRYPOINT ["java", "-cp", "/lib/*", "no.nav.modiapersonoversikt.MainKt"]
